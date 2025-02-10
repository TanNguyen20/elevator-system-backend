package elevator.system.service.impl;

import elevator.system.constant.ScheduleTask;
import elevator.system.enums.Direction;
import elevator.system.enums.DoorState;
import elevator.system.enums.ElevatorState;
import elevator.system.model.dto.request.ElevatorCreateRequestDTO;
import elevator.system.model.dto.response.ElevatorResponseDTO;
import elevator.system.model.entity.Elevator;
import elevator.system.repository.ElevatorRepository;
import elevator.system.service.intf.ElevatorService;
import elevator.system.service.intf.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableScheduling
public class ElevatorServiceImpl implements ElevatorService {
    private final ElevatorRepository elevatorRepository;
    private final WebSocketService webSocketService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);


    @Autowired
    public ElevatorServiceImpl(ElevatorRepository elevatorRepository, WebSocketService webSocketService) {
        this.elevatorRepository = elevatorRepository;
        this.webSocketService = webSocketService;
    }

    @Override
    public List<ElevatorResponseDTO> getAllElevators() {
        List<Elevator> elevators = elevatorRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return ElevatorResponseDTO.fromEntities(elevators);
    }

    @Override
    public void addElevator(ElevatorCreateRequestDTO elevatorCreateRequestDTO) {
        elevatorRepository.save(ElevatorCreateRequestDTO.toEntity(elevatorCreateRequestDTO));
    }

    @Override
    @Transactional
    public void requestElevator(int userCurrentFloor, Direction direction) {
        List<Elevator> elevators = elevatorRepository.findAll();

        // Find the best available elevator
        Optional<Elevator> bestElevator = elevators.stream()
            .filter(e -> e.getState().equals(ElevatorState.IDLE) ||
                (e.getDirection() == direction && isOnTheWay(e, userCurrentFloor)))
            .min(Comparator.comparingInt(e -> Math.abs(e.getCurrentFloor() - userCurrentFloor)));

        bestElevator.ifPresent(elevator -> {
            elevator.getPendingFloors().add(userCurrentFloor);
            elevator.setState(ElevatorState.MOVING);
            elevatorRepository.save(elevator);
            webSocketService.sendMessageToAllClients(elevator);
            log.info("Elevator {} assigned to floor {} request.", elevator.getId(), userCurrentFloor);
        });
    }

    @Override
    @Transactional
    public void goToFloor(Long elevatorId, int destinationFloor) {
        Optional<Elevator> optionalElevator = elevatorRepository.findById(elevatorId);

        optionalElevator.ifPresent(elevator -> {
            if (!elevator.getPendingFloors().contains(destinationFloor)) {
                elevator.getPendingFloors().add(destinationFloor);
            }
            elevator.setDoorState(DoorState.CLOSED);
            elevator.setState(ElevatorState.MOVING);
            elevatorRepository.save(elevator);
            webSocketService.sendMessageToAllClients(elevator);
            log.info("Elevator {} is moving to floor {}", elevator.getId(), destinationFloor);
        });
    }

    @Override
    @Transactional
    public void moveElevator(Long elevatorId) {
        Optional<Elevator> optionalElevator = elevatorRepository.findById(elevatorId);

        optionalElevator.ifPresent(elevator -> {
            if (elevator.getPendingFloors().isEmpty()) {
                elevator.setState(ElevatorState.IDLE);
                elevator.setDirection(Direction.NONE);
                elevatorRepository.save(elevator);
                webSocketService.sendMessageToAllClients(ElevatorResponseDTO.fromEntity(elevator));
            } else {
                int nextFloor = elevator.getPendingFloors().get(0);
                moveOneStep(elevator, nextFloor);
            }
        });
    }

    @Scheduled(fixedRate = ScheduleTask.MOVING_INTERVAL_TIME)
    @Transactional
    public void updateMovingElevators() {
        log.info("Updating moving elevators...");

        List<Elevator> movingElevators = elevatorRepository.findAll()
            .stream()
            .filter(e -> e.getState().equals(ElevatorState.MOVING))
            .toList();

        for (Elevator elevator : movingElevators) {
            moveElevator(elevator.getId());
            scheduler.schedule(
                this::updateStoppedElevators,
                ScheduleTask.STOPPED_AFTER_MOVING_TIME,
                TimeUnit.MILLISECONDS
            );
        }
    }

    @Transactional
    public void updateStoppedElevators() {
        List<Elevator> stoppedElevators = elevatorRepository.findAll()
            .stream()
            .filter(e -> e.getState().equals(ElevatorState.STOPPED))
            .toList();

        for (Elevator elevator : stoppedElevators) {
            if (elevator.getPendingFloors().isEmpty()) {
                elevator.setDoorState(DoorState.CLOSED);
                elevator.setState(ElevatorState.IDLE);
                elevator.setDirection(Direction.NONE);
            } else {
                elevator.setDoorState(DoorState.CLOSED);
                elevator.setState(ElevatorState.MOVING);
            }
            elevatorRepository.save(elevator);
            webSocketService.sendMessageToAllClients(ElevatorResponseDTO.fromEntity(elevator));
        }
    }

    private void moveOneStep(Elevator elevator, int destinationFloor) {
        if (elevator.getCurrentFloor() < destinationFloor) {
            elevator.setCurrentFloor(elevator.getCurrentFloor() + 1);
            elevator.setDirection(Direction.UP);
        } else if (elevator.getCurrentFloor() > destinationFloor) {
            elevator.setCurrentFloor(elevator.getCurrentFloor() - 1);
            elevator.setDirection(Direction.DOWN);
        }

        if (elevator.getCurrentFloor() == destinationFloor && !elevator.getState().equals(ElevatorState.IDLE)) {
            if (!elevator.getPendingFloors().isEmpty()) {
                elevator.getPendingFloors().remove(0);
            }
            elevator.setDoorState(DoorState.OPEN);
            elevator.setState(ElevatorState.STOPPED);
        }
        elevatorRepository.save(elevator);
        webSocketService.sendMessageToAllClients(ElevatorResponseDTO.fromEntity(elevator));
        log.info("Elevator {} moved to floor {}", elevator.getId(), elevator.getCurrentFloor());
    }

    private boolean isOnTheWay(Elevator elevator, int userCurrentFloor) {
        return elevator.getState().equals(ElevatorState.MOVING) &&
            (elevator.getDirection() == Direction.UP && elevator.getCurrentFloor() < userCurrentFloor) ||
            (elevator.getDirection() == Direction.DOWN && elevator.getCurrentFloor() > userCurrentFloor);
    }

    @Override
    @Transactional
    public void openDoor(Long elevatorId) {
        elevatorRepository.findById(elevatorId).ifPresent(elevator -> {
            if (elevator.getState().equals(ElevatorState.STOPPED) ||
                elevator.getState().equals(ElevatorState.IDLE)) {
                elevator.setDoorState(DoorState.OPEN);
                webSocketService.sendMessageToAllClients(ElevatorResponseDTO.fromEntity(elevator));
            }
        });
    }

    @Override
    @Transactional
    public void closeDoor(Long elevatorId) {
        elevatorRepository.findById(elevatorId).ifPresent(elevator -> {
            if (elevator.getState().equals(ElevatorState.STOPPED) ||
                elevator.getState().equals(ElevatorState.IDLE)) {
                elevator.setDoorState(DoorState.CLOSED);
                webSocketService.sendMessageToAllClients(ElevatorResponseDTO.fromEntity(elevator));
            }
        });
    }
}

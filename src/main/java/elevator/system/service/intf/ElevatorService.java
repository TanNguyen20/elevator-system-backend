package elevator.system.service.intf;

import elevator.system.enums.Direction;
import elevator.system.model.dto.request.ElevatorCreateRequestDTO;
import elevator.system.model.dto.response.ElevatorResponseDTO;

import java.util.List;

public interface ElevatorService {
    List<ElevatorResponseDTO> getAllElevators();

    void addElevator(ElevatorCreateRequestDTO elevatorCreateRequestDTO);

    void requestElevator(int floor, Direction direction);

    void moveElevator(Long elevatorId);

    void goToFloor(Long elevatorId, int destinationFloor);

    void openDoor(Long elevatorId);

    void closeDoor(Long elevatorId);
}
package elevator.system.controller;

import elevator.system.enums.Direction;
import elevator.system.model.dto.request.ElevatorCreateRequestDTO;
import elevator.system.model.dto.response.ElevatorResponseDTO;
import elevator.system.service.intf.ElevatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elevators")
@RequiredArgsConstructor
public class ElevatorController {
    private final ElevatorService elevatorService;

    @GetMapping
    public ResponseEntity<List<ElevatorResponseDTO>> getAllElevators() {
        return new ResponseEntity<>(elevatorService.getAllElevators(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Void> addElevator(@RequestBody ElevatorCreateRequestDTO elevatorCreateRequestDTO) {
        elevatorService.addElevator(elevatorCreateRequestDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/request")
    public ResponseEntity<Void> requestElevator(@RequestParam int floor, @RequestParam Direction direction) {
        elevatorService.requestElevator(floor, direction);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{elevatorId}/move")
    public ResponseEntity<Void> moveElevator(@PathVariable Long elevatorId) {
        elevatorService.moveElevator(elevatorId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{elevatorId}/goto/{floor}")
    public ResponseEntity<Void> goToFloor(@PathVariable Long elevatorId, @PathVariable int floor) {
        elevatorService.goToFloor(elevatorId, floor);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{elevatorId}/door/open")
    public ResponseEntity<Void> openDoor(@PathVariable Long elevatorId) {
        elevatorService.openDoor(elevatorId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{elevatorId}/door/close")
    public ResponseEntity<Void> closeDoor(@PathVariable Long elevatorId) {
        elevatorService.closeDoor(elevatorId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

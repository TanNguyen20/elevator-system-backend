package elevator.system.model.dto.request;

import elevator.system.enums.Direction;
import elevator.system.enums.DoorState;
import elevator.system.enums.ElevatorState;
import elevator.system.model.entity.Elevator;
import lombok.Data;

@Data
public class ElevatorCreateRequestDTO {
    private int currentFloor;

    private ElevatorState state;

    private Direction direction;

    private DoorState doorState;

    public static Elevator toEntity(ElevatorCreateRequestDTO dto) {
        return Elevator.builder()
            .state(dto.getState())
            .direction(dto.getDirection())
            .doorState(dto.getDoorState())
            .currentFloor(dto.getCurrentFloor())
            .build();
    }
}

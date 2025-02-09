package elevator.system.model.dto.response;

import elevator.system.enums.Direction;
import elevator.system.enums.DoorState;
import elevator.system.enums.ElevatorState;
import elevator.system.model.entity.Elevator;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ElevatorResponseDTO {
    private Long id;

    private int currentFloor;

    private ElevatorState state;

    private Direction direction;

    private DoorState doorState;

    public static ElevatorResponseDTO fromEntity(Elevator entity) {
        return ElevatorResponseDTO.builder()
            .id(entity.getId())
            .state(entity.getState())
            .direction(entity.getDirection())
            .doorState(entity.getDoorState())
            .currentFloor(entity.getCurrentFloor())
            .build();
    }

    public static List<ElevatorResponseDTO> fromEntities(List<Elevator> entities) {
        return entities.stream()
            .map(ElevatorResponseDTO::fromEntity)
            .toList();
    }
}

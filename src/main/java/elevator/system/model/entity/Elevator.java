package elevator.system.model.entity;

import elevator.system.enums.Direction;
import elevator.system.enums.DoorState;
import elevator.system.enums.ElevatorState;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Elevator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int currentFloor;

    @Enumerated(EnumType.STRING)
    private ElevatorState state;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    private DoorState doorState;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> pendingFloors = new ArrayList<>();
}
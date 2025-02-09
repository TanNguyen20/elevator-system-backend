package elevator.system.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ElevatorSystemException extends RuntimeException {
    private final HttpStatus status;

    public ElevatorSystemException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}

package elevator.system;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(title = "Elevator System API", version = "1.0", description = "Documentation Elevator System API v1.0")
)
public class ElevatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElevatorApplication.class, args);
    }

}

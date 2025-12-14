package a2.device_simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DeviceSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceSimulatorApplication.class, args);
    }

}

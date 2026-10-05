package co.edu.unisimon.corenotificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@ComponentScan(basePackages = {"co.edu.unisimon.corenotificacion"})
@SpringBootApplication
public class CoreNotificacionApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreNotificacionApiApplication.class, args);
	}

}

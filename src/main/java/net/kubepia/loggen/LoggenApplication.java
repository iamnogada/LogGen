package net.kubepia.loggen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoggenApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoggenApplication.class, args);
	}

}

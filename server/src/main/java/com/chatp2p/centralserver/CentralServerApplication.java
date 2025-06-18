package com.chatp2p.centralserver;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CentralServerApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);
		SpringApplication.run(CentralServerApplication.class, args);
	}
}

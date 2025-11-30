package com.github.b3kt.aviation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AviationApiApplication {
	public static void main(String[] args) {
		reactor.core.publisher.Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(AviationApiApplication.class, args);
	}
}

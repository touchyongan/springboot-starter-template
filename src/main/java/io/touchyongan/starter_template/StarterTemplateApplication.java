package io.touchyongan.starter_template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class StarterTemplateApplication {

	public static void main(String[] args) {
		SpringApplication.run(StarterTemplateApplication.class, args);
	}

}

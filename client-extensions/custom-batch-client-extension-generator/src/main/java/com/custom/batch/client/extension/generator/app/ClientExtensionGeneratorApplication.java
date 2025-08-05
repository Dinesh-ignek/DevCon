
package com.custom.batch.client.extension.generator.app;

import com.liferay.client.extension.util.spring.boot.ClientExtensionUtilSpringBootComponentScan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(ClientExtensionUtilSpringBootComponentScan.class)
@SpringBootApplication
public class ClientExtensionGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientExtensionGeneratorApplication.class, args);
	}

}
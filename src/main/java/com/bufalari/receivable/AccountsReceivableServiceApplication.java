// Path: src/main/java/com/bufalari/receivable/AccountsReceivableServiceApplication.java
package com.bufalari.receivable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients; // Import and Enable
// import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // If using config class

/**
 * Main application class for the Accounts Receivable Service.
 * Classe principal da aplicação para o Serviço de Contas a Receber.
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.bufalari.receivable.client") // Scan for Feign clients
// @EnableJpaAuditing // If using config class
public class AccountsReceivableServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountsReceivableServiceApplication.class, args);
	}

}
package com.example.J_Settlement.Ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JSettlementLedgerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JSettlementLedgerApplication.class, args);
	}

}

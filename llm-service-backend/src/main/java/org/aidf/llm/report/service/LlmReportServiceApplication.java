package org.aidf.llm.report.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class LlmReportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LlmReportServiceApplication.class, args);
	}

}

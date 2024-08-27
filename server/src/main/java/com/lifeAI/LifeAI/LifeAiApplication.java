package com.lifeAI.LifeAI;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
public class LifeAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LifeAiApplication.class, args);
	}

	@Bean
	public TokenTextSplitter tokenTextSplitter(){
		return new TokenTextSplitter();
	}
}

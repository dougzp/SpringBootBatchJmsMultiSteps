package com.batchjms.example;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class RunBatch {

	public static void main(String[] args) {
	  new SpringApplicationBuilder(RunBatch.class)
        .web(WebApplicationType.NONE)
        .build()
        .run(args);
		
	}

	
}

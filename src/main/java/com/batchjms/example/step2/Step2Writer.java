package com.batchjms.example.step2;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jms.core.JmsTemplate;

import com.batchjms.example.dto.ModelDTO;

public class Step2Writer implements ItemWriter<ModelDTO> {

	public static final Logger logger = LoggerFactory.getLogger(Step2Writer.class.getName());

	
	@Override
	public void write(List<? extends ModelDTO> items) throws Exception {
		
		for (ModelDTO model : items) {
				System.out.println("TITTLE UPPERCASE:" + model.getTitle());
			
			}
		}
	



}

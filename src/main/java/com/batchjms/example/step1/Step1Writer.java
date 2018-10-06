package com.batchjms.example.step1;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jms.core.JmsTemplate;

import com.batchjms.example.dto.ModelDTO;

public class Step1Writer implements ItemWriter<ModelDTO> {

	public static final Logger logger = LoggerFactory.getLogger(Step1Writer.class.getName());

	private JmsTemplate jmsTemplate2;
	
	public Step1Writer(JmsTemplate jmsTemplate2) {
		super();
		this.jmsTemplate2 = jmsTemplate2;
	}

	@Override
	public void write(List<? extends ModelDTO> items) throws Exception {
		
		for (ModelDTO model : items) {
				jmsTemplate2.convertAndSend(model);
			
			}
		}
	



}

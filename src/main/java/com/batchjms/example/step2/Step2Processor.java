package com.batchjms.example.step2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.batchjms.example.dto.ModelDTO;

public class Step2Processor implements ItemProcessor<ModelDTO, ModelDTO>{

	public static final Logger logger = LoggerFactory.getLogger(Step2Processor.class.getName());

	

	@Override
	public ModelDTO process(ModelDTO item) throws Exception {
		ModelDTO result = new ModelDTO();
		result.setTitle("NEW TITLE"+item.getTitle());
		return result;
	}

	
	
}

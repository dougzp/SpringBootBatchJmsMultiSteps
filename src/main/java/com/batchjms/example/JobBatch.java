package com.batchjms.example;

import java.util.Collections;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.sql.DataSource;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.jms.JmsItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;


import com.batchjms.example.dto.ModelDTO;
import com.batchjms.example.step1.Step1Processor;
import com.batchjms.example.step1.Step1Writer;
import com.batchjms.example.step2.Step2Processor;
import com.batchjms.example.step2.Step2Writer;
@Configuration
@EnableJms
@EnableBatchProcessing
@Primary
public class JobBatch  extends DefaultBatchConfigurer {

    @Override
    public void setDataSource(DataSource dataSource) {

    }
    String [] items = {"item#01","item#02","item#03","item#04","item#05"};
   
    private int size;
    
    @Override
    public void initialize() {
    //COUNT ITEMS
    	size=items.length;
    	super.initialize();
    }


    @Autowired
    public JobBuilderFactory jobBuilderFactory;

  
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    
    public static final String LOCAL_Q1 = "localQ1Example";
    public static final String LOCAL_Q2 = "localQ1Example";

    
  
    @Bean
    @Primary
    public ConnectionFactory jmsConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://embeddedQueue?broker.persistent=true&broker.useJmx=false,useShutdownHook=false");
        return connectionFactory;
    }

    @Bean
    public QueueConnectionFactory jmsConnectionFactory2() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://embeddedQueue2?broker.persistent=true&broker.useJmx=false,useShutdownHook=false");
 
        return connectionFactory;
    }

    @Bean
    @Primary
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(jmsConnectionFactory());
        jmsTemplate.setDefaultDestinationName(LOCAL_Q1);
        jmsTemplate.setMessageConverter(messageConverter() );
        jmsTemplate.setReceiveTimeout(1000l);


        return jmsTemplate;
    }

    @Bean
    public JmsTemplate jmsTemplate2() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(jmsConnectionFactory2());
        jmsTemplate.setMessageConverter(messageConverter() );
        jmsTemplate.setDefaultDestinationName(LOCAL_Q2);
        jmsTemplate.setReceiveTimeout(1000l);
        
        jmsTemplate.browseSelected(LOCAL_Q1, new BrowserCallback<Integer>() {
   

			@Override
			public Integer doInJms(Session session, QueueBrowser browser) throws JMSException {
				// TODO Auto-generated method stub
				return Collections.list(browser.getEnumeration()).size();
			}
        });
        
        return jmsTemplate;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory2(
            @Qualifier("jmsConnectionFactory2") ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }
    @Primary
    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Primary
    @Bean
    public JmsItemReader<ModelDTO> Step1JmsItemReader(MessageConverter messageConverter) {
    	JmsItemReader<ModelDTO> itemReader = new JmsItemReader<>();
    	itemReader.setJmsTemplate(jmsTemplate());
    	itemReader.setItemType(ModelDTO.class);
        
        return itemReader;
    }
  
    @Bean
    public JmsItemReader<ModelDTO> Step2JmsItemReader(MessageConverter messageConverter) {
    	JmsItemReader<ModelDTO> itemReader = new JmsItemReader<>();
    	itemReader.setJmsTemplate(jmsTemplate2());
    	itemReader.setItemType(ModelDTO.class);
        
        return itemReader;
    }


    @Bean
    public Job importUserJob() {
    	
    	
    	
    	
        return jobBuilderFactory.get("multiStepJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener())
                .start(step1())
                .next(step2())
                .build();
    }

    private Step step1() {
    	
    
        return stepBuilderFactory.get("step1")
                .<ModelDTO, ModelDTO>chunk(size)
                .reader(Step1JmsItemReader(messageConverter()))
                .processor(new Step1Processor())
                .writer(new Step1Writer(jmsTemplate2()))
                .build();
    }


    private Step step2() {
    	
    
        return stepBuilderFactory.get("step2")
                .<ModelDTO, ModelDTO>chunk(size)
                .reader(Step2JmsItemReader(messageConverter()))
                .processor(new Step2Processor())
                .writer(new Step2Writer())
                .build();
    }


    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
     
            	 
            	  for (String string : items) {
            		  ModelDTO model = new ModelDTO();
            		  model.setTitle(string);
					jmsTemplate().convertAndSend(model);
				}
            
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
            	System.exit(0);
            }
        };
    }
	
    
    

    
}

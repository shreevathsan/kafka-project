package com.appsdeveloperblog.ws.emailnotification;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import com.appsdeveloperblog.ws.emailnotification.error.NonRetryableException;
import com.appsdeveloperblog.ws.emailnotification.error.RetryableException;

@Configuration
public class KafkaConsumerConfiguration {

	@Autowired
	private Environment environment;
	

	@Bean
	ConsumerFactory<String, Object> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(configMap());
	}

	public Map<String, Object> configMap() {
		Map<String, Object> configMap = new HashMap<>();
		configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				environment.getProperty("spring.kafka.consumer.bootstrap-servers"));
		configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		/**
		 * this property is configured to handle error cases , it means if the value is
		 * not of json type it will handle the error properly and handles other proper
		 * JSON value request
		 */
		configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		configMap.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
		configMap.put("spring.json.trusted.packages", "com.appsdeveloperblog.ws.core");
		configMap.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("consumer.group-id"));

		return configMap;
	}

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
			ConsumerFactory<String, Object> consumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {

		DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(
				new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(5000, 3));
		defaultErrorHandler.addNotRetryableExceptions(NonRetryableException.class);
		defaultErrorHandler.addRetryableExceptions(RetryableException.class);

		ConcurrentKafkaListenerContainerFactory<String, Object> consumerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
		consumerContainerFactory.setConsumerFactory(consumerFactory);
		consumerContainerFactory.setCommonErrorHandler(defaultErrorHandler);

		return consumerContainerFactory;
	}

	@Bean
	KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}

	@Bean
	ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> configMap = new HashMap<>();

		configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				environment.getProperty("spring.kafka.consumer.bootstrap-servers"));
		configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		return new DefaultKafkaProducerFactory<>(configMap);

	}

}

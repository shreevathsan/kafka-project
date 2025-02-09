package com.appsdeveloperblog.ws.products.kafka;

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION;
import static org.apache.kafka.clients.producer.ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.appsdeveloperblog.ws.core.ProductCreatedEvent;

@Configuration
public class KafkaConfig {

	@Value("${spring.kafka.producer.bootstrap-servers}")
	private String bootStrapServers;

	@Value("${spring.kafka.producer.key-serializer}")
	private String keySerializer;

	@Value("${spring.kafka.producer.value-serializer}")
	private String valueSerializer;

	@Value("${spring.kafka.producer.acks}")
	private String acks;

	@Value("${spring.kafka.producer.properties.delivery.timeout.ms}")
	private String deliveryTimeout;

	@Value("${spring.kafka.producer.properties.linger.ms}")
	private String linger;

	@Value("${spring.kafka.producer.properties.request.timeout.ms}")
	private String requestTimeout;

	@Value("${spring.kafka.producer.properties.enable.idempotence}")
	private Boolean isIdemPotenceEnabled;

	@Value("${spring.kafka.producer.properties.max.in.flight.requests.per.connection}")
	private String maxInFlightRequest;

	Map<String, Object> producerConfigs() {
		Map<String, Object> configMap = new HashMap<>();

		configMap.put(BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
		configMap.put(KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
		configMap.put(VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
		configMap.put(ACKS_CONFIG, acks);
		configMap.put(DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);
		configMap.put(LINGER_MS_CONFIG, linger);
		configMap.put(REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
		
		configMap.put(ENABLE_IDEMPOTENCE_CONFIG, isIdemPotenceEnabled);
		configMap.put(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequest);
		//configMap.put(RETRIES_CONFIG, Integer.MAX_VALUE);

		return configMap;
	}

	@Bean
	ProducerFactory<String, ProductCreatedEvent> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	NewTopic createTopic() {
		return TopicBuilder.name("product-created-events-topic").partitions(3).replicas(3)
				.configs(Map.of("min.insync.replicas", "2")).build();
	}
}

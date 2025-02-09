package com.appsdeveloperblog.ws.products.service;

import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.appsdeveloperblog.ws.core.ProductCreatedEvent;
import com.appsdeveloperblog.ws.products.model.CreateProductRestModel;

@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

	@Autowired
	private KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

	@SuppressWarnings("null")
	@Override
	public String createProduct(CreateProductRestModel createProductRestModel) throws Exception {
		String productId = UUID.randomUUID().toString();
		/**
		 * Mocking Data Persistence Logic inorder to focus only on the Kafka Part
		 */
		ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId, createProductRestModel.getTitle(),
				createProductRestModel.getPrice(), createProductRestModel.getQuantity());

		// Sending message to the Kafka broker Asynchronously

//		CompletableFuture<SendResult<String, ProductCreatedEvent>> future = kafkaTemplate
//				.send("product-created-events-topic", productId, productCreatedEvent);
//		future.whenComplete((result, exception) -> {
//			if (null == exception) {
//				LOGGER.error("********** Exception occured for product event :: {}, message :: {}", productId,
//						exception.getMessage());
//			} else {
//				LOGGER.info("*********** Kafka Message Sent Successfully for the productId : {}, result : {}", productId,
//						result.getRecordMetadata());
//			}
//		});

		// Synchronous Communications to the Kafka Broker
		
		ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<String, ProductCreatedEvent>(
				"product-created-events-topic", productId, productCreatedEvent);
		record.headers().add("messageId", UUID.randomUUID().toString().getBytes());
		
		SendResult<String, ProductCreatedEvent> result = kafkaTemplate
				.send(record).get();

		LOGGER.info("****** Kafka Topic Name : {}", result.getRecordMetadata().topic());
		LOGGER.info("****** Kafka Partition Name : {}", result.getRecordMetadata().partition());
		LOGGER.info("****** Kafka Offset Name : {}", result.getRecordMetadata().offset());
		return productId;
	}

}

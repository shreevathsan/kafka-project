package com.appsdeveloperblog.ws.emailnotification.handler;

import java.util.Objects;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.support.KafkaHeaders;

import com.appsdeveloperblog.ws.core.ProductCreatedEvent;
import com.appsdeveloperblog.ws.emailnotification.entity.ProcessedEventEntity;
import com.appsdeveloperblog.ws.emailnotification.error.NonRetryableException;
import com.appsdeveloperblog.ws.emailnotification.error.RetryableException;
import com.appsdeveloperblog.ws.emailnotification.repository.ProcessedEventRepository;

@Transactional
@Component
@KafkaListener(topics = "product-created-events-topic")
public class ProductCreatedEventListener {

	private RestTemplate restTemplate;
	private ProcessedEventRepository processedEventRepository;

	public ProductCreatedEventListener(RestTemplate restTemplate, ProcessedEventRepository processedEventRepository) {
		this.restTemplate = restTemplate;
		this.processedEventRepository = processedEventRepository;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductCreatedEventListener.class);

	@KafkaHandler
	public void handle(@Payload ProductCreatedEvent productCreatedEvent,
			@Header(value = "messageId", required = true) String messageId,
			@Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
		LOGGER.info("Received the new even: {}", productCreatedEvent.getTitle());

		// check if the message was already processed
		if (Objects.nonNull(processedEventRepository.findByMessageId(messageId))) {
			LOGGER.info("Message was already processed , messageId :: {}", messageId);
			return;
		}

		String requestUrl = "http://localhost:8082";
		try {
			ResponseEntity<String> responseEntity = restTemplate.exchange(requestUrl, HttpMethod.GET, null,
					String.class);
			if (responseEntity.getStatusCode().value() == HttpStatus.OK.value()) {
				LOGGER.info("Received response from the remote server");
			}
		} catch (ResourceNotFoundException ex) {
			LOGGER.error(ex.getMessage());
			throw new RetryableException(ex);
		} catch (HttpServerErrorException ex) {
			LOGGER.error(ex.getMessage());
			throw new NonRetryableException(ex);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
			throw new NonRetryableException(ex);
		}

		// save unique message id in database
		try {
			processedEventRepository.save(new ProcessedEventEntity(messageId, productCreatedEvent.getProductId()));
		} catch (DataIntegrityViolationException ex) {
			throw new NonRetryableException(ex.getMessage());
		}

	}

}

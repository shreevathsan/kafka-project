package com.appsdeveloperblog.ws.emailnotification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "processed-events")
public class ProcessedEventEntity {

	@Id
	@GeneratedValue
	private long id;

	@Column(name = "message_id", nullable = false, unique = true)
	private String messageId;

	@Column(name = "product_id", nullable = false, unique = true)
	private String productId;

	public ProcessedEventEntity(String messageId, String productId) {

		this.messageId = messageId;
		this.productId = productId;
	}

	public ProcessedEventEntity() {

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

}

/*
 * 
 * Copyright (c) 2018 Solace Corp.
 * 
 */
package org.springframework.cloud.stream.app.utah.transaction.gen.source;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the inventory lookup processor.
 *
 * @author Solace Corp.
 */
@ConfigurationProperties("utah.transaction.gen")
public class UtahTransactionGenSourceProperties {
	public static final int DEFAULT_EMIT_INTERVAL_SEC = 1;
	public static final int DEFAULT_MAX_PURCHASE_ITEM_COUNT = 5;
	public static final int DEFAULT_MAX_QUANTITY_PER_ITEM = 3;
	public static final String DEFAULT_PUBLISH_INVENTORY_TOPIC_PREFIX = "inventory/tx/";

	private int emitIntervalSec = DEFAULT_EMIT_INTERVAL_SEC;
	private int maxPurchaseItemCount = DEFAULT_MAX_PURCHASE_ITEM_COUNT;
	private int maxQuantityPerItem = DEFAULT_MAX_QUANTITY_PER_ITEM;
	private String publishInventoryTopicPrefix = DEFAULT_PUBLISH_INVENTORY_TOPIC_PREFIX;

	public int getEmitIntervalSec() {
		return emitIntervalSec;
	}

	public void setEmitIntervalSec(int emitIntervalSec) {
		this.emitIntervalSec = emitIntervalSec;
	}

	public int getMaxPurchaseItemCount() {
		return maxPurchaseItemCount;
	}

	public void setMaxPurchaseItemCount(int maxPurchaseItemCount) {
		this.maxPurchaseItemCount = maxPurchaseItemCount;
	}

	public int getMaxQuantityPerItem() {
		return maxQuantityPerItem;
	}

	public void setMaxQuantityPerItem(int maxQuantityPerItem) {
		this.maxQuantityPerItem = maxQuantityPerItem;
	}

	public String getPublishInventoryTopicPrefix() {
		return publishInventoryTopicPrefix;
	}

	public void setPublishInventoryTopicPrefix(String publishInventoryTopicPrefix) {
		this.publishInventoryTopicPrefix = publishInventoryTopicPrefix;
	}
}

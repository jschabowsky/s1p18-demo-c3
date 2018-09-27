/*
 * 
 * Copyright (c) 2018 Solace Corp.
 * 
 */
package org.springframework.cloud.stream.app.utah.transaction.geo.processor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for UtahTransactionGeoProcessorConfiguration
 *
 * @author Solace Corp.
 */
@ConfigurationProperties("utah.transaction.geo")
public class UtahTransactionGeoProcessorProperties {	
	public static final boolean DEFAULT_LOG_CACHE_HITS = true;
	
	/**
	 * Flag to enable logging of cache hits for geocoded addresses
	 */
	private boolean logCacheHits = DEFAULT_LOG_CACHE_HITS;

	public boolean isLogCacheHits() {
		return logCacheHits;
	}

	public void setLogCacheHits(boolean logCacheHits) {
		this.logCacheHits = logCacheHits;
	}

}

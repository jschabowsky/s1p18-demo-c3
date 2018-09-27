/*
 * 
 * Copyright (c) 2018 Solace Corp.
 * 
 */
package org.springframework.cloud.stream.app.utah.transaction.gen.source;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.solace.demo.utahdabc.datamodel.ProductInventoryData;
import com.solace.demo.utahdabc.datamodel.StoreTransaction;
import com.solace.demo.utahdabc.datamodel.StoreTransaction.PurchaseLineItem;

import reactor.core.publisher.Flux;
import org.springframework.cloud.stream.reactive.StreamEmitter;

/**
 * See README.adoc 
 *
 * @author Solace Corp
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(UtahTransactionGenSourceProperties.class)
public class UtahTransactionGenSourceConfiguration {
	private static final Log LOG = LogFactory.getLog(UtahTransactionGenSourceConfiguration.class);	
	private static final Random _random = new Random();
    private static final String INVENTORY_CACHE_KEY = "UT_INVENTORY";
    private static final int MAX_TXGEN_RETRY_COUNT = 5;
    
    private int txGenRetryCount = 0;
    
	@Bean
	public RedisOperations<String, Object> redisTemplate(RedisConnectionFactory rcf) {
		final RedisTemplate<String, Object> template =  new RedisTemplate<String, Object>();
		template.setConnectionFactory(rcf);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		return template;
	}
	    
	@Autowired
	private UtahTransactionGenSourceProperties properties;
	
	@Autowired
	private RedisOperations<String, Object> redisOps;

	@StreamEmitter
	@Output(Source.OUTPUT)
	public Flux<StoreTransaction> emit() {
		// Assuming store list won't change and we can cache it
		String[] stores = redisOps.opsForHash().keys(INVENTORY_CACHE_KEY).toArray(new String[0]);
		
		return Flux.interval(Duration.ofSeconds(properties.getEmitIntervalSec()))
				.map(l -> generateRandomTransaction(stores));
	}
	
	private StoreTransaction generateRandomTransaction(String[] stores) {
		StoreTransaction st = new StoreTransaction();
		String storeID = stores[_random.nextInt(stores.length)];
		st.setStoreID(storeID);

		Map<String, ProductInventoryData> storeProducts = 
				(HashMap<String, ProductInventoryData>)redisOps.opsForHash().get(INVENTORY_CACHE_KEY, storeID);
		
		if (storeProducts == null) {
			LOG.error("No products found for store: " + storeID);
			return null;
		}
		
		String[] products = storeProducts.keySet().toArray(new String[0]);
		// Assuming we have at least one product in the store - use it to get the address
		st.setStoreAddress(storeProducts.get(products[0]).getStoreInventory().getStoreAddress());

		float transactionTotal = 0;
		int maxItems = properties.getMaxPurchaseItemCount() > 0 ? properties.getMaxPurchaseItemCount() : 1;		
		int purchaseItemCount = _random.nextInt(maxItems) + 1;

		List<PurchaseLineItem> lineItems = new ArrayList<PurchaseLineItem>();			
		Set<String> selectedProductsTracker = new HashSet<String>();
		for(int i = 0; i < purchaseItemCount; i++) {
			// Select a random product
			String product = products[_random.nextInt(products.length)];

			// Check if we've seen this before
			if(selectedProductsTracker.contains(product))
				continue;
			else
				selectedProductsTracker.add(product);

			ProductInventoryData pid = storeProducts.get(product);
			int currentQty = pid.getStoreInventory().getProductQty();
			if (currentQty > 0) {
				int maxQty = Math.min(properties.getMaxQuantityPerItem(), currentQty);
				int randomQty = _random.nextInt(maxQty) + 1; 
				
				PurchaseLineItem li = StoreTransaction.createLineItem(
						product,
						randomQty,
						(float) pid.getProduct().getPrice());

				transactionTotal += li.getTotalLineAmount();
				lineItems.add(li);
				
				/* Currently assuming unlimited inventory - uncomment the following to update inventory in real-time
				pid.getStoreInventory().setProductQty(currentQty - randomQty);
				storeProducts.put(product, pid);
				redisOps.opsForHash().put(INVENTORY_CACHE_KEY, storeID, storeProducts);
				*/
				pid.refreshTimestamp();	// Ensure Kibana gets updated timeseries
			}
		}
		st.setProductsPurchased(lineItems.toArray(new PurchaseLineItem[0]));		
		st.setTotalTransactionAmount(transactionTotal);

		if (st.getProductsPurchased().length > 0) {
			LOG.info(st);
		} else if (txGenRetryCount++ < MAX_TXGEN_RETRY_COUNT) {
			st = generateRandomTransaction(stores);
		} else {
			String err = "Unable to generate new random transaction within retry limit."; 
			LOG.error(err);
			throw new IllegalStateException(err);
		}

		return st;
	}
}

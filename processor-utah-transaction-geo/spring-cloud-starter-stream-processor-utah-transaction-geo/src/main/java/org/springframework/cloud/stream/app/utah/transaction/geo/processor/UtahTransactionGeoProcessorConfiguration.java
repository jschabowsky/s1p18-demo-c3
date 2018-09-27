/*
 * 
 * Copyright (c) 2018 Solace Corp.
 * 
 */
package org.springframework.cloud.stream.app.utah.transaction.geo.processor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;

import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Flux;

import org.springframework.data.geo.Point;
import com.solace.demo.utahdabc.datamodel.StoreTransaction;


/**
 * See README.adoc 
 *
 * @author Solace Corp
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(UtahTransactionGeoProcessorProperties.class)
public class UtahTransactionGeoProcessorConfiguration {
	private static final Log LOG = LogFactory.getLog(UtahTransactionGeoProcessorConfiguration.class);	
	private static final String STATE_GEO_CACHE_KEY = "UT";

	@Autowired
	private UtahTransactionGeoProcessorProperties properties;
	
	@Autowired
	private RedisOperations<String, Object> redisOps;

	@Bean
	public RedisOperations<String, Object> redisTemplate(RedisConnectionFactory rcf) {
		final RedisTemplate<String, Object> template =  new RedisTemplate<String, Object>();
		template.setConnectionFactory(rcf);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		return template;
	}

	@StreamListener
	@Output(Processor.OUTPUT)
    public Flux<StoreTransaction> process(@Input(Processor.INPUT) Flux<StoreTransaction> input) {
		return input.map(tx -> {
			List<Point> positions = redisOps.opsForGeo().position(STATE_GEO_CACHE_KEY, tx.getStoreID());
			if (positions.isEmpty() || positions.get(0) == null) {
				LOG.error("Geocache miss for store: " + tx.getStoreID());
			} else {
				Point pt = positions.get(0);
				tx.getLocation().setLat(pt.getY());
				tx.getLocation().setLon(pt.getX());
				
				if(properties.isLogCacheHits()) {
					LOG.info("Geocoded tx: " + tx.getTransactionID() + " " + tx.getLocation());
				}
			}
 
			return tx;
		});
	}
}

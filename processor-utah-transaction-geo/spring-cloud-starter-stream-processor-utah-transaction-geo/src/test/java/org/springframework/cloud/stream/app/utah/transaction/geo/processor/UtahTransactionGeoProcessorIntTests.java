/*
 * 
 * Copyright (c) 2018 Solace Corp.
 * 
 */

package org.springframework.cloud.stream.app.utah.transaction.geo.processor;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;

import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.solace.demo.utahdabc.datamodel.StoreTransaction;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

/**
 * UtahTransactionGeoProcessor UTs
 *
 * @author Solace Corp.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public abstract class UtahTransactionGeoProcessorIntTests {

	@Autowired
	protected Processor channel;

	@Autowired
	protected MessageCollector collector;

	@SpringBootTest("utah.transaction.geo.logCacheHits=true")
	public static class UsingPropsTest extends UtahTransactionGeoProcessorIntTests {		
		private static final String RESULT_SUBSTRING = "{\"transactionID\":0,\"creationTimestamp\":\"";

		@Test
		public void usingPropsTest() throws InterruptedException {			

			StoreTransaction tx = new StoreTransaction();
			tx.setStoreAddress("671 S. Pleasant Grove Blvd");
			tx.setStoreID("0044");
			channel.input().send(new GenericMessage<StoreTransaction>(tx));
			assertThat(collector.forChannel(channel.output()), receivesPayloadThat(containsString(RESULT_SUBSTRING)));

		}
	}

	@SpringBootApplication
	public static class UtahTransactionGeoProcessorApplication {

	}

}

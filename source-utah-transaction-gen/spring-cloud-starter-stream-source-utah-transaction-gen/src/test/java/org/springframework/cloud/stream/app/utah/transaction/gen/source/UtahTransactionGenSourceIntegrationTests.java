/*
 * 
 * Copyright (c) 2018 Solace Corp.
 * 
 */

package org.springframework.cloud.stream.app.utah.transaction.gen.source;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;


/**
 * Integration Tests for UtahTransactionGenSource
 *
 * @author Solace Corp.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public abstract class UtahTransactionGenSourceIntegrationTests {

	@Autowired
	protected Source channel;

	@Autowired
	protected MessageCollector collector;

	@SpringBootTest("utah.transaction.gen.emitIntervalSec=2")
	public static class EmitIntervalTest extends UtahTransactionGenSourceIntegrationTests {		
		private static final String RESULT_SUBSTRING = "{\"transactionID\":";

		@Test
		public void emitIntervalTest() throws InterruptedException {			
			Message<?> msg = collector.forChannel(channel.output()).poll(30, TimeUnit.SECONDS);
			Object payload = (msg != null) ? msg.getPayload() : null; 
			
			assertTrue(startsWith(RESULT_SUBSTRING).matches(payload));
		}
	}

	@SpringBootApplication
	public static class UtahTransactionGenSourceApplication {

	}

}

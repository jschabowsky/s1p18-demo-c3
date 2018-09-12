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

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;


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

	@SpringBootTest("utah.transaction.gen.emitIntervalSec=10")
	public static class EmitIntervalTest extends UtahTransactionGenSourceIntegrationTests {		
		private static final String RESULT_SUBSTRING = "{\"transactionID\":0,\"creationTimestamp\":\"";

		@Test
		public void emitIntervalTest() throws InterruptedException {			
			assertThat(collector.forChannel(channel.output()), receivesPayloadThat(startsWith(RESULT_SUBSTRING)));
		}
	}

	@SpringBootApplication
	public static class UtahTransactionGenSourceApplication {

	}

}

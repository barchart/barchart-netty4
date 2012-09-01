/**
 * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package osgi;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.service.event.Event;

import com.barchart.osgi.event.api.EventUtil;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class TestEvents extends TestAny {

	static final String TOPIC = UUID.randomUUID().toString();

	@Override
	public void testActivate() throws Exception {

		super.testActivate();

		registerTopic(TOPIC);

	}

	@Override
	public void testDeactivate() throws Exception {

		super.testDeactivate();

	}

	@Test
	public void testEvents() throws Exception {

		eventService.send(TOPIC);

		assertEquals(eventCount, 1);

	}

	private int eventCount;

	@Override
	public void handleEvent(final Event event) {

		log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		super.handleEvent(event);
		log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		assertTrue(EventUtil.is(event, TOPIC));

		eventCount++;

	}

}

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

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.barchart.osgi.event.api.EventAdminService;
import com.barchart.osgi.event.api.EventUtil;

@Ignore
@RunWith(PaxExam.class)
public class TestEvents extends TestAny implements EventHandler {

	static final String TOPIC = UUID.randomUUID().toString();

	@Inject
	protected EventAdminService eventService;

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
		log.info("### event topic : {}", event.getTopic());
		log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		assertTrue(EventUtil.is(event, TOPIC));

		eventCount++;

	}

}

/**
 * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.host;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.osgi.event.api.EventService;
import com.barchart.osgi.event.api.EventUtil;
import com.barchart.osgi.factory.test.Tidget;
import com.barchart.osgi.factory.test.TidgetComponent1;
import com.barchart.osgi.factory.test.TidgetManager;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class TestOSGI implements EventHandler {

	private final static Logger log = LoggerFactory.getLogger(TestOSGI.class);

	@Configuration
	public Option[] config() {

		return options(

				systemTimeout(5 * 1000),

				junitBundles(),

				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.configadmin")
						.versionAsInProject(),

				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.scr")
						.versionAsInProject(),

				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.eventadmin")
						.versionAsInProject(),

				mavenBundle().groupId("com.carrotgarden.osgi")
						.artifactId("carrot-osgi-anno-scr-core")
						.versionAsInProject(),

				//

				mavenBundle().groupId("io.netty").artifactId("netty")
						.versionAsInProject(),

				mavenBundle().groupId("com.typesafe").artifactId("config")
						.versionAsInProject(),

				mavenBundle().groupId("com.barchart.osgi")
						.artifactId("barchart-osgi-event").versionAsInProject(),

				mavenBundle().groupId("com.barchart.osgi")
						.artifactId("barchart-osgi-factory")
						.versionAsInProject(),

				mavenBundle().groupId("org.apache.sling")
						.artifactId("org.apache.sling.commons.threads")
						.versionAsInProject(),

				mavenBundle().groupId("com.barchart.netty")
						.artifactId("barchart-netty-util").versionAsInProject(),

				// workingDirectory("/work/git/barchart-osgi/barchart-osgi-factory/target/exam"),

				// keepCaches(),

				bundle("reference:file:target/classes")

		);

	}

	@Inject
	private BundleContext context;

	@Inject
	private EventAdmin eventAdmin;

	@Inject
	private EventService eventService;

	@Inject
	private TidgetManager manager;

	@Before
	public void init() {
	}

	@After
	public void done() {
	}

	static final String TOPIC = UUID.randomUUID().toString();

	@Test
	public void test() throws Exception {

		log.info("################################");

		log.info("### curren bundle " + context.getBundle().getSymbolicName());

		for (final Bundle bundle : context.getBundles()) {
			log.info("### active bundle : " + bundle.getSymbolicName());
		}

		assertNotNull(eventAdmin);

		assertNotNull(eventService);

		assertNotNull(manager);

		//

		{

			final Map<String, String> propsIn = new HashMap<String, String>();

			final Tidget tidget = manager.create(TidgetComponent1.FACTORY,
					propsIn);

			assertNotNull(tidget);

			final Map<String, String> propsOut = tidget.getProps();

			log.debug("### propsOut : {}", propsOut);

			final Map<String, String> descriptor = manager
					.getFactoryDescriptor(TidgetComponent1.FACTORY);

			log.debug("### descriptor : {}", descriptor);

		}

		{

			final Dictionary<String, Object> props = new Hashtable<String, Object>();
			props.put(EventConstants.EVENT_TOPIC, TOPIC);

			@SuppressWarnings("rawtypes")
			final ServiceRegistration reg = context.registerService(
					EventHandler.class.getName(), this, props);

			eventService.send(TOPIC);

			assertEquals(eventCount, 1);

			reg.unregister();

		}

		log.info("################################");

	}

	private int eventCount;

	@Override
	public void handleEvent(final Event event) {

		log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		assertTrue(EventUtil.is(event, TOPIC));

		eventCount++;

	}

}

/**
 * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package osgi;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.host.api.NettyDotManager;
import com.barchart.netty.matrix.api.Matrix;
import com.barchart.osgi.conf.api.ConfigAdminService;
import com.barchart.osgi.event.api.EventService;

/** shared test setup */
public abstract class TestAny implements EventHandler {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Configuration
	public Option[] config() {

		return options(

				systemTimeout(3 * 1000),

				systemProperty("java.net.preferIPv4Stack").value("true"),

				// systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
				// .value("INFO"),

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

				wrappedBundle(mavenBundle().groupId("io.netty")
						.artifactId("netty").versionAsInProject()),

				mavenBundle().groupId("com.typesafe").artifactId("config")
						.versionAsInProject(),

				mavenBundle().groupId("com.barchart.osgi")
						.artifactId("barchart-osgi-conf").versionAsInProject(),

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

				//

				bundle("reference:file:target/classes")

		);

	}

	@Inject
	protected BundleContext context;

	@Inject
	protected EventService eventService;

	@Inject
	protected ConfigAdminService configAdmin;

	@Inject
	protected NettyDotManager manager;

	@Inject
	protected Matrix matrix;

	@Before
	public void testActivate() throws Exception {

		log.info("#######################################################################");

		log.info("### java.net.preferIPv4Stack={}",
				System.getProperty("java.net.preferIPv4Stack"));

		log.info("### curren bundle " + context.getBundle().getSymbolicName());

		for (final Bundle bundle : context.getBundles()) {
			log.info("### active bundle : " + bundle.getSymbolicName());
		}

		assertNotNull(configAdmin);

		assertNotNull(eventService);

		assertNotNull(manager);

		assertNotNull(matrix);

	}

	@After
	public void testDeactivate() throws Exception {

		log.info("#######################################################################");

	}

	protected void registerTopic(final String topic) {

		final String name = EventHandler.class.getName();

		final Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(EventConstants.EVENT_TOPIC, topic);

		context.registerService(name, this, props);

	}

	@Override
	public void handleEvent(final Event event) {

		log.info("### event topic : {}", event.getTopic());

	}

}

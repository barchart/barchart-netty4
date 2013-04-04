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

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAny {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private static final String PAX_LEVEL = "DEBUG";

	@Configuration
	public Option[] config() {

		log.debug("### config");

		return options(

				systemTimeout(3 * 1000),

				systemPackage("com.sun.nio.sctp"),

				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value(PAX_LEVEL),

				junitBundles(),

				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.configadmin")
						.versionAsInProject(),
				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.eventadmin")
						.versionAsInProject(),
				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.scr")
						.versionAsInProject(),

				//

				mavenBundle().groupId("org.ops4j.pax.logging")
						.artifactId("pax-logging-api").versionAsInProject(),
				mavenBundle().groupId("org.ops4j.pax.logging")
						.artifactId("pax-logging-service").versionAsInProject(),
				//

				wrappedBundle(mavenBundle().groupId("io.netty")
						.artifactId("netty-common").versionAsInProject()),
				wrappedBundle(mavenBundle().groupId("io.netty")
						.artifactId("netty-codec").versionAsInProject()),
				wrappedBundle(mavenBundle().groupId("io.netty")
						.artifactId("netty-buffer").versionAsInProject()),
				wrappedBundle(mavenBundle().groupId("io.netty")
						.artifactId("netty-handler").versionAsInProject()),
				wrappedBundle(mavenBundle().groupId("io.netty")
						.artifactId("netty-transport").versionAsInProject()),
				wrappedBundle(mavenBundle().groupId("io.netty")
						.artifactId("netty-transport-sctp")
						.versionAsInProject()),
				wrappedBundle(mavenBundle().groupId("io.netty")
						.artifactId("netty-transport-udt").versionAsInProject()),

				//

				mavenBundle().groupId("com.typesafe").artifactId("config")
						.versionAsInProject(),

				mavenBundle().groupId("com.barchart.osgi")
						.artifactId("barchart-osgi-conf").versionAsInProject(),

				mavenBundle().groupId("com.barchart.osgi")
						.artifactId("barchart-osgi-event").versionAsInProject(),

				mavenBundle().groupId("com.barchart.osgi")
						.artifactId("barchart-osgi-factory-ca")
						.versionAsInProject(),

				//

				mavenBundle().groupId("com.barchart.netty")
						.artifactId("barchart-netty-util").versionAsInProject(),

				mavenBundle().groupId("com.barchart.proto")
						.artifactId("barchart-proto-buf-data")
						.versionAsInProject(),
				mavenBundle().groupId("com.barchart.proto")
						.artifactId("barchart-proto-buf-wrap")
						.versionAsInProject(),

				//

				mavenBundle()
						.groupId("org.apache.servicemix.bundles")
						.artifactId(
								"org.apache.servicemix.bundles.protobuf-java")
						.versionAsInProject(),

				//

				mavenBundle().groupId("com.barchart.conf")
						.artifactId("barchart-conf-util").versionAsInProject(),
				mavenBundle().groupId("com.barchart.conf")
						.artifactId("barchart-conf-list").versionAsInProject(),

				//

				mavenBundle().groupId("com.barchart.util")
						.artifactId("barchart-util-ascii").versionAsInProject(),
				mavenBundle().groupId("com.barchart.util")
						.artifactId("barchart-util-collections")
						.versionAsInProject(),
				mavenBundle().groupId("com.barchart.util")
						.artifactId("barchart-util-math").versionAsInProject(),
				mavenBundle().groupId("com.barchart.util")
						.artifactId("barchart-util-thread")
						.versionAsInProject(),
				mavenBundle().groupId("com.barchart.util")
						.artifactId("barchart-util-values")
						.versionAsInProject(),

				mavenBundle().groupId("joda-time").artifactId("joda-time")
						.versionAsInProject(),

				mavenBundle().groupId("com.barchart.udt")
						.artifactId("barchart-udt-bundle").versionAsInProject(),

				//

				bundle("reference:file:target/classes")

		);

	}

	@Inject
	private BundleContext context;

	@Inject
	private ConfigurationAdmin configAdmin;

	@Before
	public void testActivate() throws Exception {

		assertNotNull(context);
		assertNotNull(configAdmin);

		loggingActivate(configAdmin);

		log.info("#########################################");
		log.info("###              ACTIVATE             ###");

		// for (final Bundle bundle : context.getBundles()) {
		// log.info("### bundle : {}", bundle.getSymbolicName());
		// }

	}

	@After
	public void testDeactivate() throws Exception {

		log.info("###             DEACTIVATE            ###");
		log.info("#########################################");

		loggingDeactivate(configAdmin);

	}

	private static final String PAX_PID = "org.ops4j.pax.logging";
	private static final String PAX_SERVICE = "org.ops4j.pax.logging.PaxLoggingService";

	private void loggingActivate(final ConfigurationAdmin configAdmin)
			throws Exception {

		final URL propsURL = TestAny.class.getResource("/log4j.properties");

		final Properties props = new Properties();

		props.load(propsURL.openStream());

		final org.osgi.service.cm.Configuration config = configAdmin
				.getConfiguration(PAX_PID, null);

		config.update((Dictionary) props);

		final ServiceTracker tracker = new ServiceTracker(context, PAX_SERVICE,
				null);

		tracker.open(true);

		final Object service = tracker.waitForService(3 * 1000);

		assertNotNull(service);

		Thread.sleep(500);

	}

	private void loggingDeactivate(final ConfigurationAdmin configAdmin)
			throws Exception {

		final org.osgi.service.cm.Configuration config = configAdmin
				.getConfiguration(PAX_PID, null);

		config.delete();

		Thread.sleep(500);

	}

	protected void registerTopic(final String topic) {

		final String name = EventHandler.class.getName();

		final Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(EventConstants.EVENT_TOPIC, topic);

		context.registerService(name, this, props);

	}

}

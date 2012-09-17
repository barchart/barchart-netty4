/**
 * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package osgi;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.host.api.NettyDotManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/** FIXME add some traffic */
@RunWith(JUnit4TestRunner.class)
public class TestMulticastArbiter extends TestAny {

	@Inject
	private NettyDotManager manager;

	@Override
	public void testActivate() throws Exception {

		super.testActivate();

	}

	@Override
	public void testDeactivate() throws Exception {

		Thread.sleep(3 * 1000);

		super.testDeactivate();

	}

	@Test
	public void testMulticast() throws Exception {

		final Config confArbiter = ConfigFactory.load("case-03/arbiter.conf");

		{

			/** source one */

			final Config conf = confArbiter.getConfig("source-1");

			final NettyDot dot = manager.create(conf);

			assertNotNull(dot);

		}

		{

			/** source two */

			final Config conf = confArbiter.getConfig("source-2");

			final NettyDot dot = manager.create(conf);

			assertNotNull(dot);

		}

		{

			/** target */

			final Config conf = confArbiter.getConfig("target");

			final NettyDot dot = manager.create(conf);

			assertNotNull(dot);

		}

	}

}

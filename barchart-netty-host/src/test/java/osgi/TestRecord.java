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

@RunWith(JUnit4TestRunner.class)
public class TestRecord extends TestAny {

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

		{

			/** sequence writer */

			final Config config = ConfigFactory.load("case-05/point-0.conf")
					.getConfig("point");

			final NettyDot service = manager.create(config);

			assertNotNull(service);

		}

		{

			/** sequence reader */

			final Config config = ConfigFactory.load("case-05/point-1.conf")
					.getConfig("point");

			final NettyDot service = manager.create(config);

			assertNotNull(service);

		}

		{

			/** file recorder */

			final Config config = ConfigFactory.load("case-05/point-2.conf")
					.getConfig("point");

			final NettyDot service = manager.create(config);

			assertNotNull(service);

		}

	}

}
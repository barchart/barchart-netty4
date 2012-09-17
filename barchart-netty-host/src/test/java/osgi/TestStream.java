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
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.host.api.NettyDotManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class TestStream extends TestAny {

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
	public void testStreams() throws Exception {

		{

			/** echo server */

			final Config config = ConfigFactory.load("case-02/point-0.conf")
					.getConfig("point");

			final NettyDot service = manager.create(config);

			assertNotNull(service);

		}

		{

			/** echo client */

			final Config config = ConfigFactory.load("case-02/point-1.conf")
					.getConfig("point");

			final NettyDot service = manager.create(config);

			assertNotNull(service);

		}

	}

}

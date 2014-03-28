/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.util.point;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TestNetUtil {

	static final Logger log = LoggerFactory.getLogger(TestNetUtil.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPointForm() {

		final Config conf = ConfigFactory.parseString("{ "
				+ "local-address = datalan/12345, " //
				+ "remote-address = feedlan 23456, " //
				+ "packet-ttl = 33, " //
				+ "pipeline = advanced, " //
				+ "custom1 = signature, " //
				+ "custom2 = 1000, " //
				+ "custom3 = [ 1, 2, 3 ], " //
				+ "}");

		final NetPoint point = NetPoint.from(conf);

		log.debug("point : {}", point);

		assertEquals("datalan", point.getLocalAddress().getHost());
		assertEquals(12345, point.getLocalAddress().getPort());

		assertEquals("feedlan", point.getRemoteAddress().getHost());
		assertEquals(23456, point.getRemoteAddress().getPort());

		assertEquals(33, point.getPacketTTL());

		assertEquals("advanced", point.getPipeline());

		//

		assertEquals("signature", point.getString("custom1", null));

		assertEquals(1000, point.getInt("custom2", 0));

		final ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		assertEquals(list, point.getUniformList("custom3", Integer.class));

	}

}

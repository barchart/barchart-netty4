package com.barchart.netty.util.point;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNetPoint {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test0() {

		final NetPoint point = NetPoint.from("");

		log.info("point : \n{}", point);

		assertEquals("invalid", point.getId());
		assertEquals("barchart.netty.dot.invalid", point.getType());
		assertEquals("barchart.netty.pipe.invalid", point.getPipeline());

		assertEquals(3, point.getPacketTTL());
		assertEquals(262144, point.getReceiveBufferSize());
		assertEquals(262144, point.getSendBufferSize());

		assertEquals(NetAddress.formTuple("localhost:0"),
				point.getLocalAddress());
		assertEquals(NetAddress.formTuple("localhost:0"),
				point.getRemoteAddress());

	}

	@Test
	public void test1() {

		final NetPoint point1 = NetPoint.from("");

		log.info("point1 : \n{}", point1);

		assertEquals("invalid", point1.getId());

		final NetPoint point2 = point1.with("custom-text", "value");
		log.info("point2 : \n{}", point2);

		assertEquals("value", point2.getString("custom-text", null));

		final NetPoint point3 = point2.with("custom-num", 123);
		log.info("point3 : \n{}", point3);

		assertEquals(123, point3.getInt("custom-num", 0));

	}

}

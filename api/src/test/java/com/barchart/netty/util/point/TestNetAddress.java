package com.barchart.netty.util.point;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNetAddress {

	static final Logger log = LoggerFactory.getLogger(TestNetAddress.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFormTuple() {

		assertEquals(NetAddress.formTuple(null),
				NetAddress.formTuple("0.0.0.0/0"));
		assertEquals(NetAddress.formTuple(null),
				NetAddress.formTuple("0.0.0.0:0"));
		assertEquals(NetAddress.formTuple(null),
				NetAddress.formTuple("0.0.0.0 0"));

		assertEquals(NetAddress.formTuple("localhost"),
				NetAddress.formTuple("localhost/0"));
		assertEquals(NetAddress.formTuple("localhost"),
				NetAddress.formTuple("localhost:0"));
		assertEquals(NetAddress.formTuple("localhost"),
				NetAddress.formTuple("localhost 0"));

		assertEquals(NetAddress.formTuple("host/12345").getHost(), "host");
		assertEquals(NetAddress.formTuple("host : 12345").getHost(), "host");
		assertEquals(NetAddress.formTuple("host     12345").getHost(), "host");

		assertEquals(NetAddress.formTuple("host/12345").getPort(), 12345);
		assertEquals(NetAddress.formTuple("host:12345").getPort(), 12345);
		assertEquals(NetAddress.formTuple("host    12345").getPort(), 12345);

		assertEquals(NetAddress.formTuple("host ").getHost(), "host");
		assertEquals(NetAddress.formTuple("host  ").getPort(), 0);

		assertEquals(NetAddress.formTuple("host/xxx").getHost(), "host");
		assertEquals(NetAddress.formTuple("host:xxx").getHost(), "host");
		assertEquals(NetAddress.formTuple("host : xxx123").getHost(), "host");

		assertEquals(NetAddress.formTuple("host/xxx").getPort(), 0);
		assertEquals(NetAddress.formTuple("host : xxx").getPort(), 0);
		assertEquals(NetAddress.formTuple("host : xxx123").getPort(), 0);

		//

		assertEquals(NetAddress.formTuple("host : 12345"),
				NetAddress.formTuple("host / 12345"));

		assertEquals("host:12345", NetAddress.formTuple("host :/\t 12345")
				.toString());

	}

}

package com.barchart.netty.util.entry;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TestEntry {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test0() {

		final String hocon = "{ id = hello, type = world }";

		final Config config = ConfigFactory.parseString(hocon);

		final Entry entry = new Entry(config) {
		};

		log.info("entry : \n{}", entry);

		assertEquals(entry.getString("id", null), "hello");
		assertEquals(entry.getString("type", null), "world");
		assertEquals(entry.getString("none", "nada"), "nada");

	}

	@Test
	public void test1() {

		final String hocon = "{ " + //
				"id = hello, " + //
				"type = world, " + //
				"list = [ chicago, new york ]" + //
				" }";

		final Config config = ConfigFactory.parseString(hocon);

		final Entry entry = new Entry(config) {
		};

		log.info("entry : \n{}", entry);

		assertEquals(entry.getString("id", null), "hello");
		assertEquals(entry.getString("type", null), "world");

		final List<String> list = entry.getUniformList("list", String.class);

		assertEquals(2, list.size());
		assertEquals("chicago", list.get(0));
		assertEquals("new york", list.get(1));

	}

	@Test
	public void test2() {

		final String hocon = "{ " + //
				"id = hello, " + //
				"type = world, " + //
				"list = [ 123, 456 ]" + //
				" }";

		final Config config = ConfigFactory.parseString(hocon);

		final Entry entry = new Entry(config) {
		};

		log.info("entry : \n{}", entry);

		assertEquals(entry.getString("id", null), "hello");
		assertEquals(entry.getString("type", null), "world");

		final List<Integer> list = entry.getUniformList("list", Integer.class);

		assertEquals(2, list.size());
		assertEquals(123, list.get(0).intValue());
		assertEquals(456, list.get(1).intValue());

	}

}

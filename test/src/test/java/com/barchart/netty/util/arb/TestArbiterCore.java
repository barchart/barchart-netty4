package com.barchart.netty.util.arb;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestArbiterCore {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	final static Object value = new Object();

	/** basic fill */
	@Test
	public void test0() {

		final Integer value0 = new Integer(0);
		final Integer value1 = new Integer(0);
		final Integer value2 = new Integer(0);

		final ArbiterCore<Object> arbiter = new ArbiterCore<Object>();

		assertFalse(arbiter.isReady());

		arbiter.fill(1000, value0);
		assertEquals(1, arbiter.countFilled);

		assertFalse(arbiter.isReady());

		arbiter.fill(1001, value1);
		assertEquals(2, arbiter.countFilled);

		assertFalse(arbiter.isReady());

		arbiter.fill(1002, value2);
		assertEquals(3, arbiter.countFilled);

		assertFalse(arbiter.isReady());

		final List<Object> list = new ArrayList<Object>();

		arbiter.drainTo(list);

		assertFalse(arbiter.isReady());

		assertEquals(3, list.size());

		assertTrue(value0 == list.get(0));
		assertTrue(value1 == list.get(1));
		assertTrue(value2 == list.get(2));

	}

	/** fill dups */
	@Test
	public void test1() {

		final ArbiterCore<Object> arbiter = new ArbiterCore<Object>();

		final Integer value0 = new Integer(0);
		final Integer value1 = new Integer(0);
		final Integer value2 = new Integer(0);

		arbiter.fill(1000, value0);
		assertEquals(1, arbiter.countFilled);
		assertEquals(0, arbiter.countDuplicate);

		arbiter.fill(1000, value1);
		assertEquals(1, arbiter.countFilled);
		assertEquals(1, arbiter.countDuplicate);

		arbiter.fill(1000, value2);
		assertEquals(1, arbiter.countFilled);
		assertEquals(2, arbiter.countDuplicate);

		final List<Object> list = new ArrayList<Object>();

		arbiter.drainTo(list);

		assertEquals(1, list.size());

		assertTrue(value0 == list.get(0));

	}

	/** fill hole */
	@Test
	public void test2() {

		final ArbiterCore<Object> arbiter = new ArbiterCore<Object>();

		arbiter.reset(1000);

		assertFalse(arbiter.isReady());

		final Integer value0 = new Integer(0);
		final Integer value1 = new Integer(0);
		final Integer value2 = new Integer(0);

		arbiter.fill(1000, value0);
		assertEquals(1, arbiter.countFilled);
		assertEquals(0, arbiter.countDuplicate);

		assertTrue(arbiter.isReady());

		arbiter.fill(1000, value1);
		assertEquals(1, arbiter.countFilled);
		assertEquals(1, arbiter.countDuplicate);

		assertTrue(arbiter.isReady());

		arbiter.fill(1002, value2);
		assertEquals(2, arbiter.countFilled);

		assertFalse(arbiter.isReady());

		final List<Object> list = new ArrayList<Object>();

		arbiter.drainTo(list);

		assertEquals(2, list.size());

		assertTrue(value0 == list.get(0));
		assertTrue(value2 == list.get(1));

		assertEquals(1, arbiter.countLost);
		assertEquals(2, arbiter.countDrained);

	}

	/** fill order */
	@Test
	public void test3() {

		final ArbiterCore<Object> arbiter = new ArbiterCore<Object>();

		arbiter.reset(1000);

		assertFalse(arbiter.isReady());

		final Integer value0 = new Integer(0);
		final Integer value1 = new Integer(0);
		final Integer value2 = new Integer(0);

		arbiter.fill(1002, value2);

		assertFalse(arbiter.isReady());

		arbiter.fill(1000, value0);

		assertFalse(arbiter.isReady());

		arbiter.fill(1001, value1);

		assertTrue("full batch", arbiter.isReady());

		final List<Object> list = new ArrayList<Object>();

		arbiter.drainTo(list);

		assertFalse(arbiter.isReady());

		assertEquals(3, list.size());

		assertTrue(value0 == list.get(0));
		assertTrue(value1 == list.get(1));
		assertTrue(value2 == list.get(2));

		assertEquals(3, arbiter.countProcessed);
		assertEquals(3, arbiter.countFilled);
		assertEquals(0, arbiter.countDuplicate);
		assertEquals(0, arbiter.countLost);

	}

	/** fill gaps */
	@Test
	public void test4() {

		final Integer value0 = new Integer(0);
		final Integer value1 = new Integer(0);

		final ArbiterCore<Object> arbiter = new ArbiterCore<Object>();

		assertEquals(0, arbiter.numberGap);

		arbiter.reset(1000);

		arbiter.fill(1000, value0);

		assertEquals(1, arbiter.countFilled);

		assertEquals(0, arbiter.numberGap);

		final int jump = 1000 + arbiter.storeSize;

		arbiter.fill(jump, value1);

		assertEquals(1, arbiter.numberGap);

		assertEquals(2, arbiter.countFilled);
		assertEquals(0, arbiter.countDrained);
		assertEquals(1, arbiter.countLost);

		assertEquals(jump + 0, arbiter.keyOriginal);
		assertEquals(jump + 0, arbiter.keyAdvanced);
		assertEquals(jump + 1, arbiter.keyExpected);

	}

}

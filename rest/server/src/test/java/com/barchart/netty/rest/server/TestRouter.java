package com.barchart.netty.rest.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.barchart.netty.rest.server.Router;
import com.barchart.netty.rest.server.Router.URLPattern;

public class TestRouter {

	@Test
	public void testFlat() {

		final TestHandler root = new TestHandler("root");
		final TestHandler account = new TestHandler("account");
		final TestHandler permissions = new TestHandler("permissions");
		final TestHandler prefix = new TestHandler("prefix");
		final TestHandler permission = new TestHandler("permission");

		final Router router = new Router().add(null, root) //
				.add("/{id}", account) //
				.add("/{id}/permissions", permissions) //
				.add("/{id}/permissions/{prefix}", prefix) //
				.add("/{id}/permission/{token}", permission);

		assertEquals(root, router.match("/").handler());
		assertEquals(account, router.match("/1234").handler());
		assertEquals(account, router.match("/acds").handler());
		assertEquals(account, router.match("/acds/").handler());
		assertEquals(account, router.match("/acds/x").handler());
		assertEquals(permissions, router.match("/1234/permissions").handler());
		assertEquals(prefix, router
				.match("/1234/permissions/com.barchart.feed").handler());
		assertEquals(
				permission,
				router.match(
						"/1234/permission/com.barchart.feed.stream.symbols")
						.handler());

	}

	@Test
	public void testNested() {

		final TestHandler account = new TestHandler("account");
		final Router accounts = new Router().add("/{id}", account);

		final Router root = new Router().add("/accounts", accounts);

		assertEquals(accounts, root.match("/accounts/1234").handler());
		assertEquals(account, accounts.match("/1234").handler());

	}

	@Test
	public void testRequest() throws Exception {

		final TestHandler account = new TestHandler("account");
		final Router accounts = new Router().add("/{id}", account);

		final Router root = new Router().add("/accounts", accounts);

		root.handle(new TestRequest("/accounts/1234", "/accounts/1234", "1234"));

		assertEquals(1, account.requests);
		assertEquals(0, account.exceptions);

	}

	@Test
	public void testParams() throws Exception {

		final TestHandler account = new TestHandler("account");
		final Router accounts = new Router().add("/{id}", account);

		final Router root = new Router().add("/accounts", accounts);

		root.handle(new TestRequest("/accounts/1234", "/accounts/1234", "1234"));

		assertEquals(1, account.requests);
		assertEquals(0, account.exceptions);
		assertEquals(1, account.params.size());
		assertNotNull(account.params.get("id"));
		assertEquals(1, account.params.get("id").size());
		assertEquals("1234", account.params.get("id").get(0));

	}

	@Test
	public void testParamsAndStatic() throws Exception {

		final TestHandler h1 = new TestHandler("h1");
		final TestHandler h2 = new TestHandler("h2");
		final TestHandler h3 = new TestHandler("h3");

		final Router service = new Router() //
				.add("/{id}/{param}", h1) //
				.add("/{id}/reallylongstatic", h2) //
				.add("/{id}/static", h3); //

		final Router root = new Router().add("/service", service);

		root.handle(new TestRequest("/service/1/2", "/service/1/2", "user"));

		assertEquals(1, h1.requests);
		assertEquals(0, h2.requests);
		assertEquals(0, h3.requests);

		root.handle(new TestRequest("/service/1/reallylongstatic",
				"/service/1/reallylongstatic", "user"));

		assertEquals(1, h1.requests);
		assertEquals(1, h2.requests);
		assertEquals(0, h3.requests);

		root.handle(new TestRequest("/service/1/static", "/service/1/static",
				"user"));

		assertEquals(1, h1.requests);
		assertEquals(1, h2.requests);
		assertEquals(1, h3.requests);

	}

	@Test
	public void testSoloParamPrefix() throws Exception {

		final TestHandler h1 = new TestHandler("h1");
		final Router service = new Router().add("/{id}", h1);
		final Router root = new Router().add("/service", service);

		root.handle(new TestRequest("/service/xyz/sub", "/service/xyz/sub",
				"user"));

		assertEquals(1, h1.requests);

	}

	@Test
	public void testKeyEquivalence() throws Exception {

		final URLPattern u1 = new URLPattern("");
		final URLPattern u2 = new URLPattern("/settings");
		final URLPattern u3 = new URLPattern("/profiles");
		final URLPattern u4 = new URLPattern("/settings");

		assertEquals(u1, u1);
		assertEquals(u2, u2);
		assertEquals(u3, u3);
		assertEquals(u4, u4);
		assertEquals(u2, u4);

		assertNotEquals(u1, u2);
		assertNotEquals(u1, u3);
		assertNotEquals(u1, u4);
		assertNotEquals(u2, u3);
		assertNotEquals(u3, u4);

		assertEquals(u2.hashCode(), u4.hashCode());

		assertNotEquals(u1.hashCode(), u2.hashCode());
		assertNotEquals(u1.hashCode(), u3.hashCode());
		assertNotEquals(u1.hashCode(), u4.hashCode());
		assertNotEquals(u2.hashCode(), u3.hashCode());
		assertNotEquals(u3.hashCode(), u4.hashCode());

		assertEquals(0, u1.compareTo(u1));
		assertEquals(1, u1.compareTo(u2));
		assertEquals(1, u1.compareTo(u3));
		assertEquals(1, u1.compareTo(u4));
		assertEquals(0, u2.compareTo(u2));
		assertTrue(u2.compareTo(u3) > 1);
		assertEquals(0, u2.compareTo(u4));
		assertEquals(0, u3.compareTo(u3));
		assertTrue(u3.compareTo(u4) < 1);
		assertEquals(0, u4.compareTo(u4));

	}

}

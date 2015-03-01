package com.github.nill14.utils.init.impl;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.utils.init.api.IBeanInjector;
import com.github.nill14.utils.init.api.IPropertyResolver;
import com.github.nill14.utils.init.meta.Wire;

public class WireTest {

	private static final Logger log = LoggerFactory.getLogger(WireTest.class);
	private final IPropertyResolver resolver = IPropertyResolver.empty();
	
	@Test
	public void testStrawberry() {
		IBeanInjector injector = new BeanInjector(resolver);
		Strawberry strawberry = injector.wire(Strawberry.class);
		assertNotNull(strawberry);
	}
	
	@Test
	public void testOnion() {
		IBeanInjector injector = new BeanInjector(resolver);
		Onion onion = injector.wire(Onion.class);
		assertNotNull(onion.strawberry);
	}
	
	@Test
	public void testMango() {
		IBeanInjector injector = new BeanInjector(resolver);
		Mango mango = injector.wire(Mango.class);
		assertNotNull(mango.onion);
	}
	
	public static class Mango {
		@Inject
		@Wire
		Onion onion;
	}
	
	public static class Onion {
		@Inject
		@Wire
		Strawberry strawberry;
	}
	
	public static class Strawberry {
		final String hello = "hello";
	}

}
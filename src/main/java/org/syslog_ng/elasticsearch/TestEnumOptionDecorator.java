package org.syslog_ng.elasticsearch;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class TestEnumOptionDecorator extends TestOption {


	@Before
	public void setUp() throws Exception {		
		super.setUp();
		options.put("cluster", "syslog-ng");
	}

	@Test
	public void testNormal() {
		Option stringOption = new StringOption(owner, "cluster");
		Set<String> values = new HashSet<String>(Arrays.asList("syslog-ng", "test"));
		Option enumdecorator = new EnumOptionDecorator(stringOption, values);
		assertInitOptionSuccess(enumdecorator);
		
		options.put("cluster", "test");
		assertInitOptionSuccess(enumdecorator);
	}

	@Test
	public void testInvalidOption() {
		Option stringOption = new StringOption(owner, "cluster");
		Set<String> values = new HashSet<String>(Arrays.asList("syslog-ng", "test"));
		Option enumdecorator = new EnumOptionDecorator(stringOption, values);
		
		options.put("cluster", "invalid");
		assertInitOptionFailed(enumdecorator,
				"option cluster must be one of the following values: ['syslog-ng', 'test']");
		
		options.remove("cluster");
		assertEquals(null, stringOption.getValue());
		assertInitOptionFailed(enumdecorator,
				"option cluster must be one of the following values: ['syslog-ng', 'test']");
	}
}

package jfix.compiler;

import junit.framework.TestCase;
import org.junit.Assert;

public class TestCompiler extends TestCase {
	
	public void testJSPCompile() {
		JSPCompiler jspc = new JSPCompiler(TestTemplate.class);
		TestTemplate template = (TestTemplate) jspc.getObject();
		System.out.println(template.render("world").trim());
		Assert.assertEquals("Hello world-1. Hello world-2. Hello world-3.", template.render("world").trim());
	}
	
}

package org.genemania.util;

import org.genemania.AbstractTest;
import org.junit.Test;

public class ApplicationConfigTest extends AbstractTest {

	// __[test cases]__________________________________________________________
	@Test
	public void testToUnderscore() {
		assertEquals("ALLLOWERCASE", ApplicationConfig.toUnderscore("alllowercase"));
		assertEquals("ALLUPPERCASE", ApplicationConfig.toUnderscore("ALLUPPERCASE"));
		assertEquals("CAMEL_CASE1", ApplicationConfig.toUnderscore("camelCase1"));
		assertEquals("PASCAL_CASE1", ApplicationConfig.toUnderscore("PascalCase1"));
		assertEquals("CAMEL1", ApplicationConfig.toUnderscore("camel1"));
		assertEquals("ENRICHMENT_QVAL_THRESHOLD", ApplicationConfig.toUnderscore("enrichmentQValThreshold"));
	}
}

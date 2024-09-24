package fr.ifremer.scoop3.infra;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogTest {
	
	private static final Logger logger = LoggerFactory.getLogger(LogTest.class);

	@Test
	public void test() {
		
		String myString = "slf4j";
		logger.info("Ceci est un test unitaire pour {} au niveau info", myString);
		logger.trace("Ceci est un test unitaire pour {} au niveau trace", myString);
		Assert.assertTrue(true);
	}

}

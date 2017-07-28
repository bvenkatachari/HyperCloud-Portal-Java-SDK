package io.dchq.sdk.core.smoke.suiterunner;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
*
* @author Santosh Kumar.
* @since 1.0
*/


public class VlanSmokeSuiteRunner {
	
	protected final static Logger logger = LoggerFactory.getLogger(VlanSmokeSuiteRunner.class);
	
	public static void main(String[] args) {
	      Result result = JUnitCore.runClasses(VlanSmokeSuite.class);
	      for (Failure failure : result.getFailures()) {
	         logger.error("Error - "+failure.toString());
	      }
	      logger.info("Smoke test suite pass status - "+Boolean.toString(result.wasSuccessful()));
	   }
}

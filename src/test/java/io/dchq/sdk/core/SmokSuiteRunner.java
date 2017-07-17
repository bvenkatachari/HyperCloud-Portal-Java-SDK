package io.dchq.sdk.core;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class SmokSuiteRunner {
	
	public static void main(String[] args) {
	      Result result = JUnitCore.runClasses(SmokeSuite.class);
	      for (Failure failure : result.getFailures()) {
	         System.out.println(failure.toString());
	      }
	      System.out.println(result.wasSuccessful());
	   }
}

package io.dchq.sdk.core.smoke.testsuite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.dchq.sdk.core.workflow.CloudProviderFlow;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
* This class can be utilized for single test data only
* Method execution order depends on ascending order of method's name
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CloudProviderTestSuite {
	
	static CloudProviderFlow cloudProviderFlow;

	@BeforeClass
	public static void setUp() throws Exception { 
		cloudProviderFlow = new CloudProviderFlow();
	}
	
	
	@Test
	public void test1_testCloudProviderConnection() throws Exception {

		cloudProviderFlow.testConnection();
	}
	
	
	@AfterClass
	public static void cleanUp() {

	}
	
}

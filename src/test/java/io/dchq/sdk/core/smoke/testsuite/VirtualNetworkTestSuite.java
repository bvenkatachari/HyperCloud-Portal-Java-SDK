package io.dchq.sdk.core.smoke.testsuite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.dchq.sdk.core.workflow.VirtualNetworkFlow;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 * 
 * This class can be utilized for single test data only
 * Method execution order depends on ascending order of method's name
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VirtualNetworkTestSuite {


	
	static VirtualNetworkFlow networkFlow;

	@BeforeClass
	public static void setUp() throws Exception { 
		networkFlow = new VirtualNetworkFlow("cpu=1,memory=2GB,disk=20GB,generation=1","C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub1604HFT_Docker.vhdx");
	}

	
	@Test
	public void test1_createVlan() throws Exception {

		networkFlow.createVlan();
	}
	
	@Test
	public void test2_createVM() throws Exception {

		networkFlow.createDockerServer();
	}
	
	@Test
	public void test3_deployApp() throws Exception {

		networkFlow.deployApp();
	}
	
	@Test
	public void test4_createVolume() throws Exception {

		networkFlow.createVolume();
	}
	
	@Test
	public void test5_attachVolumeToDockerServer() throws Exception {

		networkFlow.attachVolumeToDockerServer();
	}
	
	@Test
	public void test6_detachVolumeToDockerServer() throws Exception {

		networkFlow.detachVolumeToDockerServer();
	}
	
	
	@AfterClass
	public static void cleanUp() {
		networkFlow.cleanUp();

	}
}
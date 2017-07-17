package io.dchq.sdk.core;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.dchq.sdk.core.vlan.VirtualNetworkCreateServiceTest;
import io.dchq.sdk.core.vpc.VPCCreateServiceTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	VPCCreateServiceTest.class, 
	VirtualNetworkCreateServiceTest.class})
public class TestSuite {

}

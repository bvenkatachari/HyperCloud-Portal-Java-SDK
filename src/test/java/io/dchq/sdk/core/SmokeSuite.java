package io.dchq.sdk.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.dchq.sdk.core.smoke.testsuite.VPCCreateServiceTest;
import io.dchq.sdk.core.smoke.testsuite.VPCUpdateServiceTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	VPCCreateServiceTest.class, 
	VPCUpdateServiceTest.class})
public class SmokeSuite {

}

package io.dchq.sdk.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	io.dchq.sdk.core.smoke.testsuite.VPCCreateServiceTest.class, 
	io.dchq.sdk.core.smoke.testsuite.VPCUpdateServiceTest.class})
public class SmokeSuite {

}

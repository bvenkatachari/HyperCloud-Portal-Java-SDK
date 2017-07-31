package io.dchq.sdk.core.smoke.suiterunner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.dchq.sdk.core.smoke.util.StopOnFirstFailureSuite;

/**
*
* @author Santosh Kumar.
* @since 1.0
*/

@RunWith(StopOnFirstFailureSuite.class)
@Suite.SuiteClasses({
	io.dchq.sdk.core.smoke.testsuite.CloudProviderTestSuite.class, 
	io.dchq.sdk.core.smoke.testsuite.VirtualNetworkTestSuite.class})
public class VlanSmokeSuite {

}

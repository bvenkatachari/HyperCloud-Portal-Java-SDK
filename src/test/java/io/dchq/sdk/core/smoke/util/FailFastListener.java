package io.dchq.sdk.core.smoke.util;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

/**
* @author Santosh Kumar.
* @since 1.0
*/
public class FailFastListener extends RunListener {

private RunNotifier runNotifier;

/**
* Allow this Listener to access runNotifier
*/
public FailFastListener(RunNotifier runNotifier) {
        super();
        this.runNotifier=runNotifier;
}


@Override
public void testFailure(Failure failure) throws Exception {
        this.runNotifier.pleaseStop();
}
}

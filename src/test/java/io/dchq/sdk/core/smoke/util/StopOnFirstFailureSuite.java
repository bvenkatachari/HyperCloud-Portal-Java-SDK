package io.dchq.sdk.core.smoke.util;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

/**
* @author Santosh Kumar.
* @since 1.0
*/
public class StopOnFirstFailureSuite extends Suite {

/**
 * 
 * @param klass
 * @param suiteClasses
 * @throws InitializationError
 */
public StopOnFirstFailureSuite(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        super(klass, suiteClasses);
}

public StopOnFirstFailureSuite(Class<?> klass) throws InitializationError {
        super(klass, klass.getAnnotation(SuiteClasses.class).value());
}



@Override
public void run(RunNotifier runNotifier) {
        runNotifier.addListener(new FailFastListener(runNotifier));
        super.run(runNotifier);
}
}

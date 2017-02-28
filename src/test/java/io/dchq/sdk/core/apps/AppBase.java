package io.dchq.sdk.core.apps;

import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.provision.App;


/**
 * Created by Saurabh Bhatia on 2/27/2017.
 */
public interface AppBase {

    public App deployAndWait(Blueprint blueprint);

    public void destroyAndWait(App app);

}

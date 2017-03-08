package io.dchq.sdk.core.network;

import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.provision.App;


/**
 * Created by Saurabh Bhatia on 2/27/2017.
 */
public interface NetworkBase {

    public App deployAndWait(Blueprint blueprint, boolean error, String validationMessage);

    public void destroyAndWait(App app);

}

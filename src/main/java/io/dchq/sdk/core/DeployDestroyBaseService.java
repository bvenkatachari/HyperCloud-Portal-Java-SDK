package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;


/**
 * Created by Saurabh Bhatia on 2/27/2017.
 */
public interface DeployDestroyBaseService {

    public ResponseEntity  deployAndWait (AppService app , BlueprintService blueprint);
    public  ResponseEntity destroyAndWait (AppService app);

}

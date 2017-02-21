package io.dchq.sdk.core.network;

import com.dchq.schema.beans.one.network.DockerNetwork;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.NetworkService;
import io.dchq.sdk.core.ServiceFactory;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

/**
 * @Author Saurabh Bhatia
 *
 */


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class NetworkCreateServiceTest extends AbstractServiceTest{

    private NetworkService networkService;

@Ignore
    @org.junit.Before
    public void setUp() throws Exception{
        networkService = ServiceFactory.buildNetworkService(rootUrl, username, password);
    }

    DockerNetwork network;
    DockerNetwork networkCreated;
    boolean error;
    String validationMessage;

    public NetworkCreateServiceTest (String Name) {


    }

}

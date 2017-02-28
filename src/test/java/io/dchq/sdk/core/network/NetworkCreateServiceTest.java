package io.dchq.sdk.core.network;

import static junit.framework.TestCase.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.host.Network;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import com.dchq.schema.beans.one.network.DockerNetwork;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.NetworkService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * @Author Saurabh Bhatia
 *
 */


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
@Ignore
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


    public NetworkCreateServiceTest (
    		String name,
    		String driver,
    		String entitledUser,
    		String server
    		) {

    	if (network == null) {
            throw new IllegalArgumentException("Volume == null");
        }
    	network.setName(name);
    	network.setDriver(driver);
    	network.setDockerServerName(server);
    }
    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
 	   List<UsernameEntityBase> entitledUser = new ArrayList<>();
 	   UsernameEntityBase user = new UsernameEntityBase();
 	   user.setUsername("Everyone");
 	   entitledUser.add(user);
        return Arrays.asList(new Object[][]{
                // TODO: add more test data for all sorts of validations
 				{ "testnetwork", "bridge",entitledUser,"qa-100(DockerEngine))"},
 				{ "test@@network", "bridge",entitledUser,"qa-100(DockerEngine))"},
 				{ "test-network", "bridge",entitledUser,"qa-100(DockerEngine))"}
        });
    }
    @Ignore
    public void createTest()
    {
    	logger.info("Create network name[{}] driver [{}] server [{}]", network.getName(), network.getDriver(), network.getDockerServerName());
        ResponseEntity<DockerNetwork> response = networkService.create(network);
        for (Message message : response.getMessages()) {
            logger.warn("Error while Create request  [{}] ", message.getMessageText());
        }
        if (response.getResults() != null) {
            this.networkCreated = response.getResults();
            logger.info("Create docker network Successful..");
        }
        assertNotNull(response);
        assertNotNull(response.isErrors());
    }

}

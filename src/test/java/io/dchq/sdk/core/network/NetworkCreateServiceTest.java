package io.dchq.sdk.core.network;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
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

    @org.junit.Before
    public void setUp() throws Exception{
        networkService = ServiceFactory.buildNetworkService(rootUrl, cloudadminusername, cloudadminpassword);
    }

    DockerNetwork network;
    DockerNetwork networkCreated;
    boolean error;
    String validationMessage;


    public NetworkCreateServiceTest (
    		String name,
    		String driver,
    		String server
    		) {
    	network = new DockerNetwork();
    	network.setName(name);
    	network.setDriver(driver);
    	network.setDockerServerName(server);
    }
    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][]{
 				{ "testnetwork", "bridge","qa-100(DockerEngine))"}
        });
    }
  
    @Ignore
	public void createTest() {

		logger.info("Create network name[{}] driver [{}] server [{}]", network.getName(), network.getDriver(),
				network.getDockerServerName());
		ResponseEntity<DockerNetwork> response = networkService.create(network);
		
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
		Assert.assertFalse(response.isErrors());

		if (response.getResults() != null && !response.isErrors()) {

			this.networkCreated = response.getResults();
			logger.info("Create docker network Successful..");
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		if (this.networkCreated != null) {
			assertNotNull(response.getResults().getId());
			assertNotNull(networkCreated.getId());
			assertEquals(network.getName(), networkCreated.getName());
			assertEquals(network.getDriver(), networkCreated.getDriver());
			assertEquals(network.getDockerServerName(), networkCreated.getDockerServerName());
		}
	}
    
    @After
    public void cleanUp() {
        if (this.networkCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = networkService.delete(this.networkCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}
		}
    }
}

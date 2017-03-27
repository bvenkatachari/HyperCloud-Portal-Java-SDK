package io.dchq.sdk.core.network;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
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
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.network.DockerNetwork;
import com.dchq.schema.beans.one.network.DockerNetworkStatus;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.NetworkService;
import io.dchq.sdk.core.ServiceFactory;

/**
* @author Jagdeep Jain
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class NetworkFindServiceTest extends AbstractServiceTest {

	private NetworkService networkService;

	@org.junit.Before
	public void setUp() throws Exception {
		networkService = ServiceFactory.buildNetworkService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	DockerNetwork network;
	DockerNetwork networkCreated;
	boolean error;
	String validationMessage;

	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50);

	public NetworkFindServiceTest(
			String name, 
			String driver,
			String id,
			boolean error
			) 
	{
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		name = prefix + "-" + name;
		network = new DockerNetwork();
		network.setName(name);
		network.setDriver(driver);
		network.setDockerServer(new NameEntityBase().withId(id));
		this.error = error;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			{ "testnetwork", "bridge", dockerServerId, true },
			{ "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@########EEEEEEEEEEEEEEEE", "bridge", dockerServerId , true},
			{ "as", "bridge", dockerServerId, true },
			{ "as", "bridge", "", false },
			{ "", "bridge", dockerServerId, false }});
	}
	@Ignore
	@Test
	public void createTest() {
		try {
			logger.info("Create network name as [{}] driver [{}] server [{}]", network.getName(), network.getDriver(),
					network.getDockerServer());
			ResponseEntity<DockerNetwork> response = networkService.create(network);
			if(error)
			{
				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}

				if (response.getResults() != null && !response.isErrors()) {
					this.networkCreated = response.getResults();
					logger.info("Create docker network Successful..");
				}

				while ((networkCreated.getStatus() != DockerNetworkStatus.LIVE)
						&& (System.currentTimeMillis() < endTime)) {
					try {
						Thread.sleep(5000);
						networkCreated = networkService.findById(networkCreated.getId()).getResults();
						logger.info("Network Status is [{}]", networkCreated.getStatus());
					} catch (InterruptedException e) {
						// TODO: handling exception
					}

					response = networkService.findById(networkCreated.getId());

					assertNotNull(response);
					assertNotNull(response.isErrors());
					if (this.networkCreated != null) {
						assertNotNull(response.getResults().getId());
						assertNotNull(networkCreated.getId());
						assertEquals(network.getName(), response.getResults().getName());
						assertEquals(network.getDriver(), response.getResults().getDriver());
						assertEquals(network.getDockerServerName(), response.getResults().getDockerServerName());
					}
				}
			}
			else
			{
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}
		} catch (Exception e) {
			// ignore
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

package io.dchq.sdk.core.network;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.blueprint.RegistryAccount;
import com.dchq.schema.beans.one.network.DockerNetwork;
import com.dchq.schema.beans.one.network.DockerNetworkStatus;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.NetworkService;
import io.dchq.sdk.core.ServiceFactory;

/**
* @author Jagdeep Jain
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class NetworkUpdateServiceTest extends AbstractServiceTest {

	private NetworkService networkService, networkService2;

	@org.junit.Before
	public void setUp() throws Exception {
		networkService = ServiceFactory.buildNetworkService(rootUrl, username, password);
		networkService2 = ServiceFactory.buildNetworkService(rootUrl, username2, password2);
	}

	DockerNetwork network;
	DockerNetwork networkCreated;
	boolean error;
	String validationMessage;
	String updatedName;

	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50);

	public NetworkUpdateServiceTest(
			String name, 
			String driver,
			String id
			) 
	{
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		String prefix2 = RandomStringUtils.randomAlphabetic(3);
		name = prefix + "-" + name;
		updatedName = prefix2 + "-" + name;
		network = new DockerNetwork();
		network.setName(name);
		network.setDriver(driver);
		network.setDockerServer(new NameEntityBase().withId(id));
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { { "testnetwork", "bridge", dockerServerId } });
	}

	@Test
	public void createTest() {
		try {
			logger.info("Create network name as [{}] driver [{}] server [{}]", network.getName(), network.getDriver(),
					network.getDockerServer());
			ResponseEntity<DockerNetwork> response = networkService.create(network);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (response.getResults() != null && !response.isErrors()) {
				this.networkCreated = response.getResults();
				logger.info("Create docker network Successful..");
			}

			while ((networkCreated.getStatus() != DockerNetworkStatus.LIVE) && (System.currentTimeMillis() < endTime)) {
				try {
					Thread.sleep(5000);
					networkCreated = networkService.findById(networkCreated.getId()).getResults();
					logger.info("Network Status is [{}]", networkCreated.getStatus());
				} catch (InterruptedException e) {
					// TODO: handling exception
				}
				assertNotNull(response);
				assertNotNull(response.isErrors());
				
				// updating entitlement
				UsernameEntityBase entitledUser = new UsernameEntityBase().withId(userId2);
				List<UsernameEntityBase> entiledUsers = new ArrayList<>();
				entiledUsers.add(entitledUser);
				networkCreated.setEntitlementType(EntitlementType.CUSTOM);
				networkCreated.setEntitledUsers(entiledUsers);
				
				response = networkService.update(networkCreated);
				
				logger.info("Entitlement Type [{}] and First name [{}]", response.getResults().getEntitlementType(),
						response.getResults().getEntitledUsers().get(0).getFirstname());

				for (Message message : response.getMessages()) {
					logger.warn("Error while Update request  [{}] ", message.getMessageText());
				}

				if (networkCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
					ResponseEntity<DockerNetwork> searchResponse = networkService2.findById(networkCreated.getId());
					assertNotNull(searchResponse);
					assertNotNull(searchResponse.isErrors());
					// TODO: add tests for testing error message
					assertNotNull(searchResponse.getResults());
					assertEquals(networkCreated.getName(), searchResponse.getResults().getName());
				}
				
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

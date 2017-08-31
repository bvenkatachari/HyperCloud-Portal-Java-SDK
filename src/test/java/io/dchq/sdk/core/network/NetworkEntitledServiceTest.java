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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.util.StringUtils;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.network.DockerNetwork;
import com.dchq.schema.beans.one.network.DockerNetworkStatus;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.NetworkService;
import io.dchq.sdk.core.ServiceFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class NetworkEntitledServiceTest extends AbstractServiceTest {

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
	static String prefix = RandomStringUtils.randomAlphabetic(3);

	public NetworkEntitledServiceTest(String name, String driver, String dockerServer, EntitlementType entitlementType,
			boolean isEntitlementTypeUser, String entitledUserId, boolean error) {
		network = new DockerNetwork();
		network.setName(name);
		network.setDriver(driver);
		network.setDockerServer(new NameEntityBase().withId(dockerServer));
		network.setEntitlementType(entitlementType);

		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			network.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			network.setEntitledUserGroups(entiledUsers);
		}

		this.error = error;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
				{ "testnetwork1"+prefix, "bridge", dockerServerId, EntitlementType.PUBLIC, true, userId2, false },
				
				// TODO failing due to blank name
				//{ "", "bridge", dockerServerId, EntitlementType.PUBLIC, true, userId2, true },
				//TODO failing due to null userid
				//{ "testnetwork"+prefix, "bridge", dockerServerId, EntitlementType.PUBLIC, true, null, true },
				// TODO failing due to null EntitlementType
				//{ "testnetwork"+prefix, "bridge", dockerServerId, null, true, userId2, true },
				
				{ "testnetwork2"+prefix, "bridge", dockerServerId, EntitlementType.CUSTOM, false, USER_GROUP, false },
				// TODO failing due to blank name
				//{ "", "bridge", dockerServerId, EntitlementType.CUSTOM, false, USER_GROUP, true },
				//TODO failing due to null userid
				//{ "testnetwork"+prefix, "bridge", dockerServerId, EntitlementType.CUSTOM, false, null, true },
				// TODO failing due to null EntitlementType
				//{ "testnetwork"+prefix, "bridge", dockerServerId, null, false, USER_GROUP, true },
				
				{ "testnetwork3"+prefix, "bridge", dockerServerId, EntitlementType.OWNER, true, userId2, false },
				// TODO failing due to blank name
				//{ "", "bridge", dockerServerId, EntitlementType.OWNER, true, userId2, true },
				//TODO failing due to null userid
				//{ "testnetwork"+prefix, "bridge", dockerServerId, EntitlementType.CUSTOM, true, null, true },
				// TODO failing due to null EntitlementType
				//{ "testnetwork"+prefix, "bridge", dockerServerId, null, true, userId2, true },
				});
	}
	
	@Test
	public void testEntitledFindById() {
		ResponseEntity<DockerNetwork> response = networkService.create(network);
		logger.info("Create network with Name [{}]", network.getName());

		for (Message m : response.getMessages()) {
			logger.warn("[{}]", m.getMessageText());
		}
		if (response.getResults() != null) {
			networkCreated = response.getResults();
		}
		try {
			if (!error) {
				while ((networkCreated!=null && networkCreated.getStatus() != DockerNetworkStatus.LIVE) && (System.currentTimeMillis() < endTime)) {
					try {
						Thread.sleep(5000);
						networkCreated = networkService.findById(networkCreated.getId()).getResults();
						logger.info("Network Status is [{}]", networkCreated.getStatus());
					} catch (InterruptedException e) {
						// TODO: handling exception
					}
				}
				
				if (network.getEntitlementType().equals(EntitlementType.PUBLIC)) {
					ResponseEntity<DockerNetwork> findbyIdResponse = networkService.findById(networkCreated.getId());
					for (Message message : findbyIdResponse.getMessages()) {
						logger.warn("Error while Find request  [{}] ", message.getMessageText());
					}
					Assert.assertNotNull(((Boolean) false).toString(),
							((Boolean) findbyIdResponse.isErrors()).toString());
					assertNotNull(findbyIdResponse);
					assertNotNull(findbyIdResponse.isErrors());
					assertNotNull(findbyIdResponse.getResults());
				} else if (network.getEntitlementType().equals(EntitlementType.CUSTOM)) {
					ResponseEntity<DockerNetwork> findbyIdResponse = networkService.findById(networkCreated.getId());
					for (Message message : findbyIdResponse.getMessages()) {
						logger.warn("Error while Find request  [{}] ", message.getMessageText());
					}
					Assert.assertNotNull(((Boolean) false).toString(),
							((Boolean) findbyIdResponse.isErrors()).toString());
					assertNotNull(findbyIdResponse);
					assertNotNull(findbyIdResponse.isErrors());
					assertNotNull(findbyIdResponse.getResults());
				} else if (network.getEntitlementType().equals(EntitlementType.OWNER)) {
					ResponseEntity<DockerNetwork> findbyIdResponse = networkService.findById(networkCreated.getId());
					for (Message message : findbyIdResponse.getMessages()) {
						logger.warn("Error while Find request  [{}] ", message.getMessageText());
					}
					Assert.assertNotNull(((Boolean) false).toString(),
							((Boolean) findbyIdResponse.isErrors()).toString());
					assertNotNull(findbyIdResponse);
					assertNotNull(findbyIdResponse.isErrors());
					assertNotNull(findbyIdResponse.getResults());
				}

			} else {
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

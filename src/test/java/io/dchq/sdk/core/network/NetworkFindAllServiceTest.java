package io.dchq.sdk.core.network;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

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
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
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
public class NetworkFindAllServiceTest extends AbstractServiceTest {

	private NetworkService networkService;

	@org.junit.Before
	public void setUp() throws Exception {
		networkService = ServiceFactory.buildNetworkService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	DockerNetwork network;
	DockerNetwork networkCreated;
	DockerNetwork networkDeleted;
	boolean error;
	String validationMessage;
	private int countBeforeCreate = 0, countAfterCreate = 0;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50);

	public NetworkFindAllServiceTest(String name, String driver, String id, boolean error) {
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		if (name != null && !name.isEmpty()) {
			name = prefix + "-" + name;
		}
		network = new DockerNetwork();
		network.setName(name);
		network.setDriver(driver);
		network.setDockerServer(new NameEntityBase().withId(id));
		this.error = error;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { { "testnetwork", "bridge", dockerServerId, true },
				{ "##############$WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW", "bridge", dockerServerId, true },
				{ "a", "bridge", dockerServerId, true }, { "", "bridge", dockerServerId, false } });
	}

	public int testNetworktPosition(String id) {
		ResponseEntity<List<DockerNetwork>> response = networkService.findAll(0, 500);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (DockerNetwork obj : response.getResults()) {
				position++;
				if (obj.getId().equals(id)) {
					logger.info("  Object Matched in FindAll {}  at Position : {}", id, position);
					assertEquals("Recently Created Object is not at Positon 1 :" + obj.getId(), 1, position);
				}
			}
		}
		logger.info(" Total Number of Objects :{}", response.getResults().size());
		return response.getResults().size();
	}
	@Ignore
	@Test
	public void createTest() {
		try {
			logger.info("Create network name as [{}] driver [{}] server [{}]", network.getName(), network.getDriver(),
					network.getDockerServer());
			countBeforeCreate = testNetworktPosition(null);
			ResponseEntity<DockerNetwork> response = networkService.create(network);
			if (error) {
				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}

				assertEquals(false, response.isErrors());
				assertNotNull(response);
				assertNotNull(response.getResults());
				if (response.getResults() != null && !response.isErrors()) {
					this.networkCreated = response.getResults();
					logger.info("Create docker network Successful..");
				}

				while ((networkCreated != null && networkCreated.getStatus() != DockerNetworkStatus.LIVE)
						&& (System.currentTimeMillis() < endTime)) {
					try {
						Thread.sleep(5000);
						networkCreated = networkService.findById(networkCreated.getId()).getResults();
						logger.info("Network Status is [{}]", networkCreated.getStatus());
					} catch (InterruptedException e) {
						// TODO: handling exception
					}
				}
				assertNotNull(response);
				assertNotNull(response.getResults());
				assertNotNull(response.getResults().getId());
				// getting Count of objects after creating Object
				logger.info("FindAll User Network by Id [{}]", networkCreated.getId());
				this.countAfterCreate = testNetworktPosition(networkCreated.getId());
				assertEquals(countBeforeCreate + 1, countAfterCreate);

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
			ResponseEntity<DockerNetwork> response = networkService.delete(this.networkCreated.getId());
			networkDeleted = response.getResults();
			for (Message message : response.getMessages()) {
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}
			assertEquals(countBeforeCreate, countAfterCreate - 1);

			while (networkDeleted != null && networkDeleted.getStatus() != DockerNetworkStatus.REMOVED
					&& (System.currentTimeMillis() < endTime)) {
				response = networkService.findById(this.networkCreated.getId());
				networkDeleted = response.getResults();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}

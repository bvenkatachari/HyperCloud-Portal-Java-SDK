package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;

/**
* @author Jagdeep Jain
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeUpdateServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService, dockerVolumeService2;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;
	EntitlementType entitlementType;

	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mins

	public DockerVolumeUpdateServiceTest(String volumeName, String provider, String size, EntitlementType type, boolean error) {
		String prefix = RandomStringUtils.randomAlphabetic(3);
		if(volumeName!=null )
		{
			volumeName = prefix.toLowerCase() + volumeName;
		}
		this.dockerVolume = new DockerVolume();
		this.dockerVolume.setName(volumeName);
		this.dockerVolume.setEndpoint(provider);
		this.dockerVolume.setSize(size);
		this.dockerVolume.setEntitlementType(type);
		this.error = error;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {

			// TODO: add more test data for all sorts of validations
			{ "updatevolume", "2c9180865d35d99c015d363715c100e1",	"5", EntitlementType.OWNER, false },
			{ "updatevolume", "2c9180865d35d99c015d363715c100e1",	"2", EntitlementType.PUBLIC, false },
			{ "updatevolume", "2c9180865d35d99c015d363715c100e1",	"2", EntitlementType.CUSTOM, false },
	
		});
	}

	@Before
	public void setUp() {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
		dockerVolumeService2 = ServiceFactory.buildDockerVolumeService(rootUrl1, username2, password2);
	}
	
	@Test
	public void updateTest() throws Exception {
		// Creating new Docker Volume

		logger.info("Create docker volumne name[{}] ", dockerVolume.getName());
		ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);
		assertNotNull(response);
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}

		if(!error)
		{
			assertNotNull(response);
			assertNotNull(response.isErrors());
			Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) response.isErrors()).toString());
			Assert.assertFalse(response.isErrors());
			assertNotNull(response.getResults());
			if (response.getResults() != null && !response.isErrors()) {
				this.dockerVolumeCreated = response.getResults();
				logger.info("Create docker volumne Successful..");
			}

			// wait till status change to Live
			while (dockerVolumeCreated.getStatus().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
				try {
					Thread.sleep(10000);
					dockerVolumeCreated = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
					logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				} catch (InterruptedException e) {
					// TODO: handling exception
				}
			}

			if (!response.isErrors() && response.getResults() != null) {
				dockerVolumeCreated = response.getResults();
				assertNotNull(response.getResults());
				assertNotNull(response.getResults().getId());
				Assert.assertNotNull(dockerVolume.getName(), dockerVolumeCreated.getName());
				// updating entitlement
				UsernameEntityBase entitledUser = new UsernameEntityBase().withId(userId2);
				List<UsernameEntityBase> entiledUsers = new ArrayList<>();
				entiledUsers.add(entitledUser);
				dockerVolumeCreated.setEntitlementType(EntitlementType.CUSTOM);
				dockerVolumeCreated.setEntitledUsers(entiledUsers);

				response = dockerVolumeService.update(dockerVolumeCreated);

				logger.info("Entitlement Type [{}] and First name [{}]", response.getResults().getEntitlementType(),
						response.getResults().getEntitledUsers().get(0).getFirstname());

				for (Message message : response.getMessages()) {
					logger.warn("Error while Update request  [{}] ", message.getMessageText());
				}

				if (dockerVolumeCreated.getEntitlementType().equals(EntitlementType.CUSTOM)) {
					ResponseEntity<DockerVolume> searchResponse = dockerVolumeService2
							.findById(dockerVolumeCreated.getId());
					assertNotNull(searchResponse);
					assertNotNull(searchResponse.isErrors());
					// TODO: add tests for testing error message
					assertNotNull(searchResponse.getResults());
					assertEquals(dockerVolumeCreated.getName(), searchResponse.getResults().getName());
				}

			}
		}
		else
		{
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}

	}

	@After
	public void cleanUp() {
		if (this.dockerVolumeCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = dockerVolumeService.delete(this.dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error docker volume deletion: [{}] ", message.getMessageText());
			}
		}
	}
}

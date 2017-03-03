package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

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
import com.dchq.schema.beans.one.dockervolume.DockerVolume;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeUpdateServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;
	String updatedName;

	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mins

	public DockerVolumeUpdateServiceTest(String createdOn, String volumeName, String provider, String server) {
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		volumeName = prefix + "-" + volumeName;
		String prefix2 = RandomStringUtils.randomAlphabetic(3);
		updatedName = prefix2 + "-" + volumeName;
		this.dockerVolume = new DockerVolume();
		this.dockerVolume.setCreatedOn(createdOn);
		this.dockerVolume.setName(volumeName);
		this.dockerVolume.setEndpoint(provider);
		this.dockerVolume.setHostIp(server);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {
				// TODO: add more test data for all sorts of validations
				// passing Id of createdOn, Local Volume Provider
				{ "2c9180865a6421f0015a646c20fe0685", "testvalumn", "2c9180865a6421f0015a6485189f06b9", "qe-100" } });
	}

	@Before
	public void setUp() {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	@Test
	public void updateTest() throws Exception {
		// Creating new Docker Volume

		logger.info("Create docker volumne name[{}] ", dockerVolume.getName());
		ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);

		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}

		assertNotNull(response);
		assertNotNull(response.isErrors());
		Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) response.isErrors()).toString());
		Assert.assertFalse(response.isErrors());

		if (response.getResults() != null && !response.isErrors()) {
			this.dockerVolumeCreated = response.getResults();
			logger.info("Create docker volumne Successful..");
		}

		// wait till status change to Live
		while (!dockerVolumeCreated.getStatus().equals("LIVE") && (System.currentTimeMillis() < endTime)) {
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

			// Set docker volume name
			dockerVolumeCreated.setName(updatedName);

			logger.info("Update Request for Docker volume with Name [{}]", dockerVolumeCreated.getName());
			response = dockerVolumeService.update(dockerVolumeCreated);

			for (Message message : response.getMessages()) {
				logger.warn("Error while Update request  [{}] ", message.getMessageText());
			}
			assertNotNull(response);
			assertNotNull(response.isErrors());
			Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) response.isErrors()).toString());
			Assert.assertFalse(response.isErrors());
			Assert.assertNotNull(response.getResults());
			Assert.assertEquals(response.getResults().getName(), updatedName);

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

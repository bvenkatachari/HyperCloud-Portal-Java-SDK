package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
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

/**
 * @Author Saurabh Bhatia
 */


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeCreateServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;

	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mins

	@org.junit.Before
	public void setUp() throws Exception {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	public DockerVolumeCreateServiceTest(String createdOn, String volumeName, String provider, String server) {
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		volumeName = prefix + "-" + volumeName;

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
				{ "2c9180865a6421f0015a646c20fe0685", "testvalumn", "2c9180865a6421f0015a6485189f06b9",
						"qe-100" } });
	}

	@Test
	public void testCreate() {
		logger.info("Create docker volumne name[{}] ", dockerVolume.getName());
		ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);

		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}

		if (response.getResults() != null && !response.isErrors()) {
			this.dockerVolumeCreated = response.getResults();
			logger.info("Create docker volumne Successful..");
		}

		while (!dockerVolumeCreated.getStatus().equals("LIVE") && (System.currentTimeMillis() < endTime)) {
			try {
				Thread.sleep(10000);
				logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
			} catch (InterruptedException e) {
				// TODO: handling exception
			}
			
			assertNotNull(response);
			assertNotNull(response.isErrors());
			if (this.dockerVolumeCreated != null) {
				assertNotNull(response.getResults().getId());
				assertNotNull(dockerVolumeCreated.getId());
				assertEquals(dockerVolume.getName(), dockerVolumeCreated.getName());
				assertEquals(dockerVolume.getOptionsText(), dockerVolumeCreated.getOptionsText());
			}
		}
	}

	@After
	public void cleanUp() {
		if (this.dockerVolumeCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = dockerVolumeService.delete(this.dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
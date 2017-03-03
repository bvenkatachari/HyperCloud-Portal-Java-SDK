package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
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
public class DockerVolumeFindServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;

	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mins
	
	@Before
	public void setUp() throws Exception {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl, cloudadminusername, cloudadminpassword);
	}
	
	public DockerVolumeFindServiceTest(String createdOn, String volumeName, String provider, String server) {
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
				// passing Id of createdOn, Local Volume Provider
				{ "2c9180865a6421f0015a646c20fe0685", "testvalumn", "2c9180865a6421f0015a6485189f06b9",
						"qe-100" } });
	}
	@Test
	public void findTest() {

		try {
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
					dockerVolumeCreated = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
					logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				} catch (InterruptedException e) {
					// TODO: handling exception
				}

			}

			DockerVolume dockerVolumeCreatefForFind = dockerVolumeService.findById(dockerVolumeCreated.getId())
					.getResults();

			// Check for create
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertNotNull(dockerVolumeCreatefForFind.getId());
			assertEquals(dockerVolume.getName(), dockerVolumeCreatefForFind.getName());
			assertEquals(dockerVolume.getOptionsText(), dockerVolumeCreatefForFind.getOptionsText());
			assertEquals(dockerVolume.getEndpoint(), dockerVolumeCreatefForFind.getEndpoint());
			assertEquals(dockerVolume.getCreatedOn(), dockerVolumeCreatefForFind.getCreatedOn());
			assertEquals(dockerVolumeCreated.getId(), dockerVolumeCreatefForFind.getId());

			// check for find

		} catch (Exception e) {
			// ignore
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

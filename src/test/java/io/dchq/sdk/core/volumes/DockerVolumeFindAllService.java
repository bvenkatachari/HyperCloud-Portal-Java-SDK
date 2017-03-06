package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.blueprint.RegistryAccount;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;

/**
* @author Jagdeep Jain
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeFindAllService extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;
	long startTime = System.currentTimeMillis();
	private int countBeforeCreate = 0, countAfterCreate = 0;
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mins

	public DockerVolumeFindAllService(String createdOn, String volumeName, String provider, String server) {
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		volumeName = prefix + "-" + volumeName;

		this.dockerVolume = new DockerVolume();
		this.dockerVolume.setCreatedOn(createdOn);
		this.dockerVolume.setName(volumeName);
		this.dockerVolume.setEndpoint(provider);
		this.dockerVolume.setHostIp(server);
	}

	@Before
	public void setUp() {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {
				// TODO: add more test data for all sorts of validations
				// passing Id of createdOn, Local Volume Provider
				{ "2c9180865a6421f0015a646c20fe0685", "testvalumn", "2c9180865a6421f0015a6485189f06b9", "qe-100" } });
	}

	public int testDockerVolumetPosition(String id) {
		ResponseEntity<List<DockerVolume>> response = dockerVolumeService.findAll(0, 500);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (DockerVolume obj : response.getResults()) {
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

	@Test
	public void findAll() {
		try {
			logger.info("Create docker volumne name[{}] ", dockerVolume.getName());
			countBeforeCreate =  testDockerVolumetPosition(null);
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
				assertNotNull(response.getResults());
				assertNotNull(response.getResults().getId());
				// getting Count of objects after creating Object
				logger.info("FindAll User DockerVolume by Id [{}]", dockerVolumeCreated.getId());
				this.countAfterCreate = testDockerVolumetPosition(dockerVolumeCreated.getId());
				assertEquals(countBeforeCreate + 1, countAfterCreate);
			}
		} catch (Exception e) {

		}

	}

	@After
	public void clearUp() {

		if (this.dockerVolumeCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = dockerVolumeService.delete(this.dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error volume deletion: [{}] ", message.getMessageText());
			}
			assertEquals(countBeforeCreate, countAfterCreate - 1);
		}

	}

}

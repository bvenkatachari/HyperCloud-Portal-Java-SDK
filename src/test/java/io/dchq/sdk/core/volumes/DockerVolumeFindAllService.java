package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;

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
import com.dchq.schema.beans.one.dockervolume.DockerVolume;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeFindAllService extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;

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

	private int findCount() {
		logger.info("Find all docker volume ");
		ResponseEntity<List<DockerVolume>> responsefindBefore = dockerVolumeService.findAll();

		for (Message message : responsefindBefore.getMessages()) {
			logger.warn("Error while find all request  [{}] ", message.getMessageText());
		}
		assertNotNull(responsefindBefore);
		assertNotNull(responsefindBefore.isErrors());
		Assert.assertEquals(error, responsefindBefore.isErrors());
		return responsefindBefore.getResults().size();
	}

	@Test
	public void findAll() {
		try {
			int countBeforCreate = findCount();

			logger.info("Create docker volumne name[{}] ", dockerVolume.getName());
			ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);

			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (response.getResults() != null && !response.isErrors()) {
				logger.info("Create docker volumne Successful..");
			}

			int countAfterCreate = findCount();
			Assert.assertNotEquals(countBeforCreate, countAfterCreate);
			Assert.assertEquals(countBeforCreate, countAfterCreate - 1);
		} catch (Exception e) {
			// ignore
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
		}

	}

}

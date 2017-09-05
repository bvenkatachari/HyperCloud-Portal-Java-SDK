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
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;

/**
* @author Jagdeep Jain
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeFindServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;

	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for 3 mins
	
	@Before
	public void setUp() throws Exception {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
	}
	
	public DockerVolumeFindServiceTest(String volumeName, String provider, String size, EntitlementType type, boolean error) {
		String prefix = RandomStringUtils.randomAlphabetic(3);
		if(volumeName!=null )
		{
			volumeName = prefix.toLowerCase() + "-" + volumeName;
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
				{ "testvalume", "2c9180865d35d99c015d363715c100e1", "5", EntitlementType.OWNER, false },
				{ "testvalume", "2c9180865d35d99c015d363715c100e1", "2", EntitlementType.PUBLIC, false },
				{ "testvalume", "2c9180865d35d99c015d363715c100e1", "2", EntitlementType.CUSTOM, false }, 
		});
	}
	@Test
	public void findTest() {

		logger.info("Create docker volumne name[{}] ", dockerVolume.getName());
		ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);
		assertNotNull(response);
		
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
		if (!error) {
			assertNotNull(response.getResults());
			if (response.getResults() != null && !response.isErrors()) {
				this.dockerVolumeCreated = response.getResults();
				logger.info("Create docker volumne Successful..");
			}

			while (dockerVolumeCreated.getStatus().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
				try {
					Thread.sleep(10000);
					dockerVolumeCreated = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
					assertNotNull(dockerVolumeCreated);
					logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				} catch (InterruptedException e) {
					// TODO: handling exception
				}
			}
			logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
			assertEquals("LIVE", dockerVolumeCreated.getStatus());
			
			response = dockerVolumeService.findById(dockerVolumeCreated.getId());
			// Check for create
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertNotNull(response.getResults().getId());
			assertEquals(dockerVolumeCreated.getName(), response.getResults().getName());
			assertEquals(dockerVolumeCreated.getOptionsText(), response.getResults().getOptionsText());
			assertEquals(dockerVolumeCreated.getEndpoint(), response.getResults().getEndpoint());
			assertEquals(dockerVolumeCreated.getCreatedOn(), response.getResults().getCreatedOn());
			assertEquals(dockerVolumeCreated.getId(), response.getResults().getId());
			assertEquals(dockerVolumeCreated.getSize(), response.getResults().getSize());
		}
		else
		{
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
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
			DockerVolume dockerVolumeDelete =  (DockerVolume) response.getResults();
			
			while (dockerVolumeDelete.getStatus().equals("DESTROYING") && (System.currentTimeMillis() < endTime)) {
				try {
					Thread.sleep(10000);
					dockerVolumeDelete = dockerVolumeService.findById(dockerVolumeDelete.getId()).getResults();
				} catch (InterruptedException e) {
					// TODO: handling exception
				}
			}
		}

	}

}

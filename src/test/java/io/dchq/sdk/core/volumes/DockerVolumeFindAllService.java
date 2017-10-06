package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
public class DockerVolumeFindAllService extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;
	long startTime = System.currentTimeMillis();
	private int countBeforeCreate = 0, countAfterCreate = 0;
	long endTime = startTime + (60 * 60 * 160); // this is for 10 mins

	public DockerVolumeFindAllService(String volumeName, String provider, String size, EntitlementType type, boolean error) {
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

	@Before
	public void setUp() {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {
				// TODO: add more test data for all sorts of validations
			{ "testvolume", "2c9180865d35d99c015d363715c100e1",	"5", EntitlementType.OWNER, false },
			{ "testvolume", "2c9180865d35d99c015d363715c100e1",	"2", EntitlementType.PUBLIC, false },
			{ "testvolume", "2c9180865d35d99c015d363715c100e1",	"2", EntitlementType.CUSTOM, false },
			
		});
	}

	public int testDockerVolumetPosition(String id) {
		ResponseEntity<List<DockerVolume>> response = dockerVolumeService.findAll(0, 500);
		assertNotNull(response);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
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

			logger.info("Create docker volumne name[{}] ", dockerVolume.getName());
			countBeforeCreate =  testDockerVolumetPosition(null);
			ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);
			assertNotNull(response);
			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}
			if(!error)
			{
				assertEquals(false,response.isErrors());
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
				assertNotNull(response.getResults());
				assertNotNull(response.getResults().getId());
				// getting Count of objects after creating Object
				logger.info("FindAll User DockerVolume by Id [{}]", dockerVolumeCreated.getId());
				this.countAfterCreate = testDockerVolumetPosition(dockerVolumeCreated.getId());
				assertEquals(countBeforeCreate + 1, countAfterCreate);
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
			
			endTime = System.currentTimeMillis()  + (60 * 60 * 50);
			
			ResponseEntity<?> response = dockerVolumeService.delete(this.dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error volume deletion: [{}] ", message.getMessageText());
			}
			DockerVolume dockerVolumeDelete =  (DockerVolume) response.getResults();
			
			do{
				
				try {
					Thread.sleep(10000);
					dockerVolumeDelete = dockerVolumeService.findById(dockerVolumeDelete.getId()).getResults();
				} catch (InterruptedException e) {
					// TODO: handling exception
				}
				
			}while (dockerVolumeDelete.getStatus().equals("DESTROYING") && (System.currentTimeMillis() < endTime));
				
		}

	}

}

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
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mins
	
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
		{ "testvalume", "2c9180865ccf117a015ccf1aa46b0005",	"5", EntitlementType.OWNER, false },
		{ "test21111", "2c9180865ccf117a015ccf1aa46b0005",	"2", EntitlementType.PUBLIC, false },
		
		{ "test21111", "",	"2", EntitlementType.PUBLIC, true },
		// TODO volume name should not be blank
		//{ "", "2c9180865bb2559a015bd99819254459", "2", EntitlementType.OWNER, true },
		// TODO not accept only special characters
		//{ "@@@@@@@@", "2c9180865bb2559a015bd99819254459", "2", EntitlementType.CUSTOM, true},
		// TODO Should not accept negative volume
		//{ "nagative-volume", "2c9180865bb2559a015bd99819254459", "-2", EntitlementType.CUSTOM, true},
		
		{ "sadasdasdaaaaaaaassssssssssssssssssssssssssssssssssssssaaaaaaaaaaaaaaaaaaaaaaasdadasdad"
		 		+ "asdasdasddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
		 		+ "asdddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
		 		+ "asdddddddddddddddddddddddddddddddd", "2c9180865ccf117a015ccf1aa46b0005",	"2", EntitlementType.CUSTOM, true }
		
	
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

			while (!dockerVolumeCreated.getStatus().equals("LIVE") && (System.currentTimeMillis() < endTime)) {
				try {
					Thread.sleep(10000);
					dockerVolumeCreated = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
					assertNotNull(dockerVolumeCreated);
					logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				} catch (InterruptedException e) {
					// TODO: handling exception
				}
			}

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

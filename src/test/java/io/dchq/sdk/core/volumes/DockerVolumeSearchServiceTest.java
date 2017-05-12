package io.dchq.sdk.core.volumes;

import static junit.framework.Assert.assertFalse;
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
import com.dchq.schema.beans.one.dockervolume.DockerVolume;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;

/**
* @author Jagdeep Jain
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeSearchServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mins
	String errorMsg;

	public DockerVolumeSearchServiceTest(String volumeName, String provider, String server, boolean isPrefix, boolean error) {
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		if(isPrefix)
		{
			volumeName = prefix.toLowerCase() + "-" + volumeName;
		}
		this.dockerVolume = new DockerVolume();
		this.dockerVolume.setName(volumeName);
		this.dockerVolume.setEndpoint(provider);
		this.dockerVolume.setHostIp(server);
		this.error = error;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {
			// TODO: add more test data for all sorts of validations
		   { "testvalumn", "2c9180865bb2559a015bd99819254459",	"qe-100", true, false },
		   { "test21111", "2c9180865bb2559a015bd99819254459",	"qe-100", true, false },
			
			// TODO volume name should not be blank
		   //{ "", "2c9180865bb2559a015bd99819254459",	"qe-100", false, true }
				
			// TODO not accept only special characters
		   //{ "@@@@@@@@", "2c9180865bb2559a015bd99819254459",	"qe-100", false, true }
			 { "test21111", null,	"qe-100", true, true},
			 { "test21111", "",	"qe-100", true, true},
			 { null , null,	"qe-100", false, true },
			 
			 { "sadasdasdaaaaaaaassssssssssssssssssssssssssssssssssssssaaaaaaaaaaaaaaaaaaaaaaasdadasdad"
			 		+ "asdasdasddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
			 		+ "asdddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
			 		+ "asdddddddddddddddddddddddddddddddd", "2c9180865bb2559a015bd99819254459",	"qe-100", true, true }
		
		});
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

		if (response.getResults() != null && !response.isErrors()) {
			this.dockerVolumeCreated = response.getResults();
			logger.info("Create docker volumne Successful..");
		}

		// wait till status change to Live
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

		if(!error)
		{
			assertNotNull(response);
			Assert.assertFalse(response.isErrors());
			assertNotNull(response.getResults());
			assertNotNull(response.getResults().getId());
			Assert.assertNotNull(dockerVolume.getName(), dockerVolumeCreated.getName());

			// Updating User Group
			logger.info("Search for Request for Docker volume with Name [{}]", dockerVolumeCreated.getName());
			ResponseEntity<List<DockerVolume>> searchResponse = dockerVolumeService.search(dockerVolumeCreated.getName(), 0, 1);

			for (Message message : searchResponse.getMessages()) {
				logger.warn("Error while search docker volume request  [{}] ", message.getMessageText());
				errorMsg = message.getMessageText();
			}
			assertNotNull(searchResponse);
			assertNotNull(searchResponse.isErrors());
			assertFalse(errorMsg, searchResponse.isErrors());

			assertNotNull(searchResponse.getResults());

			Assert.assertEquals(1, searchResponse.getResults().size());

			DockerVolume searchedEntity = searchResponse.getResults().get(0);
			Assert.assertEquals(dockerVolumeCreated.getId(), searchedEntity.getId());

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

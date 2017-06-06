package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
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
import junit.framework.Assert;

/**
 * @Author Saurabh Bhatia
 * @updater Jagdeep Jain
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

	public DockerVolumeCreateServiceTest(String volumeName, String provider, String size, EntitlementType type, boolean error) {
		// random user name
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
			
				{ "testvalume", "2c9180865bb2559a015bd99819254459",	"5", EntitlementType.OWNER, false },
				{ "test21111", "2c9180865bb2559a015bd99819254459",	"2", EntitlementType.PUBLIC, false },
				
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
				 		+ "asdddddddddddddddddddddddddddddddd", "2c9180865bb2559a015bd99819254459",	"2", EntitlementType.CUSTOM, true }
								
		});
		
	}

	@Test
	public void testCreate() {
	
			logger.info("Create docker volumne name[{}] ", dockerVolume.getName());
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

				assertNotNull(response);
				assertNotNull(response.isErrors());
				if (this.dockerVolumeCreated != null) {
					assertNotNull(response.getResults().getId());
					assertNotNull(dockerVolumeCreated.getId());
					assertEquals(dockerVolume.getName(), dockerVolumeCreated.getName());
					assertEquals(dockerVolume.getOptionsText(), dockerVolumeCreated.getOptionsText());
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
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}
		}
	}
}
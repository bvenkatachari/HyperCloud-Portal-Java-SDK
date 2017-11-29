package io.dchq.sdk.core.volumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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



@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeResizeServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	DockerVolume resizeVolume;
	DockerVolume resizedVolume;
	boolean error;
	String validationMessage;

	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for 10 mins
	private String newSize;

	@org.junit.Before
	public void setUp() throws Exception {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	public DockerVolumeResizeServiceTest(String volumeName, String provider, String size, String newSize, EntitlementType type, boolean error) {
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		if(volumeName!=null )
		{
			volumeName = prefix.toLowerCase()  + volumeName;
		}
		this.dockerVolume = new DockerVolume();
		this.dockerVolume.setName(volumeName);
		this.dockerVolume.setEndpoint(provider);
		this.dockerVolume.setSize(size);
		this.dockerVolume.setEntitlementType(type);
		this.newSize = newSize;
		this.error = error;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {
				// TODO: add more test data for all sorts of validations		
				{ "resizevolume", "2c9180865d35d99c015d363715c100e1",	"2", "3", EntitlementType.OWNER, false },
				{ "resizevolume", "2c9180865d35d99c015d363715c100e1",	"2", "4", EntitlementType.PUBLIC, false },
				{ "resizevolume", "2c9180865d35d99c015d363715c100e1",	"2", "5", EntitlementType.CUSTOM, false },
								
		});
		
	}
	
	@Test
	public void testCreateThenResize() {
	
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
				// Get status 
				dockerVolumeCreated = getStatus(dockerVolumeCreated);
				
				logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				assertNotNull(response);
				assertNotNull(response.isErrors());
				assertEquals("LIVE", dockerVolumeCreated.getStatus());
				
				ResponseEntity<DockerVolume> resizeVolume = dockerVolumeService.doPost(dockerVolumeCreated, dockerVolumeCreated.getId()+"/resize/" + this.newSize);
				assertNotNull(resizeVolume.getResults());
				assertEquals(false, resizeVolume.isErrors());
				
				if (resizeVolume.getResults() != null && !resizeVolume.isErrors()) {
					this.resizedVolume = resizeVolume.getResults();
					logger.info("Resize docker volumne Successful..");
				}
				// get status after resize volume
				resizedVolume = getStatus(resizedVolume);
				assertEquals(this.newSize, resizedVolume.getSize());
			}
			else
			{
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}		
	}
	
	private DockerVolume getStatus(DockerVolume dockerVolume)
	{
		if (dockerVolume != null) {
			while ((dockerVolume.getStatus().equals("PROVISIONING") || dockerVolume.getStatus().equals("RESIZING")) && (System.currentTimeMillis() < endTime)) {
				try {
					Thread.sleep(10000);
					dockerVolume = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
					assertNotNull(dockerVolumeCreated);
					logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				} catch (InterruptedException e) {
					// TODO: handling exception
				}
			}
		}
		return dockerVolume;
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

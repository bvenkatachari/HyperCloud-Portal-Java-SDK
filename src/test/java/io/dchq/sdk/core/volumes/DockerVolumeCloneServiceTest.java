package io.dchq.sdk.core.volumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.FixMethodOrder;
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
public class DockerVolumeCloneServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;
	private DockerVolume dockerVolume;
	private DockerVolume dockerVolumeCreated;
	private DockerVolume cloneVolumeCreated;
	private String cloneName;
	private boolean success;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for 10 mins

	public DockerVolumeCloneServiceTest(String volumeName, String cloneName, String provider, String size, EntitlementType type, boolean success) {
		dockerVolume = new DockerVolume();
		String prefix = RandomStringUtils.randomAlphabetic(3);
		if (volumeName != null & !volumeName.isEmpty()) {
			volumeName = prefix.toLowerCase() + volumeName;
		}
		dockerVolume.setName(volumeName);
		dockerVolume.setEntitlementType(type);
		dockerVolume.setEndpoint(provider);
		dockerVolume.setSize(size);
		this.success = success;
		this.cloneName = cloneName;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] { 
			{ "testvalume", "clonename", "2c9180865d35d99c015d363715c100e1", "2", EntitlementType.OWNER, false },
			{ "testvalume", "clonename", "2c9180865d35d99c015d363715c100e1", "2", EntitlementType.PUBLIC, false },
			{ "testvalume", "clonename","2c9180865d35d99c015d363715c100e1",	"2", EntitlementType.CUSTOM, false }
		});
	}

	@org.junit.Before
	public void setUp() {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
	}
	
	@org.junit.Test
	public void testRegister() {
		logger.info("Clone volume with name [{}]" , dockerVolume.getName());
		ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);
		assertNotNull(response);
		if(!success)
		{
			assertNotNull(response.getResults());
			assertEquals(false, response.isErrors());
			if (response.getResults() != null && !response.isErrors()) {
				this.dockerVolumeCreated = response.getResults();
				logger.info("Create docker volumne Successful..");
			}
			// get live status after provision a volume
			dockerVolumeCreated = getStatus(this.dockerVolumeCreated);
			logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertEquals("LIVE", dockerVolumeCreated.getStatus());
			ResponseEntity<DockerVolume> cloneVolume = dockerVolumeService.doPost(dockerVolumeCreated, dockerVolumeCreated.getId()+"/clone/" + this.cloneName);
			assertNotNull(cloneVolume.getResults());
			assertEquals(false, cloneVolume.isErrors());
			
			if (response.getResults() != null && !response.isErrors()) {
				this.cloneVolumeCreated = cloneVolume.getResults();
				logger.info("clone docker volumne Successful..");
			}
			// get live status after clone a volume 
			cloneVolumeCreated = getStatus(this.cloneVolumeCreated);
			logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
			assertEquals(cloneVolumeCreated.getName(), this.cloneName);
		}
		else
		{
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}

	}
	private DockerVolume getStatus(DockerVolume volume)
	{
		while (volume.getStatus().equals("PROVISIONING") || volume.getStatus().equals("CLONING") && (System.currentTimeMillis() < endTime)) {
			try {
				Thread.sleep(10000);
				volume = dockerVolumeService.findById(volume.getId()).getResults();
				assertNotNull(volume);
				logger.info("Clone Volume Status is [{}]", volume.getStatus());
			} catch (InterruptedException e) {
				// TODO: handling exception
			}
		}
		return volume;
	}
	@org.junit.After
	public void cleanUp() 
	{
		logger.info("Clean clone volume");
		if(cloneVolumeCreated!=null)
		{
			ResponseEntity<DockerVolume> response = dockerVolumeService.delete(cloneVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}
			
		}
		logger.info("Clean created volume");
		if(dockerVolumeCreated!=null)
		{
			ResponseEntity<DockerVolume> response = dockerVolumeService.delete(dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}
			
		}
	}
}


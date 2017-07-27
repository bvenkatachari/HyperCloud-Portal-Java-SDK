package io.dchq.sdk.core.volumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
public class DockerVolumeRegisterServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;
	private DockerVolume dockerVolume;
	private DockerVolume dockerVolumeCreated;
	private boolean success;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for 10 mins

	public DockerVolumeRegisterServiceTest(String name, String hostIp, String volumeId, String destination, EntitlementType entitlement, boolean success) {
		dockerVolume = new DockerVolume();
		String prefix = RandomStringUtils.randomAlphabetic(3);
		if (name != null & !name.isEmpty()) {
			name = prefix.toLowerCase() + name;
		}
		dockerVolume.setName(name);
		dockerVolume.setEntitlementType(entitlement);
		dockerVolume.setHostIp(hostIp);
		dockerVolume.setVolumeId(volumeId);
		dockerVolume.setDestination(destination);
		this.success = success;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] { 
			{ "testregister", "dev.hypercloud.local", "SELF_REGISTERED", "Fixed", EntitlementType.OWNER, true },
			{ "", "dev.hypercloud.local", "SELF_REGISTERED", "Fixed", EntitlementType.OWNER, false },
			{ null, "dev.hypercloud.local", "SELF_REGISTERED", "Fixed", EntitlementType.OWNER, false }});
	}

	@org.junit.Before
	public void setUp() {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
	}
	@Ignore
	@org.junit.Test
	public void testRegister() {
		logger.info("Register volume with name [{}]" , dockerVolume.getName());
		ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);
		assertNotNull(response);
		if(success)
		{
			assertNotNull(response.getResults());
			assertEquals(false, response.isErrors());
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
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertEquals("LIVE", dockerVolumeCreated.getStatus());
			if (this.dockerVolumeCreated != null) {
				assertNotNull(response.getResults().getId());
				assertNotNull(dockerVolumeCreated.getId());
				assertEquals(dockerVolume.getName(), dockerVolumeCreated.getName());
				assertEquals(dockerVolume.getOptionsText(), dockerVolumeCreated.getOptionsText());
				assertEquals(dockerVolume.getSize(), dockerVolumeCreated.getSize());
			}
		}
		else
		{
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}

	}

	@org.junit.After
	public void cleanUp() 
	{
		logger.info("Clean created register volume");
		if(dockerVolumeCreated!=null)
		{
			ResponseEntity<DockerVolume> response = dockerVolumeService.delete(dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}
			
		}
	}
}

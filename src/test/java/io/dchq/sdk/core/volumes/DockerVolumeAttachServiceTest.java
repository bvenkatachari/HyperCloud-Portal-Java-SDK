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
public class DockerVolumeAttachServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;
	private DockerVolume dockerVolume;
	private DockerVolume dockerVolumeCreated;
	private DockerVolume atachedVolume;
	private boolean success;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for 10 mins
	private String vmId;

	public DockerVolumeAttachServiceTest(String name, String hostIp, String volumeId, String destination,
			EntitlementType entitlement, String vmId, boolean success) {
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
		this.vmId = vmId;
		this.success = success;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {
				{ "testregister", "dev.hypercloud.local", "SELF_REGISTERED", "Fixed", EntitlementType.OWNER, "402881875d7da3b2015d7df7639a00ac",true },
				{ "testregister", "dev.hypercloud.local", "SELF_REGISTERED", "Fixed", EntitlementType.CUSTOM, "402881875d7da3b2015d7df7639a00ac",true },
				{ "testregister", "dev.hypercloud.local", "SELF_REGISTERED", "Fixed", EntitlementType.PUBLIC, "402881875d7da3b2015d7df7639a00ac",true },
				
				{ "", "dev.hypercloud.local", "SELF_REGISTERED", "Fixed", EntitlementType.OWNER, "402881875d7da3b2015d7df7639a00ac",false },
				{ null, "dev.hypercloud.local", "SELF_REGISTERED", "Fixed", EntitlementType.OWNER, "402881875d7da3b2015d7df7639a00ac",false } });
	}

	@org.junit.Before
	public void setUp() {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	@Ignore
	@org.junit.Test
	public void testAttachVolume() {
		logger.info("Register volume with name [{}]", dockerVolume.getName());
		ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);
		assertNotNull(response);
		if (success) {
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
			ResponseEntity<DockerVolume> responseVolume = dockerVolumeService.doPost(dockerVolumeCreated, dockerVolumeCreated.getClusterId()+"/attach/" + this.vmId);
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
			assertNotNull(responseVolume.getResults());
			assertEquals(false, responseVolume.isErrors());
			if (responseVolume.getResults() != null && !responseVolume.isErrors()) {
				this.atachedVolume = response.getResults();
				logger.info("Create docker volumne Successful..");
			}
			logger.info("Volume Status is [{}]", atachedVolume.getStatus());
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertEquals("LIVE", atachedVolume.getStatus());
			
		} else {
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}

	}

	@org.junit.After
	public void cleanUp() {
		logger.info("Detaching attched volume");
		if (atachedVolume != null) {
			ResponseEntity<DockerVolume> response = dockerVolumeService.doPost(dockerVolumeCreated, dockerVolumeCreated.getClusterId()+"/detach/" + this.vmId);
			for (Message message : response.getMessages()) {
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}

		}
	}
}

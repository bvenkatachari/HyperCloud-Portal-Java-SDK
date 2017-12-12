package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import com.dchq.schema.beans.one.dockervolume.SDVolumeRequest;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * @Author Santosh Kumar
 * 
 */


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BlueprintDockerVolumeCreateServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;

	SDVolumeRequest sdv;
	DockerVolume dockerVolumeCreated;
	
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mins

	@org.junit.Before
	public void setUp() throws Exception {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	public BlueprintDockerVolumeCreateServiceTest(String blueprintId, String providerId) {
		
		sdv = new SDVolumeRequest();
		sdv.setBlueprint(blueprintId);
		sdv.setProvider(providerId);
		sdv.setYaml("Volume: \n  size: 1");
		
		
		
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {
				{ "2c9180865fe7c46e016020c5194a634c", "2c9180865d312fc4015d3160f518008e"} 
				});
	}

	
	@Test
	public void testCreate() {
		try {
			
			ResponseEntity<DockerVolume> response = dockerVolumeService.createBlueprintVolume(sdv);

			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (response.getResults() != null && !response.isErrors()) {
				this.dockerVolumeCreated = response.getResults();
				logger.info("Create docker volumne Successful..");
			}

			logger.info("Created docker volumne name[{}] ", this.dockerVolumeCreated.getName() );
			
			while (!dockerVolumeCreated.getStatus().equals("LIVE") && (System.currentTimeMillis() < endTime)) {
				try {
					Thread.sleep(10000);
					dockerVolumeCreated = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
					logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				} catch (InterruptedException e) {
					// TODO: handling exception
				}

				assertNotNull(response);
				assertNotNull(response.isErrors());
				if (this.dockerVolumeCreated != null) {
					assertNotNull(response.getResults().getId());
					assertNotNull(dockerVolumeCreated.getId());
				}
			}
		} catch (Exception e) {

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
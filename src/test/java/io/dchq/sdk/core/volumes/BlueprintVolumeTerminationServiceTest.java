package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.common.TerminationProtection;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import com.dchq.schema.beans.one.dockervolume.SDVolumeRequest;
import com.dchq.schema.beans.one.inbox.EntityType;
import com.dchq.schema.beans.one.inbox.MessageResolution;
import com.dchq.schema.beans.one.inbox.MessageStatus;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.MessageService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * @Author Santosh Kumar
 * 
 */


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BlueprintVolumeTerminationServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;
	private MessageService messageService;

	SDVolumeRequest sdv;
	DockerVolume dockerVolumeCreated;
	
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mins

	@org.junit.Before
	public void setUp() throws Exception {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
		messageService = ServiceFactory.buildMessageService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	public BlueprintVolumeTerminationServiceTest(String blueprintId, String providerId) {
		
		sdv = new SDVolumeRequest();
		sdv.setBlueprint(blueprintId);
		sdv.setProvider(providerId);
		sdv.setYaml("Volume:\n      size: 1");
		sdv.setTerminationProtection(TerminationProtection.ENABLED);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {
			     //QA_Volume_Automation, vhg01(HBS)
				{ "2c918086602f7b8c016034b7b9492d4b", "2c9180875e9f1385015ea2b00adb24e0"} 
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
			
			//Delete this volume
			dockerVolumeService.delete(this.dockerVolumeCreated.getId());
			
			@SuppressWarnings("unchecked")
			ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>> list = (ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>) messageService
					.find("open",
							new ParameterizedTypeReference<ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>>() {
							});

			for (com.dchq.schema.beans.one.inbox.Message message : list.getResults()) {
				if(message.getEntityType().equals(EntityType.DOCKER_VOLUME_DESTROY)){
					message.setMessageStatus(MessageStatus.READ);
					message.setMessageResolution(MessageResolution.APPROVED);
					ResponseEntity<com.dchq.schema.beans.one.inbox.Message> re = messageService.update(message);
					logger.info("Message approved {}", re.getResults().getBody());
					break;
				}
			}
			
			//Wait for 30 seconds for volume to be deleted completely
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			response = dockerVolumeService.findById(dockerVolumeCreated.getId());
			
			assertNotNull(response);
			assertNotNull(response.isErrors());
			Assert.assertTrue(response.getResults().getInactive());
			Assert.assertTrue(response.getResults().getDeleted());
		} catch (Exception e) {

		}
	}

	@After
	public void cleanUp() {/*
		
		//Volume is should be deleted by approving message. In case it's not deleted then retry
		if (this.dockerVolumeCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = dockerVolumeService.delete(this.dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}
		}
	*/}
}
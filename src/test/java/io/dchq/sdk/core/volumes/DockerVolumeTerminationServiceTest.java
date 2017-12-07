package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
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
import com.dchq.schema.beans.one.inbox.EntityType;
import com.dchq.schema.beans.one.inbox.MessageResolution;
import com.dchq.schema.beans.one.inbox.MessageStatus;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.MessageService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * Santosh Kumar
 * 
 */


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeTerminationServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;
	private MessageService messageService;
	DockerVolume dockerVolume;
	DockerVolume dockerVolumeCreated;
	boolean error;
	String validationMessage;

	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for 10 mins

	@org.junit.Before
	public void setUp() throws Exception {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
		messageService = ServiceFactory.buildMessageService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	public DockerVolumeTerminationServiceTest(String volumeName, String provider, String size, String volumeType,
			          String terminationProtection,EntitlementType type, boolean error) {
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		if(volumeName!=null )
		{
			volumeName = prefix.toLowerCase() + volumeName;
		}
		this.dockerVolume = new DockerVolume();
		this.dockerVolume.setName(volumeName);
		this.dockerVolume.setEndpoint(provider);
		this.dockerVolume.setSize(size);
		this.dockerVolume.setVolumeType(volumeType);
		this.dockerVolume.setEntitlementType(type);
		this.dockerVolume.setTerminationProtection(TerminationProtection.ENABLED);
		this.error = error;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		return Arrays.asList(new Object[][] {
				// TODO: add more test data for all sorts of validations		
				{ "createvolume", "2c9180865d35d99c015d363715c100e1",	"2", "SSD","ENABLED", EntitlementType.OWNER, false },
				{ "createvolume", "2c9180865d35d99c015d363715c100e1",	"2", "SSD","ENABLED", EntitlementType.PUBLIC, false },
				{ "createvolume", "2c9180865d35d99c015d363715c100e1",	"2", "SSD","ENABLED", EntitlementType.CUSTOM, false },
				// TODO volume name should not be blank
				//{ "", "2c9180865bb2559a015bd99819254459", "2", EntitlementType.OWNER, true },
				// TODO not accept only special characters
				//{ "@@@@@@@@", "2c9180865bb2559a015bd99819254459", "2", EntitlementType.CUSTOM, true},
				// TODO Should not accept negative volume
				//{ "nagative-volume", "2c9180865bb2559a015bd99819254459", "-2", EntitlementType.CUSTOM, true},
				
				{ "sadasdasdaaaaaaaassssssssssssssssssssssssssssssssssssssaaaaaaaaaaaaaaaaaaaaaaasdadasdad"
				 		+ "asdasdasddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
				 		+ "asdddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
				 		+ "asdddddddddddddddddddddddddddddddd", "2c9180865d35d99c015d363715c100e1",	"2", "SSD","ENABLED", EntitlementType.CUSTOM, true }
								
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

				dockerVolumeCreated = getStatus(this.dockerVolumeCreated);
				logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				assertNotNull(response);
				assertNotNull(response.isErrors());
				assertEquals("LIVE", dockerVolumeCreated.getStatus());
				
				
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
				logger.info("Volume Status is [{}]", volume.getStatus());
			} catch (InterruptedException e) {
				// TODO: handling exception
			}
		}
		return volume;
	}

	@After
	public void cleanUp() {/*
		
		//Volume is should be deleted by approving message. In case it's not deleted then retry
		if (this.dockerVolumeCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = dockerVolumeService.delete(this.dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error docker volume deletion: [{}] ", message.getMessageText());
			}
		}
	*/}
}
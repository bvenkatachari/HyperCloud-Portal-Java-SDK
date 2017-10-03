package io.dchq.sdk.core.backup;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import com.dchq.schema.beans.one.provider.DockerServer;
import com.google.gson.Gson;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.BackupService;
import io.dchq.sdk.core.DockerServerService;
import io.dchq.sdk.core.ServiceFactory;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BackupCreateServiceTest extends AbstractServiceTest {
	
	BackupService backupService;
	VMBackup backup;
	DockerServerService dockerServerService;
    DockerServer dockerServerCreated;
    long endTime;
	Gson gson;
	boolean success;
	
	@org.junit.Before
	public void setUp() throws Exception {
		
		backupService = ServiceFactory.buildBackupService(rootUrl1, cloudadminusername, cloudadminpassword);
		dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
		gson = new Gson();
	}
	
	
	public BackupCreateServiceTest(String jobName, boolean success) {

        backup = new VMBackup();
        backup.setJobName(jobName);

		this.success = success;
		endTime = System.currentTimeMillis() + (60 * 60 * 90); // this is for 3 mints

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { 
			{"DAILY", true }
			});
	}
	
	@Test
	public void createBackup() {
		try {
			
			DockerServer server = createDockerServer();
			logger.info("Create Backup for VM [{}] ", server.getName());
			
			backup.setVmName(server.getName());
			
			ResponseEntity<Object> response = backupService.createBackup(gson.toJson(backup));
			if (success) {
				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}

				assertNotNull(response);
				assertEquals(false, response.isErrors());


			} else {
				
				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			fail(e.getMessage());
		}

	}
	
   public DockerServer createDockerServer(){
    	
    	
		DockerServer server = new DockerServer().withName("VM_Backup")
				.withInactive(Boolean.FALSE).withImageId("C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub14HFT_DCHQ_Docker_Swarm.vhdx").withSize(1)
				.withEndpoint("2c9180865d312fc4015d3160f518008e").withHardwareId("cpu=1,memory=2GB,disk=20GB,generation=1").withNetworkId("Compute vmSwitch");
		server.setGroup("VM_Backup");
		server.setSkipAgentInstall("true");
		
		logger.info("Create Machine with Name [{}]", server.getName());
		ResponseEntity<DockerServer> serverResponse = dockerServerService.create(server);
		
		maxWaitTime = 300000;
		dockerServerCreated = serverResponse.getResults();
		String serverStatus = dockerServerCreated.getDockerServerStatus().name();
		
		while(serverStatus.equals("PROVISIONING") && (System.currentTimeMillis() < endTime)){	
			// Wait for some time until state changed from PROVISIONING to CONNECTED/PROVISIONED
			wait(10000); //wait for 10 seconds
			dockerServerCreated = dockerServerService.findById(dockerServerCreated.getId()).getResults();
			serverStatus = dockerServerCreated.getDockerServerStatus().name();
			 logger.info("Current Serverstatus   [{}] ", serverStatus);
	
		}
		
		while(serverStatus.equals("PROVISIONED") && (System.currentTimeMillis() < endTime)){	
			/*Noticed, sometimes system takes time to change the status from ‘Provisioned’ to ‘Connected’ 
			so we don’t have any exact number to wait. 
			Our script will wait for 2-3 mins and if in that time status won’t change than test will fail. */
			wait(10000); //wait for 10 seconds
			dockerServerCreated = dockerServerService.findById(dockerServerCreated.getId()).getResults();
			serverStatus = dockerServerCreated.getDockerServerStatus().name();
			 logger.info("Current Serverstatus   [{}] ", serverStatus);
	
		}
		
		return dockerServerCreated;
    }

	@After
	public void cleanUp() {

		if (this.backup != null) {
			logger.info("cleaning up Backup VM...");
			ResponseEntity<?> response = backupService.deleteBackup(gson.toJson(backup));
			for (Message message : response.getMessages()) {
				logger.warn("Error while deleting back VM: [{}] ", message.getMessageText());
			}
		}
		
		if (dockerServerCreated != null) {
            logger.info("Deleting Machine ");
            dockerServerService.delete(dockerServerCreated.getId(), true);

        }
	}

}

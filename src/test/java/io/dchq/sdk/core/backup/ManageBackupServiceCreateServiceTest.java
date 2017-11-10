package io.dchq.sdk.core.backup;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provider.DockerServer;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.BackupService;
import io.dchq.sdk.core.DockerServerService;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.dto.backup.BackupRequest;
import io.dchq.sdk.core.dto.backup.VMBackup;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class ManageBackupServiceCreateServiceTest extends AbstractServiceTest {
	
	BackupService backupService;
	BackupRequest backupRequest;
	DockerServerService dockerServerService;
    DockerServer dockerServerCreated;
    DockerServer server;
    long endTime;
	boolean success;
	
	@org.junit.Before
	public void setUp() throws Exception {
		
		backupService = ServiceFactory.buildBackupService(rootUrl1, cloudadminusername, cloudadminpassword);
		dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
	}
	
	
	public ManageBackupServiceCreateServiceTest(String jobName, String serverName, String hardwareId, String image, String networkId, String endpoint, boolean success) {

		backupRequest = new BackupRequest();
		backupRequest.setJobName(jobName);
		
		server = new DockerServer().withName(serverName)
				.withInactive(Boolean.FALSE).withImageId(image).withSize(1)
				.withEndpoint(endpoint).withHardwareId(hardwareId).withNetworkId(networkId);
		server.setGroup(serverName);
		server.setSkipAgentInstall("true");

		this.success = success;
		endTime = System.currentTimeMillis() + (60 * 60 * 90); // this is for 3 mints

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
			//HyperCloudVMware
			{"Testing_VmWare_Job", "HyperCloudVMware_Backup", "cpu=4,memory=1GB,disk=40GB", "VMT-CentOS7", "VN_501,vlanId=501", "2c9180875e9f1385015ea08e862d02e5",true},
			//HyperCloud Hyper-V
			{"Testing_JOb", "HyperCloudHyperV_Backup", "cpu=1,memory=2GB,disk=20GB,generation=1", "C:\\ClusterStorage\\HyperCloud_Templates\\Default\\Ub14HFT_DCHQ_Docker_Swarm.vhdx", "Compute vmSwitch", "2c9180865d312fc4015d3160f518008e",true}
			});
	}
	
	@Test
	public void testCreateBackup() {
		try {
			
			DockerServer server = createDockerServer();
			logger.info("Create Backup for VM [{}] ", server.getName());
			
			backupRequest.setVmName(server.getName());
			
			ResponseEntity<Object> response = backupService.createBackup(backupRequest);
			if (success) {
				for (Message message : response.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}

				assertNotNull(response);
				assertEquals(false, response.isErrors());
				
				//Wait as newly added backup VMs take some time to apper in listing
				
				wait(10000);
				
				ResponseEntity<List<VMBackup>> backupVMsResponse = backupService.findAllBackupVMs(0, 500);
				assertNotNull(backupVMsResponse);
				assertEquals(false, backupVMsResponse.isErrors());
				
                boolean found = false;
                
				for(VMBackup vm : backupVMsResponse.getResults()){
					
					if(backupRequest.getVmName().equals(vm.getName())){
						found = true;
						logger.info("BackupVM found with Name [{}]", vm.getName());
						break;
					}
				}
				
				Assert.assertTrue(found);

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
			/*
			 * Noticed, sometimes system takes time to change the status
			 * from "Provisioned" to "Connected" so we don't have any exact
			 * number to wait. Our script will wait for 2-3 mins and if in
			 * that time status won't change than test will fail.
			 */
			wait(10000); //wait for 10 seconds
			dockerServerCreated = dockerServerService.findById(dockerServerCreated.getId()).getResults();
			serverStatus = dockerServerCreated.getDockerServerStatus().name();
			 logger.info("Current Serverstatus   [{}] ", serverStatus);
	
		}
		
		return dockerServerCreated;
    }

	@After
	public void cleanUp() {

		if (this.backupRequest != null) {
			logger.info("cleaning up Backup VM...");
			ResponseEntity<?> response = backupService.deleteBackup(backupRequest);
			for (Message message : response.getMessages()) {
				logger.warn("Error while deleting back VM: [{}] ", message.getMessageText());
			}
			
			wait(10000);
			
			ResponseEntity<List<VMBackup>> backupVMs = backupService.findAllBackupVMs(0, 500);
			assertNotNull(response);
			assertEquals(false, response.isErrors());
			
            boolean found = false;
            
			for(VMBackup vm : backupVMs.getResults()){
				
				if(backupRequest.getVmName().equals(vm.getName())){
					found = true;
					break;
				}
			}
			
			Assert.assertFalse(found);
		}
		
		if (dockerServerCreated != null) {
            logger.info("Deleting Machine ");
            dockerServerService.delete(dockerServerCreated.getId(), true);

        }
	}

}

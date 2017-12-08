package io.dchq.sdk.core.backup;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provider.DockerServer;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.BackupService;
import io.dchq.sdk.core.DockerServerService;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.dto.backup.BackupJob;
import io.dchq.sdk.core.dto.backup.BackupRequest;
import io.dchq.sdk.core.dto.backup.CreateBackupJob;
import io.dchq.sdk.core.dto.backup.DeleteBackupJob;
import io.dchq.sdk.core.dto.backup.VMBackup;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BackupJobCreateServiceTest extends AbstractServiceTest {
	
	BackupRequest backupRequest;
	BackupService backupService;
	CreateBackupJob createBackupJob;
	DockerServerService dockerServerService;
	DockerServer dockerServerCreated;
    DockerServer backupDockerServerCreated;
    DockerServer server;
    long endTime;
	boolean success;
	
	@org.junit.Before
	public void setUp() throws Exception {
		
		backupService = ServiceFactory.buildBackupService(rootUrl1, cloudadminusername, cloudadminpassword);
		dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	
	public BackupJobCreateServiceTest(String jobName, String jobDesc, String repoName, String freq, String time, String fullBackup, String retainBackups, 
			String serverName, String hardwareId, String image, String networkId, String endpoint, boolean installAgent, boolean success) {

		String postfix = RandomStringUtils.randomAlphabetic(3);
		createBackupJob = new CreateBackupJob();
		createBackupJob.setJobName(jobName+postfix);
		createBackupJob.setJobDesc(jobDesc);
		createBackupJob.setRepoName(repoName);
		createBackupJob.setFreq(freq);
		createBackupJob.setTime(time);
		createBackupJob.setFullBackup(fullBackup);
		createBackupJob.setRetainBackups(retainBackups);
		
		
		server = new DockerServer().withName(serverName)
				.withInactive(Boolean.FALSE).withImageId(image).withSize(1)
				.withEndpoint(endpoint).withHardwareId(hardwareId).withNetworkId(networkId);
		server.setGroup(serverName);
		
		if(installAgent){
			this.server.setSkipAgentInstall("false");
			this.server.setImageUsername("hf");
			this.server.setImagePassword("HyperGrid123");
		}else {
		    this.server.setSkipAgentInstall("true");
		}

		this.success = success;
		endTime = System.currentTimeMillis() + (60 * 60 * 90); // this is for 3 mints

	}
	
	
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
			//HyperCloud VMWare
			{"Backup_Job", "This job is by Admin", "Default Backup Repository","Monthly", "23:30", "true", "7",
				 "HyperCloudVMware_Backup", "cpu=4,memory=1GB,disk=40GB", "VMT-CentOS7", "dvpvlan26,vlanId=26", "2c9180875e9f1385015ea08e862d02e5", true, true}
			});
	}
	
	
	@Test
	public void testCreateBackupJob() {
		try {
			
			backupDockerServerCreated = createDockerServer(server);
			
			logger.info("Create Backup Job [{}] ", createBackupJob.getJobName());
			
			createBackupJob.setVmName(backupDockerServerCreated.getName());
			
			ResponseEntity<Object> createResponse = backupService.createBackUpJob(createBackupJob);
		
			for (Message message : createResponse.getMessages()) {
	            logger.warn("Error while Create Backup Job request  [{}] ", message.getMessageText());
	        }
			
			assertFalse(createResponse.isErrors());
			
			
			if (success) {
				
				boolean found = false;
				ResponseEntity<List<BackupJob>> getResponse = backupService.findAllBackUpJobs(0, 500);
				
				for(BackupJob job : getResponse.getResults()){
					if(createBackupJob.getJobName().equals(job.getName())){
						found = true;
						break;
					}
				}
				
				assertTrue(found);
				
				
			} else {
				
				for (Message message : createResponse.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}
				assertEquals(null, createResponse.getResults());
				assertEquals(true, createResponse.isErrors());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			fail(e.getMessage());
		}

	}
	
	
	@Test
	public void testCreateBackupJobAndBackupVM() {
		try {
			
			backupDockerServerCreated = createDockerServer(server);
			
			logger.info("Create Backup Job [{}] ", createBackupJob.getJobName());
			
			createBackupJob.setVmName(backupDockerServerCreated.getName());
			
			ResponseEntity<Object> createResponse = backupService.createBackUpJob(createBackupJob);
		
			assertFalse(createResponse.isErrors());
			
			for (Message message : createResponse.getMessages()) {
	            logger.warn("Error while Create Backup Job request  [{}] ", message.getMessageText());
	        }
			
			if (success) {
				
		      //Backup Newly created VM
				server.setGroup("BackupVMTest");
				dockerServerCreated = createDockerServer(server);
				
				backupRequest = new BackupRequest();
				backupRequest.setJobName(createBackupJob.getJobName());
				backupRequest.setVmName(dockerServerCreated.getName());
				
				ResponseEntity<Object> backupResponse = backupService.createBackup(backupRequest);
				
				assertNotNull(backupResponse);
				assertEquals(false, backupResponse.isErrors());
				
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
				
				for (Message message : createResponse.getMessages()) {
					logger.warn("Error while Create request  [{}] ", message.getMessageText());
				}
				assertEquals(null, createResponse.getResults());
				assertEquals(true, createResponse.isErrors());
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			fail(e.getMessage());
		}

	}
	
   public DockerServer createDockerServer(DockerServer server){
		
		logger.info("Create Machine with Name [{}]", server.getName());
		ResponseEntity<DockerServer> serverResponse = dockerServerService.create(server);
		
		maxWaitTime = 300000;
		DockerServer dockerServer_created = serverResponse.getResults();
		String serverStatus = dockerServer_created.getDockerServerStatus().name();
		
		while(serverStatus.equals("PROVISIONING") && (System.currentTimeMillis() < endTime)){	
			// Wait for some time until state changed from PROVISIONING to CONNECTED/PROVISIONED
			wait(10000); //wait for 10 seconds
			dockerServer_created = dockerServerService.findById(dockerServer_created.getId()).getResults();
			serverStatus = dockerServer_created.getDockerServerStatus().name();
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
			dockerServer_created = dockerServerService.findById(dockerServer_created.getId()).getResults();
			serverStatus = dockerServer_created.getDockerServerStatus().name();
			 logger.info("Current Serverstatus   [{}] ", serverStatus);
	
		}
		
		return dockerServer_created;
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
		
		if (createBackupJob != null) {
            logger.info("Deleting Backup  Job ");
            DeleteBackupJob job = new DeleteBackupJob();
            job.setJobName(createBackupJob.getJobName());
            backupService.deleteBackUpJob(job);

        }
		
		if (backupDockerServerCreated != null) {
            logger.info("Deleting backup VM Machine ");
            dockerServerService.delete(backupDockerServerCreated.getId(), true);

        }
		
		if (dockerServerCreated != null) {
            logger.info("Deleting Machine ");
            dockerServerService.delete(dockerServerCreated.getId(), true);

        }
	}
}

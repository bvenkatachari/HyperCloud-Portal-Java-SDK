package io.dchq.sdk.core.backup;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provider.DockerServer;
import com.dchq.schema.beans.one.provider.SDIRequest;

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
public class BlueprintBackupCreateServiceTest extends AbstractServiceTest {

	BackupService backupService;
	List<BackupRequest> backups;
	DockerServerService dockerServerService;
	SDIRequest sdi;
	List<DockerServer> vms;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 90); // this is for 3 mints

	@org.junit.Before
	public void setUp() throws Exception {
		backupService = ServiceFactory.buildBackupService(rootUrl1, cloudadminusername, cloudadminpassword);
		dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	public BlueprintBackupCreateServiceTest(String blueprintId, String cloudProviderId, int tinout) {

		sdi = new SDIRequest();
		sdi.setBlueprint(blueprintId);
		sdi.setCloudProvider(cloudProviderId);
		// sdi.setCluster(clusterId);
		maxWaitTime = tinout;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
			// Backup-Hyper-V, vhg01cluster(HCS)
			{ "2c9180865f9ffa1a015fa4a2c99330e8", "2c9180865d312fc4015d3160f518008e", 300000 } });
	}

	@Ignore
	@org.junit.Test
	public void testCreateBackup() throws Exception {

		logger.info("Deploying VM Blueprint");
		ResponseEntity<List<DockerServer>> response = dockerServerService.deploy(sdi);

		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}

		if (response.getResults() != null && !response.isErrors()) {

			vms = response.getResults();
			logger.info("Total number of VMs created [{}]", vms.size());

			ExecutorService threadPool = Executors.newFixedThreadPool(vms.size());

			for (DockerServer server : vms) {
				Worker thread = new Worker(server);
				threadPool.submit(thread);
			}

			threadPool.shutdown();
			// wait for the threads to finish if necessary
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

			for (DockerServer server : vms) {
				server = dockerServerService.findById(server.getId()).getResults();
				logger.info("Docker Server " + server.getName() + " current status - "
						+ server.getDockerServerStatus().name());
				Assert.assertEquals("CONNECTED", server.getDockerServerStatus().name());

			}
		}

		ResponseEntity<List<VMBackup>> backupVMs = backupService.findAllBackupVMs(0, 500);
		assertNotNull(response);
		assertEquals(false, response.isErrors());

		boolean found = false;
		for (DockerServer server : vms) {
			for (VMBackup vm : backupVMs.getResults()) {

				if (server.getName().equals(vm.getName())) {
					found = true;
					
					BackupRequest request = new BackupRequest();
					request.setVmName(vm.getName());
					request.setJobName(vm.getJob());
					
					backups.add(request);
					
					logger.info("BackupVM found with Name [{}]", vm.getName());
					break;
				}
			}

			Assert.assertTrue(found);
		}
	}

	private class Worker implements Runnable {
		DockerServer server;

		Worker(DockerServer server) {
			this.server = server;
		}

		public void run() {
			logger.info("VM with name [{}] gets created.", server.getName());
			String serverStatus = server.getDockerServerStatus().name();

			while (serverStatus.equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
				// Wait for some time until state changed from PROVISIONING to
				// CONNECTED/PROVISIONED
				try {
					Thread.sleep(10000); // wait for 10 seconds
				} catch (Exception e) {
					logger.warn("Error @ Wait [{}] ", e.getMessage());
				}

				server = dockerServerService.findById(server.getId()).getResults();
				serverStatus = server.getDockerServerStatus().name();
				logger.info("Server " + server.getName() + " current status   [{}] ", serverStatus);

			}

			while (serverStatus.equals("PROVISIONED") && (System.currentTimeMillis() < endTime)) {
				/*
				 * Noticed, sometimes system takes time to change the status
				 * from "Provisioned" to "Connected" so we don't have any exact
				 * number to wait. Our script will wait for 2-3 mins and if in
				 * that time status won't change than test will fail.
				 */
				try {
					Thread.sleep(10000); // wait for 10 seconds
				} catch (Exception e) {
					logger.warn("Error @ Wait [{}] ", e.getMessage());
				}

				server = dockerServerService.findById(server.getId()).getResults();
				serverStatus = server.getDockerServerStatus().name();
				logger.info("Server " + server.getName() + " current status   [{}] ", serverStatus);

			}

		}

	}

	@After
	public void cleanUp() {

		if (this.backups != null) {

			for (BackupRequest backupRequest : backups) {
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

				for (VMBackup vm : backupVMs.getResults()) {

					if (backupRequest.getVmName().equals(vm.getName())) {
						found = true;
						break;
					}
				}

				Assert.assertFalse(found);
			}
		}

		if (vms != null) {
			for (DockerServer server : vms) {
				logger.info("Deleting Machine " + server.getName());
				dockerServerService.delete(server.getId(), true);

			}

		}

	}
}

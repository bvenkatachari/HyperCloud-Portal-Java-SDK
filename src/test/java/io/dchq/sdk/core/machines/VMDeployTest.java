package io.dchq.sdk.core.machines;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.provider.DockerServer;
import com.dchq.schema.beans.one.provider.SDIRequest;

import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class VMDeployTest extends DockerServerTest {

	SDIRequest sdi;
	List<DockerServer> vms;

	@org.junit.Before
	public void setUp() throws Exception {
		dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	public VMDeployTest(String blueprintId, String cloudProviderId, String clusterId, int tinout) {

		sdi = new SDIRequest();
		sdi.setBlueprint(blueprintId);
		sdi.setCloudProvider(cloudProviderId);
		// sdi.setCluster(clusterId);
		maxWaitTime = tinout;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
				// MultiVM_2V, vhg01cluster(HCS),Sam_Docker_Swarm_Cluster
				{ "2c9180875dcf94f1015dcfdf842502e9", "2c9180865d312fc4015d3160f518008e", "2c9180875dded97c015de03986f30a37", 300000 },
				// AWS Ubuntu 4GB, Amazon EC2
				{ "2c9180875e03a363015e05c4e3df0a5e", "2c9180865d400da0015d42805e9b01e1", "", 300000 },
				// Azure_Windows_2016, Microsoft Azure
				{ "2c9180875e9da16c015e9e5729200253", "2c9180865d400da0015d4273880c01e0", "", 300000 }
			});
	}

	@org.junit.Test
	public void testVMDeploy() throws Exception {

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
			 
			 for(DockerServer server : vms){
				 server = dockerServerService.findById(server.getId()).getResults();
				 logger.info("Docker Server "+server.getName() +" current status - "+server.getDockerServerStatus().name());
				 Assert.assertEquals("CONNECTED", server.getDockerServerStatus().name());
				 
			 }

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
				logger.info("Server "+server.getName() +" current status   [{}] ", serverStatus);

			}

			while (serverStatus.equals("PROVISIONED") && (System.currentTimeMillis() < endTime)) {
				/*
				 * Noticed, sometimes system takes time to change the status
				 * from ‘Provisioned’ to ‘Connected’ so we don’t have any exact
				 * number to wait. Our script will wait for 2-3 mins and if in
				 * that time status won’t change than test will fail.
				 */
				try {
		            Thread.sleep(10000); // wait for 10 seconds
		        } catch (Exception e) {
		            logger.warn("Error @ Wait [{}] ", e.getMessage());
		        }
				
				server = dockerServerService.findById(server.getId()).getResults();
				serverStatus = server.getDockerServerStatus().name();
				logger.info("Server "+server.getName() +" current status   [{}] ", serverStatus);

			}
			
		}

	}

	@After
	public void cleanUp() {

		if (vms != null) {
			/*for (DockerServer server : vms) {
				logger.info("Deleting Machine "+server.getName());
				dockerServerService.delete(server.getId(), true);
				validateProvision(server, "DESTROYING");
			}*/

		}

	}
}

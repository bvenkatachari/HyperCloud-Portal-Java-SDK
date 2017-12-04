package io.dchq.sdk.core.machines;

import static junit.framework.TestCase.assertNotNull;

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
import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.common.TerminationProtection;
import com.dchq.schema.beans.one.inbox.EntityType;
import com.dchq.schema.beans.one.inbox.MessageResolution;
import com.dchq.schema.beans.one.inbox.MessageStatus;
import com.dchq.schema.beans.one.provider.DockerServer;
import com.dchq.schema.beans.one.provider.SDIRequest;

import io.dchq.sdk.core.MessageService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class BlueprintVMTerminationServiceTest extends DockerServerTest {

	private MessageService messageService;
	SDIRequest sdi;
	List<DockerServer> vms;

	@org.junit.Before
	public void setUp() throws Exception {
		dockerServerService = ServiceFactory.buildDockerServerService(rootUrl, cloudadminusername, cloudadminpassword);
		messageService = ServiceFactory.buildMessageService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	public BlueprintVMTerminationServiceTest(String blueprintId, String cloudProviderId, int tinout) {

		sdi = new SDIRequest();
		sdi.setBlueprint(blueprintId);
		sdi.setCloudProvider(cloudProviderId);
		sdi.setTerminationProtection(TerminationProtection.ENABLED);
		maxWaitTime = tinout;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
				// QA_Automation_VM_Termination , vhg01cluster(HCS)
				{ "2c9180865fe7c46e016020e5a04263d6", "2c9180865d312fc4015d3160f518008e", 300000 }
			});
	}

	@Ignore
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
				
				 dockerServerService.delete(server.getId(), true);
				 
				 @SuppressWarnings("unchecked")
					ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>> list = (ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>) messageService
							.find("open",
									new ParameterizedTypeReference<ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>>() {
									});

					
					for (com.dchq.schema.beans.one.inbox.Message message : list.getResults()) {
						if(message.getEntityType().equals(EntityType.DOCKER_SERVER_DESTROY)){
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
					
					ResponseEntity<DockerServer> response2 = dockerServerService.findById(dockerServerCreated.getId());
					
					assertNotNull(response2);
					assertNotNull(response2.isErrors());
					Assert.assertTrue(response2.getResults().getInactive());
					Assert.assertTrue(response2.getResults().getDeleted());
				 
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
				logger.info("Server "+server.getName() +" current status   [{}] ", serverStatus);

			}
			
		}

	}

	@After
	public void cleanUp() {
		
		//VM will be deleted by approving messages
	}
}

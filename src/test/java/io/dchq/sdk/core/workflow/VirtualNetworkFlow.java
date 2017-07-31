package io.dchq.sdk.core.workflow;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.PkEntityBase;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import com.dchq.schema.beans.one.provider.DataCenter;
import com.dchq.schema.beans.one.provider.DockerServer;
import com.dchq.schema.beans.one.provision.App;
import com.dchq.schema.beans.one.provision.ProvisionState;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vlan.VirtualNetwork;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.AppService;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.DataCenterService;
import io.dchq.sdk.core.DockerServerService;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.VirtualNetworkService;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 * 
 *        Methods: 
 *        1) Create Vlan 
 *        2) Create VM 
 *        3) Deploy App 
 *        4) Create Volume 
 *        5) Attach Volume 
 *        6) Detach Volume 
 *        7) Delete data
 *        
 *    Methods having dependency on each other.
 *
 */

public class VirtualNetworkFlow extends AbstractServiceTest {
	
	private String prifix;
	private VirtualNetworkService vlanService;
	private VirtualNetwork virtualNetwork;
	private VirtualNetwork virtualNetworkCreated;

	private DockerVolumeService dockerVolumeService;
	private DockerVolume dockerVolume;
	private DockerVolume dockerVolumeCreated;

	private DataCenterService dataCenterService;
	private DockerServerService dockerServerService;
	private DockerServer dockerServer;
	private DockerServer dockerServerCreated;
	private String hardwareId;
	private String imageId;

	private AppService appService;
	private BlueprintService blueprintService;
	private App app;
	private Blueprint blueprint;

	private boolean success;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints

	public VirtualNetworkFlow(){
		setUp();
	}

	public VirtualNetworkFlow(String hardwareId, String imageId){
		this.hardwareId = hardwareId;
		this.imageId = imageId;
		this.success = true;
		setUp();
	}
	
	public void setUp() {
		prifix = RandomStringUtils.randomAlphabetic(3);
		vlanService = ServiceFactory.buildVirtualNetworkService(rootUrl1, cloudadminusername, cloudadminpassword);
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
		dataCenterService = ServiceFactory.buildDataCenterService(rootUrl1, cloudadminusername, cloudadminpassword);
		dockerServerService = ServiceFactory.buildDockerServerService(rootUrl1, cloudadminusername, cloudadminpassword);
		appService = ServiceFactory.buildAppService(rootUrl1, cloudadminusername, cloudadminpassword);
        blueprintService = ServiceFactory.buildBlueprintService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	public void createVlan() throws Exception {

		try {
			virtualNetwork = new VirtualNetwork();
			virtualNetwork.setName("TestVlan" + prifix);
			virtualNetwork.setDriver(networkProviderId);
			virtualNetwork.setEntitlementType(EntitlementType.OWNER);

			logger.info("Create vlan name[{}] ", virtualNetwork.getName());
			ResponseEntity<VirtualNetwork> resultResponse = vlanService.create(virtualNetwork);
			Assert.assertNotNull(resultResponse);

			for (Message msg : resultResponse.getMessages()) {
				logger.warn("Error [{}]  " + msg.getMessageText());
			}
			if (this.success) {
				Assert.assertEquals(false, resultResponse.isErrors());
				Assert.assertNotNull(resultResponse.getResults());

				if (resultResponse.getResults() != null && !resultResponse.isErrors()) {
					this.virtualNetworkCreated = resultResponse.getResults();
					logger.info("Create Vlan Successfully..");
				}
				logger.info("VLan state [{}]", virtualNetworkCreated.getStatus().name());
				while (virtualNetworkCreated.getStatus().name().equals("PROVISIONING")
						&& (System.currentTimeMillis() < endTime)) {
					try {
						// wait for some time
						Thread.sleep(10000);
						resultResponse = vlanService.findById(virtualNetworkCreated.getId());
						logger.info("VLan status [{}]", virtualNetworkCreated.getStatus().name());
						Assert.assertEquals(false, resultResponse.isErrors());
						Assert.assertNotNull(resultResponse.getResults());
						this.virtualNetworkCreated = resultResponse.getResults();
					} catch (InterruptedException e) {
						fail(e.getMessage());
					}

				}
				logger.info("VLan status [{}]", virtualNetworkCreated.getStatus().name());
				Assert.assertEquals("LIVE", virtualNetworkCreated.getStatus().name());
				Assert.assertEquals(virtualNetworkCreated.getName(), virtualNetwork.getName());
				Assert.assertEquals(virtualNetworkCreated.getDriver(), virtualNetwork.getDriver());

			} else {

				Assert.assertEquals(true, resultResponse.isErrors());
				Assert.assertEquals(null, resultResponse.getResults());
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	
	public void createDockerServer() throws Exception {

		try {
			String serverName = "TestVM" + prifix;
			String networkId = "Compute vmSwitch,vlanId="+virtualNetworkCreated.getVlanId();
			ResponseEntity<DataCenter> responseEntity = dataCenterService.findById(clusterID);
			this.dockerServer = new DockerServer().withDatacenter(responseEntity.getResults()).withName(serverName)
					.withInactive(Boolean.FALSE).withImageId(imageId).withSize(1)
					.withEndpoint("2c9180865d312fc4015d3160f518008e").withHardwareId(hardwareId)
					.withNetworkId(networkId);
			this.dockerServer.setGroup(serverName);
			this.dockerServer.setSkipAgentInstall("true");
			this.dockerServer.setOperatingSystem("LINUX");

			logger.info("Create Machine with Name [{}]", dockerServer.getName());
			ResponseEntity<DockerServer> response = dockerServerService.create(dockerServer);

			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (response.getResults() != null && !response.isErrors()) {
				dockerServerCreated = response.getResults();

				dockerServerCreated = validateProvision(dockerServerCreated, "PROVISIONING");
				Assert.assertNotNull("Machine is not in Running State.", dockerServerCreated);
				if (dockerServerCreated != null) {
					Assert.assertEquals(dockerServer.getInactive(), dockerServerCreated.getInactive());
					Assert.assertEquals(dockerServer.getEndpoint(), dockerServerCreated.getEndpoint());
					Assert.assertEquals("CONNECTED", dockerServerCreated.getDockerServerStatus().name());

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	
	public void deployApp() throws Exception{

		try {

			ResponseEntity<Blueprint> blueprintResponseEntity = blueprintService.findById(blueprintAppId);

			if (blueprintResponseEntity != null && !blueprintResponseEntity.isErrors()) {
				blueprint = blueprintResponseEntity.getResults();
			}

			if (blueprint != null) {
				// Set Cluster ID
				PkEntityBase dc = new PkEntityBase();
				dc.setId(clusterID);
				blueprint.setDatacenter(dc);

				// Deploying using blueprint object
				ResponseEntity<App> appResponseEntity = appService.deploy(blueprint);

				if (appResponseEntity.isErrors()) {
					for (Message m : appResponseEntity.getMessages()) {
						logger.warn("Error while deploying App [{}]", m.getMessageText());
					}
				}

				assertNotNull(appResponseEntity.getResults());

				app = appResponseEntity.getResults();

				if (app != null) {

					while ((app.getProvisionState() != ProvisionState.RUNNING)
							&& (app.getProvisionState() != ProvisionState.PROVISIONING_FAILED)
							&& (System.currentTimeMillis() < endTime)) {
						logger.info("Found app [{}] with status [{}]", app.getName(), app.getProvisionState());
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							logger.warn(e.getLocalizedMessage(), e);
						}
						app = appService.findById(app.getId()).getResults();
					}
					logger.info("Fished provisioning app [{}] with status [{}]", app.getName(),
							app.getProvisionState());

					assertNotNull(app.getId());
				}
				if (app.getProvisionState() != ProvisionState.RUNNING) {
					fail("App Status doesn't get changed to RUNNING, still showing : " + app.getProvisionState());
				}
				app = appService.findById(app.getId()).getResults();
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	
	public void createVolume() throws Exception {

		try {
			this.dockerVolume = new DockerVolume();
			this.dockerVolume.setName("TestVolume" + prifix);
			this.dockerVolume.setEndpoint(volumeProviderId);
			this.dockerVolume.setSize("2");
			this.dockerVolume.setEntitlementType(EntitlementType.OWNER);

			logger.info("Create docker volume name[{}] ", dockerVolume.getName());
			ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);
			assertNotNull(response);

			for (Message message : response.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
			}

			if (success) {
				assertEquals(false, response.isErrors());
				assertNotNull(response.getResults());

				if (response.getResults() != null && !response.isErrors()) {
					this.dockerVolumeCreated = response.getResults();
					logger.info("Create docker volume Successful..");
				}

				while (dockerVolumeCreated.getStatus().equals("PROVISIONING")
						&& (System.currentTimeMillis() < endTime)) {
					try {
						Thread.sleep(10000);
						dockerVolumeCreated = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
						assertNotNull(dockerVolumeCreated);
						logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
					} catch (InterruptedException e) {
						fail(e.getMessage());
					}
				}
				logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				assertNotNull(response);
				assertNotNull(response.isErrors());
				assertEquals("LIVE", dockerVolumeCreated.getStatus());
				if (this.dockerVolumeCreated != null) {
					assertNotNull(response.getResults().getId());
					assertNotNull(dockerVolumeCreated.getId());
					assertEquals(dockerVolume.getName(), dockerVolumeCreated.getName());
					assertEquals(dockerVolume.getOptionsText(), dockerVolumeCreated.getOptionsText());
					assertEquals(dockerVolume.getSize(), dockerVolumeCreated.getSize());
				}
			} else {
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	
	public void attachVolumeToDockerServer() throws Exception {

		try {

			logger.info("Attach docker volume name[{}] ", dockerVolume.getName());
			ResponseEntity<DockerVolume> response = dockerVolumeService.attachVolume(dockerVolumeCreated.getId(),
					dockerServerCreated.getId());
			assertNotNull(response);

			for (Message message : response.getMessages()) {
				logger.warn("Error while Attach Volume request  [{}] ", message.getMessageText());
			}

			if (success) {
				assertEquals(false, response.isErrors());
				assertNotNull(response.getResults());

				if (response.getResults() != null && !response.isErrors()) {
					this.dockerVolumeCreated = response.getResults();
					logger.info("Attach docker volume Successful..");
				}

				while (dockerVolumeCreated.getStatus().equals("PROVISIONING")
						&& (System.currentTimeMillis() < endTime)) {
					try {
						Thread.sleep(10000);
						dockerVolumeCreated = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
						assertNotNull(dockerVolumeCreated);
						logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
					} catch (InterruptedException e) {
						fail(e.getMessage());
					}
				}
				logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				assertNotNull(response);
				assertNotNull(response.isErrors());
				assertEquals("LIVE", dockerVolumeCreated.getStatus());
				if (this.dockerVolumeCreated != null) {
					assertNotNull(response.getResults().getId());
					assertNotNull(dockerVolumeCreated.getId());
					assertEquals(dockerVolume.getName(), dockerVolumeCreated.getName());
					assertEquals(dockerVolume.getOptionsText(), dockerVolumeCreated.getOptionsText());
					assertEquals(dockerVolume.getSize(), dockerVolumeCreated.getSize());
				}
			} else {
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}


	public void detachVolumeToDockerServer() throws Exception {

		try {

			logger.info("Detach docker volume name[{}] ", dockerVolume.getName());
			ResponseEntity<DockerVolume> response = dockerVolumeService.detachVolume(dockerVolumeCreated.getId(),
					dockerServerCreated.getId());
			assertNotNull(response);

			for (Message message : response.getMessages()) {
				logger.warn("Error while Detach Volume request  [{}] ", message.getMessageText());
			}

			if (success) {
				assertEquals(false, response.isErrors());
				assertNotNull(response.getResults());

				if (response.getResults() != null && !response.isErrors()) {
					this.dockerVolumeCreated = response.getResults();
					logger.info("Attach docker volume Successful..");
				}

				while (dockerVolumeCreated.getStatus().equals("PROVISIONING")
						&& (System.currentTimeMillis() < endTime)) {
					try {
						Thread.sleep(10000);
						dockerVolumeCreated = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
						assertNotNull(dockerVolumeCreated);
						logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
					} catch (InterruptedException e) {
						fail(e.getMessage());
					}
				}
				logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				assertNotNull(response);
				assertNotNull(response.isErrors());
				assertEquals("LIVE", dockerVolumeCreated.getStatus());
				if (this.dockerVolumeCreated != null) {
					assertNotNull(response.getResults().getId());
					assertNotNull(dockerVolumeCreated.getId());
					assertEquals(dockerVolume.getName(), dockerVolumeCreated.getName());
					assertEquals(dockerVolume.getOptionsText(), dockerVolumeCreated.getOptionsText());
					assertEquals(dockerVolume.getSize(), dockerVolumeCreated.getSize());
				}
			} else {
				assertEquals(null, response.getResults());
				assertEquals(true, response.isErrors());
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	private DockerServer validateProvision(DockerServer inputDocker, String action) {
		int warningCount = 0;
		waitTime = 0;
		maxWaitTime = 300000;
		DockerServer outDockerserver = null;
		DockerServer tempDockerserver = null;
		String serverStatus = inputDocker.getDockerServerStatus() == null ? ""
				: inputDocker.getDockerServerStatus().name();
		provision: do {

			if (wait(10000) == 0)
				break provision;
			ResponseEntity<DockerServer> response = dockerServerService.findById(inputDocker.getId());

			assertNotNull(response);
			assertNotNull(response.isErrors());

			if (response.getResults() != null) {
				tempDockerserver = response.getResults();
				serverStatus = tempDockerserver.getDockerServerStatus().name();
				logger.info("Current Serverstatus   [{}] ", serverStatus);
				if (serverStatus.equals("PROVISIONED") || serverStatus.equals("CONNECTED")
						|| serverStatus.equals("DESTROYED"))
					break provision;

			}
			if (serverStatus == "WARNINGS") {
				warningCount++;
				serverStatus = action;
			} else if (warningCount > 0 || warningCount < 5) {
				warningCount++;
			}

		} while (serverStatus == action);
		if (tempDockerserver.getDockerServerStatus().name().equals("PROVISIONED")
				|| tempDockerserver.getDockerServerStatus().name().equals("CONNECTED")
				|| tempDockerserver.getDockerServerStatus().name().equals("DESTROYED")) {
			outDockerserver = tempDockerserver;
		}
		return outDockerserver;

	}


	public void cleanUp() {

		if (this.dockerVolumeCreated != null) {
			logger.info("cleaning up Volume...");
			ResponseEntity<?> response = dockerVolumeService.delete(this.dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error volume deletion: [{}] ", message.getMessageText());
			}
		}

		if (app != null) {
			logger.info("cleaning up App...");
			appService.destroy(app.getId());

			app = appService.findById(app.getId()).getResults();
			while ((app.getProvisionState() != ProvisionState.DESTROYED) && (System.currentTimeMillis() < endTime)) {
				logger.info("Destroying app [{}] with status [{}]", app.getName(), app.getProvisionState());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					logger.warn(e.getLocalizedMessage(), e);
				}
				app = appService.findById(app.getId()).getResults();

			}

			if (app.getProvisionState() != ProvisionState.DESTROYED) {
				fail("App Status doesn't get changed to DESTROYED, still showing : " + app.getProvisionState());
			}
			logger.info("Finisheded Destroying provisioning app [{}] with status [{}]", app.getName(),
					app.getProvisionState());
		}

		if (dockerServerCreated != null) {
			logger.info("cleaning up Machine ");
			ResponseEntity<?> response = dockerServerService.delete(dockerServerCreated.getId(), true);
			for (Message message : response.getMessages()) {
				logger.warn("Error Machine deletion: [{}] ", message.getMessageText());
			}

		}

		if (virtualNetworkCreated != null) {
			logger.info("cleaning up Virtual Network...");
			ResponseEntity<VirtualNetwork> responseDelete = vlanService.delete(virtualNetworkCreated.getId(),
					"release/");
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error vlan deletion: [{}] ", message.getMessageText());
			}
		}

	}

}

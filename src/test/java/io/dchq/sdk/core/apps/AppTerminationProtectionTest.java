package io.dchq.sdk.core.apps;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.PkEntityBase;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.common.TerminationProtection;
import com.dchq.schema.beans.one.inbox.EntityType;
import com.dchq.schema.beans.one.inbox.MessageResolution;
import com.dchq.schema.beans.one.inbox.MessageStatus;
import com.dchq.schema.beans.one.provision.App;
import com.dchq.schema.beans.one.provision.AppLifecycleProfile;
import com.dchq.schema.beans.one.provision.ProvisionState;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.AppService;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.MessageService;
import io.dchq.sdk.core.ServiceFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class AppTerminationProtectionTest extends AbstractServiceTest {

	private AppService appService;
	private BlueprintService blueprintService;
	private Blueprint blueprint;
	private String blueprintId;
	
	private App appObject;
	private MessageService messageService;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints

	public AppTerminationProtectionTest(String blueprintId) {
		this.blueprintId = blueprintId;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays
				.asList(new Object[][] { 
					//nginx
					{ "402881864e1a36cc014e1a399cf90101"} 
				});
	}

	@Before
	public void setUp() {
		appService = ServiceFactory.buildAppService(rootUrl1, cloudadminusername, cloudadminpassword);
		blueprintService = ServiceFactory.buildBlueprintService(rootUrl1, cloudadminusername, cloudadminpassword);
		messageService = ServiceFactory.buildMessageService(rootUrl1, cloudadminusername, cloudadminpassword);
	}

	
	@Test
	public void deployApp() {
		logger.info("Start deploying");
		// Getting blueprint object
		ResponseEntity<Blueprint> blueprintResponse = blueprintService.findById(blueprintId);
		assertNotNull(blueprintResponse);
		assertEquals(false, blueprintResponse.isErrors());

		blueprint = blueprintResponse.getResults();
		PkEntityBase dc = new PkEntityBase();
		dc.setId(clusterID);
		blueprint.setDatacenter(dc);
		blueprint.setTerminationProtection(TerminationProtection.ENABLED);
		
		ResponseEntity<App> response = appService.deploy(blueprint);
		assertNotNull(response);
		assertEquals(false, response.isErrors());
		if (response.getResults() != null && !response.isErrors()) {
			appObject = response.getResults();
			logger.info("App deploy Successfully..");
		}
		logger.info("App deployment state [{}]", appObject.getProvisionState());

		appObject = appService.findById(appObject.getId()).getResults();
		
		while (appObject.getProvisionState().name().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
			try {
				// wait for some time
				Thread.sleep(10000);
				response = appService.findById(appObject.getId());
				Assert.assertEquals(false, response.isErrors());
				Assert.assertNotNull(response.getResults());
				appObject = response.getResults();
			} catch (InterruptedException e) {
				// ignore
			}

		}
		
		
			AppLifecycleProfile appProfile = new AppLifecycleProfile();
			appProfile.setNote("Destroy");
			appProfile.setAllSelected(true);
			ResponseEntity<App> resp = appService.doPost(appProfile, appObject.getId() + "/destroy/false");
			
		for (Message message : resp.getMessages()) {
			logger.warn("Error container app deletion: [{}] ", message.getMessageText());
		}
		
		
		@SuppressWarnings("unchecked")
		ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>> list = (ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>) messageService
				.find("open",
						new ParameterizedTypeReference<ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>>() {
						});

		for (com.dchq.schema.beans.one.inbox.Message message : list.getResults()) {
			if(message.getEntityType().equals(EntityType.PROVISION_TASK_TYPE)){
				message.setMessageStatus(MessageStatus.READ);
				message.setMessageResolution(MessageResolution.APPROVED);
				ResponseEntity<com.dchq.schema.beans.one.inbox.Message> re = messageService.update(message);
				logger.info("Message approved {}", re.getResults().getBody());
				break;
			}
		}
		
		//Wait for 30 seconds for App to be deleted completely
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ResponseEntity<App> response2 = appService.findById(appObject.getId());
		
		assertNotNull(response2);
		assertNotNull(response2.isErrors());
		Assert.assertEquals(ProvisionState.DESTROYED, response2.getResults().getProvisionState());
		
	}

	@After
	public void cleanUp() {
		
	}
}

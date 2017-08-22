package io.dchq.sdk.core.appdeploy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.PkEntityBase;
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.container.Container;
import com.dchq.schema.beans.one.inbox.MessageResolution;
import com.dchq.schema.beans.one.inbox.MessageStatus;
import com.dchq.schema.beans.one.provision.App;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.AppService;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.MessageService;
import io.dchq.sdk.core.ServiceFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class MYF417ThreeTierE2ETest extends AbstractServiceTest{

	private AppService appService;
	private BlueprintService blueprintService;
	private Blueprint blueprint;
	private String blueprintId;
	private String clusterId;
	private String akey = "taIEQ6VPlsDe1NHwwnEv";
	private String skey = "LjVh2sEwJlycnmkdXHesjeky9OxAtYivnwJQQLuj";
	private App appObject;
	private MessageService messageService;
	ParameterizedTypeReference<ResponseEntity<List<Message>>> listTypeReference = new ParameterizedTypeReference<ResponseEntity<List<Message>>>() {
	};
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 50); // this is for 3 mints

	public MYF417ThreeTierE2ETest(String blueprintId, String clusterId) {
		this.blueprintId = blueprintId;
		this.clusterId = clusterId;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays
				.asList(new Object[][] { 
					{ "2c9180875d833a34015d87cba8a20c6c", "2c9180865d312fc4015d314b5d510069" },
					{ "402881864e1a36cc014e1a399cf90102", "2c9180865d312fc4015d314b5d510069" }});
	}

	@Before
	public void setUp() {
		appService = ServiceFactory.buildAppService(rootUrl1, akey, skey);
		blueprintService = ServiceFactory.buildBlueprintService(rootUrl1, akey, skey);
		messageService = ServiceFactory.buildMessageService(rootUrl1, akey, skey);
	}
	@Ignore
	@Test
	public void deploy3Tier() {
		logger.info("Start deploying");
		// Getting blueprint object
		ResponseEntity<Blueprint> blueprintResponse = blueprintService.findById(blueprintId);
		assertNotNull(blueprintResponse);
		assertEquals(false, blueprintResponse.isErrors());

		blueprint = blueprintResponse.getResults();
		PkEntityBase dc = new PkEntityBase();
		dc.setId(clusterId);
		blueprint.setDatacenter(dc);
		ResponseEntity<App> response = appService.deploy(blueprint);
		assertNotNull(response);
		assertEquals(false, response.isErrors());
		if (response.getResults() != null && !response.isErrors()) {
			appObject = response.getResults();
			logger.info("App deploy Successfully..");
		}
		logger.info("App deployment state [{}]", appObject.getProvisionState());

		if (appObject.getProvisionState().name().equals("WAITING_APPROVAL")) {

			ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>> list = (ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>) messageService
					.find("unread",
							new ParameterizedTypeReference<ResponseEntity<List<com.dchq.schema.beans.one.inbox.Message>>>() {
							});

			for (com.dchq.schema.beans.one.inbox.Message message : list.getResults()) {
				message.setMessageStatus(MessageStatus.READ);
				message.setMessageResolution(MessageResolution.APPROVED);
				ResponseEntity<com.dchq.schema.beans.one.inbox.Message> re = messageService.update(message);
				logger.info("Message approved {[]}", re.getResults().getBody());
			}
		}
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
		assertEquals(3, appObject.getContainers());
		for(Container container: appObject.getContainers())
		{
			assertEquals("RUNNING", container.getContainerStatus().name());
		}
	}

	@After
	public void cleanUp() {
		logger.info("Deleting app");
		if (appObject != null) {
			ResponseEntity<App> resp = null;
			if (appObject.getProvisionState().name().equals("RUNNING")) {
				resp = appService.doPost(appObject, appObject.getId() + "/destroy/false");
			} else {
				resp = appService.delete(appObject.getId());
			}
			for (Message message : resp.getMessages()) {
				logger.warn("Error container app deletion: [{}] ", message.getMessageText());
			}
		}
	}
}

package io.dchq.sdk.core.volumes;

import java.util.Arrays;
import java.util.Collection;

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
import com.dchq.schema.beans.one.blueprint.Blueprint;
import com.dchq.schema.beans.one.blueprint.BlueprintType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.BlueprintService;
import io.dchq.sdk.core.ServiceFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeComposeDeployTest extends DockerVolumeBaseImpl {

	private Blueprint blueprint, bluePrintCreated;
	private BlueprintService blueprintService;
	boolean error;
	private String validationMessage;
	
	public DockerVolumeComposeDeployTest(
			BlueprintType blueprintType, 
			String blueprintName, 
			String yml
			) 
	{
		// random user name
		String prefix = RandomStringUtils.randomAlphabetic(3);
		blueprintName = prefix + blueprintName;
		this.blueprint = new Blueprint().withBlueprintType(blueprintType).withName(blueprintName);
		this.blueprint.setYml(yml);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(
				new Object[][] { { BlueprintType.VOLUME, "lib-volume-compose", "testvolume:\n  driver: local" } });
	}

	@org.junit.Before
	public void setUp() throws Exception {
		blueprintService = ServiceFactory.buildBlueprintService(rootUrl, username, password);
	}

	// create volume compose
	@Test
	public void testVolumeCompose() {

		ResponseEntity<Blueprint> response = blueprintService.create(blueprint);

		logger.info("Blueprint created by name [{}] and yaml [{}] ", blueprint.getName(), blueprint.getYml());

		for (Message m : response.getMessages()) {
			logger.warn("[{}]", m.getMessageText());
		}
		if (response.getResults() != null) {
			bluePrintCreated = response.getResults();
		}

		Assert.assertEquals(blueprint.getBlueprintType(), bluePrintCreated.getBlueprintType());
		Assert.assertEquals(blueprint.getName(), bluePrintCreated.getName());
		Assert.assertEquals(blueprint.getYml(), bluePrintCreated.getYml());
		
		/*
		 app = deployAndWait(blueprint, error, validationMessage);
	     logger.info("App Provision State is [{}] " + app.getProvisionState());
		 */
	}

	@After
	public void cleanUp() {
		// TODO: find out what can be done to clean up volume compose blueprint
	}

}

package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.springframework.util.StringUtils;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import com.dchq.schema.beans.one.security.EntitlementType;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;
/**
 * 
 * @author msys
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeEntitledServiceTest extends AbstractServiceTest {

	private DockerVolumeService dockerVolumeService;
	private DockerVolumeService dockerVolumeService2;
	private DockerVolumeService dockerVolumeService3;
	DockerVolume createVolume;
	DockerVolume dockerVolumeCreated;
	boolean sussess;
	long startTime = System.currentTimeMillis();
	long endTime = startTime + (60 * 60 * 160); // this is for aprox 10 mints
	boolean isEntitlementTypeUser;

	public DockerVolumeEntitledServiceTest(String volumeName, String provider, String size, EntitlementType entitlementType,
			boolean isEntitlementTypeUser, String entitledUserId,  boolean success) {
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (volumeName != null && !volumeName.isEmpty() ) {
			volumeName = (volumeName + prifix).toLowerCase();
		}
		createVolume = new DockerVolume();

		if (!StringUtils.isEmpty(entitledUserId) && isEntitlementTypeUser) {
			UsernameEntityBase entitledUser = new UsernameEntityBase().withId(entitledUserId);
			List<UsernameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.createVolume.setEntitledUsers(entiledUsers);
		} else if (!StringUtils.isEmpty(entitledUserId)) { // assume user-group
			NameEntityBase entitledUser = new NameEntityBase().withId(entitledUserId);
			List<NameEntityBase> entiledUsers = new ArrayList<>();
			entiledUsers.add(entitledUser);
			this.createVolume.setEntitledUserGroups(entiledUsers);
		}

		createVolume.setName(volumeName);
		createVolume.setEntitlementType(entitlementType);
		createVolume.setEndpoint(provider);
		createVolume.setSize(size);
		this.sussess = success;
		this.isEntitlementTypeUser = isEntitlementTypeUser;
	}

	@Before
	public void setUp() {
		dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl1, cloudadminusername, cloudadminpassword);
		dockerVolumeService2 = ServiceFactory.buildDockerVolumeService(rootUrl1, username2, password2);
		dockerVolumeService3 = ServiceFactory.buildDockerVolumeService(rootUrl1, username3, password3);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		// provider id "8a818a105c83f42a015c83fd71240014" Intesar's machine
		return Arrays.asList(new Object[][] { 
			{ "createvolume", "2c9180865d35d99c015d363715c100e1",	"2", EntitlementType.OWNER, false, null, false },
			{ "createvolume", "2c9180865d35d99c015d363715c100e1",	"2", EntitlementType.PUBLIC, false, null, false },
			{ "createvolume", "2c9180865d35d99c015d363715c100e1",	"2", EntitlementType.CUSTOM, true, userId2, false },
			
		});
	}
	@Test
	public void findEntitleTest() {
		logger.info("Create Volume name[{}] ", createVolume.getName());
		ResponseEntity<DockerVolume> resultResponse = dockerVolumeService.create(createVolume);
		Assert.assertNotNull(resultResponse);

		for (Message msg : resultResponse.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		if (!this.sussess) {
			Assert.assertEquals(false, resultResponse.isErrors());
			Assert.assertNotNull(resultResponse.getResults());

			if (resultResponse.getResults() != null && !resultResponse.isErrors()) {
				this.dockerVolumeCreated = resultResponse.getResults();
				logger.info("Create Volume Successfully..");
			}
			logger.info("Volume state [{}]", dockerVolumeCreated.getStatus());
			while (dockerVolumeCreated.getStatus().equals("PROVISIONING") && (System.currentTimeMillis() < endTime)) {
				try {
					Thread.sleep(10000);
					dockerVolumeCreated = dockerVolumeService.findById(dockerVolumeCreated.getId()).getResults();
					assertNotNull(dockerVolumeCreated);
					logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
				} catch (InterruptedException e) {
					// TODO: handling exception
				}
			}
			logger.info("Volume Status is [{}]", dockerVolumeCreated.getStatus());
			
			if (createVolume.getEntitlementType().equals(EntitlementType.OWNER)) {
				ResponseEntity<DockerVolume> resultResponse1 = dockerVolumeService2.findById(dockerVolumeCreated.getId());
				for (Message message : resultResponse1.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				////User may not be entitled to the Volume
				Assert.assertNotNull(((Boolean) true).toString(), ((Boolean) resultResponse1.isErrors()).toString());
				Assert.assertEquals(null, resultResponse1.getResults());
				
			} else if (createVolume.getEntitlementType().equals(EntitlementType.PUBLIC)) {
				
				ResponseEntity<DockerVolume> resultResponse1 = dockerVolumeService2.findById(dockerVolumeCreated.getId());
				for (Message message : resultResponse1.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponse1.isErrors()).toString());
				assertNotNull(resultResponse1.getResults());
				assertNotNull(resultResponse1.getResults().getId());
				assertEquals(dockerVolumeCreated.getId(), resultResponse1.getResults().getId());

			} else if (createVolume.getEntitlementType().equals(EntitlementType.CUSTOM)) {
				
				ResponseEntity<DockerVolume> resultResponse1 = dockerVolumeService2.findById(dockerVolumeCreated.getId());
				for (Message message : resultResponse1.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponse1.isErrors()).toString());
				assertNotNull(resultResponse1.getResults());
				assertNotNull(resultResponse1.getResults().getId());
				assertEquals(dockerVolumeCreated.getId(), resultResponse1.getResults().getId());
				
			} else if (createVolume.getEntitlementType().equals(EntitlementType.CUSTOM) && !isEntitlementTypeUser) {
				
				ResponseEntity<DockerVolume> resultResponseForGroupUser2 = dockerVolumeService3.findById(dockerVolumeCreated.getId());
				for (Message message : resultResponseForGroupUser2.getMessages()) {
					logger.warn("Error while Find request  [{}] ", message.getMessageText());
				}
				Assert.assertNotNull(((Boolean) false).toString(), ((Boolean) resultResponseForGroupUser2.isErrors()).toString());
				assertNotNull(resultResponseForGroupUser2.getResults());
				assertNotNull(resultResponseForGroupUser2.getResults().getId());
				assertEquals(dockerVolumeCreated.getId(), resultResponseForGroupUser2.getResults().getId());
				
			}

		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}
	}

	@After
	public void cleanUp() {
		if (this.dockerVolumeCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<DockerVolume> responseDelete = dockerVolumeService.delete(dockerVolumeCreated.getId());
			//Assert.assertEquals(false, responseDelete.isErrors());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error Volume deletion: [{}] ", message.getMessageText());
			}
		}
	}
}


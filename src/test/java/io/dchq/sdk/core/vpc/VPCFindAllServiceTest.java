package io.dchq.sdk.core.vpc;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.VPCService;

/**
 * 
 * @author Jagdeep Jain
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class VPCFindAllServiceTest extends AbstractServiceTest {
	private VPCService vpcService;

	VirtualPrivateCloud createVPC;
	VirtualPrivateCloud createdVPC;
	private int countBeforeCreate = 0, countAfterCreate = 0;
	boolean sussess;

	public VPCFindAllServiceTest(String vpcName, boolean success) {
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (vpcName != null && !vpcName.isEmpty()) {
			vpcName = vpcName + "-" + prifix;
		}
		createVPC = new VirtualPrivateCloud();
		createVPC.setName(vpcName);
		this.sussess = success;
	}

	@Before
	public void setUp() {
		vpcService = ServiceFactory.buildVPCService(rootUrl, cloudadminusername, cloudadminpassword);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] { { "", "" } });
	}
	public int testVPCPosition(String id) {
		ResponseEntity<List<VirtualPrivateCloud>> response = vpcService.findAll(0, 500);
		assertNotNull(response);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]  " + message.getMessageText());
		}
		assertNotNull(response.isErrors());
		assertEquals(false, response.isErrors());
		int position = 0;
		if (id != null) {
			for (VirtualPrivateCloud obj : response.getResults()) {
				position++;
				if (obj.getId().equals(id)) {
					logger.info("  Object Matched in FindAll {}  at Position : {}", id, position);
					assertEquals("Recently Created Object is not at Positon 1 :" + obj.getId(), 1, position);
				}
			}
		}
		logger.info(" Total Number of Objects :{}", response.getResults().size());
		return response.getResults().size();
	}
	@Ignore
	@Test
	public void findAllTest() {
		logger.info("Create VPC name[{}] ", createVPC.getName());
		countBeforeCreate = testVPCPosition(null);
		ResponseEntity<VirtualPrivateCloud> response = vpcService.create(createVPC);
		Assert.assertNotNull(response);
		for (Message msg : response.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		
		if (this.sussess) {
			Assert.assertEquals(false, response.isErrors());
			Assert.assertNotNull(response.getResults());
			if (response.getResults() != null && !response.isErrors()) {
				this.createdVPC = response.getResults();
				logger.info("Create VPC Successful..");
			}
			countAfterCreate = testVPCPosition(createdVPC.getId());
			assertEquals(countBeforeCreate + 1, countAfterCreate);

		} else {

			Assert.assertEquals(true, response.isErrors());
			Assert.assertEquals(null, response.getResults());
		}
	}

	@After
	public void cleanUp() {
		if(this.createdVPC !=null)
		{
			logger.info("cleaning up...");
			ResponseEntity<VirtualPrivateCloud> responseDelete = vpcService.delete(createdVPC.getId());
			for (Message message : responseDelete.getMessages()) {
				logger.warn("Error volume deletion: [{}] ", message.getMessageText());
			}
		}
	}
}

package io.dchq.sdk.core.vpc;

import java.util.Arrays;
import java.util.Collection;

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
public class VPCCreateServiceTest extends AbstractServiceTest {
	
	private VPCService vpcService;
	
	VirtualPrivateCloud createVPC;
	VirtualPrivateCloud createdVPC;
	boolean sussess;
	
	
	public VPCCreateServiceTest(String vpcName, boolean success)
	{
		String prifix = RandomStringUtils.randomAlphabetic(3);

		if (vpcName != null && !vpcName.isEmpty()) {
			vpcName = vpcName + "-" + prifix;
		}
		createVPC = new VirtualPrivateCloud();
		createVPC.setName(vpcName);
		this.sussess = success;

	}
	@Before
	public void setUp()
	{
		vpcService = ServiceFactory.buildVPCService(rootUrl, cloudadminusername, cloudadminpassword);
	}
	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][]{ 
				{"testvpc", true}
		});
	}
	@Ignore
	@Test
	public void createTest()
	{
		logger.info("Create VPC name[{}] ", createVPC.getName());
		ResponseEntity<VirtualPrivateCloud> resultResponse = vpcService.create(createVPC);
		Assert.assertNotNull(resultResponse);

		for (Message msg : resultResponse.getMessages()) {
			logger.warn("Error [{}]  " + msg.getMessageText());
		}
		if (this.sussess) {
			Assert.assertEquals(false, resultResponse.isErrors());
			Assert.assertNotNull(resultResponse.getResults());

			if (resultResponse.getResults() != null && !resultResponse.isErrors()) {
				this.createdVPC = resultResponse.getResults();
				logger.info("Create VPC Successful..");
			}

		} else {

			Assert.assertEquals(true, resultResponse.isErrors());
			Assert.assertEquals(null, resultResponse.getResults());
		}
	}
	@After
	public void cleanUp()
	{
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

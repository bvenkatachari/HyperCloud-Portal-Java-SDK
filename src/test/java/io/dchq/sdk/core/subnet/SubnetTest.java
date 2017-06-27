package io.dchq.sdk.core.subnet;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.Subnet;
import com.dchq.schema.beans.one.vpc.VirtualPrivateCloud;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.SubnetService;
import io.dchq.sdk.core.VPCService;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */
public class SubnetTest extends AbstractServiceTest {

	public final String vpcId = "402881845c9458a6015c945ac24c0004";

	// Create VPC
	VPCService vpcService;
	VirtualPrivateCloud createdVPC;

	// Create Subnet
	SubnetService subnetService;
	Subnet subnet;
	Subnet subnetCreated;
	boolean success;

	// TO DO
	public VirtualPrivateCloud getVPC(String vpcName, boolean success) {
		logger.info("Create VPC with Name [{}]", vpcName);
		this.vpcService = ServiceFactory.buildVPCService(rootUrl, cloudadminusername, cloudadminpassword);
		VirtualPrivateCloud vpc = new VirtualPrivateCloud();
		vpc.setName(vpcName);

		ResponseEntity<VirtualPrivateCloud> responseEntity = vpcService.create(vpc);
		if (responseEntity.isErrors())
			logger.warn("Message from Server... {}", responseEntity.getMessages().get(0).getMessageText());

		return responseEntity.getResults();

	}

}

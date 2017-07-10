package io.dchq.sdk.core.networkaclrule;

import org.apache.commons.lang3.RandomStringUtils;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.NameEntityBase;
import com.dchq.schema.beans.one.security.EntitlementType;
import com.dchq.schema.beans.one.vpc.NetworkACL;
import com.dchq.schema.beans.one.vpc.Rule;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.NetworkACLRuleService;
import io.dchq.sdk.core.NetworkACLService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */
public class NetworkACLRuleTest extends AbstractServiceTest {

	// Create Network ACL
	NetworkACLService networkACLService;
	NetworkACL networkACL;

	// Create Network ACL Rule
	NetworkACLRuleService ruleService;
	Rule rule;
	Rule ruleCreated;
	boolean success;

	public NetworkACL getNetworkACL() {

		networkACLService = ServiceFactory.buildNetworkACLService(rootUrl1, username, password);
		
		String postfix = RandomStringUtils.randomAlphabetic(3);
		String name = "networkACL" + postfix;

		NetworkACL networkACL = new NetworkACL();
		networkACL.setName(name);
		networkACL.setEntitlementType(EntitlementType.PUBLIC);
		
		NameEntityBase subnet = new NameEntityBase();
		subnet.setId(subnetId);
		
		networkACL.setSubnet(subnet);

		ResponseEntity<NetworkACL> responseEntity = networkACLService.create(networkACL);
		if (responseEntity.isErrors())
			logger.warn("Message from Server... {}", responseEntity.getMessages().get(0).getMessageText());

		return responseEntity.getResults();

	}
}

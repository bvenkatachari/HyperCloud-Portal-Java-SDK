package io.dchq.sdk.core.subnet;

import java.util.List;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.vpc.NetworkACL;
import com.dchq.schema.beans.one.vpc.SecurityGroup;
import com.dchq.schema.beans.one.vpc.Subnet;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.NetworkACLService;
import io.dchq.sdk.core.SecurityGroupService;
import io.dchq.sdk.core.SubnetService;

/**
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */
public class SubnetTest extends AbstractServiceTest {

	// Create Subnet
	SubnetService subnetService;
	Subnet subnet;
	Subnet subnetCreated;
	boolean success;
	
	NetworkACLService networkACLService;
	SecurityGroupService securityGroupService;

	
	public void deleteNetworkACL(){
		ResponseEntity<List<NetworkACL>> response = networkACLService.findAll();
		if (response.getResults() != null && !response.isErrors()) {
			for (NetworkACL acl : response.getResults()) {
				
				NetworkACL netowrkACL = networkACLService.findById(acl.getId()).getResults();
				
				if(netowrkACL.getSubnet().getId().equals(this.subnetCreated.getId())){
					logger.info("cleaning up Network ACL with name - "+acl.getName());
					
					ResponseEntity<?> deleteResponse = networkACLService.delete(acl.getId());
					for (Message message : deleteResponse.getMessages()) {
						logger.warn("Error Network ACL deletion: [{}] ", message.getMessageText());
					}
				}
			}
		}
	}
	
	public void deleteSecurityGroup(){
		ResponseEntity<List<SecurityGroup>> response = securityGroupService.findAll();
		if (response.getResults() != null && !response.isErrors()) {
			for (SecurityGroup sg : response.getResults()) {
				
				SecurityGroup securityGroup = securityGroupService.findById(sg.getId()).getResults();
				
				if(securityGroup.getSubnet().getId().equals(this.subnetCreated.getId())){
					logger.info("cleaning up Security Group with name - "+sg.getName());
					
					ResponseEntity<?> deleteResponse = securityGroupService.delete(sg.getId());
					for (Message message : deleteResponse.getMessages()) {
						logger.warn("Error Security Group deletion: [{}] ", message.getMessageText());
					}
				}
			}
		}
	}

}

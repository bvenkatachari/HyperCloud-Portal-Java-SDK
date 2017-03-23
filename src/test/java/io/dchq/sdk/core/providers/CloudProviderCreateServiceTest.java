/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dchq.sdk.core.providers;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.blueprint.AccountType;
import com.dchq.schema.beans.one.blueprint.RegistryAccount;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.RegistryAccountService;
import io.dchq.sdk.core.ServiceFactory;

/**
 *
 * @author Abedeen.
 * @updater Jagdeep Jain
 * @since 1.0
 */

/**
 * Cloud Providers: Create
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class CloudProviderCreateServiceTest extends AbstractServiceTest {
    
	private RegistryAccountService registryAccountService;
    private RegistryAccount registryAccount;
    private boolean success;
    private RegistryAccount registryAccountCreated;
    
    static String prefix = RandomStringUtils.randomAlphabetic(3);
    
    public CloudProviderCreateServiceTest (
    		// below fields are for Rackspace, Amazon, Digital Ocean, Google Cloud, Aliyun
    		AccountType accountType,
    		String accountName, 
    		String testUsername, // application client id on UI
    		String apiKey, // password, application client secret id on UI
    		
    		// additional fields for Microsoft Azure
    		String subscriptionId, // email
    		String TenantId, // region
    		
    		// additional field for IBM Softlayer
    		String domainName, // groupName
 
    		// additional fields for private cloud
    		String vmDestination, // hardwareId
    		String template, // imageId
    		
    		// additional field for volume provider
    		String opts,
    		Integer size,
    		
    		// advanced fields
    		/*
    		String groupName, // securityGroupId
    		String vmQuota, // vmQuota
    		String leaseTime, // 
    		Boolean freeFormEntitlement, // freeFormEntitlement
    		Boolean approvalEnforced, 
    		EntitlementType blueprintEntitlementType // blueprintEntitlementType
    		*/
    		
    		boolean success
    		) 
	{
		
		this.registryAccount = new RegistryAccount().withName(accountName).withUsername(testUsername)
				.withPassword(apiKey).withAccountType(accountType);
		this.registryAccount.setRegion(TenantId);
		this.registryAccount.setEmail(subscriptionId);
		this.registryAccount.setGroupName(domainName);
		this.registryAccount.setHardwareId(vmDestination);
		this.registryAccount.setImageId(template);
		this.registryAccount.setOpts(opts);
		this.registryAccount.setSizeLimit(size);
		this.success = success;
	}

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{

				// public clouds
				{ AccountType.RACKSPACE, "Rackspace US 2 testAccount"+prefix, "dchqinc", "apiKey", null, null, null, null,
						null, null, null, false },
				{ AccountType.AWS_EC2, "Amazon EC2 testAccount"+prefix, "dchqinc", "apiKey", null, null, null, null, null,
						null, null, false },
				{ AccountType.DIGITALOCEAN, "Digital Ociean testAccount"+prefix, "dchqinc", "apiKey", null, null, null, null,
						null, null, null, false },
				//TODO: This seems to be a bug, need to verify
				{ AccountType.GOOGLE_COMPUTE_ENGINE, "Google Cloud testAccount"+prefix, "dchqinc", "password", null, null, null, null, null, null, null, false },
				
				{ AccountType.ALICLOUD, "Biggest ALICLOUD testAccount"+prefix, "dchqinc", "password", null, null, null, null, null,
						null, null, false },
				{ AccountType.MICROSOFT_AZURE, "Microsoft Azure 1 testAccount"+prefix, "dchqinc", "password", "user@dchq.io",
						"tenantId", null, null, null, null, null, false },
				{ AccountType.SOFTLAYER, "IBM Softlayer testAccount"+prefix, "dchqinc", "password", null, null,
						"http://dchq.co.in", null, null, null, null, false },

				// private cloud
				{ AccountType.OPENSTACK, "Given Openstack testAccount"+prefix, "dchqinc", "password", null, null,
						"http://dchq.co.in", null, null, null, null, false },
				
				//{ AccountType.VSPHERE, "VMware vSphere testAccount", "dchqinc", "password", null, null,
				//		"http://dchq.co.in", null, null, null, null, false },
				
				{ AccountType.HYPER_GRID, "Hypergrid Cloud testAccount"+prefix, "dchqinc", "password", null, null,
						"http://dchq.co.in", "hardwareId", "templateId", null, null, false },
				{ AccountType.HYPER_V, "Microsoft Hyper-V testAccount"+prefix, "dchqinc", "password", null, null,
						"http://dchq.co.in", "hardwareId", "templateId", null, null, false },

				// Docker Registry
				{ AccountType.DOCKER_REGISTRY, "Docker Registry testAccount"+prefix, "dchqinc", "password", "user@dchq.io", null,
						"http://dchq.co.in", null, null, null, null, false },

				// Jenkins/Hudson
				{ AccountType.JENKINS, "Jenkins testAccount"+prefix, "dchqinc", "password", null, null, "http://dchq.co.in",
						null, null, null, null, false },

				// Credentials
				{ AccountType.CREDENTIALS, "Given Credentials testAccount"+prefix, "dchqinc", "password", null, null, null, null,
						null, null, null, false },

				// volume provider
				{ AccountType.VOLUME_PROVIDER, "Volume Provider testAccount"+prefix, "dchqinc", "password", null, null,
						"http://dchq.co.in", "hardwareId", "templateId", "opts", 10, false },

				{ AccountType.RACKSPACE, "Rackspace US 2 testAccount"+prefix, "dchqinc", "apiKey", null, null, null, null,  null, null, null, false },
				//{ AccountType.RACKSPACE, "    ", "dchqinc", "apiKey", null, null, null, null,  null, null, null, false },
				//{ AccountType.RACKSPACE, "Rackspace US 1 testAccount", "     ", "apiKey", null, null, null, null,  null, null, null, false },
				//{ AccountType.RACKSPACE, "Rackspace US 1 testAccount", "dchqinc", "     ", null, null, null, null,  null, null, null, false },
				//TODO : Failing
				{ null, "Rackspace US 1 testAccount"+prefix, "dchqinc", "apiKey", null, null, null, null,  null, null, null, true },



				{ AccountType.AWS_EC2, "Amazon EC2 1 testAccount"+prefix, "dchqinc", "apiKey", null, null, null, null, null, null, null, false},
				{ AccountType.AWS_EC2, "   ", "dchqinc", "apiKey", null, null, null, null, null, null, null, true},
				{ AccountType.AWS_EC2, "Amazon EC2 testAccount"+prefix, "   ", "apiKey", null, null, null, null, null, null, null, false},
				{ AccountType.AWS_EC2, "Amazon EC2 testAccount"+prefix, "dchqinc", "   ", null, null, null, null, null, null, null, false},
				{ null, "Amazon EC2 testAccount"+prefix, "dchqinc", "apiKey", null, null, null, null, null, null, null, true},



				{ AccountType.DIGITALOCEAN, "Digital Ocean testAccount"+prefix, "dchqinc", "apiKey", null, null, null, null, null, null, null, false },
				{ AccountType.DIGITALOCEAN, "    ", "dchqinc", "apiKey", null, null, null, null, null, null, null, true },
				{ AccountType.DIGITALOCEAN, "Digital Ocean testAccount"+prefix, "  ", "apiKey", null, null, null, null, null, null, null, false },
				{ AccountType.DIGITALOCEAN, "Duplicate Digital Ocean testAccount"+prefix, "dchqinc", "   ", null, null, null, null, null, null, null, false },
				{ null, "Digital Ocean testAccount"+prefix, "dchqinc", "apiKey", null, null, null, null, null, null, null, true },


				{ AccountType.ALICLOUD, "A ALICLOUD testAccount"+prefix, "dchqinc", "password", null, null, null, null, null,null, null, false },
				{ AccountType.ALICLOUD, "   ", "dchqinc", "password", null, null, null, null, null,null, null, true },
				{ AccountType.ALICLOUD, "B ALICLOUD testAccount"+prefix, "  ", "password", null, null, null, null, null,null, null, false },
				{ AccountType.ALICLOUD, "C ALICLOUD testAccount"+prefix, "dchqinc", "   ", null, null, null, null, null,null, null, false },
				{ null, "ALICLOUD testAccount"+prefix, "dchqinc", "password", null, null, null, null, null,null, null, true },


				{ AccountType.MICROSOFT_AZURE, "Microsoft 11 Azure testAccount"+prefix, "dchqinc", "password", "user@dchq.io",  "tenantId", null, null, null, null, null, false },
				{ AccountType.MICROSOFT_AZURE, "     ", "dchqinc", "password", "user@dchq.io",  "tenantId", null, null, null, null, null, true },
        		// TODO Failing due to blank testUsername 
				//{ AccountType.MICROSOFT_AZURE, "Microsoft 12 Azure testAccount"+prefix, " ", "password", "user@dchq.io",  "tenantId", null, null, null, null, null, true },
				{ AccountType.MICROSOFT_AZURE, "Microsoft 13 Azure testAccount"+prefix, "dchqinc", "   ", "user@dchq.io",  "tenantId", null, null, null, null, null, false},
				
				// TODO Failing due to blank subscription data
				//{ AccountType.MICROSOFT_AZURE, "Microsoft Azure testAccount"+prefix, "dchqinc", "password", "  ",  "tenantId", null, null, null, null, null, true },
				{ AccountType.MICROSOFT_AZURE, "Microsoft Azure testAccount", "dchqinc", "password", "user@dchq.io",  "    ", null, null, null, null, null, true },
				{ null, "Microsoft Azure testAccount"+prefix, "dchqinc", "password", "user@dchq.io",  "tenantId", null, null, null, null, null, true },


				{ AccountType.SOFTLAYER, "IBM Softlayer testAccount"+prefix, "dchqinc", "password", null, null,  "http://dchq.co.in", null, null, null, null, false },
				{ AccountType.SOFTLAYER, "    ", "dchqinc", "password", null, null,  "http://dchq.co.in", null, null, null, null, true },
				
				// TODO Failing due to blank testUsername 
				//{ AccountType.SOFTLAYER, "IBM Softlayer testAccount"+prefix, "   ", "password", null, null,  "http://dchq.co.in", null, null, null, null, true },
				// TODO Failing due to blank testUsername
				//{ AccountType.SOFTLAYER, "IBM Softlayer testAccount"+prefix, "dchqinc", "   ", null, null,  "http://dchq.co.in", null, null, null, null, true },
				// TODO Failing due to blank url
				//{ AccountType.SOFTLAYER, "IBM Softlayer testAccount"+prefix, "dchqinc", "password", null, null,  "   ", null, null, null, null, true },
				{ null, "IBM Softlayer testAccount", "dchqinc", "password", null, null,  "http://dchq.co.in", null, null, null, null, true },


		});
    }
    
    @Before
    public void setUp() throws Exception {
        registryAccountService = ServiceFactory.buildRegistryAccountService(rootUrl, username, password);
    }

    @Test
    public void testCreate() throws Exception {
		boolean tempSuccess = success;
		ResponseEntity<RegistryAccount> response = registryAccountService.create(registryAccount);
		if (!tempSuccess) {
			logger.info("Create Registry Account with Name [{}]", registryAccount.getName());
			if (success) {
				logger.info("Expecting Error while Create Registry Account with Name [{}]", registryAccount.getName());
			}
			if (response.isErrors()) {
				logger.warn("Message from Server... {}", response.getMessages().get(0).getMessageText());
			}
			if (success && !response.isErrors()) {
				success = false;
				this.registryAccountCreated = response.getResults();
			}
			assertNotNull(response);
			assertNotNull(response.isErrors());
			this.registryAccountCreated = response.getResults();
			assertNotNull(response.getResults());
			assertNotNull(response.getResults().getId());
			logger.info(" Registry Account Created with Name [{}] and ID [{}]", registryAccountCreated.getName(), registryAccountCreated.getId());
			assertEquals(registryAccount.getAccountType(), registryAccountCreated.getAccountType());
			assertEquals(registryAccount.getName(), registryAccountCreated.getName());
			assertEquals(registryAccount.getUsername(), registryAccountCreated.getUsername());
			// password should always be empty
			assertThat("password-hidden", is(registryAccountCreated.getPassword()));
			assertEquals(registryAccount.getEmail(), registryAccountCreated.getEmail());
			assertEquals(registryAccount.getRegion(), registryAccountCreated.getRegion());
			assertEquals(registryAccount.getGroupName(), registryAccountCreated.getGroupName());
			assertEquals(registryAccount.getHardwareId(), registryAccountCreated.getHardwareId());
			assertEquals(registryAccount.getImageId(), registryAccountCreated.getImageId());
			assertEquals(registryAccount.getOpts(), registryAccountCreated.getOpts());
			assertEquals(registryAccount.getSizeLimit(), registryAccountCreated.getSizeLimit());
		}
		else
		{
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
		}
    }

    @After
    public void cleanUp() {
        if (registryAccountCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = registryAccountService.delete(registryAccountCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error user deletion: [{}] ", message.getMessageText());
			}
		}
    }
}

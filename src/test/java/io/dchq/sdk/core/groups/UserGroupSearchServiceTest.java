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
package io.dchq.sdk.core.groups;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.security.UserGroup;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.UserGroupService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * @author Intesar Mohammed
 * @updater SaurabhB
 * @since 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class UserGroupSearchServiceTest extends AbstractServiceTest {


    private UserGroupService userGroupService;
    private  String   messageText;

    @org.junit.Before
    public void setUp() throws Exception {
        userGroupService = ServiceFactory.builduserGroupService(rootUrl, cloudadminusername, cloudadminpassword);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
        	{"Sam_D",  false},
            {"Find_Group", false},
            //TODO Group name should not start with special character 
           // {"(Find-Group)", true},
            {"Find Group123", false},
            {"123", false},
            // TODO Group name should contains special characters
            //{"Find Group!@#", true},
            {"    Find Group", false},
            // TODO Group name should contains special characters
            //{"%Find Group%", true},
            {"12345678gROUP", false},
            // TODO Group name should be blank
            //{"   ", true},
           // TODO Group name should contains special characters
           // {"@Test321$_@Group$", true},
            // TODO Group name should contains special characters
           // {"  @Find   -Group_", true},
            //check with Empty Group Name
            {"", true},
            
          //TODO Group name should not be blank spaces. 
            //{"    ", false, true},
    		
            // Group Name Length 256.
            {"tQ9ukuIEBiYsSGkM1cRfES7DctIaE1W3GJ3K4WCQQxwYcNPy6NArpf2RFCEUXfmmmRkMVsvkh3TDQwWdxcyuWbbzX8xgxcfX6XwvCqVkbLE7rQ348EInhBNkIupRSvsMKaR51KFrVS7cNMi1WmJsNxWA3vEaKczJ2EHSauHx7Rs3Ln8UiEcjazU2qluzdaoQCTNBayw4VFJAAPVFHLG3wNV9OPjRUj39mNjCZBsZQJI1g2NYw6gQ1qkhqNOcWeFw", true},
        });
    }

    private UserGroup userGroup;
    private boolean errors;
    private UserGroup userGroupCreated;

    public UserGroupSearchServiceTest(String gname, boolean errors) {

        // random group Name
        if (gname == null){
            throw new IllegalArgumentException("Group Name==null");
        }

        if (!gname.isEmpty()) {
            String prefix = RandomStringUtils.randomAlphabetic(3);
            gname = prefix + gname;
            gname = org.apache.commons.lang3.StringUtils.lowerCase(gname);
        }

        this.userGroup = new UserGroup().withName(gname);
        this.errors = errors;
    }

    @Test
    public void testSearch() throws Exception {

        logger.info("Creating Group with Group Name [{}]", userGroup.getName());
        ResponseEntity<UserGroup> response = userGroupService.create(userGroup);

        if (response.isErrors()) {
            logger.warn("Message from Server... {}", response.getMessages().get(0).getMessageText());
            Assert.assertEquals(response.getMessages().get(0).getMessageText() ,true, response.isErrors());
        }

        if(!this.errors)
		{

        	assertNotNull(response);
            assertNotNull(response.isErrors());
            Assert.assertNotNull(((Boolean) errors).toString(), ((Boolean) response.isErrors()).toString());
            
			if (!response.isErrors() && response.getResults() != null) {
				userGroupCreated = response.getResults();
				assertNotNull(response.getResults());
				assertNotNull(response.getResults().getId());
				Assert.assertNotNull(userGroup.getName(), userGroupCreated.getName());
			}

			// let's search for the group
			ResponseEntity<List<UserGroup>> userGroupsResponseEntity = userGroupService
					.search(userGroupCreated.getName(), 0, 1);
			assertNotNull(userGroupsResponseEntity);
			assertNotNull(userGroupsResponseEntity.isErrors());

			for (Message message : userGroupsResponseEntity.getMessages()) {
				logger.warn("Error while Create request  [{}] ", message.getMessageText());
				messageText += message.getMessageText() + "\n";
			}

			assertFalse("Test : " + messageText, userGroupsResponseEntity.isErrors());
			assertNotNull(userGroupsResponseEntity.getResults());
			System.out.println("Page Size : " + userGroupsResponseEntity.getResults().size());
			assertEquals(1, userGroupsResponseEntity.getResults().size());

			UserGroup searchedEntity = userGroupsResponseEntity.getResults().get(0);
			assertEquals(userGroupCreated.getId(), searchedEntity.getId());
			assertEquals(userGroupCreated.getName(), searchedEntity.getName());
		} 
        else
        {
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
        }
    }

    @After
    public void cleanUp() {
        logger.info("cleaning up...");

        if (userGroupCreated != null) {
            ResponseEntity<UserGroup> deleteResponse  =  userGroupService.delete(userGroupCreated.getId());
            if (deleteResponse.getResults() != null)
           //     userGroupDeleted = deleteResponse.getResults();
            for (Message m : deleteResponse.getMessages()){
                logger.warn("[{}]", m.getMessageText());
                messageText = m.getMessageText();}
           // Assert.assertFalse(messageText ,deleteResponse.isErrors());
            //        Assert.assertEquals(messageText ,error, deleteResponse.isErrors());
        }
        }
}





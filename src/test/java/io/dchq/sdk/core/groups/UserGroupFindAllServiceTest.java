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

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.security.UserGroup;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.UserGroupService;

/**
 * 
 *
 * @author Santosh Kumar
 * @since 1.0
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class UserGroupFindAllServiceTest extends AbstractServiceTest {

	private UserGroupService userGroupService;
	private UserGroup userGroup;
	private UserGroup userGroupCreated;
	
	private int countBeforeCreate;
	private int countAfterCreate;

	@org.junit.Before
	public void setUp() throws Exception {
		userGroupService = ServiceFactory.builduserGroupService(rootUrl1, tenant_username, tenant_password);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
			{ "GroupName", false }
		});
	}

	public UserGroupFindAllServiceTest(String groupName, boolean active) {

		String prefix = RandomStringUtils.randomAlphabetic(3);
		groupName = groupName + prefix;
		this.userGroup = new UserGroup().withName(groupName).withInactive(active);
	}
	
	public int testUserGroupPosition(String id) {
		ResponseEntity<List<UserGroup>> response = null;
		try {
			response = userGroupService.findAll(0, 5000);
			for (Message message : response.getMessages()) {
				logger.warn("Error [{}]  " + message.getMessageText());
			}
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertEquals(false, response.isErrors());
			int position = 0;
			if (id != null) {
				for (UserGroup obj : response.getResults()) {
					position++;
					if (obj.getId().equals(id)) {
						logger.info("  Object Matched in FindAll {}  at Position : {}", id, position);
						assertEquals("Recently Created Object is not at Positon 1 :" + obj.getId(), 1, position);
					}
				}
			}
			logger.info(" Total Number of Objects :{}", response.getResults().size());
		} catch (Exception e) {

		}
		if (response == null)
			if (id == null)
				return 0;
			else
				return 1;
		else
			return response.getResults().size();
	}


	@org.junit.Test
	public void testFindAll() throws Exception {
		
		this.countBeforeCreate = testUserGroupPosition(null);

		logger.info("Create Group with Group Name [{}]", userGroup.getName());
		ResponseEntity<UserGroup> response = userGroupService.create(userGroup);

		for (Message m : response.getMessages()) {
			logger.warn("[{}]", m.getMessageText());
		}
		
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertNotNull(response.getResults().getId());

		if (response.getResults() != null)
			userGroupCreated = response.getResults();

		Assert.assertEquals("Group Name does not match input value", userGroup.getName(), userGroupCreated.getName());
		Assert.assertEquals("User group Active/Inactive status does not match input Value", userGroup.getInactive(),
				userGroupCreated.getInactive());
		
		this.countAfterCreate = testUserGroupPosition(userGroupCreated.getId());
		
		assertEquals(
				"Count of Find all Users Group between before and after create does not have diffrence of 1 for UserGroupId :"
						+ userGroupCreated.getId(),
				countBeforeCreate + 1, countAfterCreate);

	}

	@After
	public void cleanUp() {
		logger.info("cleaning up user group...");

		if (userGroupCreated != null) {
			ResponseEntity<UserGroup> deleteResponse = userGroupService.delete(userGroupCreated.getId());
			for (Message m : deleteResponse.getMessages()) {
				logger.warn("[{}]", m.getMessageText());
			}
		}
	}
}

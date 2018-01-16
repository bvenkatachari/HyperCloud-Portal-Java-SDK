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

package io.dchq.sdk.core.users;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import com.dchq.schema.beans.one.security.Users;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.UserService;

/**
 * 
 *
 * @author Santosh Kumar.
 * @since 1.0
 *
 */

/**
 * Users: Create
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class UsersUpdateServiceTest extends AbstractServiceTest {

	private UserService service;
	private Users user;
	private Users userCreated;

	public UsersUpdateServiceTest(String firstName, String lastName, String userName, String password, String email,
			List<String> authorities) {

		String prefix = RandomStringUtils.randomAlphabetic(3);
		userName = userName + prefix;

		user = new Users();
		user.setFirstname(firstName);
		user.setLastname(lastName);
		user.setUsername(userName);
		user.setPassword(password);
		user.setEmail(email);
		user.setAuthorities(authorities);
		user.setEnabled(true);
		user.setInactive(false);

	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() throws Exception {

		List<String> authorities = new ArrayList<String>();
		authorities.add("ROLE_USER");

		return Arrays.asList(new Object[][] {
				{ "FirstName", "LastName", "UserName", "password", "user12@hypergrid.com", authorities } });
	}

	@Before
	public void setUp() throws Exception {
		service = ServiceFactory.buildUserService(rootUrl, tenant_username, tenant_password);
	}

	@Test
	public void testCreate() {
		logger.info("Create user fn [{}] ln [{}] username [{}]", user.getFirstname(), user.getLastname(),
				user.getUsername());

		ResponseEntity<Users> response = service.create(user);
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
		if (response.getResults() != null) {
			this.userCreated = response.getResults();
			logger.info("Create user Successful..");
		}

		assertNotNull(response);
		assertNotNull(response.isErrors());

		assertNotNull(response.getResults());
		assertNotNull(response.getResults().getId());

		String modifiedUserName = userCreated.getUsername() + "_Update";
		String modifiedFirstName = userCreated.getFirstname() + "_Update";
		// updating name
		userCreated.setUsername(modifiedUserName);
		userCreated.setFirstname(modifiedFirstName);
		logger.info("Update User with username [{}]", userCreated.getUsername());
		response = service.update(userCreated);
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}] ", message.getMessageText());
		}
		if (!response.isErrors() && response.getResults() != null) {
			assertEquals(userCreated.getFirstname(), response.getResults().getFirstname());
			assertEquals(userCreated.getUsername(), response.getResults().getUsername());
		}
	}

	@After
	public void cleanUp() {
		if (userCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = service.delete(userCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error user deletion: [{}] ", message.getMessageText());
			}
		}
	}

}
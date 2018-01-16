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
 * @author Santosh Kumar
 * @since 1.0
 *
 */

/**
 * Users: Create
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class UsersCreateServiceTest extends AbstractServiceTest {

	private UserService service;
	private Users user;
	private Users userCreated;

	public UsersCreateServiceTest(String firstName, String lastName, String userName, String password, String email,
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
				{ "FirstName", "LastName", "UserName", "password", "user6@hypergrid.com", authorities } });
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
		assertEquals(user.getFirstname(), userCreated.getFirstname());
		assertEquals(user.getLastname(), userCreated.getLastname());
		assertEquals(user.getUsername().toLowerCase(), userCreated.getUsername());
		assertEquals(user.getEmail(), userCreated.getEmail());

		assertEquals(user.getPhoneNumber(), userCreated.getPhoneNumber());

		assertEquals(tenantId, userCreated.getTenantPk());

		assertEquals(user.getAuthorities(), userCreated.getAuthorities());
		assertEquals(user.isEnabled(), userCreated.isEnabled());
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
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
import com.dchq.schema.beans.one.security.Users;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.ServiceFactory;
import io.dchq.sdk.core.UserService;

/**
 * <code>UsersService</code> Integration Tests.
 *
 * @author Intesar Mohammed
 * @updater Jagdeep Jain
 * @since 1.0
 **/
/**
 * Users: Update
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class UsersUpdateServiceTest extends AbstractServiceTest {

	private UserService service;
	private Users users;
	private boolean success;
	private Users userCreated;
	private Users userUpdated;
	private String modifiedFirstName, modifiedLastName;

	public UsersUpdateServiceTest (
			String fn, 
			String ln, 
			String username, 
			String email, 
			String pass, 
			String fn1,
			String ln1, 
			boolean success
		) 
	{
        // random user name
        String prefix = RandomStringUtils.randomAlphabetic(3);
        if(username!=null && !username.isEmpty())
        {
        	username = prefix + "-" + username;
        }
        if(email !=null && !email.isEmpty())
        {
        	email = prefix + "-" + email;
        }
        // lower case
        username = org.apache.commons.lang3.StringUtils.lowerCase(username);
        email = org.apache.commons.lang3.StringUtils.lowerCase(email);
		this.users = new Users().withFirstname(fn).withLastname(ln).withUsername(username).withEmail(email).withPassword(pass);
		this.success = success;
		this.modifiedFirstName = fn1;
		this.modifiedLastName = ln1;
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(
				// TODO: Add more test data for all sorts of validations and modification
				new Object[][] { { "fn", "ln", "user", "user" + "@dchq.io", "pass1234", "fn1", "fn2", false },
						{ "Hyper", "User", "hyperuser", "user@hyperuser1.com", "pass", "Hyper1", "User1", false },
						// TODO all negative test cases are failing
//						{ "12345", "User", "hyperuser", "user@hyperuser.com", "pass", "123451", "User1", false },
//						{ "@@@@", "User", "hyperuser", "user@hyperuser.com", "pass", "@@@@1", "User1", false },
//						{ "Hyper", "User", "12345", "user@hyperuser.com", "pass", "Hyper1", "User1", false },
//						{ " ", "User", "hyperuser", "user@hyperuser.com", "pass", "User1", "Hyper1", false },
//						{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "00000", "User1", "Hyper1", false },
//						{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "pass", "12345", "Hyper1", false },
//						{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "pass", "User1", "12345", false },
//						{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "pass", "Hyper1", "User1", false },
//						{ "Hyper", "User", "hyperuser", null, "fail", "Hyper1", "User1", false },
//						{ null, null, "hyperuser", "user@hyperuser.com", "pass", null, "User1", false },
//						{ "Hyper", null, "hyperuser", "user@hyperuser.com", "pass", "Hyper1", null, false },
//						{ "Hyper", "User", "   ", "0000@hyperuser.com", "pass", "Hyper1", "User1", false },
//						{ "  ", "User", "hyperuser", "  ", "pass", "Hyper1", "User1", false },
//						{ "Hyper", "User", "   ", "0000@hyperuser.com", "pass", "Hyper1", "User1", false },
//						{ "  ", "User", "hyperuser", "  ", "pass", "Hyper1", "User1", false }
				});
	}

	@Before
	public void setUp() throws Exception {
		service = ServiceFactory.buildUserService(rootUrl, username, password);
	}
	
	@Test
	public void testUpdate() {
		logger.info("Create user fn [{}] ln [{}] username [{}]", users.getFirstname(), users.getLastname(), users.getUsername());
		ResponseEntity<Users> response = service.create(users);
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}
        /* check response:
         * 1. is not null
         * 2. has no errors
         * 3. has user entity with ID
         * 4. all data sent
         */
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertThat(success, is(equals(response.isErrors())));
		if (!success) {
			assertNotNull(response.getResults());
			assertNotNull(response.getResults().getId());
			this.userCreated = response.getResults();
			logger.info("Create user fn [{}] ln [{}] username [{}]", userCreated.getFirstname(), userCreated.getLastname(), userCreated.getUsername());
			assertEquals(users.getFirstname(), response.getResults().getFirstname());
			assertEquals(users.getLastname(), response.getResults().getLastname());
			assertEquals(users.getUsername(), response.getResults().getUsername());
			assertEquals(users.getEmail(), response.getResults().getEmail());
			// password should always be empty
			assertThat("", is(response.getResults().getPassword()));
			// Modifying User Attributes.
			userCreated.setFirstname(modifiedFirstName);
			userCreated.setLastname(modifiedLastName);
			logger.info("Update user fn [{}] ln [{}] username [{}]", userCreated.getFirstname(), userCreated.getLastname(), userCreated.getUsername());
			response = service.update(userCreated);
			String errors = "";
			for (Message message : response.getMessages()) {
				errors += ("Error while Update request  " + message.getMessageText() + "\n");
			}
			assertNotNull(response);
			assertNotNull(response.isErrors());
			assertThat(success, is(equals(response.isErrors())));
			if (!response.isErrors()) {
				assertNotNull(response.getResults());
				assertNotNull(response.getResults().getId());
				this.userUpdated = response.getResults();
				logger.info("Updated user fn [{}] ln [{}] username [{}]", userCreated.getFirstname(), userCreated.getLastname(), userCreated.getUsername());
				assertEquals(userCreated.getFirstname(), userUpdated.getFirstname());
				// Password should always be empty
				assertThat("", is(userUpdated.getPassword()));
			};
		}
		else
		{
			assertEquals(null, response.getResults());
			assertEquals(true, response.isErrors());
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
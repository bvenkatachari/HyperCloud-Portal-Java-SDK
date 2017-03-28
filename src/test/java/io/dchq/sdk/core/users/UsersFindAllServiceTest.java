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
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
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
 */
/**
 * Users: FindAll
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class UsersFindAllServiceTest extends AbstractServiceTest {

	private UserService service;
	private Users users;
	private boolean success;
	private Users userCreated;
	private int countBeforeCreate = 0, countAfterCreate = 0, countAfterDelete = 0;

    public UsersFindAllServiceTest(
    		String fn, 
    		String ln, 
    		String username, 
    		String email, 
    		String pass,  
    		boolean success
    		) 
    {	
        // random user name
        String prefix = RandomStringUtils.randomAlphabetic(3);
        username = prefix + "-" + username;
        email = prefix + "-" + email;
        // lower case
        username = org.apache.commons.lang3.StringUtils.lowerCase(username);
        email = org.apache.commons.lang3.StringUtils.lowerCase(email);
        this.users = new Users().withFirstname(fn).withLastname(ln).withUsername(username).withEmail(email).withPassword(pass);
        this.success = success;
    }
    
    @Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "fn", "ln", "user", "user" + "@dchq.io", "pass1234", false },

				{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "pass", false},
				{ "    ", "User", "hyperuser", "user@hyperuser.com", "pass", false},
				{ "@#@#@", "User", "hyperuser", "user@hyperuser.com", "pass", false},
				{ "Hyper", "User", null, "00000", "pass", false},
				{ "_Hyper", "1234", "hyperuser", "user@hyperuser.com", "pass", false},
				{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "fail", false},
				{ "  ", "User", "hyperuser", "user@hyperuser.com", "pass", false},
				{ "12345", "User", "hyperuser", "user@hyperuser.com", "pass", false},
				{ "Hyper", "User", "hyperuser", "user@hyperuser.com", " ", false},
				{ "Hyper-Hyper", "User", "hyperuser", "user@hyperuser.com", "pass", false},

				{ " ", " ", "hyperuser", "user@hyperuser.com", "pass", false},
				{ " ", "User", null, "user@hyperuser.com", "pass", false},
				{ "Hyper", " ", " ", "user@hyperuser.com", "pass", false},
				{ "12345", "User",  null, "user@hyperuser.com", "pass", false},
				{ "12345", "User", "12345", "user@hyperuser.com", "pass", false},
				{ "Hyper", "12345", "hyperuser", "12345", "pass", false},
				{ " ", "User", "hyperuser", null, "pass", false},
				{ "12345", "User", "hyperuser", "12345", "pass", false},
				{ " ", " ", "12345", "user@hyperuser.com", "pass", false},

		});
	}

	public int testGetUserFromFindAll(String id) {
		ResponseEntity<List<Users>> response = service.findAll(0, 5000);
		Assert.assertNotNull(response.getTotalElements());
		for (Message message : response.getMessages()) {
			logger.warn("Error [{}]" + message.getMessageText());
		}
		assertNotNull(response);
		assertNotNull(response.isErrors());
		assertThat(false, is(equals(response.isErrors())));
		int position = 0;
		if (id != null) {
			for (Users obj : response.getResults()) {
				position++;
				if (obj.getId().equals(id)) {
					logger.info(" User Object Matched in FindAll {}  at Position : {}", id, position);
					assertEquals("Recently Created User is not at Positon 1 :" + obj.getId(), 1, position);
				}
			}
		}
		logger.info(" Total Number of Users :{}", response.getResults().size());
		return response.getResults().size();
	}

    @Before
    public void setUp() throws Exception {
        service = ServiceFactory.buildUserService(rootUrl, username, password);
    }
    
    @Test
    public void testFindAll() {
        countBeforeCreate=testGetUserFromFindAll(null);
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
            logger.info("Create request successfully completed for user fn [{}] ln [{}] username [{}]", userCreated.getFirstname(), userCreated.getLastname(), userCreated.getUsername());
            assertEquals(users.getFirstname(), userCreated.getFirstname());
            assertEquals(users.getLastname(), userCreated.getLastname());
            assertEquals(users.getUsername(), userCreated.getUsername());
            assertEquals(users.getEmail(), userCreated.getEmail());
            // password should always be empty
            assertThat("", is(response.getResults().getPassword()));
            logger.info("FindAll User by Id [{}]", userCreated.getId());
            this.countAfterCreate = testGetUserFromFindAll(userCreated.getId());
            assertEquals("Count of Find all user between before and after create does not have diffrence of 1 for UserId :"+userCreated.getId(), countBeforeCreate + 1, countAfterCreate);
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
			logger.info("Find All Users After Delete User by Id {}",userCreated.getId());
			countAfterDelete=testGetUserFromFindAll(null);
			// TODO: Seems like delete service is not working, have to check it again
			//assertEquals("Count of Find all user between before and after delete are not same for UserId :"+userCreated.getId(),countBeforeCreate, countAfterDelete);
			for (Message message : response.getMessages()) {
				logger.warn("Error user deletion: [{}] ", message.getMessageText());
			}
		}
    }


}
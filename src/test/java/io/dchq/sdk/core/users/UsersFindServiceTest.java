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
import org.junit.*;
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
/*
 * Users: Find
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class UsersFindServiceTest extends AbstractServiceTest {

    private UserService service;
    private Users users;
    private boolean success;
    private Users userCreated;
    private Users userFind;

    public UsersFindServiceTest(
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
        if(username!=null && !username.isEmpty())
        {
        	username = prefix + username;
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
    }
    
	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(
				new Object[][] { { "fn", "ln", "user", "user" + "@dchq.io", "pass1234", false },
                        { "Hyper", "User", "hyperuser", "user@hyperuser.com", "pass", false},
                        
                        // TODO first name should not contains blank space 
                        //{ "    ", "User", "hyperuser", "user@hyperuser.com", "pass", true},
                        // TODO first name should not contains special characters 
                        //{ "@#@#@", "User", "hyperuser", "user@hyperuser.com", "pass", true},
                        // TODO username should not be null 
                        //{ "Hyper", "User", null, "00000", "pass", true},
                        // TODO first name should not contains special characters 
                        //{ "_Hyper", "1234", "hyperuser", "user@hyperuser.com", "pass", true},
                        // TODO first name should not contains numeric values
                        //{ "12345", "User", "hyperuser", "user@hyperuser.com", "pass", true},
                        // TODO password should not be blank 
                        //{ "Hyper", "User", "hyperuser", "user@hyperuser.com", " ", true},
                        // TODO first name should not contains special characters 
                        //{ "Hyper-Hyper", "User", "hyperuser", "user@hyperuser.com", "pass", true},
                        // TODO null values not accepted for last name, username and emailid 
                        //{ "Hyper", null, null, null, "pass", true},
                        // TODO last name should not be null
                        //{ "Hyper", null, "1234", "user@hyperuser.com", "pass", true},
                        //TODO email id should not be blank
                        //{ "Hyper", "User", "hyperuser", "   ", "pass", true},
                        //{ "Hyper", "User", "hyperuser", null, "pass", true},
                        //{ "Hyper", "User", null, "user@hyperuser.com", "pass", true},
                        //{ "12345", "12345", "hyperuser", "user@hyperuser.com", "pass", true},
                        //{ null, null, "hyperuser", "user@hyperuser.com", "pass", true},
                        //{ null, null, null, "user@hyperuser.com", "pass", true},
                        //{ "Hyper", "  ", " ", "user@hyperuser.com", "pass", true},
                        //{ "Hyper", "User", " ", "  ", "pass", true},
                        //{ " ", "User", "hyperuser", " ", "pass", true},
                        //{ " ", "User", "12345", "user@hyperuser.com", "pass", true},
                        //{ null, null, "hyperuser", "12345", "pass", true},
                        //{ "Hyper", null, "hyperuser", null, "pass", true},
                        //{ "Hyper", "User", "12345", "12345", "pass", true},

                });
	}

    @Before
    public void setUp() throws Exception {
        service = ServiceFactory.buildUserService(rootUrl, username, password);
    }

    @Test
    public void testFind() {
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
        // TODO: create user test assertions required?
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
            logger.info("Find User by Id [{}]", userCreated.getId());
            // find by #id
            response = service.findById(userCreated.getId());
            for (Message message : response.getMessages()) {
                logger.info("Error [{}]", message.getMessageText());
            }
            assertNotNull(response);
            assertNotNull(response.isErrors());
            assertThat(success, is(equals(response.isErrors())));
            if (!response.isErrors()) {
                assertNotNull(response.getResults());
                assertNotNull(response.getResults().getId());
                this.userFind = response.getResults();
                logger.info("Find by ID user fn [{}] ln [{}] username [{}]", userCreated.getFirstname(), userCreated.getLastname(), userCreated.getUsername());
                assertEquals(userCreated.getFirstname(), userFind.getFirstname());
                // password should always be empty
                assertThat("", is(userFind.getPassword()));
            }
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
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
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.security.Organization;
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
 * Users: Create
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class UsersCreateServiceTest extends AbstractServiceTest {

    private UserService service;
    private Users users;
    private boolean success;
    private Users userCreated;
    private String errorMessage;

    public UsersCreateServiceTest(
            String fn,
            String ln,
            String username,
            String email,
            String company,
            String title,
            String phoneNumber,
            String tenant,
            boolean inactive,
            List<String> authorities,
            String pass,
            Boolean isActive,
            String message,
            boolean success
    ) {
        // random user name
        String prefix = RandomStringUtils.randomAlphabetic(3);
        username = prefix + "-" + username;
        email = prefix + "-" + email;
        // lower case
        username = org.apache.commons.lang3.StringUtils.lowerCase(username);
        email = org.apache.commons.lang3.StringUtils.lowerCase(email);
        this.users = new Users().withFirstname(fn).withLastname(ln).withUsername(username).withEmail(email).withPassword(pass);
        this.users.setCompany(company);
        this.users.setJobTitle(title);
        this.users.setPhoneNumber(phoneNumber);
        this.users.setTenantPk(tenant);
        this.users.setInactive(false);
        this.users.setAuthorities(authorities);
        this.users.setInactive(isActive);
        this.errorMessage = message;
        this.success = success;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][]{

                { "fname", "lname", "user", "user" + "@dchq.io", "ABC", "Engg", "123-1231-121", null, false, null,
						"pass1234", true, "comments", false },
				{ "Rahul", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, null,
						"pass", true, "NoComments", false },
                // TODO: This is a BUG
				//{ "  ", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, null,
						//"pass", true, "NoComments", false },
				{ "1234", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, null,
						"pass", true, "NoComments", false },
                // TODO: This is a BUG
                //{ null, "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, null,
						//"pass", true, "NoComments", false },
				{ "Rahul", "  ", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, null, "pass",
						true, "NoComments", false },
				{ "Rahul", "Khanna", "   ", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, null, "pass",
						true, "NoComments", false },
				{ "Rahul", "Khanna", "RahulKhanna", null, "BMW", "ENGG", "9848098480", null, false, null, "pass", true,
						"NoComments", false },
				//{ "Rahul", "Khanna", "RahulKhanna", "1234@123.com", "BMW", "ENGG", "9848098480", null, false, null,
				//		"pass", true, "NoComments", false },
				{ "Rahul", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "FINANCE", "asdfgh", null, false, null,
						"pass", true, "NoComments", false },
				//{ "Raj", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, null,
				//		"1234", true, "NoComments", false },
				{ "Raj", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, false, null,
						"fail", true, "  ", false },
				//{ "@@@@", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, true, null,
				//		"pass", true, "Comments", false },
				{ "@@@@", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, true, null,
						"pass", false, "Comments", false },
				{ "@@@@", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, true, null,
						"pass", true, "Comments", false },
                // TODO: This is a failing
                //{ "@@@@", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, false, null,
                //"fail", true, "Comments", true },

                { "Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "Hyper_Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "Hyper", "User", "hyperuser", "user@hyperuser.com", "     ", "QA", "ABC", null, false, null,  "pass", true, "     ", false },
                { "12345", "&&&&", "hyperuser", "user@hyperuser.com", null, "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, true, null,   "pass", true, "Comments", false },
                { "Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, true, null,  "fail", true, null, false },
                { "Hyper", "User", "hyperuser", "user@hyperuser.com", "12345", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "0000000", "9999999", null, false, null, "pass", true, "Comments", false },
                { "Hyper", null, null, null, "Hyper User", "QA", "9999999", null, false, null, "pass", true, "Comments", false },
                { "Hyper1234", "    ", "&&&&", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "  ", "User", "12345", null, "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "#####", "2020", "    ", "user@hyperuser.com", "Hyper User", "100", "XYZ", null, false, null,   "pass", true, "Comments", false },
                { "Hyper", "User", "hyperuser", "00000", null, "QA", "9999999", null, false, null,   "pass", true, "    ", false },
                { "123_123Hyper", "User-New", null, "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },

                { " ", null, "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, null, "pass", true, "Comments", false },
                { null, null, null, "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { " ", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", null, null, false, null,   "pass", true, "Comments", false },
                { "Hyper", "User", " ", " ", "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "Hyper", "  ", "hyperuser", " ", "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { " ", "User", "hyperuser", "user@hyperuser.com", " ", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { " ", "User", "hyperuser", "user@hyperuser.com", "Hyper User", " ", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "12345", "12345", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "12345", "12345", "hyperuser", "user@hyperuser.com", "12345", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },
                { "12345", "12345", "12345", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, null,   "pass", true, "Comments", false },


        });
        }

    @Before
    public void setUp() throws Exception {
        service = ServiceFactory.buildUserService(rootUrl, username, password);
    }
    
    @Test
    public void testCreate() {
        logger.info("Create user fn [{}] ln [{}] username [{}]", users.getFirstname(), users.getLastname(), users.getUsername());
        ResponseEntity<Users> response = service.create(users);
        for (Message message : response.getMessages()) {
            logger.warn("Error while Create request  [{}] ", message.getMessageText());
        }
        if (response.getResults() != null) {
            this.userCreated = response.getResults();
            logger.info("Create user Successful..");
        }
        /* check response:
         * 1. is not null
         * 2. has no errors
         * 3. has user entity with ID
         * 4. all data sent
         */
        assertNotNull(response);
        assertNotNull(response.isErrors());
        assertEquals("Expected :\n" + errorMessage, success, response.isErrors());
        if (!success) {
            assertNotNull(response.getResults());
            assertNotNull(response.getResults().getId());
            assertEquals(users.getFirstname(), userCreated.getFirstname());
            assertEquals(users.getLastname(), userCreated.getLastname());
            assertEquals(users.getUsername(), userCreated.getUsername());
            assertEquals(users.getEmail(), userCreated.getEmail());
            // password should always be empty
            assertThat("", is(response.getResults().getPassword()));
            assertEquals(users.getCompany(), userCreated.getCompany());
            assertEquals(users.getJobTitle(), userCreated.getJobTitle());
            assertEquals(users.getPhoneNumber(), userCreated.getPhoneNumber());
            if (isNullOrEmpty(users.getTenantPk())) {
                assertNotNull(userCreated.getTenantPk());
            } else {
                assertEquals(users.getTenantPk(), userCreated.getTenantPk());
            }
            assertEquals(users.getAuthorities(), userCreated.getAuthorities());
            assertEquals(users.isEnabled(), userCreated.isEnabled());
        }
    }

    @After
    public void cleanUp() {
        if (userCreated != null) {
            logger.info("cleaning up...");
//            ResponseEntity<?> response = service.delete(userCreated.getId());
//            for (Message message : response.getMessages()) {
//                logger.warn("Error user deletion: [{}] ", message.getMessageText());
//            }
        }
    }
    

    public static Organization getOrganization(String name, Boolean inActive, Boolean deleted) {
        return new Organization().withName(name).withInactive(inActive).withDeleted(deleted);
    }

}
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
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
            boolean success,
            boolean isUsernamePrefix,
            boolean isEmailIdPrefix
    ) {
        // random user name
        String prefix = RandomStringUtils.randomAlphabetic(3);
        if(isUsernamePrefix)
        {
        	username = prefix + "-" + username;
        }
        if(isEmailIdPrefix)
        {
        	email = prefix + "-" + email;
        }
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
    	
    	List<String> authorities = new ArrayList<String>();
		authorities.add("ROLE_ORG_ADMIN");
		
        return Arrays.asList(new Object[][]{

               	{ "fname", "lname", "user", "user" + "@dchq.io", "ABC", "Engg", "123-1231-121", null, false, authorities, "pass1234", true, "comments", false , true, true },

              //  { "fname", "lname", "user", "user" + "@dchq.io", "ABC", "Engg", "123-1231-121", null, false, "pass1234", true, "comments", false , true, true },
        	
			//  	{ "Rahul", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "passwqeqweq", true, "NoComments", false , true, true },
                // TODO: This is a BUG
				//{ "  ", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", true , true, true },
			  	
				//{ "1234", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", false , true, true },
                // TODO: This is a BUG
                //{ null, "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", true , true, true },
				//{ "Rahul", "  ", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", false , true, true },
				//{ "Rahul", "Khanna", "   ", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", false , false, true },
				//{ "Rahul", "Khanna", "RahulKhanna", null, "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", false , true, false },
				//{ "Rahul", "Khanna", "RahulKhanna", "1234@123.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", false , true, true },
				//{ "Rahul", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "FINANCE", "asdfgh", null, false, authorities, "pass1234", true, "NoComments", false , true, true },
				//{ "Raj", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "1234", true, "NoComments", false , true, true },
				//{ "Raj", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, false, authorities, "fail", true, "  ", false , true, true },
				//{ "@@@@", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, true, authorities,	"pass1234", true, "Comments", true , true, true },
			  	
				//TODO failing 
				//{ "@@@@", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, true, authorities,	"pass1234", false, "Comments", true , true, true },
				//{ "@@@@", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, true, authorities,	"pass1234", true, "Comments", true , true, true },
                // TODO: This is a failing
                //{ "@@@@", "Malhotra", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "@@@@@@@", null, false, authorities, "fail", true, "Comments", true , true, true },
                //{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", false , true, true },
                //{ "Hyper_Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", false , true, true },
                //{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "     ", "QA", "ABC", null, false, authorities,  "pass1234", true, "     ", false , true, true },
                //{ "12345", "&&&&", "hyperuser", "user@hyperuser.com", null, "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", false , true, true },
                //{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, true, authorities,   "pass1234", true, "Comments", false , true, true },
                //{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, true, authorities,  "fail", true, null, false , true, true },
                //{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "12345", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", false , true, true },
                //{ "Hyper", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "0000000", "9999999", null, false, authorities, "pass1234", true, "Comments", false , true, true },
                //{ "Hyper", null, null, null, "Hyper User", "QA", "9999999", null, false, authorities, "pass1234", true, "Comments", false , false, false },
                //{ "Hyper1234", "    ", "&&&&", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", false , false, true },
                // TODO: This is a BUG 
                //{ "  ", "User", "12345", null, "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", true , true, true },
			  	// TODO: This is a BUG
			  	// { "#####", "2020", "    ", "user@hyperuser.com", "Hyper User", "100", "XYZ", null, false, authorities,   "pass1234", true, "Comments", true , true, true },
                //{ "Hyper", "User", "hyperuser", "00000", null, "QA", "9999999", null, false, authorities,   "pass1234", true, "    ", false , true, true },
                //{ "123_123Hyper", "User-New", null, "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", false , false, true },
			  	// TODO: This is a BUG
                //{ " ", null, "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, authorities, "pass1234", true, "Comments", true , true, true },
			  	// TODO: This is a BUG
                //{ null, null, null, "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", true , false, true },
			  	// TODO: This is a BUG
                //{ " ", "User", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", null, null, false, authorities,   "pass1234", true, "Comments", true , true, true },
                //{ "Hyper", "User", " ", " ", "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", true , false, false },
                //{ "Hyper", "  ", "hyperuser", " ", "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", true , true, true },
			  	// TODO: This is a BUG
                //{ " ", "User", "hyperuser", "user@hyperuser.com", " ", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", true , true, true },
			  	// TODO: This is a BUG
                //{ " ", "User", "hyperuser", "user@hyperuser.com", "Hyper User", " ", "9999999", null, false, authorities,   "pass1234", true, "Comments", true , true, true },
                //{ "12345", "12345", "hyperuser", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", false , true, true },
                //{ "12345", "12345", "hyperuser", "user@hyperuser.com", "12345", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", false , true, true },
                //{ "12345", "12345", "12345", "user@hyperuser.com", "Hyper User", "QA", "9999999", null, false, authorities,   "pass1234", true, "Comments", false , true, true },
				
				// TODO first name should have limit of length
				//{ "Rahulsdasdasdasdadasdasdasdadsadadasdadadasdasdasdasdadasdasdasdasdasdasdasdassadasddddddddddaaaaaassssssssssssssssssssssssssssssssssssssssssssssssssssss"
				//		+ "b", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", true , true, true },
        	    // TODO last name should have limit of length
				//{ "test ", "Rahulsdasdasdasdadasdasdasdadsadadasdadadasdasdasdasdadasdasdasdasdasdasdasdassadasddddddddddaaaaaassssssssssssssssssssssssssssssssssssssssssssssssssssss"
					//		, "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", true , true, true },
        	     // TODO username should have limit of length
				//{ "test ", "Khanna", "Rahulsdasdasdasdadasdasdasdadsadadasdadadasdasdasdasdadasdasdasdasdasdasdasdassadasddddddddddaaaaaassssssssssssssssssssssssssssssssssssssssssssssssssssss"
				//		+ "", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234", true, "NoComments", true , true, true },
				
				//TODO duplicate username testing
				//{ "Rahul", "Khanna", "Rahul", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass1234567", true, "NoComments", true , false, true },
				
				// duplicate emailId testing
				// -----{ "Rahul", "Khanna", "RahulKhanna", "rahul@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "pass212345", true, "NoComments", true , true, false },
				
				// TODO password length should not be less then 8 
				//{ "Rahul", "Khanna", "RahulKhanna", "rahulddd@bmw.com", "BMW", "ENGG", "9848098480", null, false, authorities, "passwww222", true, "NoComments", true , true, true }
				
			  	// TODO Authorities(Roles) is mandatory field, it should not be null 
				//{ "Rahul", "Khanna", "RahulKhanna", "rahulkhanna21@bmw.com", "BMW", "ENGG", "9848098480", null, false, null, "pass123456", true, "NoComments", true , true, true },
				
				// TODO Company name should not allow only special characters
				//{ "Rahul", "Khanna", "RahulKhanna", "rahulkhanna21@bmw.com", "@@@@@", "ENGG", "9848098480", null, false, authorities, "pass123456", true, "NoComments", true , true, true },
				//{ "Rahul", "Khanna", "RahulKhanna", "rahulkhanna21@bmw.com", "---+++++==", "ENGG", "9848098480", null, false, authorities, "pass123456", true, "NoComments", true , true, true },
				
				// TODO Title name should not be allow only special characters
				//{ "Rahul", "Khanna", "RahulKhanna", "rahulkhanna21@bmw.com", "maruti", "++++444444444", "9848098480", null, false, authorities, "pass123456", true, "NoComments", true , true, true },
				
				// TODO phone number should have valid length
				//{ "Rahul", "Khanna", "RahulKhanna", "rahulkhanna21@bmw.com", "maruti", "ENGG", "984", null, false, authorities, "pass123456", true, "NoComments", true , true, true },
				// TODO Phone number should accept only numeric values
				//{ "Rahul", "Khanna", "RahulKhanna", "rahulkhanna21@bmw.com", "maruti", "ENGG", "@@@@@@", null, false, authorities, "pass123456", true, "NoComments", true , true, true },

        });
        }

    @Before
    public void setUp() throws Exception {
        service = ServiceFactory.buildUserService(rootUrl, cloudadminusername, cloudadminpassword);
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
        
        if (!success) {
        	assertNotNull(response);
            assertNotNull(response.isErrors());
            assertEquals("Expected :\n" + errorMessage, success, response.isErrors());
            
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
    

    public static Organization getOrganization(String name, Boolean inActive, Boolean deleted) {
        return new Organization().withName(name).withInactive(inActive).withDeleted(deleted);
    }

}
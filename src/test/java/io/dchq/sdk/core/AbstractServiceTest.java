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

package io.dchq.sdk.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstracts class for holding test credentials.
 *
 * @author Intesar Mohammed
 * @Updater Saurabh B.
 * @since 1.0
 */
public abstract class AbstractServiceTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

   protected String cloudadminusername = "zYUANpeAE0ByMsNKu6oj";   //access key of admin
   protected String cloudadminpassword = "xYLiIJNwI457n4azKteMqsjyUNYj3wNfKOixnr2m"; //secret-key for admin

   protected String rootUrl = "https://172.16.1.30:443/api/1.0/";
    protected String rootUrl1 = "https://172.16.1.30:443/api/";
  // protected String rootUrl = "https://hcdevtest.skygrid.cloud:443/api/1.0/";
  protected String userId = "2c9180865d312fc4015d314702ce005b";
    protected String username = "YOBRfhNQzhsY4jBMO4ox";   // access-key for user1 <QEAutomation1@dchq.io>
    protected String password = "I61rFLyQe1DQzdUeBSNn3e57iMxYl1DSKUowL20i";  // secret-key for user1

    
    //Tenant Details
    //Login password for all users are "password"
    protected static String tenantId = "2c91808760d4843b0160d4965e3800bb"; //<Tenant QAAutomation>
    protected String tenant_username = "KDDHWtdnTbfYXZKeNfXi";// accesskey 
    protected String tenant_password = "zBAcdu3h7MHNjRR3or9dnTkwcnyLBN8Xl9uNzcvp";//secret key
    
    //Quota associated with Tenant QAAutomation
    protected static String quotaName = "Quota_QA_Automation";
    protected static String quotaId = "2c91808760d4843b0160df48adea52d0";
    
    // Create user  for entitlement check inside Tenant QAAutomation
    protected static String userId1 = "2c91808760d4843b0160df510a305310";
    protected String username1 = "pWdVF9AOQ7tcEIqdLpsI";// accesskey <user1@hypergrid.com>
    protected String password1 = "S7fNiWsJN4SnhHEZyXXSQxBt8q7LlysEJGcFMdki";//secret key
    
    // Create another user  for entitlement check
    protected static String userId2 = "2c91808760d4843b0160df51bbef5319";
    protected String username2 = "c9JLl0fD1tasHf9fn8n7";// accesskey <user2@hypergrid.com>
    protected String password2 = "1YZeyiBKxTg0uyCbt7ENqiJoNAiY8CTi9mShxkFj";//secret key

    // UserGroup with userId1 & userId2 entitled user
    protected static String USER_GROUP = "2c91808760d4843b0160df58b1b8534f"; //QAAutomation_Group


   
    protected static String userId3 = "2c9180865d312fc4015d31492a870066";
    protected static String username3 = "qB74EOboKenqSSto9k1i";
    protected static String password3 = "46ru9wNuKvqmRCUv2WjufJZt4OO5J2CILgbnrocM";

  //  protected  static String bluePrintID = "2c9180865a4a48d9015a52b610c8080c"; //App & Machines Blueprints Test
    protected  static String clusterID = "2c918086602f7b8c01603495f64f2cc4";   //Cluster name : Sam_Docker_Network
    
    protected static String dockerServerId = "2c9180875e3673ab015e37352d37024d"; // VM-Network
    
    protected static String vpcId = "2c9180865d312fc4015d3158a5c00078";
    protected static String subnetId = "2c9180865d312fc4015d3160f3b2008c";
    protected static String vlanId = "2c9180875dc0e30d015dc1a5ff8804c7"; // 518
    
    //Test Suite data
    protected static String networkProviderId = "2c9180865d312fc4015d3134e4ab0006"; // SkyGridC02 (HNS)
    protected static String volumeProviderId = "2c9180865d312fc4015d3134e40d0004"; // SkyGridC02 (HBS)
    protected static String computeProviderId = "2c9180865d312fc4015d3160f518008e"; // vhg01cluster (HCS)
    protected static String blueprintAppId = "402881864e1a36cc014e1a399cf90102"; // 3-Tier Java (Nginx  Jetty  MySQL)

    protected int waitTime = 0, maxWaitTime = 0;

    public boolean isNullOrEmpty(Object inObj) {
        if (inObj == null) {
            return true;
        } else if (inObj.getClass().equals(String.class)) {
            String str = inObj.toString();
            if (str == null || str.isEmpty()) return true;
            else return false;
        } else if (inObj != null) return false;
        return false;
    }

    public int wait(int milliSeconds) {
        logger.info("Waiting for [{}]  seconds  ", milliSeconds / 1000);
        if (maxWaitTime <= waitTime) {
            logger.info("wait Time Exceeded the Limit [{}]  ", formatMillis(maxWaitTime));
            return 0;
        }
        try {
            Thread.sleep(milliSeconds);
        } catch (Exception e) {
            logger.warn("Error @ Wait [{}] ", e.getMessage());
        }
        waitTime += milliSeconds;
        logger.info("Time Wait during Provisioning [{}] Hours:Minutes:Seconds ", formatMillis(waitTime));
        return 1;
    }

	public String formatMillis(long val) {
		StringBuilder buf = new StringBuilder(20);
		String sgn = "";
		if (val < 0) {
			sgn = "-";
			val = Math.abs(val);
		}
		append(buf, sgn, 0, (val / 3600000));
		append(buf, ":", 2, ((val % 3600000) / 60000));
		append(buf, ":", 2, ((val % 60000) / 1000));
		// append(buf,".",3,( val %1000));
		return buf.toString();
	}

	private void append(StringBuilder tgt, String pfx, int dgt, long val) {
		tgt.append(pfx);
		if (dgt > 1) {
			int pad = (dgt - 1);
			for (long xa = val; xa > 9 && pad > 0; xa /= 10) {
				pad--;
			}
			for (int xa = 0; xa < pad; xa++) {
				tgt.append('0');
			}
		}
		tgt.append(val);
	}

	public String getDateSuffix(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_hh_mm");
		if (format != null)
			sdf = new SimpleDateFormat(format);
		String date = sdf.format(new Date());
		return date;
	}

}

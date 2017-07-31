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

   protected String cloudadminusername = "IZgKyvVXoiaFCcB0QdYh";   //access key of admin
   protected String cloudadminpassword = "xri9g67zbxxN0Qh3Kg0t860WOrWO7ooaBLxsKwOT"; //secret-key for admin

   protected String rootUrl = "https://172.16.1.30:443/api/1.0/";
    protected String rootUrl1 = "https://172.16.1.30:443/api/";
  // protected String rootUrl = "https://hcdevtest.skygrid.cloud:443/api/1.0/";
  protected String userId = "2c9180865d312fc4015d314702ce005b";
    protected String username = "YOBRfhNQzhsY4jBMO4ox";   // access-key for user1 <QEAutomation1@dchq.io>
    protected String password = "I61rFLyQe1DQzdUeBSNn3e57iMxYl1DSKUowL20i";  // secret-key for user1

    /********Intesar M/C

    protected String rootUrl = "http://73.189.41.57:9090/api/";
    protected String rootUrl1 = "https://hcdevtest.skygrid.cloud:443/api/1.0/";
    protected String userId = "2c9180865ce8bc11015ce8e7fe1e0051";
    protected String username = "30Ryl7lq6alGLxty8Qg1";   // access-key for user1 <QEAutomation1@dchq.io>
    protected String password = "KUvizs2FpC0mGdrigl83ZXhF2arJGSANVP31PnfB";  // secret-key for user1
******************/
    // Create another user  for entitlement check
    protected static String userId2 = "2c9180865d312fc4015d3147a4d4005e";
    protected String username2 = "ua7fE7TNe0groIEPhEjb";// accesskey <QEAutomation2@dchq.io>
    protected String password2 = "luQ09MXPNAERxPriA4dK9a1MRSNS4zpKFNWRItNQ";//secret key

    // UserGroup with userId2 entitled user
    protected static String USER_GROUP = "2c9180865d312fc4015d314845140061";


    // new tenant user 
    // login: qeautomationtenant@hypercloud.local
    // password: 7DjBpB6xBIK!
    protected static String userId3 = "2c9180865d312fc4015d31492a870066";
    protected static String username3 = "qB74EOboKenqSSto9k1i";
    protected static String password3 = "46ru9wNuKvqmRCUv2WjufJZt4OO5J2CILgbnrocM";

  //  protected  static String bluePrintID = "2c9180865a4a48d9015a52b610c8080c"; //App & Machines Blueprints Test
    protected  static String clusterID = "2c9180865d312fc4015d314b5d510069";   //Cluster name : Sam_Automation_Cluster
    
    protected static String dockerServerId = "2c9180865a6421f0015a646c20fe0685"; // qe-100
    
    protected static String vpcId = "2c9180865d312fc4015d3158a5c00078";
    protected static String subnetId = "2c9180865d312fc4015d3160f3b2008c";
    protected static String vlanId = "2c9180865d312fc4015d3158c366007b"; // VLAN-ID 505
    
    //Test Suite data
    protected static String networkProviderId = "2c9180865d312fc4015d3134e4ab0006"; // SkyGridC02 (HNS)
    protected static String volumeProviderId = "2c9180865d312fc4015d3134e40d0004"; // SkyGridC02 (HBS)
    protected static String computeProviderId = "2c9180865d312fc4015d3134e26d0002"; // SkyGridC02 (HCS)
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

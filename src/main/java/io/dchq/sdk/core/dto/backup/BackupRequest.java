package io.dchq.sdk.core.dto.backup;


/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

public class BackupRequest {
	
	String jobName;
    String vmName;
    
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getVmName() {
		return vmName;
	}
	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

}

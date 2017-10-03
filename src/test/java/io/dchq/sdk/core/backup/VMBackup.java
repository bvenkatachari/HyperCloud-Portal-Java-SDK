package io.dchq.sdk.core.backup;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

public class VMBackup {

	protected String vmName;
    protected String jobName;
    
    
	public String getVmName() {
		return vmName;
	}
	public void setVmName(String vmName) {
		this.vmName = vmName;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
    
}

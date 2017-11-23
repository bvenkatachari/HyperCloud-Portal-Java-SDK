package io.dchq.sdk.core.dto.backup;


/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

public class CreateBackupJob {

	private String jobName;
    private String jobDesc;
    private String vmName;
    private String repoName;
    private String freq;
    private String time;
    private String fullBackup;
    private String retainBackups;
    
    
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobDesc() {
		return jobDesc;
	}
	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}
	public String getVmName() {
		return vmName;
	}
	public void setVmName(String vmName) {
		this.vmName = vmName;
	}
	public String getRepoName() {
		return repoName;
	}
	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}
	public String getFreq() {
		return freq;
	}
	public void setFreq(String freq) {
		this.freq = freq;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getFullBackup() {
		return fullBackup;
	}
	public void setFullBackup(String fullBackup) {
		this.fullBackup = fullBackup;
	}
	public String getRetainBackups() {
		return retainBackups;
	}
	public void setRetainBackups(String retainBackups) {
		this.retainBackups = retainBackups;
	}
    
}

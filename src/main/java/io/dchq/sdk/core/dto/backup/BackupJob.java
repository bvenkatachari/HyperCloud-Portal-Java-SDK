package io.dchq.sdk.core.dto.backup;


/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

public class BackupJob {

	private String name;
    private String type;
    private String platform;
    private String description;
    
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    
}

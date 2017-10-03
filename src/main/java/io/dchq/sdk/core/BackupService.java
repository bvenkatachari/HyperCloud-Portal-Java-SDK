package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

public interface BackupService extends GenericService<Object, ResponseEntity<List<Object>>, ResponseEntity<Object>> {
	
	
	ResponseEntity<Object> createBackup(String backup);
	ResponseEntity<Object> deleteBackup(String backup);

}

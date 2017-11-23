package io.dchq.sdk.core;

import java.util.List;

import com.dchq.schema.beans.base.ResponseEntity;

import io.dchq.sdk.core.dto.backup.BackupJob;
import io.dchq.sdk.core.dto.backup.BackupRequest;
import io.dchq.sdk.core.dto.backup.CreateBackupJob;
import io.dchq.sdk.core.dto.backup.DeleteBackupJob;
import io.dchq.sdk.core.dto.backup.VMBackup;
import io.dchq.sdk.core.dto.backup.VMRestore;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

public interface BackupService extends GenericService<BackupRequest, ResponseEntity<List<Object>>, ResponseEntity<Object>> {
	
	
	ResponseEntity<Object> createBackup(BackupRequest backup);
	ResponseEntity<Object> deleteBackup(BackupRequest backup);
	ResponseEntity<List<VMBackup>> findAllBackupVMs(int page, int size);
	ResponseEntity<Object> restoreBackup(VMRestore restore);
	ResponseEntity<Object> createBackUpJob(CreateBackupJob job);
	ResponseEntity<Object> deleteBackUpJob(DeleteBackupJob job);
	ResponseEntity<List<BackupJob>> findAllBackUpJobs(int page, int size);

}

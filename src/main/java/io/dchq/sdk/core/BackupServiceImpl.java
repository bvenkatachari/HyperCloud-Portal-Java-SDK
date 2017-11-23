package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

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

public class BackupServiceImpl extends
GenericServiceImpl<BackupRequest, ResponseEntity<List<Object>>, ResponseEntity<Object>> implements BackupService {
	
	
	
	public static final String ENDPOINT = "backups/";

	/**
	 * @param baseURI
	 *            - e.g. https://dchq.io/api/1.0/
	 * @param username
	 *            - registered username with DCHQ.io
	 * @param password
	 *            - password used with the username
	 */

	public BackupServiceImpl(String baseURI, String username, String password) {

		super(baseURI, ENDPOINT, username, password, new ParameterizedTypeReference<ResponseEntity<List<Object>>>() {
		   }, new ParameterizedTypeReference<ResponseEntity<Object>>() {
		});
	}

	@Override
	public ResponseEntity<Object> createBackup(BackupRequest backup) {
		return super.doPost(backup, "job/addvm");
	}
	
	@Override
	public ResponseEntity<Object> deleteBackup(BackupRequest backup) {
		return super.doPost(backup, "job/delvm");
	}
	
	@Override
	public ResponseEntity<List<VMBackup>> findAllBackupVMs(int page, int size) {
		return super.findAll(page, size, "vm", new ParameterizedTypeReference<ResponseEntity<List<VMBackup>>>() {});
	}
	
	@Override
	public ResponseEntity<Object> restoreBackup(VMRestore restore) {
		return super.doPost(restore, "vm/restore");
	}
	
	@Override
	public ResponseEntity<Object> createBackUpJob(CreateBackupJob job) {
		return super.update(job, "job");
	}
	
	@Override
	public ResponseEntity<List<BackupJob>> findAllBackUpJobs(int page, int size) {
		return super.findAll(page, size, "job", new ParameterizedTypeReference<ResponseEntity<List<BackupJob>>>() {});
	}
	
	@Override
	public ResponseEntity<Object> deleteBackUpJob(DeleteBackupJob job) {
		return super.delete(job,"job");
	}

}

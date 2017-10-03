package io.dchq.sdk.core;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;

import com.dchq.schema.beans.base.ResponseEntity;

/**
*
* @author Santosh Kumar.
* @since 1.0
*
*/

public class BackupServiceImpl extends
GenericServiceImpl<Object, ResponseEntity<List<Object>>, ResponseEntity<Object>> implements BackupService {
	
	
	
	public static final String ENDPOINT = "backups/job/";

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
	public ResponseEntity<Object> createBackup(String backup) {
		return super.doPost(backup, "addvm");
	}
	
	@Override
	public ResponseEntity<Object> deleteBackup(String backup) {
		return super.doPost(backup, "delvm");
	}
	
	

}

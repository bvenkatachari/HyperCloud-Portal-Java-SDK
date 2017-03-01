package io.dchq.sdk.core.volumes;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.dchq.schema.beans.base.Message;
import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.base.UsernameEntityBase;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;

import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;

/**
 * @Author Saurabh Bhatia
 */


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DockerVolumeCreateServiceTest extends AbstractServiceTest{

    private DockerVolumeService dockerVolumeService;

    DockerVolume dockerVolume;
    DockerVolume dockerVolumeCreated;
    boolean error;
    String validationMessage;

    @org.junit.Before
    public void setUp() throws Exception{
        dockerVolumeService = ServiceFactory.buildDockerVolumeService(rootUrl, username, password);
    }

   public DockerVolumeCreateServiceTest (
		   String volumeName,
			String options, 
			List<UsernameEntityBase> entitledUser, 
			String provider, 
			String server) {
		this.dockerVolume = new DockerVolume();
		this.dockerVolume.setName(volumeName);
		this.dockerVolume.setOptionsText(options);
		
	}
   @Parameterized.Parameters
   public static Collection<Object[]> data() throws Exception {
	   
       return Arrays.asList(new Object[][]{
               // TODO: add more test data for all sorts of validations
				{ "testvalumn", "test options", "Local Volumne Provider" , "qe-100(DockerEngine)"}
       });
   }
   
   @Ignore
   public void testCreate()
	{
		logger.info("Create docker volumne name[{}] options [{}] EntitledUser [{}] ", dockerVolume.getName(),
				dockerVolume.getOptionsText());
		ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);
		for (Message message : response.getMessages()) {
			logger.warn("Error while Create request  [{}] ", message.getMessageText());
		}

		Assert.assertFalse(response.isErrors());

		if (response.getResults() != null && !response.isErrors()) {
			this.dockerVolumeCreated = response.getResults();
			logger.info("Create docker volumne Successful..");
		}

		assertNotNull(response);
		assertNotNull(response.isErrors());
		if (this.dockerVolumeCreated != null) {
			assertNotNull(response.getResults().getId());
			assertNotNull(dockerVolumeCreated.getId());
			assertEquals(dockerVolume.getName(), dockerVolumeCreated.getName());
			assertEquals(dockerVolume.getOptionsText(), dockerVolumeCreated.getOptionsText());
		}

	}
   @After
   public void cleanUp() {
       if (this.dockerVolumeCreated != null) {
			logger.info("cleaning up...");
			ResponseEntity<?> response = dockerVolumeService.delete(this.dockerVolumeCreated.getId());
			for (Message message : response.getMessages()) {
				logger.warn("Error network deletion: [{}] ", message.getMessageText());
			}
		}
   }
}
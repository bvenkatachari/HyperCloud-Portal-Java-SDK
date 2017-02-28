package io.dchq.volumes;

import static junit.framework.TestCase.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
			String server,
			String message,
			boolean error) {

       if (volumeName == null) {
           throw new IllegalArgumentException("Volume == null");
       }

       if (!volumeName.isEmpty()) {
           String prefix = RandomStringUtils.randomAlphabetic(3);
           volumeName = prefix + volumeName;
           volumeName = org.apache.commons.lang3.StringUtils.lowerCase(volumeName);
       }

       this.dockerVolume = new DockerVolume();
       this.dockerVolume.setName(volumeName);
       this.dockerVolume.setOptionsText(options);
       this.dockerVolume.setEntitledUsers(entitledUser);
   }
   @Parameterized.Parameters
   public static Collection<Object[]> data() throws Exception {
	   List<UsernameEntityBase> entitledUser = new ArrayList<>();
	   UsernameEntityBase user = new UsernameEntityBase();
	   user.setUsername("Everyone");
	   entitledUser.add(user);
       return Arrays.asList(new Object[][]{
               // TODO: add more test data for all sorts of validations
				{ "testvalumn", "test options",entitledUser, "Local Volumne Provider" + "qe-100(DockerEngine)"},
				{ "test@123", "test @@@ options",entitledUser, "Local Volumne Provider" + "qe-100(DockerEngine)"},
				{ "test-valumn", "test options",entitledUser, "Local Volumne Provider" + "qe-100(DockerEngine)"}
       });
   }
   @Ignore
   public void testCreate()
   {
	   logger.info("Create docker volumne name[{}] options [{}] EntitledUser [{}] ", dockerVolume.getName(), dockerVolume.getOptionsText(), dockerVolume.getEntitledUsers().get(0));
       ResponseEntity<DockerVolume> response = dockerVolumeService.create(dockerVolume);
       for (Message message : response.getMessages()) {
           logger.warn("Error while Create request  [{}] ", message.getMessageText());
       }
       if (response.getResults() != null) {
           this.dockerVolumeCreated = response.getResults();
           logger.info("Create docker volumne Successful..");
       }
       assertNotNull(response);
       assertNotNull(response.isErrors());
       
   }
}
package io.dchq.volumes;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.dockervolume.DockerVolume;
import io.dchq.sdk.core.AbstractServiceTest;
import io.dchq.sdk.core.DockerVolumeService;
import io.dchq.sdk.core.ServiceFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

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

   public DockerVolumeCreateServiceTest (String volumeName) {

       // random clustername
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
   }
}

package fr.thumbnailsdb;

import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.lshbuilders.LSHManager;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mohannad on 03/12/15.
 */
public class PreloadedDescriptorsTest {
    File tmpDir = null;
    DBManager dbManager = null;
    File folder1 = null;
    MediaIndexer mediaIndexer = null;
    MediaFileDescriptorBuilder mediaFileDescriptorBuilder = null;
    LSHManager lshManager = null;

    @BeforeClass
    public void createTempDir() throws IOException, URISyntaxException {
        tmpDir = File.createTempFile("test", "");
        tmpDir.delete();
        tmpDir.mkdir();
        folder1 = new File(getClass().getResource("folder1").toURI());
        System.out.println("DBManagerTest.createTempDir Temp Dir " + tmpDir);
        System.out.println("DBManagerTest.createTempDir Folder1  " + folder1);
        mediaFileDescriptorBuilder=new MediaFileDescriptorBuilder();
        dbManager = new DBManager(tmpDir.getCanonicalPath() + "/testDB", mediaFileDescriptorBuilder );
        mediaIndexer = new MediaIndexer(dbManager, mediaFileDescriptorBuilder);
        lshManager = new LSHManager(dbManager);
    }
    @AfterClass
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(tmpDir);
        try{
            FileUtils.deleteDirectory(new File("lsh"));
            FileUtils.deleteDirectory(new File("lsh.p"));
            FileUtils.deleteDirectory(new File("lsh.t"));
        } catch (Exception e) {

        }
    }
    @Test
    public void testPreloadingOfDescriptors() throws IOException, URISyntaxException {
        mediaIndexer.processMTRoot(folder1.getCanonicalPath());
        int size = PreloadedDescriptors.getPreloadedDescriptors(dbManager).size();
        Assert.assertEquals(size,9);
    }
    @Test(dependsOnMethods={"testPreloadingOfDescriptors"})
    public void testRemovingDescriptor(){
        File f = folder1.listFiles()[5];
        MediaFileDescriptor mediaFileDescriptor = mediaFileDescriptorBuilder.buildMediaDescriptor(f);
        PreloadedDescriptors preloadedDescriptors = PreloadedDescriptors.getPreloadedDescriptors(dbManager);
        preloadedDescriptors.remove(mediaFileDescriptor);
        Iterator<MediaFileDescriptor> mediaFileDescriptorIterator = preloadedDescriptors.iterator();
        boolean found = false;
        System.out.println("++++++++++++++++++++++++++++++++++++++++++");
        System.out.println(mediaFileDescriptor.getMD5());
        while (mediaFileDescriptorIterator.hasNext()){
            MediaFileDescriptor m = mediaFileDescriptorIterator.next();
            System.out.println(m.getMD5());
            System.out.println("++++++++++++++++++++++++++++++++++++++++++");
            if(m.getMD5().equals(mediaFileDescriptor.getMD5())){
                found=true;
                break;
            }
        }

        System.out.println("size === " +preloadedDescriptors.size());

        Assert.assertTrue(!found);
    }

}

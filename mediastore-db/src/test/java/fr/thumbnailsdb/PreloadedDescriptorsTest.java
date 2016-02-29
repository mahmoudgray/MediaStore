package fr.thumbnailsdb;

import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;
import fr.thumbnailsdb.lsh.LSHManager;
import fr.thumbnailsdb.lsh.LSHManagerIF;
import fr.thumbnailsdb.mediaIndexers.MediaIndexer;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * Created by mohannad on 03/12/15.
 */
public class PreloadedDescriptorsTest {
    File tmpDir = null;
    DBManagerIF dbManagerIF = null;
    File folder1 = null;
    MediaIndexer mediaIndexer = null;
    MediaFileDescriptorBuilder mediaFileDescriptorBuilder = null;
    LSHManagerIF lshManagerIF = null;

    @BeforeClass
    public void createTempDir() throws IOException, URISyntaxException {
        tmpDir = File.createTempFile("test", "");
        tmpDir.delete();
        tmpDir.mkdir();
        folder1 = new File(getClass().getResource("folder1").toURI());
        System.out.println("PreloadedDescriptorsTest.createTempDir Temp Dir " + tmpDir);
        System.out.println("PreloadedDescriptorsTest.createTempDir Folder1  " + folder1);
        // in order to force loading full paths
        //PreloadedDescriptors.setUseFullPath(true);
        mediaFileDescriptorBuilder=new MediaFileDescriptorBuilder();
        dbManagerIF = new DBManager(tmpDir.getCanonicalPath() + "/testDB", mediaFileDescriptorBuilder );
        mediaIndexer = new MediaIndexer(dbManagerIF, mediaFileDescriptorBuilder);
        lshManagerIF = new LSHManager(dbManagerIF);
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
    public void testPreLoadedDescriptorExists(){
        Assert.assertTrue(!PreloadedDescriptors.preloadedDescriptorsExists());
    }
    @Test(dependsOnMethods={"testPreLoadedDescriptorExists"})
    public void testPreloadingOfDescriptors() throws IOException, URISyntaxException {
        mediaIndexer.processMTRoot(folder1.getCanonicalPath());
        int size = PreloadedDescriptors.getPreloadedDescriptors(dbManagerIF).size();
        Assert.assertTrue(PreloadedDescriptors.preloadedDescriptorsExists());
        Assert.assertEquals(size,9);
    }
    @Test(dependsOnMethods={"testPreloadingOfDescriptors"})
    public void testRemovingDescriptor() throws IOException {
        File f = folder1.listFiles()[0];
        MediaFileDescriptorIF mediaFileDescriptorIF = mediaFileDescriptorBuilder.getMediaFileDescriptorFromDB(f.getCanonicalPath());
        PreloadedDescriptors preloadedDescriptors = PreloadedDescriptors.getPreloadedDescriptors(dbManagerIF);
        preloadedDescriptors.remove(mediaFileDescriptorIF);
        Iterator<MediaFileDescriptorIF> mediaFileDescriptorIterator = preloadedDescriptors.iterator();
        boolean found = false;
        while (mediaFileDescriptorIterator.hasNext()){
            MediaFileDescriptorIF m = mediaFileDescriptorIterator.next();
            if(mediaFileDescriptorIF.equals(m)){
                found=true;
                break;
            }
        }
        Assert.assertTrue(!found);
    }
    @Test(dependsOnMethods={"testPreloadingOfDescriptors"})
    public void testPreloadedDescriptorFlush(){
        PreloadedDescriptors.flushPreloadedDescriptors();
        Assert.assertTrue(!PreloadedDescriptors.preloadedDescriptorsExists());
    }

}

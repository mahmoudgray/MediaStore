package fr.thumbnailsdb;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class ThumbStoreTest {

    File tmpDir = null;
    ThumbStore tb = null;
    File folder1 = null;
    MediaIndexer tg = null;


    @BeforeClass
    public void createTempDir() throws IOException, URISyntaxException {
        tmpDir = File.createTempFile("test", "");
        tmpDir.delete();
        tmpDir.mkdir();
        folder1 = new File(getClass().getResource("folder1").toURI());
        System.out.println("ThumbStoreTest.createTempDir Temp Dir " + tmpDir);
        System.out.println("ThumbStoreTest.createTempDir Folder1  " + folder1);
        tb = new ThumbStore(tmpDir.getCanonicalPath() + "/testDB");
        tg = new MediaIndexer(tb);

//        System.out.println("ThumbStoreTest.createTempDir "));
        //System.out.println("ThumbStoreTest.createTempDir " + getClass().getResource("folder1"));


    }
        /*
    @AfterClass
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(tmpDir);
        try{
        FileUtils.deleteDirectory(new File("lsh"));
        FileUtils.deleteDirectory(new File("lsh.p"));
        FileUtils.deleteDirectory(new File("lsh.t"));
        } catch (Exception e) {

        }
    }*/



    @Test
    public void testIndexing() throws IOException {
       //tb.addIndexPath();

        tg.processMTRoot(folder1.getCanonicalPath());
    }

    @Test(dependsOnMethods={"testIndexing"})
    public void testIdenticalImage() throws IOException {
        File[] list = folder1.listFiles();
        SimilarImageFinder si = new SimilarImageFinder(tb);

        for(File f : list) {
            Assert.assertEquals(si.findIdenticalMedia(f.getCanonicalPath()).size(), 1);
        }
    }


    @Test(dependsOnMethods={"testIndexing"})
    public void testSimilarImage() throws IOException {
        File[] list = folder1.listFiles();
        SimilarImageFinder si = new SimilarImageFinder(tb);

        for(File f : list) {
            Assert.assertEquals(si.findSimilarMedia(f.getCanonicalPath(),1).size(), 1);
        }
    }


//    @Test
//    public void sayHello() throws IOException {
//        System.out.println("Hello " + new File(tmpDir, "tototot-2").createNewFile());
//        System.out.println("Hello " + new File(tmpDir, "tototot-2").getAbsolutePath());
//        assert true;
//    }


}

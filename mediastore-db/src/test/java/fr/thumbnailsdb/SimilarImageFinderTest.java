package fr.thumbnailsdb;

import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.lsh.LSHManager;
import fr.thumbnailsdb.mediaIndexers.MediaIndexer;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Created by mohannad on 07/12/15.
 */
public class SimilarImageFinderTest {
    File tmpDir = null;
    DBManagerIF dbManagerIF = null;
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
        System.out.println("MediaIndexerTest.createTempDir Temp Dir " + tmpDir);
        System.out.println("MediaIndexerTest.createTempDir Folder1  " + folder1);
        mediaFileDescriptorBuilder=new MediaFileDescriptorBuilder();
        dbManagerIF = new DBManager(tmpDir.getCanonicalPath() + "/testDB", mediaFileDescriptorBuilder );
        mediaIndexer = new MediaIndexer(dbManagerIF, mediaFileDescriptorBuilder);
        lshManager = new LSHManager(dbManagerIF);
        mediaIndexer.processMTRoot(folder1.getCanonicalPath());
    }
    @AfterClass
    public void deleteDir() throws IOException {
        lshManager.clear();
        lshManager=null;
        dbManagerIF =null;
        mediaIndexer=null;
        mediaFileDescriptorBuilder=null;
        FileUtils.deleteDirectory(tmpDir);
        try{
            FileUtils.deleteDirectory(new File("lsh"));
            FileUtils.deleteDirectory(new File("lsh.p"));
            FileUtils.deleteDirectory(new File("lsh.t"));
        } catch (Exception e) {

        }
    }

    @Test
    public void testIdenticalImage() throws IOException {
        File[] list = folder1.listFiles();
        SimilarImageFinder si = new SimilarImageFinder(dbManagerIF,mediaFileDescriptorBuilder,lshManager );
        for(File f : list) {
            Assert.assertEquals(si.findIdenticalMedia(f.getCanonicalPath()).size(), 1);
        }
    }
    @Test
    public void testSimilarImage() throws IOException, SQLException {
        File[] list = folder1.listFiles();
        lshManager.buildLSH(true);
        SimilarImageFinder si = new SimilarImageFinder(dbManagerIF,mediaFileDescriptorBuilder,lshManager );
        for(File f : list) {
            Assert.assertEquals(si.findSimilarImages(f.getCanonicalPath(),1).size(), 1);
        }
    }
}

package fr.thumbnailsdb;

/**
 * Created by mohannad on 02/12/15.
 */

import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
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


public class MediaIndexerTest {

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
        System.out.println("MediaIndexerTest.createTempDir Temp Dir " + tmpDir);
        System.out.println("MediaIndexerTest.createTempDir Folder1  " + folder1);
        mediaFileDescriptorBuilder=new MediaFileDescriptorBuilder();
        dbManagerIF = new DBManager(tmpDir.getCanonicalPath() + "/testDB", mediaFileDescriptorBuilder );
        mediaIndexer = new MediaIndexer(dbManagerIF, mediaFileDescriptorBuilder);
        lshManagerIF = new LSHManager(dbManagerIF);
    }
    @AfterClass
    public void deleteDir() throws IOException {
        lshManagerIF =null;
        dbManagerIF =null;
        mediaIndexer=null;
        mediaFileDescriptorBuilder=null;
        (tmpDir).delete();
        try{
            (new File("lsh")).delete();
            (new File("lsh.p")).delete();
            (new File("lsh.t")).delete();
        } catch (Exception e) {

        }
    }
    @Test
    public void testIndexing() throws IOException, URISyntaxException {
        mediaIndexer.processMTRoot(folder1.getCanonicalPath());
        Assert.assertTrue(dbManagerIF.size()==9);
    }



}

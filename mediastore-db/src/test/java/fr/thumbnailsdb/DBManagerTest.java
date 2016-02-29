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
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.ArrayList;

public class DBManagerTest {

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
        System.out.println("DBManagerTest.createTempDir Temp Dir " + tmpDir);
        System.out.println("DBManagerTest.createTempDir Folder1  " + folder1);
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
    public void testDBConnection(){
        Connection connection = dbManagerIF.getConnection();
        Assert.assertNotNull(connection);
    }
    @Test(dependsOnMethods={"testDBConnection"})
    public void testAddIndexedPath() throws IOException {
        dbManagerIF.addIndexPath(folder1.getCanonicalPath());
        ArrayList<String> indexedPathes = dbManagerIF.getIndexedPaths();
        boolean found =false;
        for( String p : indexedPathes){
            if(p.equals(folder1.getCanonicalPath())){
                found=true;
                break;
            }
        }
        Assert.assertTrue(found);
    }
    @Test(dependsOnMethods={"testAddIndexedPath"})
    public void testSaveToDB() throws IOException{
        File file = folder1.listFiles()[0];
            MediaFileDescriptorIF mediaFileDescriptorIF1 = mediaFileDescriptorBuilder.buildMediaDescriptor(file);
            dbManagerIF.saveToDB(mediaFileDescriptorIF1);
            MediaFileDescriptorIF mediaFileDescriptorIF2 = mediaFileDescriptorBuilder.getMediaFileDescriptorFromDB(file.getCanonicalPath());
            Assert.assertTrue(mediaFileDescriptorIF1.getMD5() == mediaFileDescriptorIF2.getMD5());
            dbManagerIF.deleteFromDatabase(file.getCanonicalPath());
            mediaFileDescriptorIF2 = mediaFileDescriptorBuilder.getMediaFileDescriptorFromDB(file.getCanonicalPath());
            Assert.assertNull(mediaFileDescriptorIF2);
        dbManagerIF.deleteIndexedPath(folder1.getCanonicalPath());

    }
    @Test(dependsOnMethods={"testSaveToDB"})
    public void testDeleteIndexedPath() throws IOException {
        dbManagerIF.deleteIndexedPath(folder1.getCanonicalPath());
        ArrayList<String> indexedPathes = dbManagerIF.getIndexedPaths();
        boolean found =false;
        for( String p : indexedPathes){
            if(p.equals(folder1.getCanonicalPath())){
                found=true;
                break;
            }
        }
        Assert.assertTrue(!found);
    }

}

package fr.thumbnailsdb;

import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.lshbuilders.LSHManager;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBManagerTest {

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
    public void testDBConnection(){
        Connection connection = dbManager.getconnection();
        Assert.assertNotNull(connection);
    }

    @Test(dependsOnMethods={"testDBConnection"})
    public void testAddIndexedPath() throws IOException {
        dbManager.addIndexPath(folder1.getCanonicalPath());
        ArrayList<String> indexedPathes = dbManager.getIndexedPaths();
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
            MediaFileDescriptor mediaFileDescriptor1 = mediaIndexer.buildMediaDescriptor(file);
            dbManager.saveToDB(mediaFileDescriptor1);
            MediaFileDescriptor mediaFileDescriptor2 = mediaFileDescriptorBuilder.getMediaFileDescriptorFromDB(file.getCanonicalPath());
            Assert.assertTrue(mediaFileDescriptor1.getMD5() == mediaFileDescriptor2.getMD5());
            dbManager.deleteFromDatabase(file.getCanonicalPath());
            mediaFileDescriptor2 = mediaFileDescriptorBuilder.getMediaFileDescriptorFromDB(file.getCanonicalPath());
            Assert.assertNull(mediaFileDescriptor2);
        dbManager.deleteIndexedPath(folder1.getCanonicalPath());

    }
    @Test(dependsOnMethods={"testSaveToDB"})
    public void testDeleteIndexedPath() throws IOException {
        dbManager.deleteIndexedPath(folder1.getCanonicalPath());
        ArrayList<String> indexedPathes = dbManager.getIndexedPaths();
        boolean found =false;
        for( String p : indexedPathes){
            if(p.equals(folder1.getCanonicalPath())){
                found=true;
                break;
            }
        }
        Assert.assertTrue(!found);
    }

    @Test(dependsOnMethods={"testDeleteIndexedPath"})
    public void testIndexing() throws IOException, URISyntaxException {
        deleteDir();
        createTempDir();
        mediaIndexer.processMTRoot(folder1.getCanonicalPath());
        Assert.assertTrue(dbManager.size()!=0);
    }

    @Test(dependsOnMethods={"testIndexing"})
    public void testIdenticalImage() throws IOException {
        File[] list = folder1.listFiles();
        SimilarImageFinder si = new SimilarImageFinder(dbManager,mediaFileDescriptorBuilder,lshManager );
        for(File f : list) {
            Assert.assertEquals(si.findIdenticalMedia(f.getCanonicalPath()).size(), 1);
        }
    }

    @Test(dependsOnMethods={"testIndexing"})
    public void testSimilarImage() throws IOException, SQLException {
        File[] list = folder1.listFiles();
        lshManager.buildLSH(true);
        SimilarImageFinder si = new SimilarImageFinder(dbManager,mediaFileDescriptorBuilder,lshManager );

        for(File f : list) {
            Assert.assertEquals(si.findSimilarMedia(f.getCanonicalPath(),1).size(), 1);
        }
    }


}

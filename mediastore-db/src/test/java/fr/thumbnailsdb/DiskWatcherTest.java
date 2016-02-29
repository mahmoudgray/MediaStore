package fr.thumbnailsdb;

import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;
import fr.thumbnailsdb.diskmonitor.DiskListener;
import fr.thumbnailsdb.diskmonitor.DiskWatcher;
import fr.thumbnailsdb.lsh.LSHManager;
import fr.thumbnailsdb.mediaIndexers.MediaIndexer;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mohannad on 04/01/16.
 */
public class DiskWatcherTest implements DiskListener {

    private String event;
    File tmpDir = null;
    DiskWatcher dw;
    File folder2;
    boolean ok = false;
    Path file;

    @BeforeClass
    public void createTempDir() throws IOException, URISyntaxException {
        tmpDir = File.createTempFile("test1", "");
        tmpDir.delete();
        tmpDir.mkdir();
        folder2 = new File(getClass().getResource("folder2").toURI());
        System.out.println("DiskWatcherTest.createTempDir tempDir  " + tmpDir);
        String arg [] = {folder2.getCanonicalPath()+"/"};
        dw = new DiskWatcher(arg);
        dw.addListener(this);
        dw.processEvents();
    }
    @AfterClass
    public void deleteDir() throws IOException {
        tmpDir.delete();
        dw.removeListener(this);
        dw=null;
        this.event=null;

    }

    @Test
    public void testCreateFile() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("The first line", "The second line");
        file = Paths.get(folder2.getCanonicalPath()+"/1.txt");
        System.out.println(file);
        Files.write(file, lines, Charset.forName("UTF-8"));
        Thread.sleep(2000);
        Assert.assertEquals(this.event,"fileCreated");


    }

    @Test(dependsOnMethods={"testCreateFile"})
    public void testDeleteFile() throws IOException, InterruptedException {
        Files.delete(file);
        Thread.sleep(2000);
        Assert.assertEquals(this.event,"fileDeleted");
    }




    public void fileCreated(Path p) {
        this.event = "fileCreated";
       // ok=true;
    }
    public void fileModified(Path p) {
        this.event = "fileModified";
    }

    public void fileDeleted(Path p) {
        this.event = "fileDeleted";
        //ok=true;
    }

    public void folderCreated(Path p) {
        this.event = "folderCreated";
    }

    public void folderModified(Path p) {
        this.event = "folderModified";
    }

    public void folderDeleted(Path p) {
        this.event = "folderDeleted";
    }
}
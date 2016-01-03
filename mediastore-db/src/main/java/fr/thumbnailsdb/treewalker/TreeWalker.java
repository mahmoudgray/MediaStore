package fr.thumbnailsdb.treewalker;

import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.mediaIndexers.MediaIndexer;
import fr.thumbnailsdb.dbservices.DBManager;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by fhuet on 26/09/14.
 */
public class TreeWalker {


    FileProcessor fp;

    public TreeWalker(MediaIndexer t ) {
        this.fp =  new FileProcessor(t);
    }


    public void walk(String root) {
        Path start = Paths.get(root);
        try {
            Files.walkFileTree(start,fp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MediaFileDescriptorBuilder mediaFileDescriptorBuilder = new MediaFileDescriptorBuilder();
        DBManagerIF dbManagerIF = new DBManager(null , mediaFileDescriptorBuilder);
        TreeWalker t = new TreeWalker( new MediaIndexer(null,mediaFileDescriptorBuilder ));
        t.walk("/Users/fhuet/Documents/workspaces/imagessimilaires/MediaStore");


    }



}

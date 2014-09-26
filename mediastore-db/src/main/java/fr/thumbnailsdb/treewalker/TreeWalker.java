package fr.thumbnailsdb.treewalker;

import fr.thumbnailsdb.MediaIndexer;
import fr.thumbnailsdb.ThumbnailGenerator;

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

        TreeWalker t = new TreeWalker( new MediaIndexer(null));
        t.walk("/Users/fhuet/Documents/workspaces/imagessimilaires/MediaStore");


    }



}

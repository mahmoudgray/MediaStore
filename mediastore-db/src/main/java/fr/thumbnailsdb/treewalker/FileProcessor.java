package fr.thumbnailsdb.treewalker;

import fr.thumbnailsdb.mediaIndexers.MediaIndexer;
import fr.thumbnailsdb.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by fhuet on 26/09/14.
 */
public class FileProcessor  extends SimpleFileVisitor<Path> {


    private MediaIndexer tg ;

    public FileProcessor(MediaIndexer t) {
        this.tg = t;
    }


    // Print information about
    // each type of file.
    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr) {
//        if (attr.isSymbolicLink()) {
//            System.out.format("Symbolic link: %s ", file);
//        } else if (attr.isRegularFile()) {
//            System.out.format("Regular file: %s ", file);
//        } else {
//            System.out.format("Other: %s ", file);
//        }
//        System.out.println("(" + attr.size() + "bytes)");
        File f = file.toFile();
        if (Utils.isValideFile(f)) {
      //      System.out.println("FileProcessor.visitFile found useful file! " + f);
            tg.asyncProcessing(f);
        }

        return FileVisitResult.CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult postVisitDirectory(Path dir,
                                              IOException exc) {
      //  System.out.format("Directory: %s%n", dir);
        return FileVisitResult.CONTINUE;
    }

    // If there is some error accessing
    // the file, let the user know.
    // If you don't override this method
    // and an error occurs, an IOException
    // is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exc) {
        System.err.println(exc);
        return FileVisitResult.CONTINUE;
    }
}


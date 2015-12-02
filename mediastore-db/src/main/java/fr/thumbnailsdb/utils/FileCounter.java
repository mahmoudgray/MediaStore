package fr.thumbnailsdb.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by mohannad on 02/12/15.
 */
public final class FileCounter extends SimpleFileVisitor<Path> {
    int total = 0;

    @Override
    public FileVisitResult visitFile(
            Path aFile, BasicFileAttributes aAttrs
    ) throws IOException {
        total++;
        //System.out.println("Processing file:" + aFile);
        return FileVisitResult.CONTINUE;
    }

    public int getTotal() {
        return this.total;
    }

}

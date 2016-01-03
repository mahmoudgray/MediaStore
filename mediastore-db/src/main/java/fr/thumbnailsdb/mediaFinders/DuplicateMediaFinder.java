package fr.thumbnailsdb.mediaFinders;

import fr.thumbnailsdb.PreloadedDescriptors;
import fr.thumbnailsdb.dbservices.DBManagerIF;
import fr.thumbnailsdb.descriptorbuilders.MediaFileDescriptorIF;
import fr.thumbnailsdb.duplicate.DuplicateFileGroup;
import fr.thumbnailsdb.duplicate.DuplicateFileList;
import fr.thumbnailsdb.duplicate.DuplicateFolderList;
import fr.thumbnailsdb.utils.Logger;

import java.util.Iterator;
import java.util.List;

public class DuplicateMediaFinder {

    protected DBManagerIF dbManagerIF;

    protected DuplicateFileList duplicateFileList;

    public DuplicateMediaFinder(DBManagerIF c) {
        this.dbManagerIF = c;
    }
    public DuplicateFileList computeDuplicateSets() {
        PreloadedDescriptors r = PreloadedDescriptors.getPreloadedDescriptors(dbManagerIF);
        if (duplicateFileList != null) {
            return duplicateFileList;
        }
        duplicateFileList = new DuplicateFileList();
        DuplicateFileGroup dg = new DuplicateFileGroup();
        String currentMd5 = "";
        Iterator<MediaFileDescriptorIF> it = r.iterator();
        while (it.hasNext()) {
            MediaFileDescriptorIF mfd = it.next();
            String md5 = mfd.getMD5();
            if (md5 != null) {
                //TODO : this should be done in the DB directly
                int index = mfd.getId();
                String path = dbManagerIF.getPath(index);
                if (md5.equals(currentMd5)) {
                    // add to current group
                    dg.add(mfd.getSize(), path);
                } else {
                    if (dg.size() > 1) {
                        duplicateFileList.add(dg);
                    }
                    dg = new DuplicateFileGroup();
                    dg.add(mfd.getSize(), path);
                    currentMd5 = md5;
                }
            }
        }
        if (dg.size() > 1) {
            duplicateFileList.add(dg);
        }

        return duplicateFileList;
    }
    public DuplicateFolderList computeDuplicateFolderSets() {
        PreloadedDescriptors r = PreloadedDescriptors.getPreloadedDescriptors(dbManagerIF);
        Logger.getLogger().log("DuplicateMediaFinder.computeDuplicateFolderSets preloadedDescriptors  V2 " + r.size());

        long t0 = System.currentTimeMillis();
        DuplicateFileGroup duplicateFileGroup;
        //The table to maintain the tree of folder-couples and the
        //the number of common files they have
        DuplicateFolderList duplicateFolderList = new DuplicateFolderList();
        Iterator<String> it = r.keyIterator();
        while (it.hasNext()) {
            List<MediaFileDescriptorIF> mList = r.get(it.next());
            if (mList.size() > 1) {
                duplicateFileGroup = new DuplicateFileGroup();
                Iterator<MediaFileDescriptorIF> itMedia = mList.iterator();
                while (itMedia.hasNext()) {
                    MediaFileDescriptorIF mfd = itMedia.next();
                    int index = mfd.getId();
                    String path = dbManagerIF.getPath(index);
                    mfd.setPath(path);
                    duplicateFileGroup.add(mfd.getSize(), mfd.getPath());
                }
                if (duplicateFileGroup.size() > 1) {
                    //ok we have found a tree of duplicate files
                    //let's add their parent folder to the tree
                    //first compute the tree of folders
                    duplicateFolderList.addOrIncrement(duplicateFileGroup);
                }
            }
        }

        long t1 = System.currentTimeMillis();
        System.out.println("DuplicateMediaFinder.computeDuplicateFolderSets took "  + (t1-t0) + "  ms");
        Logger.getLogger().log("DuplicateMediaFinder.computeDuplicateFolderSets has " + duplicateFolderList.size() + " entries");
        return duplicateFolderList;
    }

}

package fr.thumbnailsdb.diskmonitor;

import com.sun.nio.file.ExtendedWatchEventModifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;


public class DiskWatcher {

    private final int FILE_CREATED = 0;
    private final int FILE_MODIFIED = 1;
    private final int FILE_DELETED = 2;
    private final int FOLDER_CREATED = 3;
    private final int FOLDER_MODIFIED = 4;
    private final int FOLDER_DELETED = 5;


    private final long MODIFY_TIMEOUT = 5000;

    private final WatchService watcher;


    private List<DiskListener> listeners;

    //we keep the path and the time of the files
    //for which we have received a EVENT_MODIFY
    private Map<Path, Long> currentModification;


    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public DiskWatcher() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        //      this.keys = new HashMap<WatchKey, Path>();
        this.currentModification = new HashMap<Path, Long>();
        listeners = new ArrayList<fr.thumbnailsdb.diskmonitor.DiskListener>();
    }

    public DiskWatcher(String[] path) throws IOException {
        this();
        for (String s : path) {
            System.out.println("DiskWatcher.DiskWatcher " + Paths.get(s).toAbsolutePath());

            registerAll(Paths.get(s).toAbsolutePath());
        }
    }


    private void register(Path dir) throws IOException {
        WatchEvent.Kind<?>[] events = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            dir.register(watcher, events, ExtendedWatchEventModifier.FILE_TREE);
        } else {
            dir.register(watcher, events);
        }


    }


    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        //on windows there is an optimisation

        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            WatchEvent.Kind<?>[] events = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};

            start.register(watcher, events, ExtendedWatchEventModifier.FILE_TREE);
        } else {
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    register(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }


    /**
     * Start a thread which will process incoming events
     */
    public void processEvents() {
        new Thread(new Runnable() {
            public void run() {
                for (; ; ) {
                    // wait for key to be signalled
                    WatchKey key;
                    try {
                        key = watcher.poll(MODIFY_TIMEOUT, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException x) {
                        return;
                    }
                    if (key != null) {

                        for (WatchEvent<?> event : key.pollEvents()) {
                            processEvent(key.watchable().toString(), event);
                        }
                        key.reset();
                    } else {
                        //timeout reached
                        processModification(null);
                    }
                }
            }
        }).start();
    }

    protected void processEvent(String path, WatchEvent event) {
        WatchEvent.Kind kind = event.kind();

        WatchEvent<Path> ev = cast(event);
        if ((ev==null) || (ev.context()==null)) {
            return;
        }
        Path name = FileSystems.getDefault().getPath(path, ev.context().toString());
        if (kind == OVERFLOW) {
            return;
        }

        // if modification, wait until no other modification
        //has been triggered, indicating the file is hopefully complete
        if (kind == ENTRY_MODIFY) {
            processModification(name);
        }
        // if directory is created, and watching recursively, then
        // register it and its sub-directories
        if (kind == ENTRY_CREATE) {
            try {
                if (Files.isDirectory(name)) {
                    this.fireEvent(name, FOLDER_CREATED);
                    registerAll(name);
                } else {
                    this.fireEvent(name, FILE_CREATED);
                }
            } catch (IOException x) {
                // ignore to keep sample readbale
            }
        }
        if (kind == ENTRY_DELETE) {
            try {
                if (Files.isDirectory(name, NOFOLLOW_LINKS)) {
                    this.fireEvent(name, FOLDER_DELETED);
                    registerAll(name);
                } else {
                    this.fireEvent(name, FILE_DELETED);
                }
            } catch (IOException x) {
                // ignore to keep sample readbale
            }
        }

    }


    /**
     * Process modifications on files
     * if path is not null, add it to the currentModification
     * map with current time
     * if path is null, check if entries in currentModification have
     * reached timeout and trigger notification
     *
     * @param path
     */

    protected void processModification(Path path) {
        if (path != null) {
            currentModification.put(path, System.currentTimeMillis());
        } else {
            Iterator<Path> it = currentModification.keySet().iterator();
            while (it.hasNext()) {
                Path p = it.next();
                long mTime = currentModification.get(p);
                if ((System.currentTimeMillis() - mTime) > MODIFY_TIMEOUT) {
                    //the file has not been modified for MODIFY_TIMEOUT, assume it is
                    //safe now
                    if (Files.isDirectory(p, NOFOLLOW_LINKS)) {
                        this.fireEvent(p, FOLDER_MODIFIED);
                    } else {
                        this.fireEvent(p, FILE_MODIFIED);
                    }
                    it.remove();
                }
            }

        }
    }

    public synchronized void addListener(DiskListener listener) {
        this.listeners.add(listener);
    }

    public synchronized DiskListener removeListener(DiskListener listener) {
        Iterator<DiskListener> it = this.listeners.iterator();
        while (it.hasNext()) {
            DiskListener tmp = it.next();
            if (tmp.equals(listener)) {
                it.remove();
                return tmp;
            }
        }
        return null;
    }


    public void fireEvent(Path p, int event) {
        synchronized (this.listeners) {
            for (DiskListener dw : this.listeners) {
                switch (event) {
                    case FILE_CREATED:
                        dw.fileCreated(p);
                        break;
                    case FILE_MODIFIED:
                        dw.fileModified(p);
                        break;
                    case FILE_DELETED:
                        dw.fileDeleted(p);
                        break;
                    case FOLDER_CREATED:
                        dw.folderCreated(p);
                        break;
                    case FOLDER_MODIFIED:
                        dw.folderModified(p);
                        break;
                    case FOLDER_DELETED:
                        dw.folderDeleted(p);
                        break;
                }
            }
        }
    }


    public static void main(String[] args) throws IOException {
        String arg [] = {"./"};
        DiskWatcher dw = new DiskWatcher(arg);
        dw.addListener(new ConsoleDiskListener());
        dw.processEvents();
    }

}

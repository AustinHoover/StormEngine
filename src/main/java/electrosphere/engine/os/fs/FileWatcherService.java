package electrosphere.engine.os.fs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.engine.signal.SignalServiceImpl;
import electrosphere.logger.LoggerInterface;

/**
 * Service that manages file watchers
 */
public class FileWatcherService extends SignalServiceImpl {

    /**
     * The file system object
     */
    FileSystem fs;

    /**
     * The watch service
     */
    WatchService watchService;

    /**
     * Map of path to watcher
     */
    Map<String,FileWatcher> pathWatcherMap;
    
    /**
     * Constructor
     */
    public FileWatcherService() {
        super(
            "FileWatcher",
            new SignalType[]{
            }
        );
        this.fs = FileSystems.getDefault();
        try {
            this.watchService = fs.newWatchService();
        } catch (IOException e) {
            LoggerInterface.loggerFileIO.ERROR(e);
        }
        this.pathWatcherMap = new HashMap<String,FileWatcher>();
    }

    /**
     * Tracks a file
     * @param watcher Tracks a file
     */
    public void trackFile(FileWatcher watcher){
        try {
            watcher.getPath().getParent().register(
                watchService,
                new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE}
            );
            pathWatcherMap.put(watcher.path.toAbsolutePath().normalize().toString(),watcher);
        } catch (IOException e) {
            LoggerInterface.loggerEngine.ERROR(e);
        }
    }

    /**
     * Scans all tracked files
     */
    public void poll(){
        WatchKey key = null;
        while((key = watchService.poll()) != null){
            List<WatchEvent<?>> events = key.pollEvents();
            Path keyParentDir = (Path)key.watchable();
            for(WatchEvent<?> event : events){
                if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY){
                    if(event.context() instanceof Path){
                        Path filePath = (Path)event.context();
                        Path resolved = keyParentDir.resolve(filePath);
                        String normalized = resolved.toAbsolutePath().normalize().toString();
                        FileWatcher watcher = this.pathWatcherMap.get(normalized);
                        if(watcher != null){
                            watcher.onWrite();
                        }
                    }
                } else if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE){
                    if(event.context() instanceof Path){
                        Path filePath = (Path)event.context();
                        Path resolved = keyParentDir.resolve(filePath);
                        String normalized = resolved.toAbsolutePath().normalize().toString();
                        FileWatcher watcher = this.pathWatcherMap.get(normalized);
                        if(watcher != null){
                            watcher.onCreate();
                        }
                    }
                } else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE){
                    if(event.context() instanceof Path){
                        Path filePath = (Path)event.context();
                        Path resolved = keyParentDir.resolve(filePath);
                        String normalized = resolved.toAbsolutePath().normalize().toString();
                        FileWatcher watcher = this.pathWatcherMap.get(normalized);
                        if(watcher != null){
                            watcher.onDelete();
                        }
                    }
                }
            }
            key.reset();
        }
    }

}

package electrosphere.engine.os.fs;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Sets a callback to fire every time a type of event happens to a fire
 */
public class FileWatcher {
    
    /**
     * The path of the file that is being watched
     */
    Path path;

    /**
     * The callback that fires when the file is created
     */
    Consumer<Path> onCreate;

    /**
     * The callback that fires when the file is edited
     */
    Consumer<Path> onWrite;

    /**
     * The callback that fires when the file is deleted
     */
    Consumer<Path> onDelete;

    /**
     * Constructor
     * @param path The path the watcher should follow
     */
    public FileWatcher(String path){
        this.path = new File(path).toPath();
    }

    /**
     * Gets the path of the file that this watcher is tracking
     * @return The path of the file that this watcher is tracking
     */
    public Path getPath(){
        return this.path;
    }

    /**
     * Sets the on-create callback for the watcher
     * @param onCreate The callback that fires when the file is created
     * @return The file watcher instance that the callback is being attached to
     */
    public FileWatcher setOnCreate(Consumer<Path> onCreate){
        this.onCreate = onCreate;
        return this;
    }

    /**
     * Gets the on-create callback
     * @return The on-create callback
     */
    public Consumer<Path> getOnCreate(){
        return this.onCreate;
    }

    /**
     * Fires when the file is created
     */
    protected void onCreate(){
        this.onCreate.accept(path);
    }

    /**
     * Sets the on-write callback for the watcher
     * @param onWrite The callback that fires when the file is edited
     * @return The file watcher instance that the callback is being attached to
     */
    public FileWatcher setOnWrite(Consumer<Path> onWrite){
        this.onWrite = onWrite;
        return this;
    }

    /**
     * Gets the on-write callback
     * @return The on-write callback
     */
    public Consumer<Path> getOnWrite(){
        return this.onWrite;
    }

    /**
     * Fires when the file is written to
     */
    protected void onWrite(){
        this.onWrite.accept(path);
    }

    /**
     * Sets the on-delete callback for the watcher
     * @param onDelete The callback that fires when the file is deleted
     * @return The file watcher instance that the callback is being attached to
     */
    public FileWatcher setOnDelete(Consumer<Path> onDelete){
        this.onDelete = onDelete;
        return this;
    }

    /**
     * Gets the on-delete callback
     * @return The on-delete callback
     */
    public Consumer<Path> getOnDelete(){
        return this.onDelete;
    }

    /**
     * Fires when the file is deleted
     */
    protected void onDelete(){
        this.onDelete.accept(path);
    }

}

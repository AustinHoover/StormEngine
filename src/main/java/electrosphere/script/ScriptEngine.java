package electrosphere.script;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import electrosphere.client.script.ScriptClientAreaUtils;
import electrosphere.client.script.ScriptClientVoxelUtils;
import electrosphere.client.ui.menu.script.ScriptLevelEditorUtils;
import electrosphere.client.ui.menu.script.ScriptMenuUtils;
import electrosphere.client.ui.menu.tutorial.TutorialMenus;
import electrosphere.engine.Main;
import electrosphere.engine.signal.Signal;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.engine.signal.SignalServiceImpl;
import electrosphere.logger.LoggerInterface;
import electrosphere.script.translation.JSServerUtils;
import electrosphere.script.utils.ScriptMathInterface;
import electrosphere.util.FileUtils;
import electrosphere.util.math.SpatialMathUtils;

/**
 * Handles the actual file loading of script files
 */
public class ScriptEngine extends SignalServiceImpl {

    /**
     * The directory with all script source files
     */
    public static final String TS_SOURCE_DIR = "./assets/Scripts";

    /**
     * The typescript cache dir
     */
    public static final String TS_CACHE_DIR = "./.cache/tscache";

    /**
     * Directory that should contain all ts source dirs
     */
    public static final String TS_SOURCE_CACHE_DIR = TS_CACHE_DIR + "/src";

    /**
     * The id for firing signals globally
     */
    public static final int GLOBAL_SCENE = -1;

    /**
     * the map of script filepaths to parsed, in-memory scripts
     */
    Map<String,Source> sourceMap;

    /**
     * The storage for the file->checksum map
     */
    ScriptFileChecksumMap checksumMap;

    /**
     * The script context
     */
    ScriptContext scriptContext = new ScriptContext();

    /**
     * The file system object
     */
    FileSystem fs;

    /**
     * The watch service
     */
    WatchService watchService;

    /**
     * Tracks the initialization status of the script engine
     */
    boolean initialized = false;

    /**
     * The files that are loaded on init to bootstrap the script engine
     */
    public static final String[] filesToLoadOnInit = new String[]{
        //polyfills
        "Scripts/compiler/require_polyfill.js",

        //main typescript engine
        "Scripts/compiler/typescript.js",

        //compiler and utilities
        "Scripts/compiler/file_resolution.js",
        "Scripts/compiler/compiler.js",
        "Scripts/compiler/host_access.js",

        //global context
        "Scripts/compiler/context.js",
    };

    /**
     * List of files that are ignored when registering new files
     */
    public static final String[] registerIgnores = new String[]{
        "/Scripts/compiler/host_access.ts",
    };

    /**
     * The classes that will be provided to the scripting engine
     * https://stackoverflow.com/a/65942034
     */
    public static final Object[][] staticClasses = new Object[][]{
        {"mathUtils",SpatialMathUtils.class},
        {"simulation",Main.class},
        {"tutorialUtils",TutorialMenus.class},
        {"serverUtils",JSServerUtils.class},
        {"menuUtils",ScriptMenuUtils.class},
        {"voxelUtils",ScriptClientVoxelUtils.class},
        {"levelEditorUtils",ScriptLevelEditorUtils.class},
        {"math",ScriptMathInterface.class},
        {"areaUtils",ScriptClientAreaUtils.class},
    };

    /**
     * singletons from the host that are provided to the javascript context
     */
    public static final Object[][] hostSingletops = new Object[][]{
        {"loggerScripts",LoggerInterface.loggerScripts},
    };


    /**
     * Constructor
     */
    public ScriptEngine(){
        super(
            "ScriptEngine",
            new SignalType[]{
                SignalType.SCRIPT_RECOMPILE,
            }
        );
        sourceMap = new HashMap<String,Source>();
        // this.fs = FileSystems.getDefault();
        // try {
        //     this.watchService = fs.newWatchService();
        // } catch (IOException e) {
        //     LoggerInterface.loggerFileIO.ERROR(e);
        // }
        // //register all source directories
        // try {
        //     Files.walkFileTree(new File(TS_SOURCE_DIR).toPath(), new SimpleFileVisitor<Path>(){
        //         @Override
        //         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) throws IOException {
        //             dir.register(
        //                 watchService,
        //                 new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE}
        //             );
        //             return FileVisitResult.CONTINUE;
        //         }
        //     });
        // } catch (IOException e) {
        //     LoggerInterface.loggerEngine.ERROR(e);
        // }
    }

    /**
     * Initializes the engine
     */
    public void initScripts(){
        //init datastructures
        initialized = false;

        //init script context
        scriptContext.init(this);

        //read files from cache
        boolean readCache = this.initCache();

        //compile
        if(!readCache){
            LoggerInterface.loggerScripts.WARNING("Recompiling scripts");
            if(!scriptContext.compileOutsideContext()){
                scriptContext.compileInContext();
            }
            if(!this.initCache()){
                throw new Error("Failed to compile!");
            }
        }

        //post init logic
        scriptContext.postInit();

        initialized = true;
    }

    /**
     * Scans the scripts directory for updates
     */
    public void scanScriptDir(){
        // WatchKey key = null;
        // while((key = watchService.poll()) != null){
        //     List<WatchEvent<?>> events = key.pollEvents();
        //     for(WatchEvent<?> event : events){
        //         if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY){
        //             if(event.context() instanceof Path){
        //                 // Path filePath = (Path)event.context();
        //                 // System.out.println(filePath);
        //             }
        //         } else if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE){
        //             throw new Error("Cannot handle create events yet");
        //         } else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE){
        //             throw new Error("Cannot handle delete events yet");
        //         }
        //     }
        //     key.reset();
        // }
    }

    /**
     * Gets the script context of the engine
     * @return The script context
     */
    public ScriptContext getScriptContext(){
        return this.scriptContext;
    }

    /**
     * Makes sure the cache folder exists
     * @return true if files were read from cache, false otherwise
     */
    private boolean initCache(){
        boolean rVal = false;
        File tsCache = new File(ScriptEngine.TS_SOURCE_CACHE_DIR);
        this.checksumMap = new ScriptFileChecksumMap();
        if(!tsCache.exists()){
            try {
                Files.createDirectories(tsCache.toPath());
            } catch (IOException e) {
                LoggerInterface.loggerFileIO.ERROR(e);
            }
        } else {
            //read file checksum map from disk if it exists
            File hashMapFile = new File(ScriptEngine.TS_CACHE_DIR + "/hashmap.json");
            if(hashMapFile.exists()){
                this.checksumMap = FileUtils.loadObjectFromFile(hashMapFile, ScriptFileChecksumMap.class);
            }

            //recursively read all files
            Value fileMap = this.scriptContext.getTopLevelValue("COMPILER").getMember("fileMap");
            rVal = this.recursivelyRegisterCachedFiles(tsCache,".",fileMap,tsCache,this.checksumMap.getFileChecksumMap(),this.checksumMap.getFileLastModifyMap());
        }
        return rVal;
    }

    /**
     * Register files recursively from the current file
     * @param tsCache The ts cache file
     * @param compoundedPath The compounded path
     * @param fileMap The file map on script side
     * @param currentDirectory The current directory
     * @param fileChecksumMap The file checksum map
     * @param fileLastModifyMap The file last modified map
     * @return true if all files were found in cache, false if some files were skipped
     */
    private boolean recursivelyRegisterCachedFiles(File tsCache, String compoundedPath, Value fileMap, File currentDirectory, Map<String,String> fileChecksumMap, Map<String,String> fileLastModifyMap){
        boolean rVal = true;
        for(File file : currentDirectory.listFiles()){
            if(file.isDirectory()){
                String newPath = compoundedPath + "/" + file.getName();
                boolean childSuccess = this.recursivelyRegisterCachedFiles(tsCache, newPath, fileMap, file, fileChecksumMap, fileLastModifyMap);
                if(!childSuccess){
                    rVal = false;
                }
            } else if(file.getPath().endsWith(".ts") || file.getPath().endsWith(".js")){
                try {
                    String relativePath = FileUtils.relativize(file, tsCache);
                    String normalizedPath = "/" + relativePath;
                    boolean shouldLoad = true;
                    File correspondingSourceFile = FileUtils.getAssetFile(compoundedPath + "/" + file.getName().replace(".js", ".ts"));

                    long lastModified = 0;
                    if(correspondingSourceFile.exists()){
                        try {
                            FileTime time = Files.getLastModifiedTime(correspondingSourceFile.toPath());
                            lastModified = time.toMillis();
                        } catch (IOException e) {
                            throw new Error("Failed to gather last modified time! " + lastModified);
                        }
                    } else {
                        shouldLoad = false;
                    }

                    //determine if we should load the file
                    if(!fileLastModifyMap.containsKey(normalizedPath)){
                        //cache does not contain this file
                        shouldLoad = false;
                    } else if(!fileLastModifyMap.get(normalizedPath).contains(lastModified + "")) {
                        //cache is up to date
                        shouldLoad = false;
                    }
                    //actually load the file
                    if(shouldLoad){
                        String fileContent = Files.readString(file.toPath());

                        //store checksum
                        try {
                            fileChecksumMap.put(normalizedPath,FileUtils.getChecksum(fileContent));
                        } catch (NoSuchAlgorithmException e) {
                            LoggerInterface.loggerScripts.ERROR(e);
                        }

                        //store on script side
                        LoggerInterface.loggerScripts.DEBUG("Preload: " + normalizedPath);
                        this.scriptContext.getTopLevelValue("COMPILER").invokeMember("preloadFile", normalizedPath, fileContent);
                    } else {
                        boolean inMap = fileLastModifyMap.containsKey(normalizedPath);
                        boolean timeMatch = false;
                        if(inMap){
                            timeMatch = fileLastModifyMap.get(normalizedPath).contains(lastModified + "");
                        }
                        LoggerInterface.loggerScripts.DEBUG("Skipping Preload: " + normalizedPath + " " + inMap + " " + timeMatch + " " + correspondingSourceFile.exists() + " " + lastModified + " " + correspondingSourceFile.toString());
                        if(!inMap){
                            continue;
                        }
                        rVal = false;
                    }
                } catch (IOException e) {
                    LoggerInterface.loggerFileIO.ERROR(e);
                }
            }
        }
        return rVal;
    }

    /**
     * Gets the checksum map
     * @return The checksum map
     */
    protected ScriptFileChecksumMap getChecksumMap(){
        return this.checksumMap;
    }

    /**
     * Writes the checksum map to disk
     */
    protected void writeChecksumMap(){
        File hashMapFile = new File(ScriptEngine.TS_CACHE_DIR + "/hashmap.json");
        //write cache map out
        FileUtils.serializeObjectToFilePath(hashMapFile, this.checksumMap);
    }

    /**
     * Gets the initialization status of the script engine
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized(){
        return this.initialized;
    }
    

    @Override
    public boolean handle(Signal signal){
        boolean rVal = false;
        switch(signal.getType()){
            case SCRIPT_RECOMPILE: {
                if(signal.getData() != null && signal.getData() instanceof Runnable){
                    scriptContext.recompile((Runnable)signal.getData());
                } else {
                    scriptContext.recompile(null);
                }
                rVal = true;
            } break;
            default: {
            } break;
        }
        return rVal;
    }
    
}

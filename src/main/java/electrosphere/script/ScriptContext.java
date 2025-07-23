package electrosphere.script;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Source.Builder;
import org.graalvm.polyglot.Value;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.script.access.FieldEnumerator;
import electrosphere.util.FileUtils;

/**
 * A context for executing scripts
 */
public class ScriptContext {

    /**
     * namespace for the engine functions exposed to the script engine
     */
    public static final String SCRIPT_NAMESPACE_ENGINE = "engine";

    /**
     * namespace for the core typescript functions
     */
    public static final String SCRIPT_NAMESPACE_SCRIPT = "script";

    /**
     * namespace for the current scene
     */
    public static final String SCRIPT_NAMESPACE_SCENE = "scene";

    /**
     * the graal context
     */
    Context context;

    /**
     * used to build source objects
     */
    Builder builder;
    
    /**
     * the javascript object that stores values
     */
    Value topLevelValue;

    /**
     * the object that contains all host values accessible to javascript land
     */
    Value hostObject;

    /**
     * the engine object
     */
    Value engineObject;

    /**
     * The hook manager
     */
    Value hookManager;

    /**
     * The parent script engine
     */
    ScriptEngine parent;

    /**
     * Locks the script engine to enforce synchronization
     */
    ReentrantLock lock = new ReentrantLock();

    /**
     * Initializes the context
     * @param engine
     */
    public void init(ScriptEngine scriptEngine){
        //register parent
        this.parent = scriptEngine;


        //create engine with flag to disable warning
        Engine engine = Engine.newBuilder()
        .option("engine.WarnInterpreterOnly", "false")
        .build();

        //Create the rules for guest accessing the host environment
        HostAccess.Builder builder = HostAccess.newBuilder(HostAccess.EXPLICIT);
        builder.allowArrayAccess(true);
        for(Field field : FieldEnumerator.getFields()){
            builder.allowAccess(field);
        }
        for(Method method : FieldEnumerator.getMethods()){
            builder.allowAccess(method);
        }
        HostAccess accessRules = builder.build();

        //create context
        context = Context.newBuilder("js")
        .allowNativeAccess(false)
        .allowHostAccess(accessRules)
        .engine(engine)
        .build();
        //save the js bindings object
        topLevelValue = context.getBindings("js");

        //put host members into environment
        this.putTopLevelValue("loggerScripts",LoggerInterface.loggerScripts);

        //load all files required to start the engine
        for(String fileToLoad : ScriptEngine.filesToLoadOnInit){
            this.loadDependency(fileToLoad);
        }

        //register engine files
        this.registerFile("/Scripts/engine/engine-init.ts");
    }

    /**
     * Logic to run after initializing
     */
    public void postInit(){
        //run script for engine init
        this.requireModule("/Scripts/engine/engine-init.ts");

        //get the engine object
        engineObject = topLevelValue.getMember("REQUIRE_CACHE").getMember("/Scripts/engine/engine-init.js").getMember("exports").getMember("engine");
        hookManager = engineObject.getMember("hookManager");

        //define host members
        this.defineHostMembers();

        //init on script side
        this.invokeModuleFunction("/Scripts/engine/engine-init.ts","ENGINE_onInit");
    }


    /**
     * Stores a variable at the top level of the js bindings
     * @param valueName The name of the variable (ie the name of the variable)
     * @param value The value that is stored at that variable
     */
    public void putTopLevelValue(String valueName, Object value){
        topLevelValue.putMember(valueName, value);
    }

    /**
     * Gets a top level value from the script engine
     * @param valueName The name of the variable
     * @return The value of the variable
     */
    public Value getTopLevelValue(String valueName){
        return topLevelValue.getMember(valueName);
    }

    /**
     * Removes a top level member from the javascript context
     * @param valueName The name of the top level member
     * @return true if successfully removed, false otherwise
     */
    public boolean removeTopLevelValue(String valueName){
        return topLevelValue.removeMember(valueName);
    }

    /**
     * Loads a script from disk
     * @param path The path to the script file
     */
    private void loadDependency(String path){
        String content;
        Source source = null;
        try {
            content = FileUtils.getAssetFileAsString(path);
            source = Source.create("js",content);
            context.eval(source);
        } catch (IOException e) {
            LoggerInterface.loggerScripts.ERROR("FAILED TO LOAD SCRIPT", e);
        } catch (PolyglotException e){
            if(source != null){
                LoggerInterface.loggerScripts.WARNING("Source language: " + source.getLanguage());
            }
            LoggerInterface.loggerScripts.ERROR("Script error", e);
            e.printStackTrace();
        }
    }

    /**
     * Prints the content of a file
     * @param path The filepath of the script
     */
    public void printScriptSource(String path){
        invokeMemberFunction("COMPILER", "printSource", path);
    }

    /**
     * Gets the contents of a file in the virtual filepath
     * @param path The virtual filepath
     * @return The contents of that file if it exists, null otherwise
     */
    public String getVirtualFileContent(String path){
        String rVal = null;
        Value compiler = this.topLevelValue.getMember("COMPILER");
        Value fileMap = compiler.getMember("fileMap");
        Value virtualFile = fileMap.getMember(path);
        rVal = virtualFile.getMember("content").asString();
        return rVal;
    }

    /**
     * Registers a file with the scripting engine to be compiled into the full binary
     * @param path The path to the script file
     */
    protected boolean registerFile(String path){
        String content;
        try {
            content = FileUtils.getAssetFileAsString(path);
            this.registerFile(path, content);
        } catch (IOException e) {
            LoggerInterface.loggerScripts.ERROR("FAILED TO LOAD SCRIPT", e);
            return false;
        }
        return true;
    }

    /**
     * Registers a file with the scripting engine to be compiled into the full binary
     * @param path The path to the script file
     */
    protected void registerFile(String path, String content){
        Value dependentFilesValue = this.invokeMemberFunction("COMPILER", "registerFile", path, content);
        //
        //register dependent files if necessary
        long dependentFilesCount = dependentFilesValue.getArraySize();
        if(dependentFilesCount > 0){
            for(int i = 0; i < dependentFilesCount; i++){
                String dependentFilePath = dependentFilesValue.getArrayElement(i).asString();
                boolean shouldRegister = true;
                for(String ignorePath : ScriptEngine.registerIgnores){
                    if(ignorePath.equals(dependentFilePath)){
                        shouldRegister = false;
                    }
                }
                if(shouldRegister){
                    LoggerInterface.loggerScripts.INFO("[HOST - Script Engine] Should register file " + dependentFilePath);
                    this.registerFile(dependentFilePath);
                } else {
                    LoggerInterface.loggerScripts.DEBUG("[HOST - Script Engine] Skipping ignorepath file " + dependentFilePath);
                }
            }
        }
    }

    /**
     * Compiles the project
     */
    protected void compileInContext(){
        ScriptFileChecksumMap checksumMap = this.parent.getChecksumMap();
        //actually compile
        this.invokeMemberFunction("COMPILER", "run");
        Value fileMap = this.topLevelValue.getMember("COMPILER").getMember("fileMap");
        //register new files, update cache where appropriate
        for(String key : fileMap.getMemberKeys()){
            Value fileData = fileMap.getMember(key);
            String content = fileData.getMember("content").asString();
            String cacheFilePath = ScriptEngine.TS_SOURCE_CACHE_DIR + key;
            File toWriteFile = new File(cacheFilePath);

            //make sure all containing folders exist
            try {
                Files.createDirectories(toWriteFile.getParentFile().toPath());
            } catch (IOException e) {
                LoggerInterface.loggerFileIO.ERROR(e);
            }

            //update cached timestamp
            {
                String pathRaw = toWriteFile.toPath() + "";
                pathRaw = pathRaw.replace(".\\.cache\\tscache\\src\\", "./assets/");
                String cacheKey = pathRaw.replace("./assets", "").replace("\\", "/");
                long lastModified = 0;
                try {
                    File correspondingSourceFile = new File(pathRaw.replace(".cache\\tscache\\src\\", "./assets/").replace(".js",".ts"));
                    FileTime time = Files.getLastModifiedTime(correspondingSourceFile.toPath());
                    lastModified = time.toMillis();
                } catch (IOException e) {
                    throw new Error("Failed to gather last modified time! " + lastModified);
                }
                checksumMap.getFileLastModifyMap().put(cacheKey, lastModified + "");
                LoggerInterface.loggerScripts.DEBUG("Putting file in cache " + cacheKey + " " + lastModified);
            }

            //write the actual file
            try {
                Files.writeString(toWriteFile.toPath(), content);
            } catch (IOException e) {
                LoggerInterface.loggerFileIO.ERROR(e);
            }
        }
        //write out cache map file
        this.parent.writeChecksumMap();
    }

    /**
     * Compiles the project
     * @return true if the process successfully forked, false otherwise
     */
    protected boolean compileOutsideContext(){
        ScriptFileChecksumMap checksumMap = this.parent.getChecksumMap();
        //actually compile
        Process process;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("cmd.exe", "/c", "tsc");
            builder.directory(new File(System.getProperty("user.dir")));
            builder.redirectOutput(Redirect.INHERIT);
            builder.redirectError(Redirect.INHERIT);
            process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new Error("Failed to execute typescript!", e);
        }
        if(process.exitValue() != 0){
            String message = "Failed to run external compiler! " + process.exitValue();
            LoggerInterface.loggerScripts.ERROR(new Error(message));
            return false;
        }
        //register new files, update cache where appropriate
        try {
            Files.walk(new File(".cache/tscache/src").toPath()).forEach((Path currPath) -> {
                if(!currPath.toFile().isDirectory()){
                    String pathRaw = currPath.toString();
                    pathRaw = pathRaw.replace(".cache\\tscache\\src\\", "./assets/");
                    String cacheKey = pathRaw.replace("./assets", "").replace("\\", "/");
                    long lastModified = 0;
                    try {
                        File correspondingSourceFile = new File(pathRaw.replace(".cache\\tscache\\src\\", "./assets/").replace(".js",".ts"));
                        if(!correspondingSourceFile.exists()){
                            checksumMap.getFileLastModifyMap().remove(cacheKey);
                            //skip non-existant file
                            return;
                        }
                        FileTime time = Files.getLastModifiedTime(correspondingSourceFile.toPath());
                        lastModified = time.toMillis();
                    } catch (IOException e) {
                        throw new Error("Failed to gather last modified time! " + lastModified);
                    }
                    checksumMap.getFileLastModifyMap().put(cacheKey, lastModified + "");
                    LoggerInterface.loggerScripts.DEBUG("Putting file in cache " + cacheKey + " " + lastModified);
                }
            });
        } catch (IOException e) {
            throw new Error("Failed to walk typescript cache dir!");
        }
        //write out cache map file
        this.parent.writeChecksumMap();
        return true;
    }

    /**
     * Recompiles the scripting engine
     */
    protected void recompile(Runnable onCompletion){
        Thread recompileThread = new Thread(() -> {
            Globals.engineState.scriptEngine.getScriptContext().executeSynchronously(() -> {
                Globals.engineState.scriptEngine.initScripts();
            });
            if(onCompletion != null){
                onCompletion.run();
            }
        });
        recompileThread.setName("Recompile Script Engine");
        recompileThread.start();
    }

    /**
     * Initializes a scene script
     * @param scenePath The scene's init script path
     * @return The id assigned to the scene instance from the script-side
     */
    public int initScene(String scenePath){
        //add files to virtual filesystem in script engine
        registerFile(scenePath);
        //load scene from javascript side
        Value sceneLoader = this.engineObject.getMember("sceneLoader");
        Value loadFunc = sceneLoader.getMember("loadScene");
        Value result = loadFunc.execute(scenePath);
        return result.asInt();
    }


    /**
     * Calls a function defined in the global scope with the arguments provided
     * @param functionName The function name
     * @param args The arguments
     */
    public Value invokeFunction(String functionName, Object... args){
        LoggerInterface.loggerScripts.DEBUG("Host execute: " + functionName);
        Value function = topLevelValue.getMember(functionName);
        if(function != null){
            return function.execute(args);
        } else {
            LoggerInterface.loggerScripts.WARNING("Failed to invoke function " + functionName);
        }
        return null;
    }

    /**
     * Calls a function on a child of the top level member
     * @param memberName The name of the child
     * @param functionName The name of the function
     * @param args The arguments for the function
     * @return The value from the function call
     */
    public Value invokeMemberFunction(String memberName, String functionName, Object ... args){
        LoggerInterface.loggerScripts.DEBUG("Host execute: " + functionName);
        Value childMember = topLevelValue.getMember(memberName);
        Value function = childMember.getMember(functionName);
        if(function != null){
            return function.execute(args);
        } else {
            LoggerInterface.loggerScripts.WARNING("Failed to invoke function " + functionName);
        }
        return null;
    }

    /**
     * Invokes a function defined in a file
     * @param filePath The file the function is defined in
     * @param functionName The function's name
     * @param args The args to pass into the function
     */
    public void invokeModuleFunction(String filePath, String functionName, Object ... args){
        Value filePathRaw = invokeFunction("FILE_RESOLUTION_getFilePath",filePath);
        Value requireCache = topLevelValue.getMember("REQUIRE_CACHE");
        Value module = requireCache.getMember(filePathRaw.asString());
        Value exports = module.getMember("exports");
        Value function = exports.getMember(functionName);
        if(function != null && function.canExecute()){
            function.execute(args);
        } else {
            LoggerInterface.loggerScripts.WARNING("Failed to invoke function " + functionName);
        }
    }

    /**
     * Requires a module into the global space
     * @param filePath The filepath of the module
     */
    public void requireModule(String filePath){
        this.invokeFunction("require", filePath);
    }

    /**
     * Invokes a function on a member of arbitrary depth on the engine object
     * @param memberName The member name
     * @param functionName The function's name
     * @param className The class of the expected return value
     * @param args The args to pass to the function
     * @return The results of the invocation or null if there was no result
     */
    public Value invokeEngineMember(String memberName, String functionName,  Object ... args){
        Value member = this.engineObject.getMember(memberName);
        if(member == null){
            throw new Error("Member is null!");
        }
        Value function = member.getMember(functionName);
        if(function == null || !function.canExecute()){
            throw new Error("Function is not executable! " + function);
        }
        Value executionResult = function.execute(args);
        if(executionResult == null){
            return null;
        }
        return executionResult;
    }

    /**
     * Executes some code synchronously that requires script engine access
     * @param function The function
     */
    public void executeSynchronously(Runnable function){
        boolean success = false;
        try {
            success = lock.tryLock(1, TimeUnit.MICROSECONDS);
        } catch (InterruptedException e) {
            LoggerInterface.loggerScripts.ERROR(e);
        }
        if(!success){
            throw new Error("Failed to acquire lock!");
        }
        function.run();
        lock.unlock();
    }

    /**
     * Defines host members within javascript context
     */
    protected void defineHostMembers(){
        hostObject = topLevelValue.getMember("HOST_ACCESS");
        //give guest access to static classes
        Value classes = engineObject.getMember("classes");
        for(Object[] currentClass : ScriptEngine.staticClasses){
            classes.putMember((String)currentClass[0], currentClass[1]);
        }
        //give access to script engine instance
        hostObject.putMember("scriptEngine", this);
    }

    /**
     * Fires a signal on a given scene
     * @param signal The signal name
     * @param sceneInstanceId The script-side instanceid of the scene
     * @param args The arguments to accompany the signal invocation
     */
    public void fireSignal(String signal, int sceneInstanceId, Object ... args){
        Value fireSignal = this.hookManager.getMember("fireSignal");
        fireSignal.execute(sceneInstanceId,signal,args);
    }

    /**
     * Evaluates a string
     * @param evalString The string
     */
    public void eval(String evalString){
        Source source = Source.create("js",evalString);
        context.eval(source);
    }

}

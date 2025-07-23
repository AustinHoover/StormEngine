package electrosphere.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import electrosphere.data.entity.creature.ai.AITreeData;
import electrosphere.data.entity.creature.ai.AITreeDataSerializer;
import electrosphere.data.entity.creature.movement.MovementSystem;
import electrosphere.data.entity.creature.movement.MovementSystemSerializer;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.macro.character.data.CharacterData;
import electrosphere.server.macro.character.data.CharacterDataSerializer;
import electrosphere.server.physics.terrain.generation.noise.NoiseModuleSerializer;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;
import electrosphere.util.annotation.AnnotationExclusionStrategy;
import electrosphere.util.math.region.Region;
import electrosphere.util.math.region.RegionSerializer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

/**
 * Utilities for dealing with files
 */
public class FileUtils {

    /**
     * Creates the gson instance
     */
    static {
        //init gson
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MovementSystem.class, new MovementSystemSerializer());
        gsonBuilder.registerTypeAdapter(AITreeData.class, new AITreeDataSerializer());
        gsonBuilder.registerTypeAdapter(NoiseSampler.class, new NoiseModuleSerializer());
        gsonBuilder.registerTypeAdapter(CharacterData.class, new CharacterDataSerializer());
        gsonBuilder.registerTypeAdapter(Region.class, new RegionSerializer());
        gsonBuilder.addDeserializationExclusionStrategy(new AnnotationExclusionStrategy());
        gsonBuilder.addSerializationExclusionStrategy(new AnnotationExclusionStrategy());
        gson = gsonBuilder.create();

        //init gamedir
        gameDir = Paths.get("").toFile();
    }
    
    //used for serialization/deserialization in file operations
    static Gson gson;

    //the game's root directory
    static File gameDir = null;
    
    //maximum number of attempt to read the file
    static final int maxReadFails = 3;
    //Timeout duration between read attempts
    static final int READ_TIMEOUT_DURATION = 5;
    /**
     * Reads a file to a string
     * @param f The file
     * @return The string
     */
    public static String readFileToString(File f){
        String rVal = "";
        BufferedReader reader;
        try {
            reader = Files.newBufferedReader(f.toPath());
            int failCounter = 0;
            boolean reading = true;
            StringBuilder builder = new StringBuilder("");
            while(reading){
                if(reader.ready()){
                    failCounter = 0;
                    int nextValue = reader.read();
                    if(nextValue == -1){
                        reading = false;
                    } else {
                        builder.append((char)nextValue);
                    }
                } else {
                    failCounter++;
                    if(failCounter > maxReadFails){
                        reading = false;
                    } else {
                        try {
                            TimeUnit.MILLISECONDS.sleep(READ_TIMEOUT_DURATION);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            rVal = builder.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return rVal;
    }
    
    
    public static String readStreamToString(InputStream resourceInputStream){
        String rVal = "";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(resourceInputStream));
            int failCounter = 0;
            boolean reading = true;
            StringBuilder builder = new StringBuilder("");
            while(reading){
                if(reader.ready()){
                    failCounter = 0;
                    int nextValue = reader.read();
                    if(nextValue == -1){
                        reading = false;
                    } else {
                        builder.append((char)nextValue);
                    }
                } else {
                    failCounter++;
                    if(failCounter > maxReadFails){
                        reading = false;
                    } else {
                        try {
                            TimeUnit.MILLISECONDS.sleep(READ_TIMEOUT_DURATION);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            rVal = builder.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return rVal;
    }
    
    
    /**
     * Sanitizes a relative file path, guaranteeing that the initial slash is correct
     * @param filePath The raw file path
     * @return The sanitized file path
     */
    public static String sanitizeFilePath(String filePath){
        String rVal = new String(filePath);
        rVal = rVal.trim();
        if(rVal.startsWith("./")){
            return rVal;
        } else if(!rVal.startsWith("/")){
            rVal = "/" + rVal;
        }
        return rVal;
    }
    
    /**
     * Serializes an object to a filepath
     * @param filePath The filepath
     * @param object The object
     */
    public static void serializeObjectToFilePath(String filePath, Object object){
        Path path = new File(filePath).toPath();
        try {
            Files.write(path, gson.toJson(object).getBytes());
        } catch (IOException ex) {
            LoggerInterface.loggerFileIO.ERROR(filePath, ex);
        }
    }

    /**
     * Serializes an object to a filepath
     * @param file The file to save to
     * @param object The object
     */
    public static void serializeObjectToFilePath(File file, Object object){
        Path path = file.toPath();
        try {
            Files.write(path, gson.toJson(object).getBytes());
        } catch (IOException ex) {
            LoggerInterface.loggerFileIO.ERROR(file.getAbsolutePath(), ex);
        }
    }
    
    /**
     * Gets an assets file
     * @param pathName The relative path in the assets folder
     * @return The file
     */
    public static File getAssetFile(String pathName){
        String sanitizedFilePath = sanitizeFilePath(pathName);
        File targetFile = new File("./assets" + sanitizedFilePath);
        return targetFile;
    }

    /**
     * Gets an assets file
     * @param pathName The relative path in the assets folder
     * @return The file
     */
    public static String getAssetFileString(String pathName){
        String sanitizedFilePath = sanitizeFilePath(pathName);
        return "./assets" + sanitizedFilePath;
    }

    /**
     * Gets an assets file as a byte buffer
     * @param pathName The relative path in the assets folder
     * @return The byte buffer containing the contents of the asset file
     * @throws IOException Thrown if there are any io issues
     */
    public static ByteBuffer getAssetFileAsByteBuffer(String pathName) throws IOException{
        String sanitizedFilePath = sanitizeFilePath(pathName);
        File targetFile = new File("./assets" + sanitizedFilePath);
        ByteBuffer buff = null;
        try(SeekableByteChannel byteChannel = Files.newByteChannel(targetFile.toPath())){
            buff = BufferUtils.createByteBuffer((int)(byteChannel.size() + 1));
            while(byteChannel.read(buff) != -1){
            }
            buff.flip();
        }
        return buff;
    }

    /**
     * Gets a cache file
     * @param pathName The relative path in the cache folder
     * @return The file
     */
    public static File getCacheFile(String pathName){
        String sanitizedFilePath = sanitizeFilePath(pathName);
        File targetFile = new File("./.cache" + sanitizedFilePath);
        return targetFile;
    }
    
    /**
     * Gets a save file
     * @param saveName The name of the save
     * @param pathName The relative path in the save's folder
     * @return the file
     */
    public static File getSaveFile(String saveName, String pathName){
        String sanitizedFilePath = sanitizeFilePath(pathName);
        String fullPath = "./saves/" + saveName + "/" + sanitizedFilePath;
        File targetFile = new File(fullPath);
        return targetFile;
    }
    
    /**
     * Gets an asset file as a stream
     * @param pathName The path of the file
     * @return The stream
     * @throws IOException Thrown if Files fails to create the stream
     */
    public static InputStream getAssetFileAsStream(String pathName) throws IOException{
        String sanitizedFilePath = sanitizeFilePath(pathName);
        File targetFile = new File("./assets" + sanitizedFilePath);
        return Files.newInputStream(targetFile.toPath());
    }
    
    /**
     * Gets an asset file as a string
     * @param pathName The path of the file
     * @return The string
     * @throws IOException Thrown if Files fails to read the file
     */
    public static String getAssetFileAsString(String pathName) throws IOException{
        String sanitizedFilePath = sanitizeFilePath(pathName);
        File targetFile = new File("./assets" + sanitizedFilePath);
        return Files.readString(targetFile.toPath());
    }
    
    /**
     * Loads an object from the assets folder
     * @param <T> The type of object
     * @param pathName The relative path to the file
     * @param className The class of the object inside the file
     * @return The file
     */
    public static <T>T loadObjectFromAssetPath(String pathName, Class<T> className){
        T rVal = null;
        String sanitizedFilePath = sanitizeFilePath(pathName);
        try {
            rVal = gson.fromJson(Files.newBufferedReader(getAssetFile(sanitizedFilePath).toPath()), className);
        } catch (IOException ex) {
            LoggerInterface.loggerFileIO.ERROR(ex);
        }
        return rVal;
    }

    /**
     * Loads an object from the assets folder
     * @param <T> The type of object
     * @param file The file to load from
     * @param className The class of the object inside the file
     * @return The file
     */
    public static <T>T loadObjectFromFile(File file, Class<T> className){
        T rVal = null;
        try {
            rVal = gson.fromJson(Files.newBufferedReader(file.toPath()), className);
        } catch (IOException ex) {
            LoggerInterface.loggerFileIO.ERROR(ex);
        }
        return rVal;
    }
    
    /**
     * Gets an sql script file as a string
     * @param pathName The path of the sql script
     * @return The file's contents as a string
     * @throws IOException Thrown if the engine fails to read the file
     */
    public static String getSQLScriptFileAsString(String pathName) throws IOException {
        String sanitizedFilePath = sanitizeFilePath(pathName);
        File targetFile = new File("./assets/DB/" + sanitizedFilePath);
        return Files.readString(targetFile.toPath());
    }
    
    /**
     * Loads an object from a save folder
     * @param <T> The type of the object
     * @param saveName The name of the save
     * @param pathName The path of the file containing the object json
     * @param className The class of the object
     * @return The object
     */
    public static <T>T loadObjectFromSavePath(String saveName, String pathName, Class<T> className){
        T rVal = null;
        String sanitizedFilePath = sanitizeFilePath(pathName);
        try {
            rVal = gson.fromJson(Files.newBufferedReader(getSaveFile(saveName,sanitizedFilePath).toPath()), className);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return rVal;
    }

    /**
     * Serializes an object to a save folder
     * @param saveName The name of the save
     * @param pathName The path within the save folder to the file
     * @param object The object to save
     */
    public static void serializeObjectToSavePath(String saveName, String pathName, Object object){
        String sanitizedFilePath = sanitizeFilePath(pathName);
        try {
            Files.write(getSaveFile(saveName,sanitizedFilePath).toPath(), gson.toJson(object).getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads a binary file as an array of bytes
     * @param saveName The save name
     * @param pathName The path within the save folder
     * @return The array of bytes
     */
    public static byte[] loadBinaryFromSavePath(String saveName, String pathName){
        byte[] rVal = null;
        String sanitizedFilePath = FileUtils.sanitizeFilePath(pathName);
        try {
            rVal = Files.readAllBytes(FileUtils.getSaveFile(saveName,sanitizedFilePath).toPath());
        } catch (IOException e) {
            LoggerInterface.loggerFileIO.ERROR(e);
        }
        return rVal;
    }

    /**
     * Gets a save files as an input stream
     * @param saveName The save name
     * @param pathName The path within the save folder
     * @return The input stream
     */
    public static InputStream getSavePathAsInputStream(String saveName, String pathName) throws IOException {
        String sanitizedFilePath = FileUtils.sanitizeFilePath(pathName);
        return Files.newInputStream(FileUtils.getSaveFile(saveName,sanitizedFilePath).toPath());
    }

    /**
     * Writes a binary file to a save folder's file
     * @param saveName The name of the save
     * @param pathName The path within the save folder
     * @param data The data to write
     */
    public static void saveBinaryToSavePath(String saveName, String pathName, byte[] data){
        String sanitizedFilePath = FileUtils.sanitizeFilePath(pathName);
        try {
            File file = FileUtils.getSaveFile(saveName,sanitizedFilePath);
            Files.createDirectories(file.getParentFile().toPath());
            Files.write(file.toPath(), data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Opens an output straem to a binary file in a save directory
     * @param saveName The save name
     * @param pathName The path name to the file
     * @return The output stream
     */
    public static OutputStream getBinarySavePathOutputStream(String saveName, String pathName) throws IOException {
        String sanitizedFilePath = FileUtils.sanitizeFilePath(pathName);
        File file = FileUtils.getSaveFile(saveName,sanitizedFilePath);
        Files.createDirectories(file.getParentFile().toPath());
        return Files.newOutputStream(file.toPath());
    }

    /**
     * Checks if a given file exists in a given save
     * @param saveName the name of the save
     * @param filePath The path to the file RELATIVE TO THE SAVE DIRECTORY
     * @return true if it exists, false otherwise
     */
    public static boolean checkSavePathExists(String saveName, String filePath){
        String sanitizedFilePath = FileUtils.sanitizeFilePath(filePath);
        return FileUtils.getSaveFile(saveName,sanitizedFilePath).exists();
    }
    
    /**
     * Checks if a directory exists
     * @param fileName
     * @return true if directory exists, false otherwise
     */
    public static boolean checkFileExists(String fileName){
        File targetDir = new File(FileUtils.sanitizeFilePath(fileName));
        if(targetDir.exists()){
            return true;
        } else {
            return false;
        }
    }
    
    
    /**
     * Trys to create a directory
     * @param directoryName
     * @return true if directory was created, false if it was not
     */
    public static boolean createDirectory(String directoryName){
        String sanitizedPath = sanitizeFilePath(directoryName);
        File targetDir = new File(sanitizedPath);
        if(targetDir.exists()){
            return false;
        } else {
            return targetDir.mkdirs();
        }
    }
    
    /**
     * Lists the files in a directory
     * @param directoryName The path of the directory
     * @return A list containing the names of all files inside that directory
     */
    public static List<String> listDirectory(String directoryName){
        List<String> rVal = new LinkedList<String>();
        String sanitizedPath = sanitizeFilePath(directoryName);
        File targetDir = new File(sanitizedPath);
        String[] files = targetDir.list();
        for(String name : files){
            rVal.add(name);
        }
        return rVal;
    }

    /**
     * Recursively deletes a path
     * @param path The path
     */
    public static void recursivelyDelete(String path){
        File file = new File(path);
        if(file.isDirectory()){
            for(File child : file.listFiles()){
                recursivelyDelete(child.getAbsolutePath());
            }
        }
        if(file.exists()){
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                LoggerInterface.loggerFileIO.ERROR(e);
            }
        }
    }

    /**
     * Writes a buffered image to a given path
     * @param image The image
     * @param path The path
     */
    public static void writeBufferedImage(BufferedImage image, String path){
        //write the buffered image out
        try {
            File outputFile = FileUtils.getAssetFile(path);
            if(!outputFile.getParentFile().exists()){
                outputFile.getParentFile().mkdirs();
            }
            if(!outputFile.exists()){
                outputFile.createNewFile();
            }
            ImageIO.write(image,"png",outputFile);
        } catch (IOException e) {
            LoggerInterface.loggerRenderer.ERROR(e);
        }
    }

    /**
     * Gets a file's path relative to a given directory
     * @param file The file
     * @param directory The directory
     * @return The relative path
     */
    public static String relativize(File file, File directory){
        return directory.toURI().relativize(file.toURI()).getPath();
    }
    
    

    /**
     * Computes the checksum of an object
     * @param object The object
     * @return The checksum
     * @throws IOException Thrown on io errors reading the file
     * @throws NoSuchAlgorithmException Thrown if MD5 isn't supported
     */
    public static String getChecksum(Serializable object) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(baos.toByteArray());
            StringBuffer builder = new StringBuffer();
            for(byte byteCurr : bytes){
                builder.append(String.format("%02x",byteCurr));
            }
            return builder.toString();
        } finally {
            oos.close();
            baos.close();
        }
    }

    /**
     * Writes a ByteBuffer to a file and compresses it
     * @param file The file
     * @param buff The buffer
     */
    public static void writeBufferToCompressedFile(File file, ByteBuffer buff) throws IOException {
        try (GZIPOutputStream outStream = new GZIPOutputStream(Files.newOutputStream(file.toPath()))){
            outStream.write(buff.array());
        }
    }

    /**
     * Writes a ByteBuffer to a file and compresses it
     * @param file The file
     * @param buff The buffer
     */
    public static ByteBuffer readBufferFromCompressedFile(File file) throws IOException {
        ByteBuffer buff = null;
        try (GZIPInputStream inStream = new GZIPInputStream(Files.newInputStream(file.toPath()))){
            byte[] bytes = inStream.readAllBytes();
            buff = ByteBuffer.allocate(bytes.length);
            buff.put(bytes);
            buff.flip();
        }
        return buff;
    }

    
    
}

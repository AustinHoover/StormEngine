package electrosphere.net.monitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;

import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.NetworkMessage;

public class NetMonitor {
    
    static final String NET_MONITOR_FOLDER = "./netmonitor";

    private Map<String,FileOutputStream> handleFileMap = new HashMap<String,FileOutputStream>();
    private Map<String,Boolean> writtenInitialMap = new HashMap<String,Boolean>();

    private Gson gson;

    public NetMonitor(){
        gson = new Gson();
    }


    /**
     * Registers a new connection object
     * @return Tracking handle to refer to the new connection object
     */
    public String registerConnection(){
        UUID uuid = UUID.randomUUID();
        String handle = uuid.toString();
        String filePath = NET_MONITOR_FOLDER + "/" + handle + ".pacap";
        try {
            File file = new File(filePath);
            Files.createDirectories(file.getParentFile().toPath());
            FileOutputStream fileStream = new FileOutputStream(file, false);
            handleFileMap.put(handle, fileStream);
            writtenInitialMap.put(handle,false);
            fileStream.write("{\"messages\":[".getBytes());
        } catch (IOException e) {
            LoggerInterface.loggerNetworking.ERROR("Can't open NetMonitor file", e);
        }
        return handle;
    }

    /**
     * Logs a message sent or received by the connection at the given handle
     * @param handle The handle of the connection
     * @param message The message that was sent or received
     * @param isIncoming True if the message was incoming into the connection object, false if outgoing
     */
    public void logMessage(String handle, NetworkMessage message, boolean isIncoming){
        long time = System.currentTimeMillis();
        LoggedMessage loggedMessage = new LoggedMessage(time,isIncoming,message);
        FileOutputStream outStream = handleFileMap.get(handle);
        if(outStream == null){
            throw new Error("Failed to find stream at handle " + handle);
        }
        String stringified = gson.toJson(loggedMessage);
        if(writtenInitialMap.get(handle) != null && writtenInitialMap.get(handle)){
                stringified = "," + stringified;
        } else {
            writtenInitialMap.put(handle, true);
        }
        try {
            outStream.write(stringified.getBytes());
        } catch (IOException e) {
            LoggerInterface.loggerNetworking.ERROR("Failed to log message to handle " + handle, e);
        }
    }

    /**
     * Closes down the NetMonitor and closes all files
     */
    public void close(){
        for(String key : handleFileMap.keySet()){
            FileOutputStream outStream = handleFileMap.get(key);
            if(outStream != null){
                try {
                    outStream.write("]}".getBytes());
                    outStream.close();
                } catch (IOException e) {
                    LoggerInterface.loggerNetworking.ERROR("NetMonitor failed to close file", e);
                }
            }
        }
    }



    static class LoggedMessage {
        long time;
        boolean isIncoming;
        NetworkMessage message;

        public LoggedMessage(long time, boolean isIncoming, NetworkMessage message){
            this.time = time;
            this.isIncoming = isIncoming;
            this.message = message;
        }
    }

}

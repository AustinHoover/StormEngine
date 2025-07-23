package electrosphere.net.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import electrosphere.logger.LoggerInterface;
import electrosphere.util.Utilities;

/**
 * A file that can be included in the root of the engine directory that
 * will preload inputs for the join game page
 */
public class NetConfig {
    
    /**
     * The address to connect to
     */
    String address;

    /**
     * The port to use
     */
    String port;

    /**
     * The username to use
     */
    String username;

    /**
     * The password to use
     */
    String password;


    /**
     * Gets the address to connect to
     * @return The address
     */
    public String getAddress(){
        return address;
    }

    /**
     * Gets the port to connect to
     * @return The port
     */
    public String getPort(){
        return port;
    }

    /**
     * Gets the username to use
     * @return The username
     */
    public String getUsername(){
        return username;
    }

    /**
     * Gets the password to use
     * @return The password
     */
    public String getPassword(){
        return password;
    }

    /**
     * Reads the net config file
     */
    public static NetConfig readNetConfig(){
        NetConfig rVal = null;
        File file = new File("./netconfig.json");
        if(file.exists()){
            try {
                rVal = Utilities.deserialize(Files.readString(file.toPath()), NetConfig.class);
            } catch (IOException e) {
                LoggerInterface.loggerFileIO.ERROR(e);
            }
        }
        return rVal;
    }

}

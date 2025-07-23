package electrosphere.net;

import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.Entity;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.EntityMessage;

/**
 * Utilities for dealing with the net
 */
public class NetUtils {
    
    /**
     * The default port
     */
    public static final int DEFAULT_PORT = 34251;
    
    /**
     * The port
     */
    static int port = DEFAULT_PORT;

    /**
     * The address
     */
    static String address = "localhost";
    
//    public static EntityMessage createSpawnEntityMessage(Entity e){
//        EntityMessage rVal = EntityMessage.constructCreateMessage(e.getId(), CreatureUtils.getCreatureType(e), 0.0f, 0.0f, 0.0f);
//        return rVal;
//    }
    
    public static EntityMessage createSetCreatureControllerIdEntityMessage(Entity e){
        LoggerInterface.loggerNetworking.DEBUG("[CLIENT] Entity " + e.getId() + " set controller id: " + CreatureUtils.getControllerPlayerId(e));
        EntityMessage rVal = EntityMessage.constructsetPropertyMessage(e.getId(), System.currentTimeMillis(), 0, CreatureUtils.getControllerPlayerId(e));
        return rVal;
    }

    /**
     * Gets the port
     * @return The port
     */
    public static int getPort() {
        return port;
    }

    /**
     * Gets the address
     * @return The address
     */
    public static String getAddress() {
        return address;
    }

    /**
     * Sets the port the program attempts to use
     * If the port is set to 0, and it is creating the server, it will create the socket with port 0.
     * This causes Java to find any available port. The Server class then re-calls setPort to update the static port with the found port.
     * @param port The port to use when doing any networking
     */
    public static void setPort(int port) {
        NetUtils.port = port;
    }

    /**
     * Sets the address
     * @param address The address
     */
    public static void setAddress(String address) {
        NetUtils.address = address;
    }
    
    
}

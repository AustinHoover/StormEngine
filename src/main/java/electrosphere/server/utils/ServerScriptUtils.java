package electrosphere.server.utils;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.server.datacell.Realm;

/**
 * Utility functions for dealing with scripts from the server
 */
public class ServerScriptUtils {

    /**
     * Fires a signal on an entity
     * @param entity The entity
     * @param signal The signal
     * @param args The args provided with the signal
     */
    public static void fireSignalOnEntity(Entity entity, String signal, Object ... args){
        Realm entityRealm = Globals.serverState.realmManager.getEntityRealm(entity);

        Object finalArgs[] = new Object[args.length + 1];
        finalArgs[0] = entity.getId();
        for(int i = 0; i < args.length; i++){
            finalArgs[i+1] = args[i];
        }
        entityRealm.fireSignal(signal, finalArgs);
    }
    
}

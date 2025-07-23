package electrosphere.engine.signal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A signal
 */
public class Signal {
    
    /**
     * A type of signal
     */
    public static enum SignalType {

        //
        //CORE ENGINE
        //
        ENGINE_SHUTDOWN,
        ENGINE_RETURN_TO_TITLE,
        ENGINE_SYNCHRONOUS_CODE,

        //
        //RENDERING
        //
        RENDERING_ENGINE_READY,

        //
        //UI
        //
        YOGA_APPLY,
        YOGA_APPLY_ROOT,
        YOGA_DESTROY,
        UI_MODIFICATION,

        //
        //Terrain
        //
        REQUEST_CHUNK,
        CHUNK_CREATED,
        REQUEST_CHUNK_EDIT,
        CHUNK_EDITED,

        //
        //Script
        //
        SCRIPT_RECOMPILE,

    }

    /**
     * Iterator for getting IDs for new signal
     */
    private static AtomicInteger signalIterator = new AtomicInteger(0);

    /**
     * The id of the signal
     */
    int id;

    /**
     * The type of the signal
     */
    SignalType type;

    /**
     * An (optional) data supplied with the signal
     */
    Object data;

    /**
     * Creates a signal
     * @param type The type of signal
     * @param data The data associated with the signal
     * @return The signal
     */
    public static Signal create(SignalType type, Object data){
        Signal rVal = new Signal();
        rVal.type = type;
        rVal.data = data;
        rVal.id = signalIterator.addAndGet(1);
        return rVal;
    }

    /**
     * Creates a signal
     * @param type The type of signal
     * @return The signal
     */
    public static Signal create(SignalType type){
        return Signal.create(type,null);
    }

    /**
     * Gets the id of the signal
     * @return The id
     */
    public int getId(){
        return this.id;
    }

    /**
     * Gets the type of the signal
     * @return The type
     */
    public SignalType getType(){
        return this.type;
    }

    /**
     * Gets the data associated with the signal
     * @return The data
     */
    public Object getData(){
        return this.data;
    }

    

}

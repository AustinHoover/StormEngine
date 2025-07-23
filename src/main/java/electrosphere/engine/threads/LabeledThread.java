package electrosphere.engine.threads;

/**
 * A thread with an associated label
 */
public class LabeledThread {

    /**
     * The label associated with the thread
     */
    public static enum ThreadLabel {
        /**
         * The server socket networking thread
         */
        NETWORKING_SERVER,
        /**
         * The client networking thread
         */
        NETWORKING_CLIENT,
        /**
         * The main asset loading thread
         */
        ASSET_LOADING,
        /**
         * The main loading thread
         */
        LOADING,
    }
    
    /**
     * The label
     */
    ThreadLabel label;

    /**
     * The actual thread
     */
    Thread thread;

    /**
     * Constructor
     * @param label
     * @param thread
     */
    public LabeledThread(ThreadLabel label, Thread thread){
        this.label = label;
        this.thread = thread;
    }

    /**
     * Gets the label
     * @return The label
     */
    public ThreadLabel getLabel(){
        return label;
    }

    /**
     * Gets the thread
     * @return The thread
     */
    public Thread getThread(){
        return thread;
    }

}

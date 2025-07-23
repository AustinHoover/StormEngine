package electrosphere.engine.os;

/**
 * Information about the operating system
 */
public class OSData {

    /**
     * A type of operating system
     */
    public static enum OSType {
        /**
         * Windows
         */
        WINDOWS,
        /**
         * Linux
         */
        LINUX,
    }
    
    /**
     * The name of the operating system
     */
    String osString;

    /**
     * The type of the operating system
     */
    OSType osType;

    /**
     * Gets the OS string
     * @return The OS string
     */
    public String getOsString() {
        return osString;
    }

    /**
     * Sets the OS string
     * @param osString The OS string
     */
    public void setOsString(String osString) {
        this.osString = osString;
    }

    /**
     * Gets the operating system type
     * @return The operating system type
     */
    public OSType getOSType() {
        return osType;
    }

    /**
     * Sets the operating system type
     * @param osType The operating system type
     */
    public void setOSType(OSType osType) {
        this.osType = osType;
    }
    
    /**
     * Constructor
     */
    public OSData(){
        this.osString = System.getProperty("os.name");
        if(this.osString.startsWith("Windows")){
            this.osType = OSType.WINDOWS;
        } else {
            this.osType = OSType.LINUX;
        }
    }


}

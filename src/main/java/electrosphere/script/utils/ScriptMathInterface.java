package electrosphere.script.utils;

import org.graalvm.polyglot.HostAccess.Export;

/**
 * Script access to specific math functions
 */
public class ScriptMathInterface {
    
    /**
     * Power function
     * @param val1 The number
     * @param val2 The exponent
     * @return The power
     */
    @Export
    public static double pow(double val1, double val2){
        return Math.pow(val1,val2);
    }

}

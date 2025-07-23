package electrosphere.util;

/**
 * Utilities for making devving faster
 */
public class CodeUtils {
    
    /**
     * Used as placeholder when haven't implemented error handling yet
     * @param e The exception
     * @param explanation Any kind of explanation string. Ideally explains what the case is
     */
    public static void todo(Exception e, String explanation){
        System.out.println("TODO: handle exception");
        System.out.println(explanation);
        e.printStackTrace();
    }

}

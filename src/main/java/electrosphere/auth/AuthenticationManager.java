package electrosphere.auth;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.db.DatabaseResult;
import electrosphere.server.db.DatabaseResultRow;

public class AuthenticationManager {
    
    /**
     * Tracks whether this is a mock authentication manager or not
     */
    private boolean isMock = false;

    /**
     * An invalid login
     */
    public static final int INVALID_LOGIN = -1;

    /**
     * ID for a mock login
     */
    public static final int MOCK_LOGIN = 0;

    /**
     * Private constructor
     */
    private AuthenticationManager(){

    }

    /**
     * Creates an authentication manager
     * @param mock true if this shoud be a mock manager, false for a real one
     * @return The authentication manager
     */
    public static AuthenticationManager create(boolean mock){
        AuthenticationManager rVal = new AuthenticationManager();
        rVal.isMock = mock;
        return rVal;
    }

    /**
     * Authenticates the player
     * @param username The username of the player
     * @param password The password of the player
     * @return The id of the player if they logged in successfully, INVALID_LOGIN if the authentication failed
     */
    public int authenticate(String username, String password){
        if(isMock){
            return MOCK_LOGIN;
        }
        //first we hash the input password
        String hashedPassword = getHashedString(password);
        //then query the database for the username and hash for the input username
        DatabaseResult result = Globals.serverState.dbController.executePreparedQuery("SELECT id, username, pwdhash FROM accounts WHERE username=?;",username);
        if(result.hasResult()){
            boolean foundRow = false;
            //if we get a valid response from the database, check that it actually matches hashes
            for(DatabaseResultRow row : result){
                foundRow = true;
                String pwdhash = row.getAsString("pwdhash");
                if(pwdhash.equals(hashedPassword)){
                    LoggerInterface.loggerAuth.INFO("Authenticated user " + username);
                    return row.getAsInteger("id");
                }
            }
            //If we didn't find a single account, go ahead and create it
            if(!foundRow){
                LoggerInterface.loggerAuth.INFO("Created user " + username);
                Globals.serverState.dbController.executePreparedStatement("INSERT INTO accounts (username, pwdhash) VALUES(?, ?);",username,hashedPassword);
                
                //verify the account was created
                result = Globals.serverState.dbController.executePreparedQuery("SELECT id, username, pwdhash FROM accounts WHERE username=?;",username);
                if(result.hasResult()){
                    foundRow = false;
                    //if we get a valid response from the database, check that it actually matches hashes
                    for(DatabaseResultRow row : result){
                        foundRow = true;
                        String pwdhash = row.getAsString("pwdhash");
                        if(pwdhash.equals(hashedPassword)){
                            LoggerInterface.loggerAuth.INFO("Authenticated user " + username);
                            return row.getAsInteger("id");
                        }
                    }
                }
            }
        }
        LoggerInterface.loggerAuth.INFO("Failed to authenticate user " + username);
        return INVALID_LOGIN;
    }

    static final int saltLength = 16;
    public static String getHashedString(String input){
        String rVal = "";

        if(input == "" || input == null){
            input = "asdf";
        }

        //generate salt
        char[] charArray = input.toCharArray();
        byte[] salt = new byte[saltLength];
        for(int i = 0; i < saltLength; i++){
            if(i < charArray.length){
                salt[i] = (byte)charArray[i];
            } else {
                salt[i] = (byte)i;
            }
        }

        //perform hash
        KeySpec spec = new PBEKeySpec(charArray, salt, 65536, 512);
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] hash = f.generateSecret(spec).getEncoded();
            Base64.Encoder enc = Base64.getEncoder();
            // System.out.printf("salt: %s%n", enc.encodeToString(salt));
            // System.out.printf("hash: %s%n", enc.encodeToString(hash));
            // System.out.println(Arrays.toString(hash));
            rVal = enc.encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            LoggerInterface.loggerAuth.ERROR("NoSuchAlgorithmException in hash string", e);
        } catch (InvalidKeySpecException e) {
            LoggerInterface.loggerAuth.ERROR("InvalidKeySpecException in hash string", e);
        }
        
        return rVal;
    }

}

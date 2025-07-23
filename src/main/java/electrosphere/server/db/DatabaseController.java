package electrosphere.server.db;

import electrosphere.logger.LoggerInterface;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Properties;

/**
 * A controller for database logic
 */
public class DatabaseController {

    /**
     * file extension for allowed database files to open
     */
    public static final String FILE_EXT = ".sqlite";

    /**
     * The in-memory path for database connections
     */
    public static final String IN_MEMORY_PATH = ":memory:";
    
    /**
     * The database connection
     */
    Connection conn;
    
    /**
     * Constructor
     */
    public DatabaseController(){ }
    
    /**
     * Connects to a database
     * @param path The path to the database
     */
    public void connect(String path){
        String dbms = "sqlite";
        Properties connectionProps = new Properties();
        String fullAddress = "jdbc:" + dbms + ":" + path;
        try {
            conn = DriverManager.getConnection(fullAddress, connectionProps);
        } catch (SQLException ex) {
            String message = "" +
            "Failure to connect to db\n" +
            ex.getMessage() +
            "";
            throw new Error(message);
        }
    }

    /**
     * Executes a write statement to the database
     * @param statementRaw The raw string for the statement
     * @param arguments The arguments to be inserted into the raw sql
     * @return true if there is a result set, false if there is an update count or no result
     */
    public boolean executePreparedStatement(String statementRaw, Object...arguments){
        if(conn == null){
            throw new Error("Connection not initialized!");
        }
        try {
            PreparedStatement statement = conn.prepareStatement(statementRaw);
            //Set arguments for prepared statements
            int argumentIndex = 1;
            for(Object currentArg : arguments){
                if(currentArg instanceof String){
                    statement.setString(argumentIndex, (String)currentArg);
                } else if(currentArg instanceof Integer){
                    statement.setInt(argumentIndex, (int)currentArg);
                } else if(currentArg instanceof Float){
                    statement.setFloat(argumentIndex, (float)currentArg);
                } else if(currentArg instanceof Boolean){
                    statement.setBoolean(argumentIndex, (boolean)currentArg);
                } else if(currentArg instanceof Long){
                    statement.setLong(argumentIndex, (long)currentArg);
                } else if(currentArg instanceof Double){
                    statement.setDouble(argumentIndex, (double)currentArg);
                }
                argumentIndex++;
            }
            //actually execute
            boolean result = statement.execute();
            SQLWarning warning = statement.getWarnings();
            if(warning != null){
                LoggerInterface.loggerDB.WARNING(warning.toString());
            }
            statement.close();
            return result;
        } catch (SQLException ex) {
            LoggerInterface.loggerFileIO.ERROR("SQL query execution error", ex);
        }
        return false;
    }

    /**
     * Executes a query against the database
     * @param statementRaw The raw sql
     * @param arguments The arguments to be injected into the raw sql
     * @return A DatabaseResult representing the results of the query, or null if there was no result
     */
    public DatabaseResult executePreparedQuery(String statementRaw, Object...arguments){
        DatabaseResult rVal = DatabaseResult.createQuery(statementRaw);
        if(conn == null){
            throw new Error("Connection not initialized!");
        }
        try {
            PreparedStatement statement = conn.prepareStatement(statementRaw);

            //Set arguments for prepared statements
            int argumentIndex = 1;
            for(Object currentArg : arguments){
                if(currentArg instanceof String){
                    statement.setString(argumentIndex, (String)currentArg);
                } else if(currentArg instanceof Integer){
                    statement.setInt(argumentIndex, (int)currentArg);
                } else if(currentArg instanceof Float){
                    statement.setFloat(argumentIndex, (float)currentArg);
                } else if(currentArg instanceof Boolean){
                    statement.setBoolean(argumentIndex, (boolean)currentArg);
                } else if(currentArg instanceof Long){
                    statement.setLong(argumentIndex, (long)currentArg);
                } else if(currentArg instanceof Double){
                    statement.setDouble(argumentIndex, (double)currentArg);
                }
                argumentIndex++;
            }
            //actually execute
            ResultSet results = statement.executeQuery();
            SQLWarning warning = results.getWarnings();
            if(warning != null){
                LoggerInterface.loggerDB.WARNING(warning.toString());
            }
            warning = statement.getWarnings();
            if(warning != null){
                LoggerInterface.loggerDB.WARNING(warning.toString());
            }
            if(results != null){
                rVal.addResultSet(results);
            }
        } catch (SQLException ex) {
            rVal.succeeded = false;
            rVal.hasResultSet = false;
            LoggerInterface.loggerFileIO.ERROR("SQL query execution error", ex);
        }
        return rVal;
    }
    
    /**
     * Checks of the connection is actually connected
     * @return true if connected, false otherwise
     */
    public boolean isConnected(){
        boolean rVal = false;
        if(conn == null){
            return false;
        }
        try {
            rVal = conn.isValid(100);
        } catch (SQLException ex) {
            LoggerInterface.loggerFileIO.ERROR("SQL error validating status of db", ex);
        }
        return rVal;
    }
    
    /**
     * Disconnects from the database
     */
    public void disconnect(){
        try {
            if(this.conn != null){
                conn.close();
            }
        } catch (SQLException ex) {
            LoggerInterface.loggerEngine.ERROR("Error disconnecting from DB", ex);
        }
    }
    
}

package electrosphere.server.db;

import com.google.gson.Gson;
import electrosphere.logger.LoggerInterface;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Result of a single database query
 */
public class DatabaseResult implements Iterable<DatabaseResultRow> {

    /**
     * The deserializer for the data
     */
    private static Gson deserializer;
    
    /**
     * Init deserializer
     */
    static {
        deserializer = new Gson();
    }

    /**
     * True if this was a query
     */
    boolean isQuery = false;

    /**
     * True if this was a statement
     */
    boolean isStatement = false;

    /**
     * True if the request succeeded, false otherwise
     */
    boolean succeeded = false;

    /**
     * True if the result has data, false otherwise
     */
    boolean hasResultSet = false;

    /**
     * The code for the query
     */
    String code;

    /**
     * The raw result data
     */
    ResultSet rs;
    
    /**
     * Creates a query
     * @param code The code for the query
     * @return The results of the query
     */
    protected static DatabaseResult createQuery(String code){
        DatabaseResult rVal = new DatabaseResult();
        rVal.isQuery = true;
        rVal.code = code;
        return rVal;
    }
    
    /**
     * Creates a statement
     * @param code The code for the statement
     * @return The results of the statement
     */
    protected static DatabaseResult createStatement(String code){
        DatabaseResult rVal = new DatabaseResult();
        rVal.isStatement = true;
        rVal.code = code;
        return rVal;
    }
    
    protected void addResultSet(ResultSet rs){
        hasResultSet = true;
        this.rs = rs;
    }
    
    protected ResultSet getResultSet(){
        return rs;
    }
    
    public boolean hasResult(){
        return hasResultSet;
    }
    
    public boolean isQuery(){
        return isQuery;
    }
    
    public boolean isStatement(){
        return isStatement;
    }
    
    public boolean success(){
        return succeeded;
    }
    
    public <T>T deserializeFirstResult(Class<T> className){
        T rVal = null;
        try {
            rs.getString(0);
            rVal = deserializer.fromJson(rs.getString(0), className);
        } catch (SQLException ex) {
            LoggerInterface.loggerFileIO.ERROR("Failure to deserialize result", ex);
        }
        return rVal;
    }

    @Override
    public DatabaseResultIterator iterator() {
        return new DatabaseResultIterator(rs);
    }
}

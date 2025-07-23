package electrosphere.server.db;

import java.util.HashMap;
import java.util.Map;

/**
 * A row result from a database operation
 */
public class DatabaseResultRow {

    /**
     * stores all the values of the row in an easily indexible-by-column-name format
     */
    Map<String,Object> values = new HashMap<String,Object>();

    /**
     * Used internally for putting values into the row
     * @param columnName
     * @param value
     */
    protected void putValue(String columnName, Object value){
        values.put(columnName,value);
    }

    /**
     * Gets the given value
     * @param columnName the name of the column to check
     * @return the value on said column
     */
    public Object getValue(String columnName){
        return values.get(columnName);
    }

    /**
     * Gets the given value as a string
     * @param columnName the name of the column to check
     * @return the value on said column as a string
     */
    public String getAsString(String columnName){
        return (String)values.get(columnName);
    }

    /**
     * Gets the given value as an integer
     * @param columnName the name of the column to check
     * @return the value on said column as an integer
     */
    public int getAsInteger(String columnName){
        return (Integer)values.get(columnName);
    }

    /**
     * Gets the given value as a long
     * @param columnName the name of the column to check
     * @return the value on said column as a long
     */
    public long getAsLong(String columnName){
        return (Long)values.get(columnName);
    }

    /**
     * Gets the given value as a float
     * @param columnName the name of the column to check
     * @return the value on said column as a float
     */
    public float getAsFloat(String columnName){
        return (Float)values.get(columnName);
    }

    /**
     * Gets the given value as a double
     * @param columnName the name of the column to check
     * @return the value on said column as a double
     */
    public double getAsDouble(String columnName){
        return (Double)values.get(columnName);
    }

}

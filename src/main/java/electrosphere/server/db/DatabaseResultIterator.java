package electrosphere.server.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import electrosphere.logger.LoggerInterface;

/**
 * Iterates through a database result
 */
public class DatabaseResultIterator implements Iterator<DatabaseResultRow> {

    /**
     * The result set
     */
    ResultSet rs;
    
    /**
     * The metadata of the result set
     */
    ResultSetMetaData metadata;

    /**
     * The type list of each column
     */
    List<Integer> typeList;
    
    /**
     * Creates a result iterator -- used internal to package
     * @param result
     */
    protected DatabaseResultIterator(ResultSet result){
        this.rs = result;
        try {
            this.metadata = this.rs.getMetaData();
            int columnCount = metadata.getColumnCount();
            this.typeList = new LinkedList<Integer>();
            for(int i = 0; i < columnCount; i++){
                //result sets are indexed starting at 1
                this.typeList.add(metadata.getColumnType(i+1));
            }
            //the sqlite driver doesn't support reverse navigation unfortunatelly
            //if it did, we'd call this to be explicitly clear where we want to start
            //instead the assumption is it always starts ON the first element
            // this.rs.first();

            //starts before the first, so must iterate once into the result set in order to not double-sample the first row
            if(this.rs.isBeforeFirst()){
                this.rs.next();
            }
        } catch (SQLException e) {
            LoggerInterface.loggerEngine.ERROR("SQL Exception", e);
        }

    }

    /**
     * Lets us know if the result has a next value
     */
    @Override
    public boolean hasNext() {
        try {
            boolean rVal = !rs.isAfterLast();
            if(!rVal && rs != null){
                rs.close();
            }
            return rVal;
        } catch (SQLException e) {
            LoggerInterface.loggerEngine.ERROR("Critical failure in DatabaseResultIterator", e);
            return false;
        }
    }

    /**
     * Gets the next value in the result
     */
    @Override
    public DatabaseResultRow next() {
        DatabaseResultRow row = new DatabaseResultRow();
        int columnIncrementer = 0;
        //basically go through each type and add it to the row object that we return
        //the types are stored in the typeList field on this object when it is created
        try {
            for(int type : typeList){
                //increment at the beginning because result sets are indexed starting at 1
                columnIncrementer++;
                switch(type){
                    case Types.INTEGER: {
                        row.putValue(metadata.getColumnName(columnIncrementer), rs.getInt(columnIncrementer));
                    } break;
                    case Types.VARCHAR: {
                        row.putValue(metadata.getColumnName(columnIncrementer), rs.getString(columnIncrementer));
                    } break;
                    case Types.BIGINT: {
                        row.putValue(metadata.getColumnName(columnIncrementer), rs.getLong(columnIncrementer));
                    } break;
                    case Types.FLOAT: {
                        row.putValue(metadata.getColumnName(columnIncrementer), rs.getFloat(columnIncrementer));
                    } break;
                    case Types.DOUBLE: {
                        row.putValue(metadata.getColumnName(columnIncrementer), rs.getDouble(columnIncrementer));
                    } break;
                    case Types.REAL: {
                        row.putValue(metadata.getColumnName(columnIncrementer), rs.getDouble(columnIncrementer));
                    } break;
                    default:
                    LoggerInterface.loggerEngine.WARNING("Unsupported type from database in DatabaseResultIterator " + type);
                    break;
                }
            }
            //make sure to increment the result set so we don't infinitely loop the first element
            this.rs.next();
        } catch (SQLException e){
            LoggerInterface.loggerEngine.ERROR("Unhandled SQL exception", e);
        }
        return row;
    }
    
}

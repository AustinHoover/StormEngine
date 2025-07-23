package electrosphere.renderer.ui.events;

/**
 * An event raised when an element changes an internal value
 */
public class ValueChangeEvent implements Event {

    /**
     * The type of the value changed
     */
    public static enum ValueType {
        STRING,
        FLOAT,
        BOOLEAN,
    }

    /**
     * The string value
     */
    String valueString;
    
    /**
     * The float value
     */
    float valueFloat;

    /**
     * The boolean value
     */
    boolean valueBoolean;

    /**
     * The type of this event
     */
    ValueType valueType;

    /**
     * Constructor for string value changes
     * @param value The string
     */
    public ValueChangeEvent(String value){
        valueString = value;
        valueType = ValueType.STRING;
    }

    /**
     * Constructor for float value changes
     * @param value The float
     */
    public ValueChangeEvent(float value){
        valueFloat = value;
        valueType = ValueType.FLOAT;
    }

    /**
     * Constructor for boolean value changes
     * @param value The boolean
     */
    public ValueChangeEvent(boolean value){
        valueBoolean = value;
        valueType = ValueType.BOOLEAN;
    }

    /**
     * Gets the type of the value
     * @return The type of the value
     */
    public ValueType getType(){
        return valueType;
    }

    /**
     * Gets the value that changed as a float
     * @return The float value
     */
    public float getAsFloat(){
        return valueFloat;
    }

    /**
     * Gets the value that changed as a string
     * @return The string value
     */
    public String getAsString(){
        return valueString;
    }

    /**
     * Gets the value that changed as a boolean
     * @return The boolean value
     */
    public boolean getAsBoolean(){
        return valueBoolean;
    }

}

package electrosphere.entity.types.creature;

import java.util.HashMap;
import java.util.Map;

import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.visualattribute.VisualAttribute;
import electrosphere.engine.Globals;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.net.synchronization.transport.StateCollection;

/**
 * The template used to construct the object on the client
 */
public class ObjectTemplate {

    /**
     * The type of the object
     */
    String objectType;

    /**
     * The category of entity
     */
    EntityType entityType;
    
    /**
     * The attribute map for visual variants
     */
    private Map<String,TemplateAttributeValue> attributeMap = new HashMap<String,TemplateAttributeValue>();

    /**
     * Data about the inventory of the object
     */
    private ObjectInventoryData inventoryData = new ObjectInventoryData();

    /**
     * The collection of synchronized values
     */
    private StateCollection stateCollection;

    /**
     * Creates the object template
     * @param category The category of entity
     * @param subtype The type of object
     * @return The object template
     */
    public static ObjectTemplate create(EntityType category, String subtype){
        ObjectTemplate rVal = new ObjectTemplate();
        rVal.entityType = category;
        rVal.objectType = subtype;
        return rVal;
    }

    /**
     * Puts an attribute value in the template
     * @param attributeName The name of the attribute
     * @param value The value of the attribute
     */
    public void putAttributeValue(String attributeName, float value){
        attributeMap.put(attributeName,new TemplateAttributeValue(value));
    }

    /**
     * Puts an attribute value into the template
     * @param attributeName The name of the attribute
     * @param value The value of the attribute
     */
    public void putAttributeValue(String attributeName, String value){
        attributeMap.put(attributeName,new TemplateAttributeValue(value));
    }

    /**
     * Gets the value of an attribte
     * @param attributeName The name of the attribute
     * @return The value of the attribute
     */
    public TemplateAttributeValue getAttributeValue(String attributeName){
        return attributeMap.get(attributeName);
    }

    /**
     * Gets the type of the object
     * @return The type of the object
     */
    public String getObjectType(){
        return objectType;
    }

    /**
     * Gets the inventory data for the object
     * @return The inventory data
     */
    public ObjectInventoryData getInventoryData(){
        return this.inventoryData;
    }

    /**
     * Gets the state collection for the object
     * @return The collection of synchronized values
     */
    public StateCollection getStateCollection(){
        return this.stateCollection;
    }

    /**
     * Sets the synchronized values for this object
     * @param stateCollection The synchronized values
     */
    public void setStateCollection(StateCollection stateCollection){
        this.stateCollection = stateCollection;
    }

    /**
     * Creates a template for the object with default values
     * @param objectType The type of object
     * @return The basic template
     */
    public static ObjectTemplate createDefault(EntityType category, String objectType){
        ObjectTemplate storedTemplate = ObjectTemplate.create(category, objectType);
        if(category == EntityType.CREATURE){
            CreatureData rawType = Globals.gameConfigCurrent.getCreatureTypeLoader().getType(objectType);
            if(rawType.getVisualAttributes() != null){
                for(VisualAttribute attributeType : rawType.getVisualAttributes()){
                    if(attributeType.getType().equals("remesh")){
                        if(attributeType.getVariants() != null && attributeType.getVariants().size() > 0){
                            //make sure stored template contains creature data
                            if(storedTemplate.getAttributeValue(attributeType.getAttributeId())==null){
                                storedTemplate.putAttributeValue(attributeType.getAttributeId(), attributeType.getVariants().get(0).getId());
                            }
                        }
                    }
                    if(attributeType.getType().equals("bone")){
                        //make sure stored template contains creature data
                        float midpoint = (attributeType.getMaxValue() - attributeType.getMinValue())/2.0f + attributeType.getMinValue();
                        storedTemplate.putAttributeValue(attributeType.getAttributeId(), midpoint);
                    }
                }
            }
        }
        return storedTemplate;
    }

    /**
     * A visual attribute of an object (ie how wide is their nose, what type of hairstyle do they have, etc)
     */
    public class TemplateAttributeValue {

        /**
         * The string value of the attribute
         */
        String variantId;

        /**
         * The float value of the attribute
         */
        float value;

        /**
         * Creates a float attribute
         * @param value The value
         */
        public TemplateAttributeValue(float value){
            this.value = value;
        }

        /**
         * Creates a string attribute
         * @param variantId The string
         */
        public TemplateAttributeValue(String variantId){
            this.variantId = variantId;
        }

        /**
         * Gets the float value of the attribute
         * @return The value
         */
        public float getValue(){
            return value;
        }

        /**
         * Gets the string value of the attribute
         * @return The string value
         */
        public String getVariantId(){
            return variantId;
        }

        /**
         * Sets the float value of the attribute
         * @param value The float value
         */
        public void setValue(float value){
            this.value = value;
        }

        /**
         * Sets the string value of the attribute
         * @param variantId The string value
         */
        public void setVariantId(String variantId){
            this.variantId = variantId;
        }

    }

}

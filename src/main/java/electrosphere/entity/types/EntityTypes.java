package electrosphere.entity.types;

/**
 * Types of entities
 */
public class EntityTypes {
    

    /**
     * <p> The distinct types of entities </p>
     * The non-common entity types have a unique creation flow
     */
    public static enum EntityType {
        /**
         * The common entity type
         */
        COMMON(0),
        /**
         * A creature
         */
        CREATURE(1),
        /**
         * An item
         */
        ITEM(2),
        /**
         * A piece of foliage
         */
        FOLIAGE(3),
        /**
         * Special engine-created entities (ie not defined in a json file)
         */
        ENGINE(4),
        ;

        /**
         * the value of the enum
         */
        private final int value;

        /**
         * Constructor
         * @param newValue The value
         */
        private EntityType(final int newValue){
            value = newValue;
        }

        /**
         * Gets the value of the enum
         * @return The value
         */
        public int getValue(){
            return value;
        }

    }

    /**
     * Gets an entity type from a value
     * @param value The value
     * @return The corresponding entity type
     */
    public static EntityType fromInt(int value){
        switch(value){
            case 0:
            return EntityType.COMMON;
            case 1:
            return EntityType.CREATURE;
            case 2:
            return EntityType.ITEM;
            case 3:
            return EntityType.FOLIAGE;
        }
        throw new IllegalArgumentException("Trying to get invalid entity type! " + value);
    }
    
}

package electrosphere.server.macro.character.goal;

import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.data.CharacterData;
import electrosphere.server.macro.character.data.CharacterDataStrings;
import electrosphere.util.annotation.Exclude;

/**
 * Utilities for working with goals on macro characters
 */
public class CharacterGoal extends CharacterData {
    
    /**
     * A specific type of goal
     */
    public enum CharacterGoalType {
        /**
         * Goals is generally to leave simulation range
         */
        LEAVE_SIM_RANGE,
        /**
         * Build a structure
         */
        BUILD_STRUCTURE,
        /**
         * Acquire an item
         */
        ACQUIRE_ITEM,
        /**
         * Move to a macro structure
         */
        MOVE_TO_MACRO_STRUCT,
    }

    /**
     * The type of goal
     */
    CharacterGoalType type;

    /**
     * The target
     */
    @Exclude
    Object target;

    /**
     * Constructor
     * @param type The type of goal
     */
    public CharacterGoal(CharacterGoalType type){
        super(CharacterDataStrings.ENTITY_GOAL);
        this.type = type;
    }

    /**
     * Constructor
     * @param type The type of goal
     * @param target The target of the goal
     */
    public CharacterGoal(CharacterGoalType type, Object target){
        super(CharacterDataStrings.ENTITY_GOAL);
        this.type = type;
        this.setTarget(target);
    }

    /**
     * Gets the type of goal that this is
     * @return The type
     */
    public CharacterGoalType getType(){
        return type;
    }

    /**
     * Sets the target of this tree
     * @param target The target
     */
    public void setTarget(Object target){
        this.target = target;
    }

    /**
     * Gets the target of the goal
     * @return The target of the goal
     */
    public Object getTarget(){
        return this.target;
    }

    /**
     * Sets the goal on a character
     * @param character The character
     * @param goal The goal
     */
    public static void setCharacterGoal(Character character, CharacterGoal goal){
        character.putData(CharacterDataStrings.ENTITY_GOAL, goal);
    }

    /**
     * Sets the goal on a character
     * @param character The character
     * @return true if the character has a goal, false otherwise
     */
    public static boolean hasCharacterGoal(Character character){
        return character.containsKey(CharacterDataStrings.ENTITY_GOAL);
    }

    /**
     * Gets the goal of the character
     * @param character The character
     * @return The goal if it exists, null otherwise
     */
    public static CharacterGoal getCharacterGoal(Character character){
        return (CharacterGoal)character.getData(CharacterDataStrings.ENTITY_GOAL);
    }

}

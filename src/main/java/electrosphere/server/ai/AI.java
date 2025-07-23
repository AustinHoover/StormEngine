package electrosphere.server.ai;

import java.util.List;

import electrosphere.data.entity.creature.ai.AITreeData;
import electrosphere.data.entity.creature.ai.AttackerTreeData;
import electrosphere.data.entity.creature.ai.BlockerTreeData;
import electrosphere.data.entity.creature.ai.StandardCharacterTreeData;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.trees.character.StandardCharacterTree;
import electrosphere.server.ai.trees.creature.AttackerAITree;
import electrosphere.server.ai.trees.hierarchy.MaslowTree;
import electrosphere.server.ai.trees.test.BlockerAITree;

/**
 * A collection of AITrees that are attached to a single entity.
 * Maintains which 
 */
public class AI {

    /**
     * The entity this ai is associated with
     */
    private Entity parent;

    /**
     * The root node of the behavior tree
     */
    private AITreeNode rootNode;

    /**
     * The blackboard for the tree
     */
    private Blackboard blackboard = new Blackboard();

    /**
     * Tracks whether this should apply even if there is a controlling player
     */
    private boolean applyToPlayer = false;

    /**
     * The status of the ai
     */
    private String status = "Idle";

    /**
     * Constructs an AI from a list of trees that should be present on the ai
     * @param treeData The list of data on trees to be provided
     * @return The AI
     */
    protected static AI constructAI(Entity parent, List<AITreeData> treeData){
        AI rVal = new AI(parent);

        //attach all trees
        for(AITreeData aiData : treeData){
            switch(aiData.getName()){
                case BlockerAITree.TREE_NAME: {
                    rVal.rootNode = BlockerAITree.create((BlockerTreeData) aiData);
                } break;
                case AttackerAITree.TREE_NAME: {
                    rVal.rootNode = AttackerAITree.create((AttackerTreeData) aiData);
                } break;
                case MaslowTree.TREE_NAME: {
                    rVal.rootNode = MaslowTree.create();
                } break;
                case StandardCharacterTree.TREE_NAME: {
                    rVal.rootNode = StandardCharacterTree.create((StandardCharacterTreeData) aiData);
                } break;
                default: {
                    LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException("Trying to construct ai tree with undefined data type! " + aiData.getName()));
                } break;
            }
        }

        return rVal;
    }

    /**
     * Private constructor
     */
    private AI(Entity parent){
        this.parent = parent;
    }

    /**
     * Simulates a single frame of this ai. Only runs simulation logic for the highest priority tree
     */
    protected void simulate(){
        if(this.shouldExecute()){
            this.rootNode.evaluate(this.parent, this.blackboard);
        }
    }

    /**
     * Sets the ai key on the entity
     * @param entity The entity
     * @param ai The ai
     */
    public static void setAI(Entity entity, AI ai){
        entity.putData(EntityDataStrings.AI, ai);
    }

    /**
     * Gets the ai on the key of the entity
     * @param entity The entity
     * @return The AI if it exists, null otherwise
     */
    public static AI getAI(Entity entity){
        if(entity.containsKey(EntityDataStrings.AI)){
            return (AI)entity.getData(EntityDataStrings.AI);
        } else {
            return null;
        }
    }

    /**
     * Gets the parent entity of this AI
     * @return The parent entity
     */
    public Entity getParent(){
        return this.parent;
    }

    /**
     * Gets the root node of the behavior tree
     * @return The root node
     */
    public AITreeNode getRootNode(){
        return this.rootNode;
    }

    /**
     * Checks if the ai should simulate or not
     * @return true if should simulate, false otherwise
     */
    public boolean shouldExecute(){
        return this.applyToPlayer || !CreatureUtils.hasControllerPlayerId(this.parent);
    }

    /**
     * Sets the status of the ai
     * @param status The status
     */
    public void setStatus(String status){
        this.status = status;
    }

    /**
     * Gets the status of the ai
     * @return The status
     */
    public String getStatus(){
        return this.status;
    }

    /**
     * Resets key components
     */
    protected void resetComponents(){
        if(ServerGroundMovementTree.getServerGroundMovementTree(this.parent) != null){
            ServerGroundMovementTree.getServerGroundMovementTree(this.parent).slowdown();
        }
    }

    /**
     * Checks if this should apply ai behavior even if the entity is controlled by a player
     * @return true if it should always apply ai behavior, false if it should defer to player
     */
    public boolean isApplyToPlayer() {
        return applyToPlayer;
    }

    /**
     * Sets whether this should apply ai behavior even if the entity is controlled by a player
     * @param applyToPlayer true if it sohuld always apply ai behavior, false if it should defer to player
     */
    public void setApplyToPlayer(boolean applyToPlayer) {
        this.applyToPlayer = applyToPlayer;
    }

    /**
     * Gets the blackboard for the ai
     * @return The blackboard
     */
    public Blackboard getBlackboard(){
        return this.blackboard;
    }
    
    
}

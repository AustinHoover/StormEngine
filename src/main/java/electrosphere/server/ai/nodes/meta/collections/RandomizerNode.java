package electrosphere.server.ai.nodes.meta.collections;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Executes a random child until it either succeeds or fails
 */
public class RandomizerNode implements CollectionNode {

    /**
     * The child nodes of the selector
     */
    List<AITreeNode> children;


    /**
     * The currently executed child
     */
    AITreeNode currentChild = null;

    /**
     * Constructor
     * @param children All the children of the randomizer
     */
    public RandomizerNode(List<AITreeNode> children){
        if(children == null){
            throw new IllegalArgumentException("Trying to create randomizer node with no children!");
        }
        if(children.size() < 1){
            throw new IllegalArgumentException("Trying to create randomizer node with no children!");
        }
        this.children = children;
    }

    /**
     * Constructor
     * @param children All the children of the randomizer
     */
    public RandomizerNode(AITreeNode ... children){
        if(children == null){
            throw new IllegalArgumentException("Trying to create randomizer node with no children!");
        }
        if(children.length < 1){
            throw new IllegalArgumentException("Trying to create randomizer node with no children!");
        }
        this.children = Arrays.asList(children);
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(this.currentChild == null){
            Random random = Globals.serverState.aiManager.getRandom();
            int selectedChild = random.nextInt(children.size());
            this.currentChild = this.children.get(selectedChild);
        }
        AITreeNodeResult childResult = this.currentChild.evaluate(entity, blackboard);
        if(childResult == AITreeNodeResult.RUNNING){
            return AITreeNodeResult.RUNNING;
        }
        this.currentChild = null;
        return childResult;
    }

    @Override
    public List<AITreeNode> getAllChildren() {
        return this.children;
    }

}

package electrosphere.server.ai.nodes.meta.collections;

import java.util.Arrays;
import java.util.List;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * A selector node
 */
public class SelectorNode implements CollectionNode {

    /**
     * The child nodes of the selector
     */
    List<AITreeNode> children;

    /**
     * Constructor
     * @param children All the children of the selector
     */
    public SelectorNode(List<AITreeNode> children){
        if(children == null){
            throw new IllegalArgumentException("Trying to create selector node with no children!");
        }
        if(children.size() < 1){
            throw new IllegalArgumentException("Trying to create selector node with no children!");
        }
        this.children = children;
    }

    /**
     * Constructor
     * @param children All the children of the selector
     */
    public SelectorNode(AITreeNode ... children){
        if(children == null){
            throw new IllegalArgumentException("Trying to create selector node with no children!");
        }
        if(children.length < 1){
            throw new IllegalArgumentException("Trying to create selector node with no children!");
        }
        this.children = Arrays.asList(children);
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        for(AITreeNode child : children){
            AITreeNodeResult result = child.evaluate(entity, blackboard);
            if(result != AITreeNodeResult.FAILURE){
                return result;
            }
        }
        return AITreeNodeResult.FAILURE;
    }

    @Override
    public List<AITreeNode> getAllChildren() {
        return this.children;
    }

}

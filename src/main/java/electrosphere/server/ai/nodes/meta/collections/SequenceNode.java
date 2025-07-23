package electrosphere.server.ai.nodes.meta.collections;

import java.util.Arrays;
import java.util.List;

import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * A sequence node
 */
public class SequenceNode implements CollectionNode {

    /**
     * The child nodes of the sequence
     */
    private List<AITreeNode> children;

    /**
     * The name associated with this node
     */
    private String name;

    /**
     * Constructor
     * @param children All the children of the sequence
     */
    public SequenceNode(String name, List<AITreeNode> children){
        if(children == null){
            throw new IllegalArgumentException("Trying to create sequence node with no children!");
        }
        if(children.size() < 1){
            throw new IllegalArgumentException("Trying to create sequence node with no children!");
        }
        this.children = children;
        this.name = name;
    }

    /**
     * Constructor
     * @param children All the children of the sequence
     */
    public SequenceNode(String name, AITreeNode ... children){
        if(children == null){
            throw new IllegalArgumentException("Trying to create sequence node with no children!");
        }
        if(children.length < 1){
            throw new IllegalArgumentException("Trying to create sequence node with no children!");
        }
        this.children = Arrays.asList(children);
        this.name = name;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        for(AITreeNode child : children){
            try {
                AITreeNodeResult result = child.evaluate(entity, blackboard);
                if(result != AITreeNodeResult.SUCCESS){
                    return result;
                }
            } catch(Throwable e){
                throw new Error(this.name, e);
            }
        }
        return AITreeNodeResult.SUCCESS;
    }

    @Override
    public List<AITreeNode> getAllChildren() {
        return this.children;
    }
    
}

package electrosphere.server.ai.nodes.meta.collections;

import java.util.List;

import electrosphere.server.ai.nodes.AITreeNode;

/**
 * A node that has a collection of children
 */
public interface CollectionNode extends AITreeNode {
    
    /**
     * Gets the list of all children that this node can branch into
     * @return The list of all children this node can branch into
     */
    public List<AITreeNode> getAllChildren();

}

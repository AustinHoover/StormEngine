package electrosphere.server.ai.nodes.meta.decorators;

import electrosphere.server.ai.nodes.AITreeNode;

/**
 * A decorator node (ie it has a single direct child)
 */
public interface DecoratorNode extends AITreeNode {
    
    /**
     * Returns the child node of this decorator
     * @return The child node
     */
    public AITreeNode getChild();

}

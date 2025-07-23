package electrosphere.server.ai.nodes.meta.debug;

import electrosphere.entity.Entity;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Triggers a debugger
 */
public class DebuggerNode implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        LoggerInterface.loggerAI.DEBUG("Start debugger!");
        return AITreeNodeResult.SUCCESS;
    }
    
}

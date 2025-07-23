package electrosphere.server.ai.nodes.meta.decorators;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * A node that waits until a timer fires before it executes the child
 */
public class TimerNode implements DecoratorNode {

    /**
     * The child node
     */
    AITreeNode child;

    /**
     * The id for the timer for this node
     */
    int timerId = -1;

    /**
     * The number of frames to wait for
     */
    int frameCount = -1;

    /**
     * Controls whether the timer has been started or not
     */
    boolean hasReset = false;

    /**
     * Constructor
     * @param child The child node
     */
    public TimerNode(AITreeNode child, int frameCount){
        if(child == null){
            throw new IllegalArgumentException("Trying to create timer node with no children!");
        }
        if(frameCount <= 0){
            throw new IllegalArgumentException("Trying to create timer node with frameCount of zero or lower!");
        }
        this.child = child;
        this.frameCount = frameCount;
        this.timerId = Globals.serverState.aiManager.getTimerService().createTimer();
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        if(Globals.serverState.aiManager.getTimerService().isActive(timerId)){
            return AITreeNodeResult.RUNNING;
        }
        if(hasReset){
            this.hasReset = false;
            return AITreeNodeResult.SUCCESS;
        } else {
            Globals.serverState.aiManager.getTimerService().resetTimer(timerId, frameCount);
            this.hasReset = true;
            return AITreeNodeResult.RUNNING;
        }
    }

    @Override
    public AITreeNode getChild() {
        return child;
    }

}

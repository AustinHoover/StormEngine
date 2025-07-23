package electrosphere.server.ai.nodes.meta;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.services.TimerService;

/**
 * Waits for a provided number of frames
 */
public class StartTimerNode implements AITreeNode {

    /**
     * The id of the timer in the service that this node references
     */
    int timerId = -1;

    /**
     * Gets the number of frames to wait
     */
    int frameCount = -1;

    /**
     * Constructor
     * @param next (Optional) The next node to execute
     */
    public StartTimerNode(int frameCount){
        if(frameCount < 1){
            LoggerInterface.loggerAI.ERROR(new IllegalArgumentException("Frame count provided to timer is <=0!"));
        }
        this.frameCount = frameCount;
        //create the timer
        Globals.serverState.aiManager.getTimerService().createTimer(this.frameCount);
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        TimerService timerService = Globals.serverState.aiManager.getTimerService();
        if(timerService.isActive(this.timerId)){
            return AITreeNodeResult.RUNNING;
        } else {
            timerService.resetTimer(timerId, frameCount);
            return AITreeNodeResult.SUCCESS;
        }
    }
    
    
}

package electrosphere.server.ai.nodes.meta;

import electrosphere.entity.Entity;
import electrosphere.logger.Logger;
import electrosphere.logger.LoggerInterface;
import electrosphere.logger.Logger.LogLevel;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;

/**
 * Logs a message
 */
public class PrintNode implements AITreeNode {

    /**
     * The level to print at
     */
    LogLevel level;

    /**
     * The logger to print into
     */
    Logger logger;

    /**
     * The message to print
     */
    String message;

    /**
     * Constructor
     * @param level The logging level to print at
     * @param logger The logger to print into
     * @param message The message to print
     */
    public PrintNode(LogLevel level, Logger logger, String message){
        this.level = level;
        this.logger = logger;
        this.message = message;
    }

    /**
     * Constructor
     * @param level The logging level to print at
     * @param message The message to print
     */
    public PrintNode(LogLevel level, String message){
        this.logger = LoggerInterface.loggerAI;
        this.level = level;
        this.message = message;
    }

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        logger.PRINT(level, message);
        return AITreeNodeResult.SUCCESS;
    }

}

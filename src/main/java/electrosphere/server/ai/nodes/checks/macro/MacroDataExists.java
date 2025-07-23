package electrosphere.server.ai.nodes.checks.macro;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.datacell.Realm;

/**
 * Checks that macro data exists
 */
public class MacroDataExists implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        Realm entityRealm = Globals.serverState.realmManager.getEntityRealm(entity);
        if(entityRealm.getMacroData() == null){
            return AITreeNodeResult.FAILURE;
        }
        return AITreeNodeResult.SUCCESS;
    }

}

package electrosphere.server.ai.nodes.actions.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3d;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode.AITreeNodeResult;
import electrosphere.server.entity.unit.UnitUtils;
import electrosphere.test.template.EntityTestTemplate;

/**
 * Tests for the melee targeting ai node
 */
public class MeleeTargetingNodeTests extends EntityTestTemplate {
    
    @IntegrationTest
    public void testStopTargetingDeadEntity(){
        float aggroRange = 10;

        //spawn test entities
        Entity swordsman = UnitUtils.spawnUnit(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "humanSwordsman");
        Entity target = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(1,0,0), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        //check if the swordsman can find a target
        Blackboard blackboard = new Blackboard();
        MeleeTargetingNode meleeTargetingNode = new MeleeTargetingNode(aggroRange);
        AITreeNodeResult eval1 = meleeTargetingNode.evaluate(swordsman, blackboard);
        assertEquals(AITreeNodeResult.SUCCESS, eval1);

        //delete the target
        ServerEntityUtils.destroyEntity(target);

        //check that the swordsman no longer finds a target
        AITreeNodeResult eval2 = meleeTargetingNode.evaluate(swordsman, blackboard);
        assertEquals(AITreeNodeResult.FAILURE, eval2);
    }

}

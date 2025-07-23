package electrosphere.server.ai.nodes.checks.macro;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.CharacterUtils;
import electrosphere.server.macro.structure.VirtualStructure;

/**
 * Checks if the character has shelter
 */
public class HasShelter implements AITreeNode {

    @Override
    public AITreeNodeResult evaluate(Entity entity, Blackboard blackboard) {
        ServerCharacterData serverCharacterData = ServerCharacterData.getServerCharacterData(entity);
        Character character = serverCharacterData.getCharacterData();
        Realm realm = Globals.serverState.realmManager.getEntityRealm(entity);
        if(character == null){
            throw new Error("Character is null");
        }
        VirtualStructure shelter = CharacterUtils.getShelter(realm.getMacroData(),character);
        if(shelter == null){
            return AITreeNodeResult.FAILURE;
        }
        return AITreeNodeResult.SUCCESS;
    }

}

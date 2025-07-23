package electrosphere.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3d;
import org.junit.jupiter.api.Disabled;

import electrosphere.data.entity.common.CommonEntityMap;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.CreatureTypeLoader;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.data.entity.foliage.FoliageTypeLoader;
import electrosphere.data.entity.item.Item;
import electrosphere.data.entity.item.ItemDataMap;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.foliage.FoliageUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests for spawning entities
 */
public class SpawnAllEntitiesTests extends EntityTestTemplate {
    
    @Disabled
    @IntegrationTest
    public void spawnAllEntities(){
        EngineState.EngineFlags.ENGINE_DEBUG = false;
        assertDoesNotThrow(() -> {

            CreatureTypeLoader creatureLoader = Globals.gameConfigCurrent.getCreatureTypeLoader();
            for(CreatureData creature : creatureLoader.getTypes()){
                CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0.1,0.1,0.1), creature.getId(), ObjectTemplate.createDefault(EntityType.CREATURE, creature.getId()));
            }

            ItemDataMap itemMap = Globals.gameConfigCurrent.getItemMap();
            for(Item item : itemMap.getTypes()){
                ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0.1,0.1,0.1), item.getId());
            }

            FoliageTypeLoader foliageTypeMap = Globals.gameConfigCurrent.getFoliageMap();
            for(FoliageType foliage : foliageTypeMap.getTypes()){
                FoliageUtils.serverSpawnTreeFoliage(Globals.serverState.realmManager.first(), new Vector3d(0.1,0.1,0.1), foliage.getId());
            }

            CommonEntityMap commonEntityMap = Globals.gameConfigCurrent.getObjectTypeMap();
            for(CommonEntityType entity : commonEntityMap.getTypes()){
                CommonEntityUtils.serverSpawnBasicObject(Globals.serverState.realmManager.first(), new Vector3d(0.1,0.1,0.1), entity.getId());
            }

            //wait for entities to propagate across network
            TestEngineUtils.simulateFrames(100);
        });
    }

}

package electrosphere.client.ui.menu.script;

import org.graalvm.polyglot.HostAccess.Export;
import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.ui.menu.debug.entity.ImGuiEntityMacros;
import electrosphere.collision.CollisionEngine;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.foliage.FoliageUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.Realm;

/**
 * Utilities for editing levels
 */
public class ScriptLevelEditorUtils {

    //vertical offset from cursor position to spawn things at
    static final Vector3d cursorVerticalOffset = new Vector3d(0,0.05,0);
    
    /**
     * Spawns the selected entity
     */
    @Export
    public static void spawnEntity(){
        if(Globals.clientState.selectedSpawntype != null){
            if(Globals.clientState.selectedSpawntype instanceof CreatureData){
                LoggerInterface.loggerEngine.INFO("spawn " + Globals.clientState.selectedSpawntype.getId() + "!");
                Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
                CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
                Vector3d cursorPos = clientCollisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
                if(cursorPos == null){
                    cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
                }
                cursorPos = cursorPos.add(cursorVerticalOffset);
                realm.getServerWorldData().clampWithinBounds(cursorPos);
                CreatureUtils.serverSpawnBasicCreature(realm, cursorPos, Globals.clientState.selectedSpawntype.getId(), null);
            } else if(Globals.clientState.selectedSpawntype instanceof Item){
                LoggerInterface.loggerEngine.INFO("spawn " + Globals.clientState.selectedSpawntype.getId() + "!");
                Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
                CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
                Vector3d cursorPos = clientCollisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
                if(cursorPos == null){
                    cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
                }
                cursorPos = cursorPos.add(cursorVerticalOffset);
                ItemUtils.serverSpawnBasicItem(realm, cursorPos, Globals.clientState.selectedSpawntype.getId());
            } else if(Globals.clientState.selectedSpawntype instanceof FoliageType){
                LoggerInterface.loggerEngine.INFO("spawn " + Globals.clientState.selectedSpawntype.getId() + "!");
                Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
                CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
                Vector3d cursorPos = clientCollisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
                if(cursorPos == null){
                    cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
                }
                cursorPos = cursorPos.add(cursorVerticalOffset);
                FoliageUtils.serverSpawnTreeFoliage(realm, cursorPos, Globals.clientState.selectedSpawntype.getId());
            } else {
                LoggerInterface.loggerEngine.INFO("spawn " + Globals.clientState.selectedSpawntype.getId() + "!");
                Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
                Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
                CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
                Vector3d cursorPos = clientCollisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
                if(cursorPos == null){
                    cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
                }
                cursorPos = cursorPos.add(cursorVerticalOffset);
                CommonEntityUtils.serverSpawnBasicObject(realm, cursorPos, Globals.clientState.selectedSpawntype.getId());
            }
        }
    }

    /**
     * Inspects the entity that the player's cursor is hovering over
     */
    @Export
    public static void inspectEntity(){
        Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
        Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
        CollisionEngine clientCollisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine(); //using client collision engine so ray doesn't collide with player entity
        Entity target = clientCollisionEngine.rayCast(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
        //show detail view here
        if(target != null){
            ImGuiEntityMacros.showEntity(target);
        }
    }

}

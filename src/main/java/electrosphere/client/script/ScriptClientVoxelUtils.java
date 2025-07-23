package electrosphere.client.script;

import org.graalvm.polyglot.HostAccess.Export;
import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.audio.movement.MovementAudioService.InteractionType;
import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.scene.ClientWorldData;
import electrosphere.client.terrain.editing.TerrainEditing;
import electrosphere.collision.CollisionEngine;
import electrosphere.controls.cursor.CursorState;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Utilities for interacting with voxels from the client
 */
public class ScriptClientVoxelUtils {
    
    /**
     * Increment to edit terrain by
     */
    static final float EDIT_INCREMENT = 0.1f;

    /**
     * Increment to remove terrain by
     */
    static final float REMOVE_INCREMENT = -0.1f;

    /**
     * vertical offset from cursor position to spawn things at
     */
    static final Vector3d cursorVerticalOffset = new Vector3d(0,0.05,0);

    /**
     * Applies the current voxel palette where the player's cursor is looking
     */
    @Export
    public static void applyEdit(){
        CollisionEngine collisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine();
        Entity camera = Globals.clientState.playerCamera;
        if(
            collisionEngine != null &&
            camera != null
        ){
            Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(camera));
            Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(camera));
            Vector3d cursorPos = collisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
            if(cursorPos == null){
                cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
            }
            if(Globals.clientState.clientSelectedVoxelType != null){
                TerrainEditing.editTerrain(cursorPos, 1.1f, Globals.clientState.clientSelectedVoxelType.getId(), EDIT_INCREMENT);
                Globals.audioEngine.movementAudioService.getAudioPath(Globals.clientState.clientSelectedVoxelType.getId(), InteractionType.STEP_SHOE_REG);
            }
        }
    }

    /**
     * Applies the current voxel palette where the player's cursor is looking
     */
    @Export
    public static void spawnWater(){
        CollisionEngine collisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine();
        Entity camera = Globals.clientState.playerCamera;
        if(
            collisionEngine != null &&
            camera != null &&
            Globals.serverState.realmManager != null &&
            Globals.serverState.realmManager.first() != null &&
            Globals.serverState.realmManager.first().getServerWorldData() != null &&
            Globals.serverState.realmManager.first().getServerWorldData().getServerFluidManager() != null
        ){
            Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(camera));
            Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(camera));
            Vector3d cursorPos = collisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
            if(cursorPos == null){
                cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
            }
            cursorPos = cursorPos.add(cursorVerticalOffset);
            Vector3i worldPos = new Vector3i(
                (int)(cursorPos.x / ServerTerrainChunk.CHUNK_DIMENSION),
                (int)(cursorPos.y / ServerTerrainChunk.CHUNK_DIMENSION),
                (int)(cursorPos.z / ServerTerrainChunk.CHUNK_DIMENSION)
            );
            Vector3i voxelPos = new Vector3i(
                (int)(Math.ceil(cursorPos.x) % ServerTerrainChunk.CHUNK_DIMENSION),
                (int)(Math.ceil(cursorPos.y) % ServerTerrainChunk.CHUNK_DIMENSION),
                (int)(Math.ceil(cursorPos.z) % ServerTerrainChunk.CHUNK_DIMENSION)
            );
            Globals.serverState.realmManager.first().getServerWorldData().getServerFluidManager().deformFluidAtLocationToValue(worldPos, voxelPos, 1.0f, 0);
        }
    }

    /**
     * Tries to dig with whatever tool is equipped
     */
    @Export
    public static void dig(){
        CollisionEngine collisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine();
        Entity camera = Globals.clientState.playerCamera;
        if(
            collisionEngine != null &&
            camera != null
        ){
            Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(camera));
            Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(camera));
            Vector3d cursorPos = collisionEngine.rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
            if(cursorPos == null){
                cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
            }
            TerrainEditing.removeTerrainGated(cursorPos, 1.1f, REMOVE_INCREMENT);
            Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.INTERACT_SFX_DIG, VirtualAudioSourceType.CREATURE, false);
        }
    }

    /**
     * Requests a block edit from the client
     * @param chunkPos The position of the chunk to edit
     * @param blockPos The position of the block to edit
     * @param blockType The type of block
     * @param blockMetadata The metadata of the block
     * @param size The size of the blocks to edit
     */
    @Export
    public static void clientRequestEditBlock(Vector3i chunkPos, Vector3i blockPos, short blockType, short blockMetadata, int size){
        Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestEditBlockMessage(
            chunkPos.x, chunkPos.y, chunkPos.z,
            blockPos.x, blockPos.y, blockPos.z,
            blockType, blockMetadata, size
        ));
    }

    /**
     * Places the currently selected fab
     */
    @Export
    public static void placeFab(){
        if(Globals.cursorState.getSelectedFabPath() != null){
            String fabPath = Globals.cursorState.getSelectedFabPath();
            Vector3d fabCursorPos = EntityUtils.getPosition(CursorState.getFabCursor());
            Vector3i chunkPos = Globals.clientState.clientWorldData.convertRealToWorldSpace(fabCursorPos);
            Vector3i voxelPos = ClientWorldData.convertRealToLocalBlockSpace(fabCursorPos);
            int rotation = Globals.cursorState.getFabCursorRotation();
            Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestPlaceFabMessage(
                chunkPos.x, chunkPos.y, chunkPos.z,
                voxelPos.x, voxelPos.y, voxelPos.z,
                rotation,
                fabPath
            ));
        }
    }

}

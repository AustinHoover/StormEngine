package electrosphere.controls.cursor;

import java.util.Arrays;
import java.util.Map;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.interact.select.AreaSelection;
import electrosphere.collision.CollisionEngine;
import electrosphere.data.block.fab.BlockFab;
import electrosphere.data.entity.grident.GridAlignedData;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.assetmanager.queue.QueuedModel;
import electrosphere.entity.DrawableUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.mem.JomlPool;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.mask.ActorTextureMask;
import electrosphere.renderer.meshgen.BlockMeshgen;
import electrosphere.renderer.ui.events.ScrollEvent;

/**
 * Tracking for the cursor state
 */
public class CursorState {

    /**
     * Token for displaying cursor
     */
    public static final String CURSOR_TOKEN = "CURSOR";

    /**
     * Token for displaying block cursor
     */
    public static final String CURSOR_BLOCK_TOKEN = "CURSOR_BLOCK";

    /**
     * Token for displaying a block area selection cursor
     */
    public static final String CURSOR_AREA_TOKEN = "CURSOR_AREA";

    /**
     * Cursor that displays a fab
     */
    public static final String CURSOR_FAB_TOKEN = "CURSOR_FAB";

    /**
     * Cursor that displays a grid-aligned model
     */
    public static final String CURSOR_GRID_ALIGNED_TOKEN = "CURSOR_GRID_ALIGNED";

    /**
     * Minimum size of the block cursor
     */
    public static final int MIN_BLOCK_SIZE = 1;

    /**
     * Default size to edit by
     */
    public static final int DEFAULT_BLOCK_SIZE = 2;

    /**
     * Maximum size of the block cursor
     */
    public static final int MAX_BLOCK_SIZE = 4;

    /**
     * The scale multiplier for scaling the block cursor
     */
    private static final float BLOCK_CURSOR_SCALE_MULTIPLIER = 0.25f;

    /**
     * Size of blocks to edit
     */
    private int blockSize = DEFAULT_BLOCK_SIZE;

    /**
     * The current selection of the area selector
     */
    private AreaSelection areaCursorSelection = null;

    /**
     * Clamps the position of the block cursor to the existing block if true
     * Clamps to the closest empty block space if false
     */
    private boolean clampToExistingBlock = false;

    /**
     * Free point selection cursor
     */
    public Entity playerCursor;

    /**
     * Block cursor
     */
    public Entity playerBlockCursor;

    /**
     * Area cursor
     */
    public Entity playerAreaCursor;

    /**
     * The fab cursor
     */
    static Entity playerFabCursor;

    /**
     * The grid-aligned cursor
     */
    static Entity playerGridAlignedCursor;

    /**
     * Data for the grid alignment
     */
    private GridAlignedData gridAlignmentData;

    /**
     * Maximum value to rotate to
     */
    private static final int MAX_ROTATION_VAL = 15;

    /**
     * The rotation of the fab cursor
     * The first two bits encode a rotation about the x axis
     * The next two bits encode a subsequent rotation about the y axis
     */
    private int fabCursorRotation = 0;

    /**
     * The currently selected fab
     */
    private BlockFab selectedFab = null;

    /**
     * The path for the selected fab
     */
    private String selectedFabPath = null;

    /**
     * Creates the cursor entities
     */
    public static void createCursorEntities(){
        //player's cursor
        Globals.cursorState.playerCursor = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(Globals.cursorState.playerCursor, AssetDataStrings.UNITSPHERE);
        Actor cursorActor = EntityUtils.getActor(Globals.cursorState.playerCursor);
        cursorActor.addTextureMask(new ActorTextureMask("sphere", Arrays.asList(new String[]{AssetDataStrings.TEXTURE_RED_TRANSPARENT})));
        DrawableUtils.makeEntityTransparent(Globals.cursorState.playerCursor);
        EntityUtils.getScale(Globals.cursorState.playerCursor).set(0.2f);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(Globals.cursorState.playerCursor, EntityTags.DRAWABLE);

        //player's block cursor
        Globals.cursorState.playerBlockCursor = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(Globals.cursorState.playerBlockCursor, AssetDataStrings.UNITCUBE);
        Actor blockCursorActor = EntityUtils.getActor(Globals.cursorState.playerBlockCursor);
        blockCursorActor.addTextureMask(new ActorTextureMask("cube", Arrays.asList(new String[]{AssetDataStrings.TEXTURE_RED_TRANSPARENT})));
        DrawableUtils.makeEntityTransparent(Globals.cursorState.playerBlockCursor);
        EntityUtils.getScale(Globals.cursorState.playerBlockCursor).set(BLOCK_CURSOR_SCALE_MULTIPLIER);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(Globals.cursorState.playerBlockCursor, EntityTags.DRAWABLE);

        //player's area cursor
        Globals.cursorState.playerAreaCursor = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(Globals.cursorState.playerAreaCursor, AssetDataStrings.UNITCUBE);
        Actor areaCursorActor = EntityUtils.getActor(Globals.cursorState.playerAreaCursor);
        areaCursorActor.addTextureMask(new ActorTextureMask("cube", Arrays.asList(new String[]{AssetDataStrings.TEXTURE_RED_TRANSPARENT})));
        DrawableUtils.makeEntityTransparent(Globals.cursorState.playerAreaCursor);
        EntityUtils.getScale(Globals.cursorState.playerAreaCursor).set(BLOCK_CURSOR_SCALE_MULTIPLIER);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(Globals.cursorState.playerAreaCursor, EntityTags.DRAWABLE);

        //player's fab cursor
        playerFabCursor = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(playerFabCursor, AssetDataStrings.UNITCUBE);
        Actor fabCursorActor = EntityUtils.getActor(playerFabCursor);
        fabCursorActor.addTextureMask(new ActorTextureMask("cube", Arrays.asList(new String[]{AssetDataStrings.TEXTURE_RED_TRANSPARENT})));
        DrawableUtils.makeEntityTransparent(playerFabCursor);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(playerFabCursor, EntityTags.DRAWABLE);

        //player's grid-aligned cursor
        playerGridAlignedCursor = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(playerGridAlignedCursor, AssetDataStrings.UNITCUBE);
        Actor gridAlignedCursorActor = EntityUtils.getActor(playerGridAlignedCursor);
        gridAlignedCursorActor.addTextureMask(new ActorTextureMask("cube", Arrays.asList(new String[]{AssetDataStrings.TEXTURE_RED_TRANSPARENT})));
        DrawableUtils.makeEntityTransparent(playerGridAlignedCursor);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(playerGridAlignedCursor, EntityTags.DRAWABLE);
    }

    /**
     * Updates the position of the player's in world cursor
     */
    public void updatePlayerCursor(){
        Globals.profiler.beginCpuSample("CursorState.updatePlayerCursor");
        CollisionEngine collisionEngine = Globals.clientState.clientSceneWrapper.getCollisionEngine();
        Entity camera = Globals.clientState.playerCamera;
        if(
            collisionEngine != null &&
            camera != null &&
            Globals.cursorState.playerCursor != null
        ){
            Vector3d eyePos = JomlPool.getD();
            eyePos.set(CameraEntityUtils.getCameraEye(camera)).mul(-1.0);
            Vector3d centerPos = JomlPool.getD();
            centerPos.set(CameraEntityUtils.getCameraCenter(camera));
            Vector3d cursorPos = collisionEngine.rayCastPosition(centerPos, eyePos, CollisionEngine.DEFAULT_INTERACT_DISTANCE);
            if(cursorPos == null){
                cursorPos = centerPos.add(eyePos.normalize().mul(CollisionEngine.DEFAULT_INTERACT_DISTANCE));
            }
            EntityUtils.setPosition(Globals.cursorState.playerCursor, cursorPos);

            //clamp block cursor to nearest voxel
            if(clampToExistingBlock){
                cursorPos = cursorPos.add(eyePos.normalize().mul(BlockChunkData.BLOCK_SIZE_MULTIPLIER));
            }
            cursorPos.set(this.clampPositionToNearestBlock(cursorPos));
            if(Globals.cursorState.playerBlockCursor != null){
                EntityUtils.setPosition(Globals.cursorState.playerBlockCursor, cursorPos);
            }
            cursorPos.sub(BlockChunkData.BLOCK_SIZE_MULTIPLIER / 2.0,BlockChunkData.BLOCK_SIZE_MULTIPLIER / 2.0,BlockChunkData.BLOCK_SIZE_MULTIPLIER / 2.0);
            EntityUtils.setPosition(CursorState.playerFabCursor, cursorPos);
            if(gridAlignmentData != null){
                CursorState.nudgeGridAlignment(cursorPos,gridAlignmentData);
            }
            EntityUtils.setPosition(CursorState.playerGridAlignedCursor, cursorPos);

            //release to pool
            JomlPool.release(eyePos);
            JomlPool.release(centerPos);
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Makes the real position cursor visible
     */
    public static void makeRealVisible(){
        CursorState.hide();
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(Globals.cursorState.playerCursor, EntityTags.DRAWABLE);
    }

    /**
     * Makes the block position cursor visible
     */
    public static void makeBlockVisible(String texture){
        CursorState.hide();
        Actor blockCursorActor = EntityUtils.getActor(Globals.cursorState.playerBlockCursor);
        blockCursorActor.addTextureMask(new ActorTextureMask("cube", Arrays.asList(new String[]{texture})));
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(Globals.cursorState.playerBlockCursor, EntityTags.DRAWABLE);
    }

    /**
     * Makes the area position cursor visible
     */
    public static void makeAreaVisible(){
        CursorState.hide();
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(Globals.cursorState.playerAreaCursor, EntityTags.DRAWABLE);
    }

    /**
     * Makes the fab placement cursor visible
     */
    public static void makeFabVisible(){
        CursorState.hide();
        Globals.cursorState.setClampToExistingBlock(false);
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(CursorState.playerFabCursor, EntityTags.DRAWABLE);
        Globals.cursorState.fabCursorRotation = 0;
    }

    /**
     * Makes the grid-aligned placement cursor visible
     */
    public static void makeGridAlignedVisible(String modelPath){
        CursorState.hide();
        Globals.cursorState.setClampToExistingBlock(true);
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(CursorState.playerGridAlignedCursor, EntityTags.DRAWABLE);
        EntityCreationUtils.makeEntityDrawable(playerGridAlignedCursor, modelPath);
        Globals.cursorState.fabCursorRotation = 0;
    }

    /**
     * Hides the cursor
     */
    public static void hide(){
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(Globals.cursorState.playerCursor, EntityTags.DRAWABLE);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(Globals.cursorState.playerBlockCursor, EntityTags.DRAWABLE);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(Globals.cursorState.playerAreaCursor, EntityTags.DRAWABLE);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(CursorState.playerFabCursor, EntityTags.DRAWABLE);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(CursorState.playerGridAlignedCursor, EntityTags.DRAWABLE);
    }

    /**
     * Selects a rectangular area
     * @param selection The area selection to encompass the cursor
     */
    public void selectRectangularArea(AreaSelection selection){
        this.areaCursorSelection = selection;
        Vector3d center = new Vector3d(areaCursorSelection.getRectStart()).add(areaCursorSelection.getRectEnd()).mul(0.5f);
        Vector3d scale = new Vector3d(areaCursorSelection.getRectStart()).sub(areaCursorSelection.getRectEnd()).absolute();
        EntityCreationUtils.makeEntityDrawable(Globals.cursorState.playerAreaCursor, AssetDataStrings.UNITCUBE);
        Actor areaCursorActor = EntityUtils.getActor(Globals.cursorState.playerAreaCursor);
        areaCursorActor.addTextureMask(new ActorTextureMask("cube", Arrays.asList(new String[]{"Textures/transparent_red.png"})));
        EntityUtils.setPosition(Globals.cursorState.playerAreaCursor, center);
        EntityUtils.getScale(Globals.cursorState.playerAreaCursor).set(scale);
    }
    
    /**
     * Clamps a real position to the nearest block
     * @param input The input real position
     * @return The real position clamped to the nearest block
     */
    public Vector3d clampPositionToNearestBlock(Vector3d input){
        double x = 0;
        double y = 0;
        double z = 0;
        double sizeMult = BlockChunkData.BLOCK_SIZE_MULTIPLIER * blockSize;
        double alignMult = BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        if(input.x % alignMult > alignMult / 2.0){
            x = input.x - input.x % alignMult + alignMult + sizeMult / 2.0;
        } else {
            x = input.x - input.x % alignMult + sizeMult / 2.0;
        }
        if(input.y % alignMult > alignMult / 2.0){
            y = input.y - input.y % alignMult + alignMult + sizeMult / 2.0;
        } else {
            y = input.y - input.y % alignMult + sizeMult / 2.0;
        }
        if(input.z % alignMult > alignMult / 2.0){
            z = input.z - input.z % alignMult + alignMult + sizeMult / 2.0;
        } else {
            z = input.z - input.z % alignMult + sizeMult / 2.0;
        }
        return new Vector3d(x,y,z);
    }

    /**
     * Clamps a real position to the corner of the block cursor
     * @param input The input real position
     * @return The corner position
     */
    public Vector3d clampPositionToBlockCorner(Vector3d input){
        double x = 0;
        double y = 0;
        double z = 0;
        double alignMult = BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        if(input.x % alignMult > alignMult / 2.0){
            x = input.x - input.x % alignMult + alignMult;
        } else {
            x = input.x - input.x % alignMult;
        }
        if(input.y % alignMult > alignMult / 2.0){
            y = input.y - input.y % alignMult + alignMult;
        } else {
            y = input.y - input.y % alignMult;
        }
        if(input.z % alignMult > alignMult / 2.0){
            z = input.z - input.z % alignMult + alignMult;
        } else {
            z = input.z - input.z % alignMult;
        }
        return new Vector3d(x,y,z);
    }

    /**
     * Nudges the position to align with the grid alignment data
     * @param position The position
     * @param data The data
     */
    private static void nudgeGridAlignment(Vector3d position, GridAlignedData data){
        if(data.getWidth() / 2 == 1){
            position.x = position.x - BlockChunkData.BLOCK_SIZE_MULTIPLIER / 2.0f;
        }
        if(data.getLength() / 2 == 1){
            position.z = position.z - BlockChunkData.BLOCK_SIZE_MULTIPLIER / 2.0f;
        }
    }

    /**
     * Gets the block cursor position
     * @return The block cursor position
     */
    public Vector3d getBlockCursorPos(){
        double sizeMult = BlockChunkData.BLOCK_SIZE_MULTIPLIER * blockSize;
        Vector3d posRaw = new Vector3d(EntityUtils.getPosition(Globals.cursorState.playerBlockCursor)).sub(sizeMult/2.0,sizeMult/2.0,sizeMult/2.0);
        return posRaw;
    }

    /**
     * Updates the cursor size
     * @param scrollEvent The scroll event
     */
    public void updateCursorSize(ScrollEvent scrollEvent){
        if(scrollEvent.getScrollAmount() > 0){
            if(this.blockSize < MAX_BLOCK_SIZE){
                this.blockSize = this.blockSize * 2;
            }
        } else {
            if(this.blockSize > MIN_BLOCK_SIZE){
                this.blockSize = this.blockSize / 2;
            }
        }
        EntityUtils.getScale(Globals.cursorState.playerBlockCursor).set(BLOCK_CURSOR_SCALE_MULTIPLIER * this.blockSize);
    }

    /**
     * Rotates the block cursor
     * @param scrollEvent The scroll event
     */
    public void rotateBlockCursor(ScrollEvent scrollEvent){
        if(scrollEvent.getScrollAmount() > 0){
            if(this.fabCursorRotation > 0){
                this.fabCursorRotation--;
            } else {
                this.fabCursorRotation = MAX_ROTATION_VAL;
            }
        } else {
            if(this.fabCursorRotation < MAX_ROTATION_VAL){
                this.fabCursorRotation++;
            } else {
                this.fabCursorRotation = 0;
            }
        }
        this.setFabCursorRotation(this.fabCursorRotation);;
    }

    /**
     * Rotates the fab cursor
     * @param rotationVal The value that encodes the rotation
     */
    public void setFabCursorRotation(int rotationVal){
        Quaterniond rotations = CursorState.getBlockRotation(rotationVal);
        EntityUtils.getRotation(playerFabCursor).set(rotations);
    }

    /**
     * Gets the rotation of a block based on its int-encoded rotation value
     * @param rotationVal The int-encoded rotation value
     * @return The corresponding quaterniond
     */
    public static Quaterniond getBlockRotation(int rotationVal){
        int tempVal = rotationVal;
        int yRotation = tempVal & 0x3;
        int xRotation = tempVal >> 2 & 0x3;
        Quaterniond rotations = new Quaterniond();
        double xRot = 0;
        double yRot = 0;
        switch(xRotation){
            case 0: {
                //no rotation applied
            } break;
            case 1: {
                xRot = Math.PI / 2.0;
            } break;
            case 2: {
                xRot = Math.PI;
            } break;
            case 3: {
                xRot = 3.0 * Math.PI / 2.0;
            } break;
        }
        switch(yRotation){
            case 0: {
                //no rotation applied
            } break;
            case 1: {
                yRot = Math.PI / 2.0;
            } break;
            case 2: {
                yRot = Math.PI;
            } break;
            case 3: {
                yRot = 3.0 * Math.PI / 2.0;
            } break;
        }
        rotations.rotateLocalZ(xRot);
        rotations.rotateLocalY(yRot);
        return rotations;
    }
    
    /**
     * Sets the block fab cursor's current fab
     * @param fab The fab
     */
    public void setSelectedFab(BlockFab fab){
        Map<Integer,Boolean> solidsMap = Globals.gameConfigCurrent.getBlockData().getSolidsMap();
        QueuedModel queuedModel = new QueuedModel(() -> {
            return BlockMeshgen.generateBlockModel(BlockMeshgen.rasterize(fab,false,solidsMap,BlockMeshgen.DEFAULT_SCALING_FACTOR));
        });
        Globals.assetManager.queuedAsset(queuedModel);
        EntityCreationUtils.makeEntityDrawablePreexistingModel(playerFabCursor, queuedModel.getPromisedPath());
        Actor fabCursorActor = EntityUtils.getActor(playerFabCursor);
        fabCursorActor.addTextureMask(new ActorTextureMask("cube", Arrays.asList(new String[]{"Textures/transparent_red.png"})));
        DrawableUtils.makeEntityTransparent(playerFabCursor);

        CursorState.makeFabVisible();
    }

    /**
     * Gets the currently selected fab
     * @return The currently selected fab
     */
    public BlockFab getSelectedFab(){
        return this.selectedFab;
    }

    /**
     * Sets the selected fab path
     * @param path The path
     */
    public void setSelectedFabPath(String path){
        this.selectedFabPath = path;
    }

    /**
     * Gets the selected fab's path
     * @return The path
     */
    public String getSelectedFabPath(){
        return this.selectedFabPath;
    }

    /**
     * Gets the size of the block cursor
     * @return The size of the block cursor
     */
    public int getBlockSize(){
        return blockSize;
    }

    /**
     * Gets the currently selected area
     * @return The currently selected area
     */
    public AreaSelection getAreaSelection(){
        return this.areaCursorSelection;
    }

    /**
     * Sets whether the block cursor should clamp to the existing block or not
     * @param clampToExisting true to clamp to existing block, false to clamp to nearest empty block space
     */
    public void setClampToExistingBlock(boolean clampToExisting){
        this.clampToExistingBlock = clampToExisting;
    }

    /**
     * Hints to clamp the cursor to existing blocks
     */
    public void hintClampToExistingBlock(){
        Globals.cursorState.setClampToExistingBlock(true);
        if(Globals.clientState.playerEntity != null && ClientToolbarState.hasClientToolbarState(Globals.clientState.playerEntity)){
            ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(Globals.clientState.playerEntity);
            if(clientToolbarState.getCurrentPrimaryItem() != null){
                Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(clientToolbarState.getCurrentPrimaryItem());
                if(Globals.cursorState.playerCursor != null && Globals.cursorState.playerBlockCursor != null){
                    if(itemData.getTokens().contains(CursorState.CURSOR_BLOCK_TOKEN)) {
                        Globals.cursorState.setClampToExistingBlock(false);
                    }
                }
            }
        }
    }

    /**
     * Hints to clear the cursor state
     */
    public void hintClearBlockCursor(){
        Globals.cursorState.setClampToExistingBlock(false);
        if(Globals.clientState.playerEntity != null && ClientToolbarState.hasClientToolbarState(Globals.clientState.playerEntity)){
            ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(Globals.clientState.playerEntity);
            boolean clearBlockCursor = true;
            if(clientToolbarState.getCurrentPrimaryItem() != null){
                Item itemData = Globals.gameConfigCurrent.getItemMap().getItem(clientToolbarState.getCurrentPrimaryItem());
                if(Globals.cursorState.playerCursor != null && Globals.cursorState.playerBlockCursor != null){
                    if(itemData.getTokens().contains(CursorState.CURSOR_BLOCK_TOKEN)) {
                        clearBlockCursor = false;
                    }
                }
            }
            if(clearBlockCursor){
                Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(Globals.cursorState.playerBlockCursor, EntityTags.DRAWABLE);
            }
        }
    }

    /**
     * Hints to show the block cursor
     */
    public void hintShowBlockCursor(){
        if(
            !Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.DRAWABLE).contains(CursorState.playerFabCursor) &&
            !Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.DRAWABLE).contains(Globals.cursorState.playerAreaCursor) &&
            !Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.DRAWABLE).contains(Globals.cursorState.playerCursor)
        ){
            CursorState.makeBlockVisible(AssetDataStrings.TEXTURE_RED_TRANSPARENT);
        }
    }

    /**
     * Gets the fab cursor
     * @return The fab cursor
     */
    public static Entity getFabCursor(){
        return playerFabCursor;
    }

    /**
     * Gets the rotation of the fab cursor
     * @return The rotation of the fab cursor
     */
    public int getFabCursorRotation(){
        return this.fabCursorRotation;
    }

    /**
     * Sets the grid alignment data
     * @param gridAlignedData The grid alignment data
     */
    public void setGridAlignmentData(GridAlignedData gridAlignedData){
        this.gridAlignmentData = gridAlignedData;
    }
    
    /**
     * Gets the position of the currently visible cursor
     * @return The position if a cursor is visible, null otherwise
     */
    public Vector3d getCursorPosition(){
        if(Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.DRAWABLE).contains(Globals.cursorState.playerCursor)){
            return EntityUtils.getPosition(Globals.cursorState.playerCursor);
        }
        if(Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.DRAWABLE).contains(Globals.cursorState.playerBlockCursor)){
            return EntityUtils.getPosition(Globals.cursorState.playerBlockCursor);
        }
        if(Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.DRAWABLE).contains(Globals.cursorState.playerAreaCursor)){
            return EntityUtils.getPosition(Globals.cursorState.playerAreaCursor);
        }
        if(Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.DRAWABLE).contains(CursorState.playerFabCursor)){
            return EntityUtils.getPosition(CursorState.playerFabCursor);
        }
        if(Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.DRAWABLE).contains(CursorState.playerGridAlignedCursor)){
            return EntityUtils.getPosition(CursorState.playerGridAlignedCursor);
        }
        return null;
    }

}

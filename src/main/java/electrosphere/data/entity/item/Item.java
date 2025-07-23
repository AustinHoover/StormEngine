package electrosphere.data.entity.item;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.controls.cursor.CursorState;
import electrosphere.data.block.BlockType;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.common.item.SpawnItemDescription;
import electrosphere.data.entity.graphics.GraphicsTemplate;
import electrosphere.data.entity.graphics.NonproceduralModel;
import electrosphere.data.voxel.VoxelType;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.meshgen.GeometryModelGen;

/**
 * Data on a given item
 */
public class Item extends CommonEntityType {

    /**
     * The default item icon path
     */
    static final String DEFAULT_ITEM_ICON_PATH = "Textures/icons/itemIconItemGeneric.png";

    /**
     * Maximum stack of a given block type
     */
    static final int MAX_BLOCK_STACK = 250;

    /**
     * Default max stack for spawn items
     */
    static final int SPAWN_ITEM_DEFAULT_MAX_STACK = 1;

    /**
     * The array of default tokens for all items
     */
    static final String[] DEFAULT_TOKENS = new String[]{
        "GRAVITY",
        "TARGETABLE",
    };

    /**
     * The idle animation for the item
     */
    String idleAnim;

    /**
     * The path for the icon texture for this item
     */
    String iconPath;

    /**
     * Weapon data for this item if it is an item
     */
    WeaponData weaponData;

    /**
     * The data defining how this item is equipped
     */
    EquipData equipData;

    /**
     * The audio data for the item
     */
    ItemAudio itemAudio;

    /**
     * The usage logic for a primary usage of this item
     */
    ItemUsage primaryUsage;

    /**
     * The usage logic for a secondary usage of this item
     */
    ItemUsage secondaryUsage;

    /**
     * Item fab data
     */
    ItemFabData fabData;

    /**
     * The maximum stack of this item
     */
    Integer maxStack;
    
    /**
     * Creates item data from a spawn item description
     * @param description The spawn item description
     * @return The item data
     */
    public static Item createSpawnItem(CommonEntityType objectData){
        SpawnItemDescription description = objectData.getSpawnItem();


        Item rVal = new Item();
        rVal.setId("spawn:" + objectData.getId());
        rVal.setDisplayName(objectData.getDisplayName());

        //max stack
        if(description.getMaxStack() != null){
            rVal.setMaxStack(description.getMaxStack());
        } else {
            rVal.setMaxStack(Item.SPAWN_ITEM_DEFAULT_MAX_STACK);
        }

        //grid-alignment data
        if(objectData.getGridAlignedData() != null){
            rVal.setGridAlignedData(objectData.getGridAlignedData());
        }


        if(description.getItemIcon() != null){
            rVal.iconPath = description.getItemIcon();
        } else {
            rVal.iconPath = Item.DEFAULT_ITEM_ICON_PATH;
        }
        if(description.getGraphicsTemplate() != null){
            rVal.setGraphicsTemplate(description.getGraphicsTemplate());
        } else {
            throw new Error("Need to implement handling for when no graphics template is provided!");
        }


        //set usage
        ItemUsage usage = new ItemUsage();
        usage.setSpawnEntityId(objectData.getId());
        rVal.setSecondaryUsage(usage);

        
        //attach common tokens
        rVal.setTokens(Arrays.asList(DEFAULT_TOKENS));

        return rVal;
    }

    /**
     * Gets the id of the item type for a given block type
     * @param blockType The block type
     * @return The id of the corresponding item data
     */
    public static String getBlockTypeId(BlockType blockType){
        return "block:" + blockType.getName();
    }

    /**
     * Creates item data from a block type
     * @param description The block type
     * @return The item data
     */
    public static Item createBlockItem(BlockType blockType){
        Item rVal = new Item();
        rVal.setId(Item.getBlockTypeId(blockType));
        rVal.setDisplayName(blockType.getName());


        if(blockType.getTexture() != null){
            rVal.iconPath = blockType.getTexture();
        } else {
            rVal.iconPath = Item.DEFAULT_ITEM_ICON_PATH;
        }


        NonproceduralModel modelData = new NonproceduralModel();
        modelData.setPath(AssetDataStrings.MODEL_BLOCK_SINGLE);
        GraphicsTemplate blockItemGraphicsTemplate = new GraphicsTemplate();
        blockItemGraphicsTemplate.setModel(modelData);
        rVal.setGraphicsTemplate(blockItemGraphicsTemplate);

        //set uniforms for the model
        Map<String,Map<String,Object>> meshUniformMap = new HashMap<String,Map<String,Object>>();
        Map<String,Object> uniforms = new HashMap<String,Object>();
        uniforms.put("blockAtlasIndex",Globals.blockTextureAtlas.getVoxelTypeOffset(blockType.getId()));
        meshUniformMap.put(GeometryModelGen.MESH_NAME_BLOCK_SINGLE,uniforms);
        modelData.setUniforms(meshUniformMap);

        //set item class
        rVal.equipData = new EquipData();
        rVal.equipData.equipClass = "tool";


        //set usage
        ItemUsage usage = new ItemUsage();
        usage.setBlockId(blockType.getId());
        usage.setOnlyOnMouseDown(true);
        rVal.setSecondaryUsage(usage);
        rVal.setPrimaryUsage(usage);

        //set stacking data
        rVal.setMaxStack(MAX_BLOCK_STACK);

        
        //attach common tokens
        List<String> tokens = new LinkedList<String>(Arrays.asList(DEFAULT_TOKENS));
        tokens.add(CursorState.CURSOR_BLOCK_TOKEN);
        rVal.setTokens(tokens);

        return rVal;
    }

    /**
     * Gets the id of the item type for a given voxel type
     * @param voxelType The voxel type
     * @return The id of the corresponding item data
     */
    public static String getVoxelTypeId(VoxelType voxelType){
        return "vox:" + voxelType.getName();
    }

    /**
     * Creates item data from a voxel type
     * @param description The voxel type
     * @return The item data
     */
    public static Item createVoxelItem(VoxelType voxelType){
        Item rVal = new Item();
        rVal.setId(Item.getVoxelTypeId(voxelType));
        rVal.setDisplayName(voxelType.getName());


        if(voxelType.getTexture() != null){
            rVal.iconPath = voxelType.getTexture();
        } else {
            rVal.iconPath = Item.DEFAULT_ITEM_ICON_PATH;
        }


        NonproceduralModel modelData = new NonproceduralModel();
        modelData.setPath(AssetDataStrings.MODEL_BLOCK_SINGLE);
        GraphicsTemplate blockItemGraphicsTemplate = new GraphicsTemplate();
        blockItemGraphicsTemplate.setModel(modelData);
        rVal.setGraphicsTemplate(blockItemGraphicsTemplate);

        //set uniforms for the model
        //TODO: texture work for single voxel
        // Map<String,Map<String,Object>> meshUniformMap = new HashMap<String,Map<String,Object>>();
        // Map<String,Object> uniforms = new HashMap<String,Object>();
        // if(Globals.voxelTextureAtlas.getVoxelTypeOffset(voxelType.getId()) == BlockTextureAtlas.MISSING && voxelType.getId() != BlockChunkData.BLOCK_TYPE_EMPTY){
        //     LoggerInterface.loggerEngine.WARNING("Block type " + voxelType.getId() + " missing in BlockTextureAtlas");
        // }
        // uniforms.put("blockAtlasIndex",Globals.voxelTextureAtlas.getVoxelTypeOffset(voxelType.getId()));
        // meshUniformMap.put(RenderUtils.MESH_NAME_BLOCK_SINGLE,uniforms);
        // modelData.setUniforms(meshUniformMap);

        //set item class
        rVal.equipData = new EquipData();
        rVal.equipData.equipClass = "tool";


        //set usage
        ItemUsage usage = new ItemUsage();
        usage.setVoxelId(voxelType.getId());
        usage.setOnlyOnMouseDown(true);
        rVal.setSecondaryUsage(usage);
        rVal.setPrimaryUsage(usage);

        //set stacking data
        rVal.setMaxStack(MAX_BLOCK_STACK);

        
        //attach common tokens
        List<String> tokens = new LinkedList<String>(Arrays.asList(DEFAULT_TOKENS));
        tokens.add(CursorState.CURSOR_TOKEN);
        rVal.setTokens(tokens);

        return rVal;
    }

    /**
     * the idle animation for the item
     * @return
     */
    public String getIdleAnim(){
        return idleAnim;
    }
    
    /**
     * the path for the icon texture for this item
     * @return
     */
    public String getIconPath(){
        return iconPath;
    }

    /**
     * weapon data for this item if it is an item
     * @return
     */
    public WeaponData getWeaponData(){
        return weaponData;
    }

    /**
     * Gets the equip data for the item type
     * @return The equip data
     */
    public EquipData getEquipData(){
        return equipData;
    }

    /**
     * Gets the item audio data
     * @return The audio data if specified, null otherwise
     */
    public ItemAudio getItemAudio(){
        return itemAudio;
    }

    /**
     * Gets the secondary usage logic of this item
     * @return The secondary usage logic
     */
    public ItemUsage getSecondaryUsage(){
        return secondaryUsage;
    }

    /**
     * Sets the secondary usage logic of this item
     * @param secondaryUsage The secondary usage logic
     */
    public void setSecondaryUsage(ItemUsage secondaryUsage){
        this.secondaryUsage = secondaryUsage;
    }

    /**
     * Gets the primary usage logic of this item
     * @return The primary usage logic
     */
    public ItemUsage getPrimaryUsage(){
        return primaryUsage;
    }

    /**
     * Sets the primary usage logic of this item
     * @param primaryUsage The primary usage logic
     */
    public void setPrimaryUsage(ItemUsage primaryUsage){
        this.primaryUsage = primaryUsage;
    }

    /**
     * Gets the fab data for this item
     * @return The fab data
     */
    public ItemFabData getFabData(){
        return fabData;
    }

    /**
     * Gets the maximum stack allowed of this item
     * @return The maximum stack allowed
     */
    public Integer getMaxStack() {
        return maxStack;
    }

    /**
     * Sets the maximum stack allowed of this item
     * @param maxStack The maximum stack allowed
     */
    public void setMaxStack(Integer maxStack) {
        this.maxStack = maxStack;
    }

    

}

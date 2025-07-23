package electrosphere.engine.assetmanager;

/**
 * Assets that should be included by the engine by default or generated
 */
public class AssetDataStrings {
    public static final String ASSET_STRING_BITMAP_FONT_MESH_NAME = "quad";
    public static final String ASSET_STRING_SKYBOX_BASIC = "skyboxBasic";
    public static final String BITMAP_CHARACTER_MODEL = "bitmapCharacterModel";
    public static final String LEAVES_MODEL = "leaves";

    /**
     * Shaders
     */
    public static final String SHADER_DEFAULT_VERT = "Shaders/VertexShader.vs";
    public static final String SHADER_DEFAULT_FRAG = "Shaders/FragmentShader.fs";
    public static final String SHADER_BLOCK_SINGLE_VERT = "Shaders/entities/blocksingle/block.vs";
    public static final String SHADER_BLOCK_SINGLE_FRAG = "Shaders/entities/blocksingle/block.fs";

    /**
     * The basic geometry of the engine
     */
    public static final String UNITSPHERE = "unitSphere";
    public static final String UNITCYLINDER = "unitCylinder";
    public static final String UNITCAPSULE = "Models/basic/geometry/unitcapsule.glb";
    public static final String UNITCUBE = "unitCube";
    public static final String MODEL_PARTICLE = "particleModel";
    public static final String TEXTURE_PARTICLE = "particleTexture";
    public static final String POSE_EMPTY = "poseEmpty";
    public static final String MODEL_BLOCK_SINGLE = "modelBlockSingle";
    public static final String MODEL_IMAGE_PLANE = "modelImagePlane";

    /**
     * Fundamental textures of the engine
     */
    public static final String TEXTURE_TEAL_TRANSPARENT = "Textures/color/transparent_teal.png";
    public static final String TEXTURE_RED_TRANSPARENT = "Textures/transparent_red.png";
    public static final String TEXTURE_YELLOW_TRANSPARENT = "Textures/color/transparent_yellow.png";
    public static final String TEXTURE_BLUE_TRANSPARENT = "Textures/transparent_blue.png";
    public static final String TEXTURE_GREY_TRANSPARENT = "Textures/transparent_grey.png";
    public static final String TEXTURE_BLACK = "Textures/b1.png";
    public static final String TEXTURE_WHITE = "Textures/w1.png";
    public static final String TEXTURE_OFF_WHITE = "Textures/ow1.png";
    public static final String TEXTURE_DEFAULT = "Textures/color/Testing1.png";

    /**
     * Atlas texture paths
     */
    public static final String TEXTURE_BLOCK_ATLAS = "textureBlockAtlas";

    /**
     * UI textures
     */
    public static final String UI_ENGINE_LOGO_1 = "Textures/engine/stormenginelogo1.png";
    public static final String UI_FRAME_TEXTURE_DEFAULT_1 = "Textures/ui/uiFrame1.png";
    public static final String UI_FRAME_TEXTURE_DEFAULT_2 = "Textures/ui/uiFrame2.png";
    public static final String UI_FRAME_TEXTURE_DEFAULT_3 = "Textures/ui/panel-001.png";

    /**
     * UI icon textures
     */
    public static final String UI_TEXTURE_ITEM_ICON_GENERIC = "Textures/icons/itemIconItemGeneric.png";

    /**
     * UI generic audio
     */
    public static final String UI_TONE_CONFIRM_PRIMARY = "Audio/ui/generic/confirm_style_4_001.wav";
    public static final String UI_TONE_CONFIRM_SECONDARY = "Audio/ui/generic/confirm_style_4_003.wav";
    public static final String UI_TONE_CURSOR_PRIMARY = "Audio/ui/generic/cursor_style_2.wav";
    public static final String UI_TONE_CURSOR_SECONDARY = "Audio/ui/generic/cursor_style_4.wav";
    public static final String UI_TONE_BACK_PRIMARY = "Audio/ui/generic/back_style_4_001.wav";
    public static final String UI_TONE_BACK_SECONDARY = "Audio/ui/generic/back_style_4_003.wav";
    public static final String UI_TONE_ERROR_PRIMARY = "Audio/ui/generic/error_style_4_002.wav";
    public static final String UI_TONE_ERROR_SECONDARY = "Audio/ui/generic/error_style_4_001.wav";

    /**
     * UI button-specific audio
     */
    public static final String UI_TONE_BUTTON_TITLE = "Audio/ui/generic/back_style_4_007.wav";

    /**
     * UI menu audio
     */
    public static final String UI_SFX_INVENTORY_OPEN = "Audio/ui/menu/Open Inventory Bag A.wav";
    public static final String UI_SFX_INVENTORY_CLOSE = "Audio/ui/menu/Close Inventory Bag A.wav";

    /**
     * UI item audio
     */
    public static final String UI_SFX_ITEM_GRAB = "Audio/ui/items/inventoryGrabItem.ogg";
    public static final String UI_SFX_ITEM_RELEASE = "Audio/ui/items/inventorySlotItem.ogg";

    /**
     * Compute shaders
     */
    public static final String COMPUTE_LIGHT_CLUSTER = "Shaders/core/light/cluster.comp";
    public static final String COMPUTE_LIGHT_CULL = "Shaders/core/light/cull.comp";

    /**
     * Interaction-specific audio
     */
    public static final String INTERACT_SFX_DIG = "Audio/interact/Medium Stones Impact C.wav";
    public static final String INTERACT_SFX_BLOCK_PICKUP = "Audio/interact/Grab Cloth High A.wav";
    public static final String INTERACT_SFX_BLOCK_PLACE = "Audio/interact/High Five A.wav";

    /**
     * Debug geometry of the engine
     */
    public static final String MODEL_WAYPOINT = "Models/engine/waypoint.glb";

    /**
     * Skybox
     */
    public static final String SHADER_SKYBOX_VERT = "Shaders/entities/skysphere/skysphere.vs";
    public static final String SHADER_SKYBOX_FRAG = "Shaders/entities/skysphere/skysphere.fs";

}

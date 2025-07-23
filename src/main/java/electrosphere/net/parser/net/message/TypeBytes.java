package electrosphere.net.parser.net.message;

/**
 * The constants used in serializing/deserializing messages
 */
public class TypeBytes {
    /**
     * Message categories
     */
    public static final byte MESSAGE_TYPE_ENTITY = 1;
    public static final byte MESSAGE_TYPE_LORE = 2;
    public static final byte MESSAGE_TYPE_PLAYER = 3;
    public static final byte MESSAGE_TYPE_TERRAIN = 4;
    public static final byte MESSAGE_TYPE_SERVER = 5;
    public static final byte MESSAGE_TYPE_AUTH = 6;
    public static final byte MESSAGE_TYPE_CHARACTER = 7;
    public static final byte MESSAGE_TYPE_INVENTORY = 8;
    public static final byte MESSAGE_TYPE_SYNCHRONIZATION = 9;
    public static final byte MESSAGE_TYPE_COMBAT = 10;
    /*
     Entity subcategories
    */
    public static final byte ENTITY_MESSAGE_TYPE_CREATE = 0;
    public static final byte ENTITY_MESSAGE_TYPE_MOVEUPDATE = 1;
    public static final byte ENTITY_MESSAGE_TYPE_ATTACKUPDATE = 2;
    public static final byte ENTITY_MESSAGE_TYPE_STARTATTACK = 3;
    public static final byte ENTITY_MESSAGE_TYPE_KILL = 4;
    public static final byte ENTITY_MESSAGE_TYPE_DESTROY = 5;
    public static final byte ENTITY_MESSAGE_TYPE_SETPROPERTY = 6;
    public static final byte ENTITY_MESSAGE_TYPE_ATTACHENTITYTOENTITY = 7;
    public static final byte ENTITY_MESSAGE_TYPE_UPDATEENTITYVIEWDIR = 8;
    public static final byte ENTITY_MESSAGE_TYPE_SYNCPHYSICS = 9;
    public static final byte ENTITY_MESSAGE_TYPE_INTERACT = 10;
    /*
     Entity packet sizes
    */
    public static final byte ENTITY_MESSAGE_TYPE_MOVEUPDATE_SIZE = 86;
    public static final byte ENTITY_MESSAGE_TYPE_ATTACKUPDATE_SIZE = 74;
    public static final byte ENTITY_MESSAGE_TYPE_STARTATTACK_SIZE = 2;
    public static final byte ENTITY_MESSAGE_TYPE_KILL_SIZE = 14;
    public static final byte ENTITY_MESSAGE_TYPE_DESTROY_SIZE = 6;
    public static final byte ENTITY_MESSAGE_TYPE_SETPROPERTY_SIZE = 22;
    public static final byte ENTITY_MESSAGE_TYPE_UPDATEENTITYVIEWDIR_SIZE = 34;
    public static final short ENTITY_MESSAGE_TYPE_SYNCPHYSICS_SIZE = 167;

    /*
     Lore subcategories
    */
    public static final byte LORE_MESSAGE_TYPE_REQUESTRACES = 0;
    public static final byte LORE_MESSAGE_TYPE_RESPONSERACES = 1;
    public static final byte LORE_MESSAGE_TYPE_TEMPORALUPDATE = 2;
    /*
     Lore packet sizes
    */
    public static final byte LORE_MESSAGE_TYPE_REQUESTRACES_SIZE = 2;

    /*
     Player subcategories
    */
    public static final byte PLAYER_MESSAGE_TYPE_SET_ID = 0;
    public static final byte PLAYER_MESSAGE_TYPE_SETINITIALDISCRETEPOSITION = 1;
    /*
     Player packet sizes
    */
    public static final byte PLAYER_MESSAGE_TYPE_SET_ID_SIZE = 6;
    public static final byte PLAYER_MESSAGE_TYPE_SETINITIALDISCRETEPOSITION_SIZE = 14;

    /*
     Terrain subcategories
    */
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTMETADATA = 0;
    public static final byte TERRAIN_MESSAGE_TYPE_RESPONSEMETADATA = 1;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTEDITVOXEL = 2;
    public static final byte TERRAIN_MESSAGE_TYPE_UPDATEVOXEL = 3;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTUSETERRAINPALETTE = 4;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTDESTROYTERRAIN = 5;
    public static final byte TERRAIN_MESSAGE_TYPE_SPAWNPOSITION = 6;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTCHUNKDATA = 7;
    public static final byte TERRAIN_MESSAGE_TYPE_SENDCHUNKDATA = 8;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDCHUNKDATA = 9;
    public static final byte TERRAIN_MESSAGE_TYPE_SENDREDUCEDCHUNKDATA = 10;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDBLOCKDATA = 11;
    public static final byte TERRAIN_MESSAGE_TYPE_SENDREDUCEDBLOCKDATA = 12;
    public static final byte TERRAIN_MESSAGE_TYPE_UPDATEBLOCK = 13;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTFLUIDDATA = 14;
    public static final byte TERRAIN_MESSAGE_TYPE_SENDFLUIDDATA = 15;
    public static final byte TERRAIN_MESSAGE_TYPE_UPDATEFLUIDDATA = 16;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTEDITBLOCK = 17;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTPLACEFAB = 18;
    /*
     Terrain packet sizes
    */
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTMETADATA_SIZE = 2;
    public static final byte TERRAIN_MESSAGE_TYPE_RESPONSEMETADATA_SIZE = 30;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTEDITVOXEL_SIZE = 34;
    public static final byte TERRAIN_MESSAGE_TYPE_UPDATEVOXEL_SIZE = 34;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTUSETERRAINPALETTE_SIZE = 38;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTDESTROYTERRAIN_SIZE = 34;
    public static final byte TERRAIN_MESSAGE_TYPE_SPAWNPOSITION_SIZE = 26;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTCHUNKDATA_SIZE = 14;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDCHUNKDATA_SIZE = 18;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTREDUCEDBLOCKDATA_SIZE = 18;
    public static final byte TERRAIN_MESSAGE_TYPE_UPDATEBLOCK_SIZE = 34;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTFLUIDDATA_SIZE = 14;
    public static final byte TERRAIN_MESSAGE_TYPE_REQUESTEDITBLOCK_SIZE = 38;

    /*
     Server subcategories
    */
    public static final byte SERVER_MESSAGE_TYPE_PING = 0;
    public static final byte SERVER_MESSAGE_TYPE_PONG = 1;
    public static final byte SERVER_MESSAGE_TYPE_DISCONNECT = 2;
    /*
     Server packet sizes
    */
    public static final byte SERVER_MESSAGE_TYPE_PING_SIZE = 2;
    public static final byte SERVER_MESSAGE_TYPE_PONG_SIZE = 2;
    public static final byte SERVER_MESSAGE_TYPE_DISCONNECT_SIZE = 2;

    /*
     Auth subcategories
    */
    public static final byte AUTH_MESSAGE_TYPE_AUTHREQUEST = 0;
    public static final byte AUTH_MESSAGE_TYPE_AUTHDETAILS = 1;
    public static final byte AUTH_MESSAGE_TYPE_AUTHSUCCESS = 2;
    public static final byte AUTH_MESSAGE_TYPE_AUTHFAILURE = 3;
    /*
     Auth packet sizes
    */
    public static final byte AUTH_MESSAGE_TYPE_AUTHREQUEST_SIZE = 2;
    public static final byte AUTH_MESSAGE_TYPE_AUTHSUCCESS_SIZE = 2;
    public static final byte AUTH_MESSAGE_TYPE_AUTHFAILURE_SIZE = 2;

    /*
     Character subcategories
    */
    public static final byte CHARACTER_MESSAGE_TYPE_REQUESTCHARACTERLIST = 0;
    public static final byte CHARACTER_MESSAGE_TYPE_RESPONSECHARACTERLIST = 1;
    public static final byte CHARACTER_MESSAGE_TYPE_REQUESTCREATECHARACTER = 2;
    public static final byte CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERSUCCESS = 3;
    public static final byte CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERFAILURE = 4;
    public static final byte CHARACTER_MESSAGE_TYPE_REQUESTSPAWNCHARACTER = 5;
    public static final byte CHARACTER_MESSAGE_TYPE_RESPONSESPAWNCHARACTER = 6;
    public static final byte CHARACTER_MESSAGE_TYPE_EDITORSWAP = 7;
    /*
     Character packet sizes
    */
    public static final byte CHARACTER_MESSAGE_TYPE_REQUESTCHARACTERLIST_SIZE = 2;
    public static final byte CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERSUCCESS_SIZE = 2;
    public static final byte CHARACTER_MESSAGE_TYPE_RESPONSECREATECHARACTERFAILURE_SIZE = 2;
    public static final byte CHARACTER_MESSAGE_TYPE_EDITORSWAP_SIZE = 2;

    /*
     Inventory subcategories
    */
    public static final byte INVENTORY_MESSAGE_TYPE_ADDITEMTOINVENTORY = 0;
    public static final byte INVENTORY_MESSAGE_TYPE_REMOVEITEMFROMINVENTORY = 1;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTEQUIPITEM = 2;
    public static final byte INVENTORY_MESSAGE_TYPE_SERVERCOMMANDMOVEITEMCONTAINER = 3;
    public static final byte INVENTORY_MESSAGE_TYPE_SERVERCOMMANDEQUIPITEM = 4;
    public static final byte INVENTORY_MESSAGE_TYPE_SERVERCOMMANDUNEQUIPITEM = 5;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTUNEQUIPITEM = 6;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTSTOREITEM = 7;
    public static final byte INVENTORY_MESSAGE_TYPE_SERVERCOMMANDSTOREITEM = 8;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTWATCHINVENTORY = 9;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTUNWATCHINVENTORY = 10;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDTOOLBAR = 11;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDNATURAL = 12;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTUPDATETOOLBAR = 13;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTPERFORMITEMACTION = 14;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTCRAFT = 15;
    public static final byte INVENTORY_MESSAGE_TYPE_SERVERUPDATEITEMCHARGES = 16;
    /*
     Inventory packet sizes
    */
    public static final byte INVENTORY_MESSAGE_TYPE_REMOVEITEMFROMINVENTORY_SIZE = 6;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTWATCHINVENTORY_SIZE = 6;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTUNWATCHINVENTORY_SIZE = 6;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDTOOLBAR_SIZE = 10;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTADDNATURAL_SIZE = 6;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTUPDATETOOLBAR_SIZE = 6;
    public static final byte INVENTORY_MESSAGE_TYPE_CLIENTREQUESTCRAFT_SIZE = 14;
    public static final byte INVENTORY_MESSAGE_TYPE_SERVERUPDATEITEMCHARGES_SIZE = 10;

    /*
     Synchronization subcategories
    */
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTSTATE = 0;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTSTRINGSTATE = 1;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTINTSTATE = 2;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTLONGSTATE = 3;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTFLOATSTATE = 4;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTDOUBLESTATE = 5;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_CLIENTREQUESTBTREEACTION = 6;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_SERVERNOTIFYBTREETRANSITION = 7;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_ATTACHTREE = 8;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_DETATCHTREE = 9;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_LOADSCENE = 10;
    /*
     Synchronization packet sizes
    */
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTSTATE_SIZE = 18;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTINTSTATE_SIZE = 18;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTLONGSTATE_SIZE = 22;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTFLOATSTATE_SIZE = 18;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_UPDATECLIENTDOUBLESTATE_SIZE = 22;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_CLIENTREQUESTBTREEACTION_SIZE = 14;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_SERVERNOTIFYBTREETRANSITION_SIZE = 18;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_ATTACHTREE_SIZE = 10;
    public static final byte SYNCHRONIZATION_MESSAGE_TYPE_DETATCHTREE_SIZE = 10;

    /*
     Combat subcategories
    */
    public static final byte COMBAT_MESSAGE_TYPE_SERVERREPORTHITBOXCOLLISION = 0;
    /*
     Combat packet sizes
    */

}


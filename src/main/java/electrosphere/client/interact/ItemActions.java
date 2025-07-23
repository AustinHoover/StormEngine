package electrosphere.client.interact;

import org.joml.Vector3d;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.script.ClientScriptUtils;
import electrosphere.client.terrain.editing.BlockEditing;
import electrosphere.collision.CollisionEngine;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attack.ClientAttackTree;
import electrosphere.entity.state.attack.ShooterTree;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.net.parser.net.message.InventoryMessage;

/**
 * Utilities for telling the server to perform actions with an item that the player has equipped
 */
public class ItemActions {

    /**
     * the item action code for left clicking
     */
    public static final int ITEM_ACTION_CODE_PRIMARY = 0;

    /**
     * the item action code for right clicking
     */
    public static final int ITEM_ACTION_CODE_SECONDARY = 1;

    /**
     * the state for performing the item action code
     */
    public static final int ITEM_ACTION_CODE_STATE_ON = 1;

    /**
     * the state for releasing the item action code
     */
    public static final int ITEM_ACTION_CODE_STATE_OFF = 0;
    
    /**
     * the state for performing the item action code
     */
    public static final int ITEM_ACTION_CODE_STATE_REPEAT = 2;
    
    /**
     * Attempts to perform the primary item action
     */
    public static void attemptPrimaryItemAction(){
        Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
        Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
        Vector3d cursorPos = Globals.cursorState.getCursorPosition();
        if(cursorPos == null){
            cursorPos = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
        }
        if(cursorPos == null){
            cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
        }

        //send server message if we're not doing a block edit
        //client sends custom packets for block editing
        boolean sendServerMessage = true;

        //do any immediate client side calculations here (ie start playing an animation until we get response from server)
        if(Globals.clientState.playerEntity != null){
            ClientAttackTree attackTree = CreatureUtils.clientGetAttackTree(Globals.clientState.playerEntity);
            if(attackTree != null){
                attackTree.start();
            }
            ShooterTree shooterTree = ShooterTree.getShooterTree(Globals.clientState.playerEntity);
            if(shooterTree != null){
                shooterTree.fire();
            }
            ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(Globals.clientState.playerEntity);
            Entity primaryEntity = clientToolbarState.getCurrentPrimaryItem();
            if(primaryEntity != null && Globals.gameConfigCurrent.getItemMap().getItem(primaryEntity) != null){
                Item data = Globals.gameConfigCurrent.getItemMap().getItem(primaryEntity);
                if(data.getPrimaryUsage() != null){
                    if(data.getPrimaryUsage().getClientHook() != null){
                        ClientScriptUtils.fireSignal(data.getPrimaryUsage().getClientHook());
                    }
                    if(data.getPrimaryUsage().getBlockId() != null){
                        BlockEditing.destroyBlock();
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.INTERACT_SFX_BLOCK_PLACE, VirtualAudioSourceType.CREATURE, false);
                    }
                    if(data.getPrimaryUsage().getSuppressServerRequest() != null && data.getPrimaryUsage().getSuppressServerRequest() == true){
                        sendServerMessage = false;
                    }
                }
            }
        }

        if(sendServerMessage){
            //tell the server we want the secondary hand item to START doing something
            Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientRequestPerformItemActionMessage(
                "handRight",
                ITEM_ACTION_CODE_PRIMARY,
                ITEM_ACTION_CODE_STATE_ON,
                cursorPos.x,
                cursorPos.y,
                cursorPos.z
            ));
        }
    }

    /**
     * Repeats the primary item action
     */
    public static void repeatPrimaryItemAction(){
        Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
        Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
        Vector3d cursorPos = Globals.cursorState.getCursorPosition();
        if(cursorPos == null){
            cursorPos = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
        }
        if(cursorPos == null){
            cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
        }
        //tell the server we want the secondary hand item to STOP doing something
        Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientRequestPerformItemActionMessage(
            "handRight",
            ITEM_ACTION_CODE_PRIMARY,
            ITEM_ACTION_CODE_STATE_REPEAT,
            cursorPos.x,
            cursorPos.y,
            cursorPos.z
        ));
        //do any immediate client side calculations here (ie start playing an animation until we get response from server)
    }

    /**
     * Releases the primary item action
     */
    public static void releasePrimaryItemAction(){
        Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
        Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
        Vector3d cursorPos = Globals.cursorState.getCursorPosition();
        if(cursorPos == null){
            cursorPos = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
        }
        if(cursorPos == null){
            cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
        }
        //tell the server we want the secondary hand item to STOP doing something
        Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientRequestPerformItemActionMessage(
            "handRight",
            ITEM_ACTION_CODE_PRIMARY,
            ITEM_ACTION_CODE_STATE_OFF,
            cursorPos.x,
            cursorPos.y,
            cursorPos.z
        ));
        //do any immediate client side calculations here (ie start playing an animation until we get response from server)
        if(Globals.clientState.playerEntity != null){
            // Vector3f cameraEyeVector = CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera);
            ClientAttackTree attackTree = CreatureUtils.clientGetAttackTree(Globals.clientState.playerEntity);
            if(attackTree != null){
                // CreatureUtils.setFacingVector(Globals.playerCharacter, new Vector3d(-cameraEyeVector.x,0,-cameraEyeVector.z).normalize());
                attackTree.release();
            }
        }
    }

    /**
     * Attempts to perform the secondary item action
     */
    public static void attemptSecondaryItemAction(){
        Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
        Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
        Vector3d cursorPos = Globals.cursorState.getCursorPosition();
        if(cursorPos == null){
            cursorPos = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
        }
        if(cursorPos == null){
            cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
        }

        //send server message if we're not doing a block edit
        //client sends custom packets for block editing
        boolean sendServerMessage = true;

        //do any immediate client side calculations here (ie start playing an animation until we get response from server)
        if(Globals.clientState.playerEntity != null){
            ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(Globals.clientState.playerEntity);
            Entity primaryEntity = clientToolbarState.getCurrentPrimaryItem();
            if(primaryEntity != null && Globals.gameConfigCurrent.getItemMap().getItem(primaryEntity) != null){
                Item data = Globals.gameConfigCurrent.getItemMap().getItem(primaryEntity);
                if(data.getSecondaryUsage() != null){
                    if(data.getSecondaryUsage().getClientHook() != null){
                        ClientScriptUtils.fireSignal(data.getSecondaryUsage().getClientHook());
                    }
                    if(data.getSecondaryUsage().getBlockId() != null){
                        BlockEditing.editBlock((short)(int)data.getSecondaryUsage().getBlockId(),(short)0);
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(AssetDataStrings.INTERACT_SFX_BLOCK_PLACE, VirtualAudioSourceType.CREATURE, false);
                    }
                    if(data.getSecondaryUsage().getSuppressServerRequest() != null && data.getSecondaryUsage().getSuppressServerRequest() == true){
                        sendServerMessage = false;
                    }
                }
            }
        }
        //send server message
        if(sendServerMessage){
            //tell the server we want the secondary hand item to START doing something
            Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientRequestPerformItemActionMessage(
                "handRight",
                ITEM_ACTION_CODE_SECONDARY,
                ITEM_ACTION_CODE_STATE_ON,
                cursorPos.x,
                cursorPos.y,
                cursorPos.z
            ));
        }
    }

    /**
     * Repeats the secondary item action
     */
    public static void repeatSecondaryItemAction(){
        Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
        Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
        Vector3d cursorPos = Globals.cursorState.getCursorPosition();
        if(cursorPos == null){
            cursorPos = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
        }
        if(cursorPos == null){
            cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
        }


        //send server message if we're not doing a block edit
        //client sends custom packets for block editing
        boolean sendServerMessage = true;
        
        //do any immediate client side calculations here (ie start playing an animation until we get response from server)
        if(Globals.clientState.playerEntity != null){
            ClientToolbarState clientToolbarState = ClientToolbarState.getClientToolbarState(Globals.clientState.playerEntity);
            Entity primaryEntity = clientToolbarState.getCurrentPrimaryItem();
            if(primaryEntity != null && Globals.gameConfigCurrent.getItemMap().getItem(primaryEntity) != null){
                Item data = Globals.gameConfigCurrent.getItemMap().getItem(primaryEntity);
                if(data.getSecondaryUsage() != null){
                    if(data.getSecondaryUsage().getOnlyOnMouseDown() != null && data.getSecondaryUsage().getOnlyOnMouseDown() == true){
                    } else {
                        if(data.getSecondaryUsage().getClientHook() != null){
                            ClientScriptUtils.fireSignal(data.getSecondaryUsage().getClientHook());
                        }
                        if(data.getSecondaryUsage().getSuppressServerRequest() != null && data.getSecondaryUsage().getSuppressServerRequest() == true){
                            sendServerMessage = false;
                        }
                    }
                }
            }
        }

        //tell the server we want the secondary hand item to STOP doing something
        if(sendServerMessage){
            Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientRequestPerformItemActionMessage(
                "handRight",
                ITEM_ACTION_CODE_SECONDARY,
                ITEM_ACTION_CODE_STATE_REPEAT,
                cursorPos.x,
                cursorPos.y,
                cursorPos.z
            ));
        }
    }

    /**
     * Releases the secondary item action
     */
    public static void releaseSecondaryItemAction(){
        Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
        Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
        Vector3d cursorPos = Globals.cursorState.getCursorPosition();
        if(cursorPos == null){
            cursorPos = Globals.clientState.clientSceneWrapper.getCollisionEngine().rayCastPosition(new Vector3d(centerPos), new Vector3d(eyePos).mul(-1.0), CollisionEngine.DEFAULT_INTERACT_DISTANCE);
        }
        if(cursorPos == null){
            cursorPos = new Vector3d(centerPos).add(new Vector3d(eyePos).normalize().mul(-CollisionEngine.DEFAULT_INTERACT_DISTANCE));
        }
        //tell the server we want the secondary hand item to STOP doing something
        Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientRequestPerformItemActionMessage(
            "handRight",
            ITEM_ACTION_CODE_SECONDARY,
            ITEM_ACTION_CODE_STATE_OFF,
            cursorPos.x,
            cursorPos.y,
            cursorPos.z
        ));
        //do any immediate client side calculations here (ie start playing an animation until we get response from server)
    }

}

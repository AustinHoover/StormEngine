package electrosphere.client.interact;

import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.entity.crosshair.Crosshair;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.dialog.DialogMenuGenerator;
import electrosphere.client.ui.menu.ingame.InventoryMainWindow;
import electrosphere.data.entity.common.interact.InteractionData;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.state.inventory.ClientInventoryState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.types.common.CommonEntityFlags;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.InventoryMessage;

/**
 * Stores logic for interaction button
 */
public class ButtonInteraction {

    /**
     * Handles a button interaction event
     */
    public static void handleButtonInteraction(){
        if(Globals.clientState.playerEntity != null && Globals.clientState.playerCamera != null){
            Entity camera = Globals.clientState.playerCamera;
            Vector3d eyePos = new Vector3d(CameraEntityUtils.getCameraEye(camera)).mul(-1.0);
            Vector3d centerPos = new Vector3d(CameraEntityUtils.getCameraCenter(camera));
            Entity target = ClientInteractionEngine.rayCast(centerPos, eyePos);
            if(target != null && CommonEntityFlags.isInteractable(target)){
                Globals.clientState.interactionTarget = target;
                ButtonInteraction.performInteraction(target);
            } else if(ClientEquipState.hasEquipState(Globals.clientState.playerEntity) && Crosshair.hasTarget()){
                if(InventoryUtils.hasNaturalInventory(Globals.clientState.playerEntity)){
                    ClientInventoryState.clientAttemptStoreItem(Globals.clientState.playerEntity, Crosshair.getTarget());
                }
            }
        }
    }

    /**
     * Performs a button interaction
     * @param target The targeted entity
     */
    private static void performInteraction(Entity target){
        InteractionData interactionData = CommonEntityUtils.getCommonData(target).getButtonInteraction();
        switch(interactionData.getOnInteract()){
            case InteractionData.ON_INTERACT_MENU: {
                WindowUtils.openInteractionMenu(interactionData.getWindowTarget(), interactionData.getWindowData());
            } break;
            case InteractionData.ON_INTERACT_HARVEST: {
                int serverEntityId = Globals.clientState.clientSceneWrapper.mapClientToServerId(target.getId());
                Globals.clientState.clientConnection.queueOutgoingMessage(EntityMessage.constructinteractMessage(serverEntityId, InteractionData.ON_INTERACT_HARVEST));
            } break;
            case InteractionData.ON_INTERACT_DOOR: {
                int serverEntityId = Globals.clientState.clientSceneWrapper.mapClientToServerId(target.getId());
                Globals.clientState.clientConnection.queueOutgoingMessage(EntityMessage.constructinteractMessage(serverEntityId, InteractionData.ON_INTERACT_DOOR));
            } break;
            case InteractionData.ON_INTERACT_DIALOG: {
                DialogMenuGenerator.displayEntityDialog(target);
            } break;
            case InteractionData.ON_INTERACT_INVENTORY: {
                LoggerInterface.loggerEngine.DEBUG("Interacting with inventory");
                InventoryMainWindow.viewInventory(target);
                InventoryMainWindow.viewInventory(Globals.clientState.playerEntity);
                Globals.clientState.clientConnection.queueOutgoingMessage(InventoryMessage.constructclientRequestWatchInventoryMessage(Globals.clientState.clientSceneWrapper.mapClientToServerId(target.getId())));
            } break;
            default: {
                throw new Error("Unhandled interaction signal " + interactionData.getOnInteract());
            }
        }
    }
    
}

package electrosphere.client.ui.menu.debug;

import org.joml.Vector3d;
import org.ode4j.ode.DBody;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.ui.menu.debug.entity.ImGuiEntityMacros;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.state.attack.ClientAttackTree;
import electrosphere.entity.state.server.ServerPlayerViewDirTree;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.net.parser.net.message.CharacterMessage;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import imgui.ImGui;

/**
 * Menus for player entity in particular
 */
public class ImGuiPlayerEntity {

    //player entity details
    public static ImGuiWindow playerEntityWindow;
    
    /**
     * Create player entity debug menu
     */
    public static void createPlayerEntityDebugWindow(){
        playerEntityWindow = new ImGuiWindow("Player Entity");
        playerEntityWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //player entity details
                ImGui.text("Player Entity Details");

                //data about player entity
                if(Globals.clientState.playerEntity != null){
                    Vector3d clientEntityPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
                    ImGui.text("Position: " + String.format("%.2f",clientEntityPos.x) + " " + String.format("%.2f",clientEntityPos.y) + " " + String.format("%.2f",clientEntityPos.z));
                    ImGui.text("Rotation: " + EntityUtils.getRotation(Globals.clientState.playerEntity));
                    
                    //physics on client
                    if(ImGui.collapsingHeader("Physics Data")){
                        drawPhysicsData();
                    }

                    //sync data
                    if(ImGui.collapsingHeader("Synchronization Data")){
                        drawSynchronizationData();
                    }

                    //camera details
                    if(ImGui.collapsingHeader("Camera Data")){
                        drawCameraData();
                    }

                }

                //
                //Camera controls
                if(ImGui.button("Toggle Player Camera Lock")){
                    Globals.cameraHandler.setTrackPlayerEntity(!Globals.cameraHandler.getTrackPlayerEntity());
                }
                if(ImGui.button("Toggle 1st/3rd Person")){
                    Globals.controlHandler.setIsThirdPerson(!Globals.controlHandler.cameraIsThirdPerson());
                }

                //
                //quick launch entity details
                if(ImGui.button("Client Details")){
                    ImGuiEntityMacros.showEntity(Globals.clientState.playerEntity);
                }
                ImGui.sameLine();

                //
                //swap editor/noneditor
                if(ImGui.button("Swap Entity")){
                    Globals.clientState.clientConnection.queueOutgoingMessage(CharacterMessage.constructEditorSwapMessage());
                }
                ImGui.sameLine();


                if(ImGui.button("1st Person Details")){
                    ImGuiEntityMacros.showEntity(Globals.clientState.firstPersonEntity);
                }
                ImGui.sameLine();
                
                if(ImGui.button("Server Details")){
                    int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId());
                    Entity serverPlayerEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
                    ImGuiEntityMacros.showEntity(serverPlayerEntity);
                }

                //
                //teleport to camera
                if(ImGui.button("TP to Cam")){
                    Globals.engineState.signalSystem.post(SignalType.ENGINE_SYNCHRONOUS_CODE, () -> {
                        Vector3d camPos = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
                        int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId());
                        Entity serverPlayerEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
                        ServerEntityUtils.repositionEntity(serverPlayerEntity, camPos);
                    });
                }


            }
        });
        playerEntityWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(playerEntityWindow);
    }

    /**
     * Draws data about physics for client entity
     */
    private static void drawPhysicsData(){
        //client-side tree stuff
        DBody body = PhysicsEntityUtils.getDBody(Globals.clientState.playerEntity);
        ClientAttackTree attackTree = ClientAttackTree.getClientAttackTree(Globals.clientState.playerEntity);
        if(body != null){
            ImGui.text("Velocity: " + body.getLinearVel());
            ImGui.text("Force: " + body.getForce());
            ImGui.text("Angular Velocity: " + body.getAngularVel());
            ImGui.text("Torque: " + body.getTorque());
            ImGui.text("Move Vector: " + CreatureUtils.getFacingVector(Globals.clientState.playerEntity));
            if(attackTree != null){
                ImGui.text("Attack Tree State: " + attackTree.getState());
            }
        }
    }

    /**
     * Draws data on server side for synchronization comparison
     */
    private static void drawSynchronizationData(){
        //server pos
        int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId());
        Entity serverPlayerEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
        if(serverPlayerEntity != null){
            ImGui.text("Position (Server): " + EntityUtils.getPosition(serverPlayerEntity));
            ImGui.text("Rotation (Server): " + EntityUtils.getRotation(serverPlayerEntity));

            //server-side physics stuff
            DBody serverBody = PhysicsEntityUtils.getDBody(serverPlayerEntity);
            if(serverBody != null){
                ImGui.text("Velocity (Server): " + serverBody.getLinearVel());
                ImGui.text("Force (Server): " + serverBody.getForce());
                ImGui.text("Move Vector (Server): " + CreatureUtils.getFacingVector(serverPlayerEntity));
                ImGui.text("Velocity (Server): " + CreatureUtils.getVelocity(serverPlayerEntity));
            }
            ImGui.text("View yaw (Server): " + ServerPlayerViewDirTree.getTree(serverPlayerEntity).getYaw());
            ImGui.text("View pitch (Server): " + ServerPlayerViewDirTree.getTree(serverPlayerEntity).getPitch());
        }
        if(Globals.serverState.server != null && Globals.serverState.server.getFirstConnection() != null && Globals.serverState.server.getFirstConnection().getPlayer() != null){
            ImGui.text("Player Object World Pos (Server): " + Globals.serverState.server.getFirstConnection().getPlayer().getWorldPos());
        }
    }

    /**
     * Draws camera data
     */
    private static void drawCameraData(){
        ImGui.text("Yaw: " + CameraEntityUtils.getCameraYaw(Globals.clientState.playerCamera));
        ImGui.text("Pitch: " + CameraEntityUtils.getCameraPitch(Globals.clientState.playerCamera));
    }

}

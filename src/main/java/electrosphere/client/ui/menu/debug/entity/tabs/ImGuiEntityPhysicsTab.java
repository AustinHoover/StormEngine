package electrosphere.client.ui.menu.debug.entity.tabs;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DSpace;

import electrosphere.client.ui.components.imgui.CollidableEditBlock;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import imgui.ImGui;

/**
 * Tab for both exploring and editing physics on this entity
 */
public class ImGuiEntityPhysicsTab {
    
    /**
     * Physics view
     */
    public static void drawPhysicsView(boolean show, Entity detailViewEntity){
        if(show && ImGui.collapsingHeader("Physics Details")){
            ImGui.indent();
            if(PhysicsEntityUtils.getDBody(detailViewEntity) != null){
                DBody physicsBody = PhysicsEntityUtils.getDBody(detailViewEntity);
                if(physicsBody != null){
                    ImGui.text("Position: " + physicsBody.getPosition());
                    ImGui.text("Rotation: " + physicsBody.getQuaternion());
                    ImGui.text("Velocity: " + physicsBody.getLinearVel());
                    ImGui.text("Force: " + physicsBody.getForce());
                    ImGui.text("Angular Velocity: " + physicsBody.getAngularVel());
                    ImGui.text("Torque: " + physicsBody.getTorque());
                    ImGui.text("Move Vector: " + CreatureUtils.getFacingVector(detailViewEntity));
                    ImGui.text("Velocity: " + CreatureUtils.getVelocity(detailViewEntity));
                    ImGui.text("Enabled: " + physicsBody.isEnabled());
                    ImGui.text("Kinematic: " + physicsBody.isKinematic());
                }
                //synchronized data
                if(
                    Globals.clientState.clientSceneWrapper.getScene().getEntityFromId(detailViewEntity.getId()) != null &&
                    Globals.clientState.clientSceneWrapper.mapClientToServerId(detailViewEntity.getId()) != -1
                ){
                    //detailViewEntity is a client entity
                    //get server entity
                    int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(detailViewEntity.getId());
                    Entity serverEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
                    DBody serverPhysicsBody = PhysicsEntityUtils.getDBody(serverEntity);
                    if(serverPhysicsBody != null){
                        ImGui.newLine();
                        ImGui.text("Linked server entity:");
                        ImGui.text("Position (Server): " + physicsBody.getPosition());
                        ImGui.text("Rotation (Server): " + physicsBody.getQuaternion());
                        ImGui.text("Velocity (Server): " + serverPhysicsBody.getLinearVel());
                        ImGui.text("Force (Server): " + serverPhysicsBody.getForce());
                        ImGui.text("Angular Velocity: " + serverPhysicsBody.getAngularVel());
                        ImGui.text("Torque: " + serverPhysicsBody.getTorque());
                        ImGui.text("Move Vector (Server): "+ CreatureUtils.getFacingVector(serverEntity));
                        ImGui.text("Velocity (Server): " + CreatureUtils.getVelocity(serverEntity));
                        ImGui.text("Enabled: " + serverPhysicsBody.isEnabled());
                        ImGui.text("Kinematic: " + serverPhysicsBody.isKinematic());
                    }
                } else if(Globals.clientState.clientSceneWrapper.containsServerId(detailViewEntity.getId())){
                    //detailViewEntity is a server entity
                    //get client entity
                    int clientId = Globals.clientState.clientSceneWrapper.mapServerToClientId(detailViewEntity.getId());
                    Entity clientEntity = Globals.clientState.clientSceneWrapper.getScene().getEntityFromId(clientId);
                    DBody clientPhysicsBody = PhysicsEntityUtils.getDBody(clientEntity);
                    if(clientPhysicsBody != null){
                        ImGui.newLine();
                        ImGui.text("Linked client entity:");
                        ImGui.text("Position (Client): " + physicsBody.getPosition());
                        ImGui.text("Rotation (Client): " + physicsBody.getQuaternion());
                        ImGui.text("Velocity (Client): " + clientPhysicsBody.getLinearVel());
                        ImGui.text("Force (Client): " + clientPhysicsBody.getForce());
                        ImGui.text("Angular Velocity: " + clientPhysicsBody.getAngularVel());
                        ImGui.text("Torque: " + clientPhysicsBody.getTorque());
                        ImGui.text("Move Vector (Client): " + CreatureUtils.getFacingVector(clientEntity));
                        ImGui.text("Velocity (Client): " + CreatureUtils.getVelocity(clientEntity));
                        ImGui.text("Enabled: " + clientPhysicsBody.isEnabled());
                        ImGui.text("Kinematic: " + clientPhysicsBody.isKinematic());
                    }
                }
                //Collidable editing
                if(physicsBody != null && physicsBody.getFirstGeom() != null && ImGui.collapsingHeader("Modify")){
                    CollidableTemplate template = (CollidableTemplate)detailViewEntity.getData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE);
                    CollidableEditBlock.drawCollidableEdit(physicsBody, template);
                }
            }

            if(PhysicsEntityUtils.getDGeom(detailViewEntity) != null){
                DGeom collider = PhysicsEntityUtils.getDGeom(detailViewEntity);
                if(collider != null){
                    if(collider instanceof DSpace space){
                        int i = 0;
                        for(DGeom child : space.getGeoms()){
                            ImGui.text("Child " + i);
                            ImGui.indent();
                            ImGui.text("Position: " + child.getPosition());
                            ImGui.text("Rotation: " + child.getQuaternion());
                            ImGui.text("Offset Position: " + child.getOffsetPosition());
                            ImGui.unindent();
                            i++;
                        }
                    } else {
                        ImGui.text("Position: " + collider.getPosition());
                        ImGui.text("Rotation: " + collider.getQuaternion());
                        ImGui.text("Offset Position: " + collider.getOffsetPosition());
                    }
                }
                //synchronized data
                if(
                    Globals.clientState.clientSceneWrapper.getScene().getEntityFromId(detailViewEntity.getId()) != null &&
                    Globals.clientState.clientSceneWrapper.containsServerId(detailViewEntity.getId())
                ){
                    //detailViewEntity is a client entity
                    //get server entity
                    int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(detailViewEntity.getId());
                    Entity serverEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
                    DGeom serverCollider = PhysicsEntityUtils.getDGeom(serverEntity);
                    if(serverCollider != null){
                        ImGui.newLine();
                        ImGui.text("Linked server entity:");
                        if(serverCollider instanceof DSpace space){
                            int i = 0;
                            for(DGeom child : space.getGeoms()){
                                ImGui.text("Child " + i);
                                ImGui.indent();
                                ImGui.text("Position: " + child.getPosition());
                                ImGui.text("Rotation: " + child.getQuaternion());
                                ImGui.text("Offset Position: " + child.getOffsetPosition());
                                ImGui.unindent();
                                i++;
                            }
                        } else {
                            ImGui.text("Position: " + serverCollider.getPosition());
                            ImGui.text("Rotation: " + serverCollider.getQuaternion());
                            ImGui.text("Offset Position: " + serverCollider.getOffsetPosition());
                        }
                    }
                } else if(Globals.clientState.clientSceneWrapper.containsServerId(detailViewEntity.getId())){
                    //detailViewEntity is a server entity
                    //get client entity
                    int clientId = Globals.clientState.clientSceneWrapper.mapServerToClientId(detailViewEntity.getId());
                    Entity clientEntity = Globals.clientState.clientSceneWrapper.getScene().getEntityFromId(clientId);
                    DGeom clientCollider = PhysicsEntityUtils.getDGeom(clientEntity);
                    if(clientCollider != null){
                        ImGui.newLine();
                        ImGui.text("Linked client entity:");
                        if(clientCollider instanceof DSpace space){
                            int i = 0;
                            for(DGeom child : space.getGeoms()){
                                ImGui.text("Child " + i);
                                ImGui.indent();
                                ImGui.text("Position: " + child.getPosition());
                                ImGui.text("Rotation: " + child.getQuaternion());
                                ImGui.text("Offset Position: " + child.getOffsetPosition());
                                ImGui.unindent();
                                i++;
                            }
                        } else {
                            ImGui.text("Position: " + clientCollider.getPosition());
                            ImGui.text("Rotation: " + clientCollider.getQuaternion());
                            ImGui.text("Offset Position: " + clientCollider.getOffsetPosition());
                        }
                    }
                }
            }
            ImGui.unindent();
        }
    }

}

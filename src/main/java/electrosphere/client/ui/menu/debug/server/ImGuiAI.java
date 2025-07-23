package electrosphere.client.ui.menu.debug.server;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.mask.ActorTextureMask;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import electrosphere.server.ai.AI;
import electrosphere.server.ai.blackboard.Blackboard;
import electrosphere.server.ai.nodes.plan.PathfindingNode;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.gridded.GriddedDataCellManager;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.CharacterUtils;
import electrosphere.server.macro.character.goal.CharacterGoal;
import electrosphere.server.macro.character.goal.CharacterGoal.CharacterGoalType;
import electrosphere.server.pathfinding.recast.PathingProgressiveData;
import electrosphere.server.pathfinding.voxel.VoxelPathfinder;
import electrosphere.server.pathfinding.voxel.VoxelPathfinder.PathfinderNode;
import imgui.ImGui;

/**
 * AI debug menus
 */
public class ImGuiAI {
    
    /**
     * window for viewing information about the ai state
     */
    public static ImGuiWindow aiWindow;

    /**
     * The number of iterations to step through
     */
    static int numIterations = 1;

    /**
     * The position that was solved for to display
     */
    static Vector3d solvedPosition = new Vector3d();

    /**
     * The entity to display debug info with
     */
    static List<Entity> displayEntity = new LinkedList<Entity>();

    /**
     * Client scene entity view
     */
    public static void createAIDebugWindow(){
        aiWindow = new ImGuiWindow("AI");
        aiWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //ui framework text
                ImGui.text("AI");

                //ai manager stuff
                ImGui.text("AI Manager active: " + Globals.serverState.aiManager.isActive());
                if(ImGui.button("Toggle AI Manager")){
                    Globals.serverState.aiManager.setActive(!Globals.serverState.aiManager.isActive());
                }


                if(ImGui.collapsingHeader("Statuses")){
                    for(AI ai : Globals.serverState.aiManager.getAIList()){
                        ImGui.indent();
                        if(ImGui.collapsingHeader(ai.getParent().getId() + " - " + ai.getStatus())){
                            if(ImGui.button("Draw current pathing")){
                                Blackboard blackboard = ai.getBlackboard();
                                PathingProgressiveData pathData = PathfindingNode.getPathfindingData(blackboard);
                                if(pathData != null){
                                    List<Vector3d> points = pathData.getPoints();
                                    Globals.renderingEngine.getDebugContentPipeline().setPathPoints(points);
                                }
                            }
                            if(ImGui.button("Send off map")){
                                Entity entity = ai.getParent();
                                ServerCharacterData serverCharacterData = ServerCharacterData.getServerCharacterData(entity);
                                Character character = Globals.serverState.characterService.getCharacter(serverCharacterData.getCharacterData().getId());
                                CharacterGoal.setCharacterGoal(character, new CharacterGoal(CharacterGoalType.LEAVE_SIM_RANGE));
                            }
                            if(ImGui.button("Jump To")){
                                Entity aiEnt = ai.getParent();
                                Vector3d aiEntPos = EntityUtils.getPosition(aiEnt);
                                Globals.controlHandler.setIsThirdPerson(true);
                                Globals.cameraHandler.setTrackPlayerEntity(false);
                                CameraEntityUtils.setCameraCenter(Globals.clientState.playerCamera, new Vector3d(aiEntPos));
                            }
                        }
                        ImGui.unindent();
                    }
                }

                if(ImGui.collapsingHeader("Debug Player AI")){
                    int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId());
                    Entity serverPlayerEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
                    AI playerAi = AI.getAI(serverPlayerEntity);
                    Vector3d playerTargetPos = null;
                    if(PathfindingNode.hasPathfindingPoint(playerAi.getBlackboard())){
                        playerTargetPos = PathfindingNode.getPathfindingPoint(playerAi.getBlackboard());
                    }
                    
                    ImGui.text("AI applied to player entity: " + playerAi.isApplyToPlayer());
                    if(playerTargetPos != null){
                        ImGui.text("Target pos: " + playerTargetPos.x + "," + playerTargetPos.y + "," + playerTargetPos.z);
                    } else {
                        ImGui.text("Target pos: UNDEFINED");
                    }
                    if(ImGui.button("Toggle AI on player entity")){
                        playerAi.setApplyToPlayer(!playerAi.isApplyToPlayer());
                    }
                    if(ImGui.button("Reset iterations")){
                        numIterations = 1;
                    }
                    if(ImGui.button("Increase pathfinding Iteration Cap (" + numIterations + ")")){
                        numIterations = numIterations + 1;
                        VoxelPathfinder voxelPathfinder = new VoxelPathfinder();
                        GriddedDataCellManager griddedDataCellManager = (GriddedDataCellManager)Globals.serverState.realmManager.getEntityRealm(serverPlayerEntity).getDataCellManager();
                        Vector3d playerPos = new Vector3d(EntityUtils.getPosition(serverPlayerEntity));
                        Vector3d targetPos = new Vector3d(playerPos).add(10,0,0);
                        if(playerTargetPos != null){
                            targetPos = playerTargetPos;
                        }
                        List<PathfinderNode> closedSet = voxelPathfinder.aStarStep(griddedDataCellManager, playerPos, targetPos, 1000, numIterations);

                        if(displayEntity.size() > 0){
                            for(Entity entity : displayEntity){
                                ClientEntityUtils.destroyEntity(entity);
                            }
                        }
                        for(PathfinderNode node : closedSet){
                            Entity newEnt = EntityCreationUtils.createClientSpatialEntity();
                            EntityCreationUtils.makeEntityDrawable(newEnt, AssetDataStrings.UNITCUBE);
                            Actor blockCursorActor = EntityUtils.getActor(newEnt);
                            blockCursorActor.addTextureMask(new ActorTextureMask("cube", Arrays.asList(new String[]{AssetDataStrings.TEXTURE_RED_TRANSPARENT})));
                            ClientEntityUtils.initiallyPositionEntity(newEnt, node.getPosition(), new Quaterniond());
                            EntityUtils.getScale(newEnt).set(0.4f);
                            displayEntity.add(newEnt);
                        }
                    }
                    if(ImGui.button("Draw Open Set (" + numIterations + ")")){
                        numIterations = numIterations + 1;
                        VoxelPathfinder voxelPathfinder = new VoxelPathfinder();
                        GriddedDataCellManager griddedDataCellManager = (GriddedDataCellManager)Globals.serverState.realmManager.getEntityRealm(serverPlayerEntity).getDataCellManager();
                        Vector3d playerPos = new Vector3d(EntityUtils.getPosition(serverPlayerEntity));
                        Vector3d targetPos = new Vector3d(playerPos).add(10,0,0);
                        if(PathfindingNode.hasPathfindingPoint(playerAi.getBlackboard())){
                            targetPos = PathfindingNode.getPathfindingPoint(playerAi.getBlackboard());
                        }
                        List<PathfinderNode> closedSet = voxelPathfinder.aStarStepOpen(griddedDataCellManager, playerPos, targetPos, 1000, numIterations);

                        if(displayEntity.size() > 0){
                            for(Entity entity : displayEntity){
                                ClientEntityUtils.destroyEntity(entity);
                            }
                        }
                        for(PathfinderNode node : closedSet){
                            Entity newEnt = EntityCreationUtils.createClientSpatialEntity();
                            EntityCreationUtils.makeEntityDrawable(newEnt, AssetDataStrings.UNITCUBE);
                            Actor blockCursorActor = EntityUtils.getActor(newEnt);
                            blockCursorActor.addTextureMask(new ActorTextureMask("cube", Arrays.asList(new String[]{AssetDataStrings.TEXTURE_RED_TRANSPARENT})));
                            ClientEntityUtils.initiallyPositionEntity(newEnt, node.getPosition(), new Quaterniond());
                            EntityUtils.getScale(newEnt).set(0.4f);
                            displayEntity.add(newEnt);
                        }
                    }
                }

                if(ImGui.button("Spawn test macro character")){
                    Realm realm = Globals.serverState.realmManager.first();
                    GriddedDataCellManager griddedDataCellManager = (GriddedDataCellManager)realm.getDataCellManager();
                    Entity serverEnt = EntityLookupUtils.getServerEquivalent(Globals.clientState.playerEntity);
                    Vector3d spawnPos = griddedDataCellManager.getMacroEntryPoint(new Vector3d(EntityUtils.getPosition(serverEnt)).add(50,0,0));
                    CharacterUtils.spawnCharacter(realm, spawnPos, CharacterUtils.DEFAULT_RACE);
                }
                
            }
        });
        aiWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(aiWindow);
    }

}

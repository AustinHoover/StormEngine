package electrosphere.net.synchronization.transport;


import electrosphere.entity.state.lod.ServerLODComponent;
import electrosphere.entity.state.lod.ClientLODComponent;
import electrosphere.entity.state.growth.ServerGrowthComponent;
import electrosphere.entity.state.growth.ClientGrowthComponent;
import electrosphere.entity.state.furniture.ServerDoorState;
import electrosphere.entity.state.furniture.ClientDoorState;
import electrosphere.entity.state.item.ServerChargeState;
import electrosphere.entity.state.item.ClientChargeState;
import electrosphere.entity.state.movement.editor.ServerEditorMovementTree;
import electrosphere.entity.state.movement.editor.ClientEditorMovementTree;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.stance.ServerStanceComponent;
import electrosphere.entity.state.stance.ClientStanceComponent;
import electrosphere.entity.state.movement.sprint.ServerSprintTree;
import electrosphere.entity.state.movement.sprint.ClientSprintTree;
import java.util.LinkedList;
import java.util.List;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attack.ClientAttackTree;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.entity.state.block.ClientBlockTree;
import electrosphere.entity.state.block.ServerBlockTree;
import electrosphere.entity.state.gravity.ClientGravityTree;
import electrosphere.entity.state.gravity.ServerGravityTree;
import electrosphere.entity.state.idle.ClientIdleTree;
import electrosphere.entity.state.idle.ServerIdleTree;
import electrosphere.entity.state.life.ClientLifeTree;
import electrosphere.entity.state.life.ServerLifeTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.entity.state.movement.jump.ServerJumpTree;
import electrosphere.entity.state.movement.walk.ClientWalkTree;
import electrosphere.entity.state.movement.walk.ServerWalkTree;
import electrosphere.net.synchronization.enums.BehaviorTreeIdEnums;
import electrosphere.net.synchronization.enums.FieldIdEnums;

/**
 * A collection of values for synchronized variables.
 * Used to transport data from server to client on initially loading a given entity.
 */
public class StateCollection {
    
    /**
     * The synchronized values
     */
    List<SynchronizedFieldValue> values = new LinkedList<SynchronizedFieldValue>();

    /**
     * Get the synchronized field's values
     * @return The values
     */
    public List<SynchronizedFieldValue> getValues(){
        return values;
    }

    /**
     * Sets a given value
     * @param value The value
     */
    public void setValue(SynchronizedFieldValue value){
        this.values.add(value);
    }

    /**
     * Checks if the entity has a state collection
     * @param entity The entity
     * @return true if it has a state collection, false otherwise
     */
    public static boolean hasStateCollection(Entity entity){
        return Globals.serverState.entityValueTrackingService.getEntityTrees(entity) != null;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Gets the state collection for the given entity
     * </p>
     * @param entity The entity
     * @return The state collection
     */
    public static StateCollection getStateCollection(Entity entity){
        StateCollection collection = new StateCollection();
        for(int treeId : Globals.serverState.entityValueTrackingService.getEntityTrees(entity)){
            switch(treeId){
                case BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID: {
                    ServerAttackTree tree = ServerAttackTree.getServerAttackTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID,FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_STATE_ID,ClientAttackTree.getAttackTreeStateEnumAsShort(tree.getState())));
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID,FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_DRIFTSTATE_ID,ClientAttackTree.getAttackTreeDriftStateEnumAsShort(tree.getDriftState())));
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID,FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_CURRENTMOVEID_ID,tree.getCurrentMoveId()));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID: {
                    ServerBlockTree tree = ServerBlockTree.getServerBlockTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID,FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_STATE_ID,ClientBlockTree.getBlockStateEnumAsShort(tree.getState())));
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID,FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_CURRENTBLOCKVARIANT_ID,tree.getCurrentBlockVariant()));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERTOOLBARSTATE_ID: {
                    ServerToolbarState tree = ServerToolbarState.getServerToolbarState(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERTOOLBARSTATE_ID,FieldIdEnums.TREE_SERVERTOOLBARSTATE_SYNCEDFIELD_SELECTEDSLOT_ID,tree.getSelectedSlot()));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERDOOR_ID: {
                    ServerDoorState tree = ServerDoorState.getServerDoorState(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERDOOR_ID,FieldIdEnums.TREE_SERVERDOOR_SYNCEDFIELD_STATE_ID,ClientDoorState.getDoorStateEnumAsShort(tree.getState())));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERGRAVITY_ID: {
                    ServerGravityTree tree = ServerGravityTree.getServerGravityTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERGRAVITY_ID,FieldIdEnums.TREE_SERVERGRAVITY_SYNCEDFIELD_STATE_ID,ClientGravityTree.getGravityTreeStateEnumAsShort(tree.getState())));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERGROWTH_ID: {
                    ServerGrowthComponent tree = ServerGrowthComponent.getServerGrowthComponent(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERGROWTH_ID,FieldIdEnums.TREE_SERVERGROWTH_SYNCEDFIELD_STATUS_ID,tree.getStatus()));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERIDLE_ID: {
                    ServerIdleTree tree = ServerIdleTree.getServerIdleTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERIDLE_ID,FieldIdEnums.TREE_SERVERIDLE_SYNCEDFIELD_STATE_ID,ClientIdleTree.getIdleTreeStateEnumAsShort(tree.getState())));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERCHARGESTATE_ID: {
                    ServerChargeState tree = ServerChargeState.getServerChargeState(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERCHARGESTATE_ID,FieldIdEnums.TREE_SERVERCHARGESTATE_SYNCEDFIELD_CHARGES_ID,tree.getCharges()));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERLIFETREE_ID: {
                    ServerLifeTree tree = ServerLifeTree.getServerLifeTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERLIFETREE_ID,FieldIdEnums.TREE_SERVERLIFETREE_SYNCEDFIELD_STATE_ID,ClientLifeTree.getLifeStateEnumEnumAsShort(tree.getState())));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERLODTREE_ID: {
                    ServerLODComponent tree = ServerLODComponent.getServerLODComponent(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERLODTREE_ID,FieldIdEnums.TREE_SERVERLODTREE_SYNCEDFIELD_LODLEVEL_ID,tree.getLodLevel()));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERSTANCECOMPONENT_ID: {
                    ServerStanceComponent tree = ServerStanceComponent.getServerStanceComponent(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERSTANCECOMPONENT_ID,FieldIdEnums.TREE_SERVERSTANCECOMPONENT_SYNCEDFIELD_STATE_ID,ClientStanceComponent.getCombatStanceEnumAsShort(tree.getState())));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVEREDITORMOVEMENTTREE_ID: {
                    ServerEditorMovementTree tree = ServerEditorMovementTree.getServerEditorMovementTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVEREDITORMOVEMENTTREE_ID,FieldIdEnums.TREE_SERVEREDITORMOVEMENTTREE_SYNCEDFIELD_FACING_ID,ClientEditorMovementTree.getEditorMovementRelativeFacingEnumAsShort(tree.getFacing())));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERGROUNDMOVEMENTTREE_ID: {
                    ServerGroundMovementTree tree = ServerGroundMovementTree.getServerGroundMovementTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERGROUNDMOVEMENTTREE_ID,FieldIdEnums.TREE_SERVERGROUNDMOVEMENTTREE_SYNCEDFIELD_FACING_ID,ClientGroundMovementTree.getMovementRelativeFacingEnumAsShort(tree.getFacing())));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID: {
                    ServerJumpTree tree = ServerJumpTree.getServerJumpTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID,FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_STATE_ID,ClientJumpTree.getJumpStateEnumAsShort(tree.getState())));
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID,FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTFRAME_ID,tree.getCurrentFrame()));
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID,FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTJUMPFORCE_ID,tree.getCurrentJumpForce()));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERSPRINTTREE_ID: {
                    ServerSprintTree tree = ServerSprintTree.getServerSprintTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERSPRINTTREE_ID,FieldIdEnums.TREE_SERVERSPRINTTREE_SYNCEDFIELD_STATE_ID,ClientSprintTree.getSprintTreeStateEnumAsShort(tree.getState())));
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERWALKTREE_ID: {
                    ServerWalkTree tree = ServerWalkTree.getServerWalkTree(entity);
                    collection.setValue(new SynchronizedFieldValue(BehaviorTreeIdEnums.BTREE_SERVERWALKTREE_ID,FieldIdEnums.TREE_SERVERWALKTREE_SYNCEDFIELD_STATE_ID,ClientWalkTree.getWalkStateEnumAsShort(tree.getState())));
                } break;
    
            }
        }
        return collection;
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Applies the state collection to the given entity
     * </p>
     * @param entity The entity
     * @param collection The state collection
     */
    public static void clientApplyStateCollection(Entity entity, StateCollection collection){
        for(SynchronizedFieldValue syncedValue : collection.getValues()){
            switch(syncedValue.getBehaviorTreeId()){
                case BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID: {
                    ClientAttackTree tree = ClientAttackTree.getClientAttackTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientAttackTree.getAttackTreeStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                        case(FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_DRIFTSTATE_ID): {
                            tree.setDriftState(ClientAttackTree.getAttackTreeDriftStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                        case(FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_CURRENTMOVEID_ID): {
                            tree.setCurrentMoveId((String)syncedValue.getValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID: {
                    ClientBlockTree tree = ClientBlockTree.getClientBlockTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientBlockTree.getBlockStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                        case(FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_CURRENTBLOCKVARIANT_ID): {
                            tree.setCurrentBlockVariant((String)syncedValue.getValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERTOOLBARSTATE_ID: {
                    ClientToolbarState tree = ClientToolbarState.getClientToolbarState(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERTOOLBARSTATE_SYNCEDFIELD_SELECTEDSLOT_ID): {
                            tree.setSelectedSlot(((Double)syncedValue.getValue()).intValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERDOOR_ID: {
                    ClientDoorState tree = ClientDoorState.getClientDoorState(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERDOOR_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientDoorState.getDoorStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERGRAVITY_ID: {
                    ClientGravityTree tree = ClientGravityTree.getClientGravityTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERGRAVITY_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientGravityTree.getGravityTreeStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERGROWTH_ID: {
                    ClientGrowthComponent tree = ClientGrowthComponent.getClientGrowthComponent(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERGROWTH_SYNCEDFIELD_STATUS_ID): {
                            tree.setStatus(((Double)syncedValue.getValue()).intValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERIDLE_ID: {
                    ClientIdleTree tree = ClientIdleTree.getClientIdleTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERIDLE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientIdleTree.getIdleTreeStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERCHARGESTATE_ID: {
                    ClientChargeState tree = ClientChargeState.getClientChargeState(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERCHARGESTATE_SYNCEDFIELD_CHARGES_ID): {
                            tree.setCharges(((Double)syncedValue.getValue()).intValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERLIFETREE_ID: {
                    ClientLifeTree tree = ClientLifeTree.getClientLifeTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERLIFETREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientLifeTree.getLifeStateEnumShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERLODTREE_ID: {
                    ClientLODComponent tree = ClientLODComponent.getClientLODComponent(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERLODTREE_SYNCEDFIELD_LODLEVEL_ID): {
                            tree.setLodLevel(((Double)syncedValue.getValue()).intValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERSTANCECOMPONENT_ID: {
                    ClientStanceComponent tree = ClientStanceComponent.getClientStanceComponent(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERSTANCECOMPONENT_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientStanceComponent.getCombatStanceShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVEREDITORMOVEMENTTREE_ID: {
                    ClientEditorMovementTree tree = ClientEditorMovementTree.getClientEditorMovementTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVEREDITORMOVEMENTTREE_SYNCEDFIELD_FACING_ID): {
                            tree.setFacing(ClientEditorMovementTree.getEditorMovementRelativeFacingShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERGROUNDMOVEMENTTREE_ID: {
                    ClientGroundMovementTree tree = ClientGroundMovementTree.getClientGroundMovementTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERGROUNDMOVEMENTTREE_SYNCEDFIELD_FACING_ID): {
                            tree.setFacing(ClientGroundMovementTree.getMovementRelativeFacingShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID: {
                    ClientJumpTree tree = ClientJumpTree.getClientJumpTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientJumpTree.getJumpStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                        case(FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTFRAME_ID): {
                            tree.setCurrentFrame(((Double)syncedValue.getValue()).intValue());
                        } break;
                        case(FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTJUMPFORCE_ID): {
                            tree.setCurrentJumpForce(((Double)syncedValue.getValue()).floatValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERSPRINTTREE_ID: {
                    ClientSprintTree tree = ClientSprintTree.getClientSprintTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERSPRINTTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientSprintTree.getSprintTreeStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERWALKTREE_ID: {
                    ClientWalkTree tree = ClientWalkTree.getClientWalkTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERWALKTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientWalkTree.getWalkStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
    
            }
        }
    }

    /**
     * <p> Automatically generated </p>
     * <p>
     * Applies the state collection to the given entity
     * </p>
     * @param entity The entity
     * @param collection The state collection
     */
    public static void serverApplyStateCollection(Entity entity, StateCollection collection){
        for(SynchronizedFieldValue syncedValue : collection.getValues()){
            switch(syncedValue.getBehaviorTreeId()){
                case BehaviorTreeIdEnums.BTREE_SERVERATTACKTREE_ID: {
                    ServerAttackTree tree = ServerAttackTree.getServerAttackTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientAttackTree.getAttackTreeStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                        case(FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_DRIFTSTATE_ID): {
                            tree.setDriftState(ClientAttackTree.getAttackTreeDriftStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                        case(FieldIdEnums.TREE_SERVERATTACKTREE_SYNCEDFIELD_CURRENTMOVEID_ID): {
                            tree.setCurrentMoveId((String)syncedValue.getValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERBLOCKTREE_ID: {
                    ServerBlockTree tree = ServerBlockTree.getServerBlockTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientBlockTree.getBlockStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                        case(FieldIdEnums.TREE_SERVERBLOCKTREE_SYNCEDFIELD_CURRENTBLOCKVARIANT_ID): {
                            tree.setCurrentBlockVariant((String)syncedValue.getValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERTOOLBARSTATE_ID: {
                    ServerToolbarState tree = ServerToolbarState.getServerToolbarState(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERTOOLBARSTATE_SYNCEDFIELD_SELECTEDSLOT_ID): {
                            tree.setSelectedSlot(((Double)syncedValue.getValue()).intValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERDOOR_ID: {
                    ServerDoorState tree = ServerDoorState.getServerDoorState(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERDOOR_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientDoorState.getDoorStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERGRAVITY_ID: {
                    ServerGravityTree tree = ServerGravityTree.getServerGravityTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERGRAVITY_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientGravityTree.getGravityTreeStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERGROWTH_ID: {
                    ServerGrowthComponent tree = ServerGrowthComponent.getServerGrowthComponent(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERGROWTH_SYNCEDFIELD_STATUS_ID): {
                            tree.setStatus(((Double)syncedValue.getValue()).intValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERIDLE_ID: {
                    ServerIdleTree tree = ServerIdleTree.getServerIdleTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERIDLE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientIdleTree.getIdleTreeStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERCHARGESTATE_ID: {
                    ServerChargeState tree = ServerChargeState.getServerChargeState(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERCHARGESTATE_SYNCEDFIELD_CHARGES_ID): {
                            tree.setCharges(((Double)syncedValue.getValue()).intValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERLIFETREE_ID: {
                    ServerLifeTree tree = ServerLifeTree.getServerLifeTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERLIFETREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientLifeTree.getLifeStateEnumShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERLODTREE_ID: {
                    ServerLODComponent tree = ServerLODComponent.getServerLODComponent(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERLODTREE_SYNCEDFIELD_LODLEVEL_ID): {
                            tree.setLodLevel(((Double)syncedValue.getValue()).intValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERSTANCECOMPONENT_ID: {
                    ServerStanceComponent tree = ServerStanceComponent.getServerStanceComponent(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERSTANCECOMPONENT_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientStanceComponent.getCombatStanceShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVEREDITORMOVEMENTTREE_ID: {
                    ServerEditorMovementTree tree = ServerEditorMovementTree.getServerEditorMovementTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVEREDITORMOVEMENTTREE_SYNCEDFIELD_FACING_ID): {
                            tree.setFacing(ClientEditorMovementTree.getEditorMovementRelativeFacingShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERGROUNDMOVEMENTTREE_ID: {
                    ServerGroundMovementTree tree = ServerGroundMovementTree.getServerGroundMovementTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERGROUNDMOVEMENTTREE_SYNCEDFIELD_FACING_ID): {
                            tree.setFacing(ClientGroundMovementTree.getMovementRelativeFacingShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERJUMPTREE_ID: {
                    ServerJumpTree tree = ServerJumpTree.getServerJumpTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientJumpTree.getJumpStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                        case(FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTFRAME_ID): {
                            tree.setCurrentFrame(((Double)syncedValue.getValue()).intValue());
                        } break;
                        case(FieldIdEnums.TREE_SERVERJUMPTREE_SYNCEDFIELD_CURRENTJUMPFORCE_ID): {
                            tree.setCurrentJumpForce(((Double)syncedValue.getValue()).floatValue());
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERSPRINTTREE_ID: {
                    ServerSprintTree tree = ServerSprintTree.getServerSprintTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERSPRINTTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientSprintTree.getSprintTreeStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
                case BehaviorTreeIdEnums.BTREE_SERVERWALKTREE_ID: {
                    ServerWalkTree tree = ServerWalkTree.getServerWalkTree(entity);
                    switch(syncedValue.getFieldId()){
                        case(FieldIdEnums.TREE_SERVERWALKTREE_SYNCEDFIELD_STATE_ID): {
                            tree.setState(ClientWalkTree.getWalkStateShortAsEnum(((Double)syncedValue.getValue()).shortValue()));
                        } break;
                    }
                } break;
    
            }
        }
    }

    /**
     * The value of a single synchronized field
     */
    public static class SynchronizedFieldValue {

        /**
         * The behavior tree this field is on
         */
        int behaviorTreeId;

        /**
         * The id of the field
         */
        int fieldId;

        /**
         * The value of the field
         */
        Object value;

        /**
         * Creates a synchronized value
         * @param behaviorTreeId The behavior tree id of the field
         * @param fieldId The field id of the field
         * @param value The value of the field currently
         */
        public SynchronizedFieldValue(int behaviorTreeId, int fieldId, Object value){
            this.behaviorTreeId = behaviorTreeId;
            this.fieldId = fieldId;
            this.value = value;
        }

        /**
         * Gets the behavior tree this field is in
         * @return the id of the behavior tree
         */
        public int getBehaviorTreeId(){
            return behaviorTreeId;
        }

        /**
         * Gets the id of the field
         * @return The id
         */
        public int getFieldId(){
            return fieldId;
        }

        /**
         * Gets the current value for this field
         * @return The current value
         */
        public Object getValue(){
            return value;
        }

    }

}

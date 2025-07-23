package electrosphere.entity.types.common;

import java.util.stream.Collectors;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DBody;

import electrosphere.client.interact.ClientInteractionEngine;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.SprintSystem;
import electrosphere.data.entity.creature.attack.AttackMove;
import electrosphere.data.entity.creature.movement.EditorMovementSystem;
import electrosphere.data.entity.creature.movement.FallMovementSystem;
import electrosphere.data.entity.creature.movement.GroundMovementSystem;
import electrosphere.data.entity.creature.movement.JumpMovementSystem;
import electrosphere.data.entity.creature.movement.MovementSystem;
import electrosphere.data.entity.creature.movement.WalkMovementSystem;
import electrosphere.data.entity.creature.rotator.RotatorConstraint;
import electrosphere.data.entity.creature.rotator.RotatorItem;
import electrosphere.data.entity.creature.rotator.RotatorSystem;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.data.entity.graphics.GraphicsTemplate;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.PhysicsMeshQueueItem;
import electrosphere.entity.DrawableUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attack.ClientAttackTree;
import electrosphere.entity.state.attack.ServerAttackTree;
import electrosphere.entity.state.attack.ShooterTree;
import electrosphere.entity.state.block.ClientBlockTree;
import electrosphere.entity.state.block.ServerBlockTree;
import electrosphere.entity.state.client.particle.ClientParticleEmitterComponent;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.equip.ServerEquipState;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.furniture.ClientDoorState;
import electrosphere.entity.state.furniture.ServerDoorState;
import electrosphere.entity.state.gravity.ClientGravityTree;
import electrosphere.entity.state.gravity.ServerGravityTree;
import electrosphere.entity.state.growth.ClientGrowthComponent;
import electrosphere.entity.state.growth.ServerGrowthComponent;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.idle.ServerIdleTree;
import electrosphere.entity.state.inventory.ClientInventoryState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.RelationalInventoryState;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.state.life.ClientLifeTree;
import electrosphere.entity.state.life.ServerLifeTree;
import electrosphere.entity.state.light.ClientPointLightComponent;
import electrosphere.entity.state.lod.ClientLODComponent;
import electrosphere.entity.state.lod.ServerLODComponent;
import electrosphere.entity.state.movement.editor.ClientEditorMovementTree;
import electrosphere.entity.state.movement.editor.ServerEditorMovementTree;
import electrosphere.entity.state.movement.fall.ClientFallTree;
import electrosphere.entity.state.movement.fall.ServerFallTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.entity.state.movement.jump.ServerJumpTree;
import electrosphere.entity.state.movement.sprint.ClientSprintTree;
import electrosphere.entity.state.movement.sprint.ServerSprintTree;
import electrosphere.entity.state.movement.walk.ClientWalkTree;
import electrosphere.entity.state.movement.walk.ServerWalkTree;
import electrosphere.entity.state.physicssync.upright.ClientAlwaysUprightTree;
import electrosphere.entity.state.physicssync.upright.ServerAlwaysUprightTree;
import electrosphere.entity.state.rotator.RotatorHierarchyNode;
import electrosphere.entity.state.rotator.RotatorTree;
import electrosphere.entity.state.rotator.ServerRotatorTree;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.collision.CollisionObjUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.creature.ObjectInventoryData;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.net.parser.net.message.EntityMessage;
import electrosphere.net.parser.net.message.NetworkMessage;
import electrosphere.net.server.player.Player;
import electrosphere.net.synchronization.transport.StateCollection;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.ActorBoneRotator;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.ServerBehaviorTreeUtils;
import electrosphere.server.datacell.utils.ServerEntityTagUtils;
import electrosphere.server.entity.poseactor.PoseActor;
import electrosphere.server.entity.serialization.ContentSerialization;
import electrosphere.server.entity.serialization.EntitySerialization;
import electrosphere.util.math.SpatialMathUtils;

/**
 * Utilities for creating all entity types
 */
public class CommonEntityUtils {
    
    /**
     * Performs transforms common to all entity types given an entity and a template
     * @param entity The entity to perform transforms on
     * @param template The type of entity
     * @return The entity
     */
    public static Entity clientApplyCommonEntityTransforms(Entity entity, CommonEntityType rawType){

        //
        //Set typing stuff
        //
        CommonEntityUtils.setTyping(entity,rawType);
        CommonEntityUtils.setCommonData(entity,rawType);

        //tracks whether to generate a drawable or not
        boolean generateDrawable = true;

        //
        //
        // Tokens that should be processed before other work is done
        //
        //
        if(rawType.getTokens() != null){
            for(String token : rawType.getTokens()){
                switch(token){
                    case "GENERATE_COLLISION_OBJECT": {
                        Globals.assetManager.addCollisionMeshToQueue(new PhysicsMeshQueueItem(Globals.clientState.clientSceneWrapper.getCollisionEngine(),rawType.getGraphicsTemplate().getModel().getPath()));
                        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(new BehaviorTree() {public void simulate(float deltaTime) {
                            DBody collisionObject = Globals.assetManager.fetchCollisionObject(Globals.clientState.clientSceneWrapper.getCollisionEngine(),rawType.getGraphicsTemplate().getModel().getPath());
                            if(collisionObject != null){
                                Globals.clientState.clientSceneWrapper.getScene().deregisterBehaviorTree(this);
                                CollisionObjUtils.clientAttachCollisionObjectToEntity(entity, collisionObject, 0, Collidable.TYPE_OBJECT);
                            }
                        }});
                    } break;
                    case "GENERATE_COLLISION_TERRAIN": {
                        Globals.assetManager.addCollisionMeshToQueue(new PhysicsMeshQueueItem(Globals.clientState.clientSceneWrapper.getCollisionEngine(),rawType.getGraphicsTemplate().getModel().getPath()));
                        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(new BehaviorTree() {public void simulate(float deltaTime) {
                            DBody collisionObject = Globals.assetManager.fetchCollisionObject(Globals.clientState.clientSceneWrapper.getCollisionEngine(),rawType.getGraphicsTemplate().getModel().getPath());
                            if(collisionObject != null){
                                Globals.clientState.clientSceneWrapper.getScene().deregisterBehaviorTree(this);
                                CollisionObjUtils.clientAttachCollisionObjectToEntity(entity, collisionObject, 0, Collidable.TYPE_STATIC);
                            }
                        }});
                    } break;
                    case "SEEDED": {
                        generateDrawable = false;
                    } break;
                }
            }
        }

        //
        //
        //Drawable stuff
        //
        //
        if(rawType.getGraphicsTemplate() != null){
            GraphicsTemplate graphicsTemplate = rawType.getGraphicsTemplate();
            if(graphicsTemplate.getModel() != null && EntityUtils.getActor(entity) == null && generateDrawable == true){
                DrawableUtils.applyNonproceduralModel(entity, graphicsTemplate.getModel());
            }
        }
        Actor creatureActor = EntityUtils.getActor(entity);

        //apply uniforms if they are pre-set

        ///
        ///
        /// HITBOX DATA
        ///
        ///
        if(rawType.getHitboxes() != null){
            HitboxCollectionState.attachHitboxState(Globals.clientState.clientSceneWrapper.getHitboxManager(), false, entity, rawType.getHitboxes());
        }


        //
        //
        // PHYSICS
        //
        //
        if(rawType.getCollidable() != null){
            ClientLODComponent.attachTree(entity);
        }
        
        //
        //
        //   MOVEMENT SYSTEMS
        //
        //
        if(rawType.getMovementSystems() != null){
            for(MovementSystem movementSystem : rawType.getMovementSystems()){
                switch(movementSystem.getType()){
                    //
                    // Generic ground
                    case GroundMovementSystem.GROUND_MOVEMENT_SYSTEM:
                        GroundMovementSystem groundMovementSystem = (GroundMovementSystem)movementSystem;
                        ClientGroundMovementTree moveTree = ClientGroundMovementTree.attachTree(entity, CollisionObjUtils.getCollidable(entity), groundMovementSystem);
                        if(groundMovementSystem.getAnimationStartup() != null){
                            moveTree.setAnimationStartUp(groundMovementSystem.getAnimationStartup().getNameThirdPerson());
                        }
                        if(groundMovementSystem.getAnimationLoop() != null){
                            moveTree.setAnimationMain(groundMovementSystem.getAnimationLoop().getNameThirdPerson());
                        }
                        if(groundMovementSystem.getAnimationWindDown()!= null){
                            moveTree.setAnimationSlowDown(groundMovementSystem.getAnimationWindDown().getNameThirdPerson());
                        }
                        //sprint system
                        if(groundMovementSystem.getSprintSystem() != null){
                            SprintSystem sprintSystem = groundMovementSystem.getSprintSystem();
                            ClientSprintTree sprintTree = ClientSprintTree.attachTree(entity, sprintSystem);
                            if(sprintSystem.getAnimationStartUp()!= null){
                                moveTree.setAnimationSprintStartUp(sprintSystem.getAnimationStartUp().getNameThirdPerson());
                            }
                            if(sprintSystem.getAnimationMain()!= null){
                                moveTree.setAnimationSprint(sprintSystem.getAnimationMain().getNameThirdPerson());
                            }
                            if(sprintSystem.getAnimationWindDown()!= null){
                                moveTree.setAnimationSprintWindDown(sprintSystem.getAnimationWindDown().getNameThirdPerson());
                            }
                            moveTree.setSprintTree(sprintTree);
                            Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.SPRINTABLE);
                        }
                        //round out end of move system
                        entity.putData(EntityDataStrings.CLIENT_MOVEMENT_BT, moveTree);
                        CreatureUtils.setFacingVector(entity, SpatialMathUtils.getOriginVector());
                        entity.putData(EntityDataStrings.DATA_STRING_MAX_NATURAL_VELOCITY, groundMovementSystem.getMaxVelocity());
                        entity.putData(EntityDataStrings.DATA_STRING_ACCELERATION, groundMovementSystem.getAcceleration());
                        entity.putData(EntityDataStrings.DATA_STRING_VELOCITY, 0f);
                        Globals.clientState.clientScene.registerBehaviorTree(moveTree);
                        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.MOVEABLE);
                        break;
                        //
                        // Jump
                    case JumpMovementSystem.JUMP_MOVEMENT_SYSTEM:
                        JumpMovementSystem jumpMovementSystem = (JumpMovementSystem)movementSystem;
                        ClientJumpTree jumpTree = ClientJumpTree.attachTree(entity, jumpMovementSystem);
                        if(jumpMovementSystem.getAnimationJump() != null){
                            jumpTree.setAnimationJump(jumpMovementSystem.getAnimationJump().getNameThirdPerson());
                        }
                        if(CreatureUtils.clientGetEntityMovementTree(entity) != null && CreatureUtils.clientGetEntityMovementTree(entity) instanceof ClientGroundMovementTree){
                            ((ClientGroundMovementTree)CreatureUtils.clientGetEntityMovementTree(entity)).setClientJumpTree(jumpTree);
                        }
                        if(ClientFallTree.getFallTree(entity)!=null){
                            ClientFallTree.getFallTree(entity).setJumpTree(jumpTree);
                        }
                        break;
                        //
                        // Falling
                    case FallMovementSystem.FALL_MOVEMENT_SYSTEM:
                        FallMovementSystem fallMovementSystem = (FallMovementSystem)movementSystem;
                        ClientFallTree fallTree = new ClientFallTree(entity, fallMovementSystem);
                        if(CreatureUtils.clientGetEntityMovementTree(entity) != null && CreatureUtils.clientGetEntityMovementTree(entity) instanceof ClientGroundMovementTree){
                            ((ClientGroundMovementTree)CreatureUtils.clientGetEntityMovementTree(entity)).setClientFallTree(fallTree);
                        }
                        if(ClientJumpTree.getClientJumpTree(entity)!=null){
                            fallTree.setJumpTree(ClientJumpTree.getClientJumpTree(entity));
                        }
                        entity.putData(EntityDataStrings.FALL_TREE, fallTree);
                        Globals.clientState.clientScene.registerBehaviorTree(fallTree);
                        break;
                    case WalkMovementSystem.WALK_MOVEMENT_SYSTEM: {
                        ClientWalkTree.attachTree(entity, (WalkMovementSystem)movementSystem);
                    } break;
                    case EditorMovementSystem.EDITOR_MOVEMENT_SYSTEM: {
                        ClientEditorMovementTree.attachTree(entity, (EditorMovementSystem)movementSystem);
                    } break;
                }
            }
        }

        //
        //
        // Interaction Logic
        //
        //
        if(rawType.getButtonInteraction() != null){
            CommonEntityFlags.setIsInteractable(entity, true);
            if(rawType.getButtonInteraction().getInteractionShape() != null){
                ClientInteractionEngine.attachCollidableTemplate(entity, rawType.getButtonInteraction().getInteractionShape());
            } else if(rawType.getCollidable() != null){
                ClientInteractionEngine.attachCollidableTemplate(entity, rawType.getCollidable());
            }
        }

        //
        //
        // Light generation
        //
        //
        if(rawType.getPointLight() != null){
            ClientPointLightComponent.attachTree(entity, rawType.getPointLight());
        }

        //
        //
        // Particle emitter
        //
        //
        if(rawType.getParticleEmitter() != null){
            ClientParticleEmitterComponent.attachTree(entity, rawType.getParticleEmitter());
        }

        if(rawType.getEquipPoints() != null && rawType.getEquipPoints().size() > 0){
            ClientEquipState.attachTree(entity, rawType.getEquipPoints());
            InventoryUtils.setEquipInventory(entity, RelationalInventoryState.buildRelationalInventoryStateFromEquipList(rawType.getEquipPoints()));
        }
        if(rawType.getToolbarData() != null){
            ClientToolbarState.attachTree(entity, rawType.getToolbarData());
            InventoryUtils.setToolbarInventory(entity, RelationalInventoryState.buildToolbarInventory());
        }
        if(rawType.getBlockSystem() != null){
            ClientBlockTree.attachTree(entity, rawType.getBlockSystem());
        }
        if(rawType.getGrowthData() != null){
            ClientGrowthComponent.attachTree(entity, rawType.getGrowthData());
        }
        if(rawType.getTokens() != null){
            for(String token : rawType.getTokens()){
                switch(token){
                    case "ATTACKER":
                        ClientAttackTree.attachTree(entity);
                        entity.putData(EntityDataStrings.ATTACK_MOVE_TYPE_ACTIVE, null);
                        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.ATTACKER);
                        //add all attack moves
                        if(rawType.getAttackMoves() != null && rawType.getAttackMoves().size() > 0){
                            for(AttackMove attackMove : rawType.getAttackMoves()){
                                entity.putData(attackMove.getType(), rawType.getAttackMoveResolver().getMoveset(attackMove.getType()));
                                // switch(attackMove.getType()){
                                //     case EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND:
                                //         rVal.putData(EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND, rawType.getAttackMoveResolver().getMoveset(EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND));
                                //         break;
                                //     case EntityDataStrings.ATTACK_MOVE_TYPE_BOW_TWO_HAND:
                                //     rVal.putData(EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND, rawType.getAttackMoveResolver().getMoveset(EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND));
                                //     break;
                                // }
                            }
                        }
                        break;
                    case "SHOOTER": {
                        ShooterTree shooterTree = new ShooterTree(entity);
                        ShooterTree.setShooterTree(entity, shooterTree);
                        Globals.clientState.clientScene.registerBehaviorTree(shooterTree);
                    } break;
                    case "GRAVITY": {
                        Collidable collidable = (Collidable)entity.getData(EntityDataStrings.PHYSICS_COLLIDABLE);
                        DBody collisionObject = PhysicsEntityUtils.getDBody(entity);
                        ClientGravityTree.attachTree(entity, collidable, collisionObject, 30);
                        entity.putData(EntityDataStrings.GRAVITY_ENTITY, true);
                    } break;
                    case "TARGETABLE": {
                        Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.TARGETABLE);
                    } break;
                    case "OUTLINE": {
                        entity.putData(EntityDataStrings.DRAW_OUTLINE, true);
                    } break;
                    case "UNIT_CONTROLS": {
                        ClientAlwaysUprightTree.attachTree(entity);
                        CreatureUtils.setFacingVector(entity, SpatialMathUtils.getOriginVector());
                    } break;
                    case "SPAWNPOINT": {
                        //ignore on client
                    } break;
                }
            }
        }
        //rotator system
        if(rawType.getRotatorSystem() != null && rawType.getRotatorSystem().getRotatorItems() != null){
            RotatorSystem system = rawType.getRotatorSystem();
            RotatorTree rotatorTree = new RotatorTree(entity);
            for(RotatorItem item : system.getRotatorItems()){
                //put actor rotator
                ActorBoneRotator newRotator = new ActorBoneRotator();
                creatureActor.getAnimationData().addBoneRotator(item.getBoneName(), newRotator);
                //construct node for tree
                RotatorHierarchyNode hierarchyNode = new RotatorHierarchyNode();
                hierarchyNode.setBone(item.getBoneName());
                for(RotatorConstraint constraint : item.getConstraints()){
                    hierarchyNode.addRotatorConstraint(new electrosphere.entity.state.rotator.RotatorConstraint(constraint));
                }
                rotatorTree.addRotatorNode(hierarchyNode);
            }
            entity.putData(EntityDataStrings.CLIENT_ROTATOR_TREE, rotatorTree);
            Globals.clientState.clientScene.registerBehaviorTree(rotatorTree);
        }
        //bone groups
        if(rawType.getBoneGroups() != null){
            creatureActor.getAnimationData().setBoneGroups(rawType.getBoneGroups());
        }
        //grid alignment
        if(rawType.getGridAlignedData() != null){
            Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.BLOCK_OCCUPANT);
        }
        //furniture data
        if(rawType.getFurnitureData() != null){
            if(rawType.getFurnitureData().getDoor() != null){
                ClientDoorState.attachTree(entity, rawType.getFurnitureData().getDoor());
            }
        }
        //add health system
        if(rawType.getHealthSystem() != null){
            ClientLifeTree.attachTree(entity,rawType.getHealthSystem());
            Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.LIFE_STATE);
        }
        if(rawType.getInventoryData() != null){
            if(rawType.getInventoryData().getNaturalSize() != null){
                entity.putData(EntityDataStrings.NATURAL_INVENTORY,UnrelationalInventoryState.createUnrelationalInventory(rawType.getInventoryData().getNaturalSize()));
                ClientInventoryState.setClientInventoryState(entity, ClientInventoryState.clientCreateInventoryState(entity));
            }
        }

        return entity;
    }


    /**
     * Spawns a server-side creature
     * @param realm The realm to spawn the creature in
     * @param position The position of the creature in that realm
     * @param type The type of creature
     * @param template The creature template to use
     * @return The creature entity
     */
    public static Entity serverApplyCommonEntityTransforms(Realm realm, Vector3d position, Entity entity, CommonEntityType rawType){

        double startX = position.x;
        double startY = position.y;
        double startZ = position.z;

        //
        //Set typing stuff
        //
        CommonEntityUtils.setTyping(entity,rawType);
        CommonEntityUtils.serverAttachToTag(entity,rawType);
        CommonEntityUtils.setCommonData(entity,rawType);

        //tracks whether to generate a drawable or not
        boolean generateDrawable = true;

        //
        //
        // Tokens that should be processed before other work is done
        //
        //
        if(rawType.getTokens() != null){
            for(String token : rawType.getTokens()){
                switch(token){
                    case "GENERATE_COLLISION_OBJECT": {
                        Globals.assetManager.addCollisionMeshToQueue(new PhysicsMeshQueueItem(realm.getCollisionEngine(),rawType.getGraphicsTemplate().getModel().getPath()));
                        ServerBehaviorTreeUtils.attachBTreeToEntity(entity, new BehaviorTree() {public void simulate(float deltaTime) {
                            DBody collisionObject = Globals.assetManager.fetchCollisionObject(realm.getCollisionEngine(),rawType.getGraphicsTemplate().getModel().getPath());
                            if(collisionObject != null){
                                ServerBehaviorTreeUtils.detatchBTreeFromEntity(entity, this);
                                CollisionObjUtils.serverAttachCollisionObjectToEntity(entity, collisionObject, 0, Collidable.TYPE_OBJECT);
                            }
                        }});
                    } break;
                    case "GENERATE_COLLISION_TERRAIN": {
                        Globals.assetManager.addCollisionMeshToQueue(new PhysicsMeshQueueItem(realm.getCollisionEngine(),rawType.getGraphicsTemplate().getModel().getPath()));
                        ServerBehaviorTreeUtils.attachBTreeToEntity(entity, new BehaviorTree() {public void simulate(float deltaTime) {
                            DBody collisionObject = Globals.assetManager.fetchCollisionObject(realm.getCollisionEngine(),rawType.getGraphicsTemplate().getModel().getPath());
                            if(collisionObject != null){
                                ServerBehaviorTreeUtils.detatchBTreeFromEntity(entity, this);
                                CollisionObjUtils.serverAttachCollisionObjectToEntity(entity, collisionObject, 0, Collidable.TYPE_STATIC);
                            }
                        }});
                    } break;
                    case "SEEDED": {
                        generateDrawable = false;
                    } break;
                }
            }
        }

        //
        //
        //Posing stuff
        //
        //
        if(rawType.getGraphicsTemplate() != null){
            GraphicsTemplate graphicsTemplate = rawType.getGraphicsTemplate();
            if(graphicsTemplate.getModel() != null && graphicsTemplate.getModel().getPath() != null && EntityUtils.getPoseActor(entity) == null && generateDrawable == true){
                EntityCreationUtils.makeEntityPoseable(entity, graphicsTemplate.getModel().getPath());
            }
            //idle tree & generic stuff all creatures have
            if(graphicsTemplate.getModel() != null && graphicsTemplate.getModel().getIdleData() != null){
                ServerIdleTree.attachTree(entity, graphicsTemplate.getModel().getIdleData());
            }
        }
        PoseActor creatureActor = EntityUtils.getPoseActor(entity);
        //
        //
        // Hitbox stuff
        //
        //
        if(position.x != startX || position.y != startY || position.z != startZ){
            throw new Error("Position mutated while spawning entity!");
        }
        if(rawType.getHitboxes() != null){
            HitboxCollectionState.attachHitboxState(realm.getHitboxManager(), true, entity, rawType.getHitboxes());
        }
        //
        //
        // Physics stuff
        //
        //
        if(rawType.getCollidable() != null){
            //actually attach collidable
            CollidableTemplate physicsTemplate = rawType.getCollidable();
            if(Globals.serverState.lodEmitterService.isFullLod(position)){
                PhysicsEntityUtils.serverAttachCollidableTemplate(realm, entity, physicsTemplate,position);
            } else {
                PhysicsEntityUtils.serverAttachGeom(realm, entity, physicsTemplate, position);
            }
            ServerLODComponent.attachTree(entity);
        }
        //
        //
        //   MOVEMENT SYSTEMS
        //
        //
        if(rawType.getMovementSystems() != null){
            for(MovementSystem movementSystem : rawType.getMovementSystems()){
                switch(movementSystem.getType()){
                    //
                    // Generic ground
                    case GroundMovementSystem.GROUND_MOVEMENT_SYSTEM:
                        GroundMovementSystem groundMovementSystem = (GroundMovementSystem)movementSystem;
                        ServerGroundMovementTree moveTree = ServerGroundMovementTree.attachTree(entity,CollisionObjUtils.getCollidable(entity),groundMovementSystem);
                        if(groundMovementSystem.getAnimationStartup() != null){
                            moveTree.setAnimationStartUp(groundMovementSystem.getAnimationStartup().getNameThirdPerson());
                        }
                        if(groundMovementSystem.getAnimationLoop() != null){
                            moveTree.setAnimationMain(groundMovementSystem.getAnimationLoop().getNameThirdPerson());
                        }
                        if(groundMovementSystem.getAnimationWindDown()!= null){
                            moveTree.setAnimationSlowDown(groundMovementSystem.getAnimationWindDown().getNameThirdPerson());
                        }
                        //sprint system
                        if(groundMovementSystem.getSprintSystem() != null){
                            SprintSystem sprintSystem = groundMovementSystem.getSprintSystem();
                            ServerSprintTree sprintTree = ServerSprintTree.attachTree(entity, sprintSystem);
                            if(sprintSystem.getAnimationStartUp()!= null){
                                moveTree.setAnimationSprintStartUp(sprintSystem.getAnimationStartUp().getNameThirdPerson());
                            }
                            if(sprintSystem.getAnimationMain()!= null){
                                moveTree.setAnimationSprint(sprintSystem.getAnimationMain().getNameThirdPerson());
                            }
                            if(sprintSystem.getAnimationWindDown()!= null){
                                moveTree.setAnimationSprintWindDown(sprintSystem.getAnimationWindDown().getNameThirdPerson());
                            }
                            sprintTree.setServerGroundMovementTree(moveTree);
                            moveTree.setServerSprintTree(sprintTree);
                            ServerEntityTagUtils.attachTagToEntity(entity, EntityTags.SPRINTABLE);
                        }
                        //round out end of move system
                        entity.putData(EntityDataStrings.SERVER_MOVEMENT_BT, moveTree);
                        CreatureUtils.setFacingVector(entity, SpatialMathUtils.getOriginVector());
                        entity.putData(EntityDataStrings.DATA_STRING_MAX_NATURAL_VELOCITY, groundMovementSystem.getMaxVelocity());
                        entity.putData(EntityDataStrings.DATA_STRING_ACCELERATION, groundMovementSystem.getAcceleration());
                        entity.putData(EntityDataStrings.DATA_STRING_VELOCITY, 0f);
                        ServerBehaviorTreeUtils.attachBTreeToEntity(entity, moveTree);
                        ServerEntityTagUtils.attachTagToEntity(entity, EntityTags.MOVEABLE);
                        break;
                        //
                        // Jump
                    case JumpMovementSystem.JUMP_MOVEMENT_SYSTEM:
                        JumpMovementSystem jumpMovementSystem = (JumpMovementSystem)movementSystem;
                        ServerJumpTree jumpTree = ServerJumpTree.attachTree(entity, jumpMovementSystem);
                        if(jumpMovementSystem.getAnimationJump() != null){
                            jumpTree.setAnimationJump(jumpMovementSystem.getAnimationJump().getNameThirdPerson());
                        }
                        if(CreatureUtils.serverGetEntityMovementTree(entity) != null && CreatureUtils.serverGetEntityMovementTree(entity) instanceof ClientGroundMovementTree){
                            ((ServerGroundMovementTree)CreatureUtils.serverGetEntityMovementTree(entity)).setServerJumpTree(jumpTree);
                        }
                        if(ServerFallTree.getFallTree(entity)!=null){
                            ServerFallTree.getFallTree(entity).setServerJumpTree(jumpTree);
                        }
                        break;
                        //
                        // Falling
                    case FallMovementSystem.FALL_MOVEMENT_SYSTEM:
                        FallMovementSystem fallMovementSystem = (FallMovementSystem)movementSystem;
                        ServerFallTree fallTree = new ServerFallTree(entity,fallMovementSystem);
                        if(CreatureUtils.serverGetEntityMovementTree(entity) != null && CreatureUtils.serverGetEntityMovementTree(entity) instanceof ClientGroundMovementTree){
                            ((ServerGroundMovementTree)CreatureUtils.serverGetEntityMovementTree(entity)).setServerFallTree(fallTree);
                        }
                        if(ServerJumpTree.getServerJumpTree(entity)!=null){
                            fallTree.setServerJumpTree(ServerJumpTree.getServerJumpTree(entity));
                        }
                        entity.putData(EntityDataStrings.FALL_TREE, fallTree);
                        ServerBehaviorTreeUtils.attachBTreeToEntity(entity, fallTree);
                        break;
                    case WalkMovementSystem.WALK_MOVEMENT_SYSTEM: {
                        ServerWalkTree.attachTree(entity, (WalkMovementSystem)movementSystem);
                    } break;
                    case EditorMovementSystem.EDITOR_MOVEMENT_SYSTEM: {
                        ServerEditorMovementTree.attachTree(entity, (EditorMovementSystem)movementSystem);
                    } break;
                }
            }
        }

        //
        //
        // Interaction Logic
        //
        //
        if(rawType.getButtonInteraction() != null){
            CommonEntityFlags.setIsInteractable(entity, true);
        }
        
        //
        //
        //   EQUIP STATE
        //
        //
        if(rawType.getEquipPoints() != null && rawType.getEquipPoints().size() > 0){
            ServerEquipState.attachTree(entity, rawType.getEquipPoints());
            InventoryUtils.setEquipInventory(entity, RelationalInventoryState.buildRelationalInventoryStateFromEquipList(rawType.getEquipPoints()));
        }
        if(rawType.getToolbarData() != null){
            ServerToolbarState.attachTree(entity, rawType.getToolbarData());
            InventoryUtils.setToolbarInventory(entity, RelationalInventoryState.buildToolbarInventory());
        }

        //
        //
        //   BLOCK STATE
        //
        //
        if(rawType.getBlockSystem() != null){
            ServerBlockTree.attachTree(entity, rawType.getBlockSystem());
        }

        //
        //
        //   TOKENS
        //
        //
        if(rawType.getTokens() != null){
            for(String token : rawType.getTokens()){
                switch(token){
                    case "ATTACKER": {
                        ServerAttackTree.attachTree(entity);
                        entity.putData(EntityDataStrings.ATTACK_MOVE_TYPE_ACTIVE, null);
                        ServerEntityTagUtils.attachTagToEntity(entity, EntityTags.ATTACKER);
                        //add all attack moves
                        if(rawType.getAttackMoves() != null && rawType.getAttackMoves().size() > 0){
                            for(AttackMove attackMove : rawType.getAttackMoves()){
                                entity.putData(attackMove.getType(), rawType.getAttackMoveResolver().getMoveset(attackMove.getType()));
                                // switch(attackMove.getType()){
                                //     case EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND:
                                //         rVal.putData(EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND, rawType.getAttackMoveResolver().getMoveset(EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND));
                                //         break;
                                //     case EntityDataStrings.ATTACK_MOVE_TYPE_BOW_TWO_HAND:
                                //     rVal.putData(EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND, rawType.getAttackMoveResolver().getMoveset(EntityDataStrings.ATTACK_MOVE_TYPE_MELEE_SWING_ONE_HAND));
                                //     break;
                                // }
                            }
                        }
                    } break;
                    case "SHOOTER": {
                        ShooterTree shooterTree = new ShooterTree(entity);
                        ShooterTree.setShooterTree(entity, shooterTree);
                        ServerBehaviorTreeUtils.attachBTreeToEntity(entity, shooterTree);
                    } break;
                    case "GRAVITY": {
                        Collidable collidable = (Collidable)entity.getData(EntityDataStrings.PHYSICS_COLLIDABLE);
                        DBody collisionObject = PhysicsEntityUtils.getDBody(entity);
                        ServerGravityTree.attachTree(entity, collidable, collisionObject, 30);
                        entity.putData(EntityDataStrings.GRAVITY_ENTITY, true);
                    } break;
                    case "TARGETABLE": {
                        ServerEntityTagUtils.attachTagToEntity(entity, EntityTags.TARGETABLE);
                    } break;
                    case "OUTLINE": {
                        entity.putData(EntityDataStrings.DRAW_OUTLINE, true);
                    } break;
                    case "UNIT_CONTROLS": {
                        ServerAlwaysUprightTree.attachTree(entity);
                        CreatureUtils.setFacingVector(entity, SpatialMathUtils.getOriginVector());
                    } break;
                    case "SPAWNPOINT": {
                        realm.registerSpawnPoint(position);
                    } break;
                }
            }
        }
        
        //rotator system
        if(rawType.getRotatorSystem() != null && rawType.getRotatorSystem().getRotatorItems() != null){
            RotatorSystem system = rawType.getRotatorSystem();
            ServerRotatorTree rotatorTree = new ServerRotatorTree(entity);
            for(RotatorItem item : system.getRotatorItems()){
                //put actor rotator
                ActorBoneRotator newRotator = new ActorBoneRotator();
                creatureActor.addBoneRotator(item.getBoneName(), newRotator);
                //construct node for tree
                RotatorHierarchyNode hierarchyNode = new RotatorHierarchyNode();
                hierarchyNode.setBone(item.getBoneName());
                for(RotatorConstraint constraint : item.getConstraints()){
                    hierarchyNode.addRotatorConstraint(new electrosphere.entity.state.rotator.RotatorConstraint(constraint));
                }
                rotatorTree.addRotatorNode(hierarchyNode);
            }
            entity.putData(EntityDataStrings.SERVER_ROTATOR_TREE, rotatorTree);
            ServerBehaviorTreeUtils.attachBTreeToEntity(entity, rotatorTree);
        }

        //bone groups
        if(rawType.getBoneGroups() != null){
            creatureActor.setBoneGroups(rawType.getBoneGroups());
        }

        //grid alignment
        if(rawType.getGridAlignedData() != null){
            Globals.clientState.clientScene.registerEntityToTag(entity, EntityTags.BLOCK_OCCUPANT);
            //TODO: must register with all nearby scenes as well because it could possibly occupy other chunks
        }

        //furniture data
        if(rawType.getFurnitureData() != null){
            if(rawType.getFurnitureData().getDoor() != null){
                ServerDoorState.attachTree(entity, rawType.getFurnitureData().getDoor());
            }
        }

        if(rawType.getGrowthData() != null){
            ServerGrowthComponent.attachTree(entity, rawType.getGrowthData());
        }

        if(rawType.getInventoryData() != null){
            if(rawType.getInventoryData().getNaturalSize() != null){
                entity.putData(EntityDataStrings.NATURAL_INVENTORY,UnrelationalInventoryState.createUnrelationalInventory(rawType.getInventoryData().getNaturalSize()));
                ServerInventoryState.setServerInventoryState(entity, ServerInventoryState.serverCreateInventoryState(entity));
            }
        }

        ///
        ///
        /// AI (This SHOULD only be applied on the server with the way AI architected currently)
        ///
        ///
        if(rawType.getAITrees() != null){
            Globals.serverState.aiManager.attachAI(entity, rawType.getAITrees());
        }

        //add health system
        if(rawType.getHealthSystem() != null){
            ServerLifeTree.attachTree(entity, rawType.getHealthSystem());
            ServerEntityTagUtils.attachTagToEntity(entity, EntityTags.LIFE_STATE);
        }

        if(Globals.serverState.entityDataCellMapper.getEntityDataCell(entity) == null){
            throw new Error("Failed to map entity to cell!");
        }

        
        return entity;
    }

    /**
     * Spawns an object in the client scene
     * @param type The type of object
     * @return The object entity
     */
    public static Entity clientSpawnBasicObject(String type){
        CommonEntityType rawType = Globals.gameConfigCurrent.getObjectTypeMap().getType(type);
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();

        if(rawType == null){
            String message = "Failed to lookup type: " + type + "\n" +
            Globals.gameConfigCurrent.getObjectTypeMap().getTypes().stream().map((CommonEntityType typeObj) -> typeObj.getId()).collect(Collectors.toList()) +
            "";
            throw new Error(message);
        }
        //
        //
        //Common entity transforms
        //
        //
        CommonEntityUtils.clientApplyCommonEntityTransforms(rVal, rawType);

        //
        //
        //Object specific transforms
        //
        //

        return rVal;
    }



    /**
     * Spawns a server-side object
     * @param type The type of object to spawn
     * @return The object
     */
    public static Entity serverSpawnBasicObject(Realm realm, Vector3d position, String type){
        CommonEntityType rawType = Globals.gameConfigCurrent.getObjectTypeMap().getType(type);
        Entity rVal = EntityCreationUtils.createServerEntity(realm, position);

        //
        //
        //Common entity transforms
        //
        //
        CommonEntityUtils.serverApplyCommonEntityTransforms(realm, position, rVal, rawType);

        //
        //
        //Object specific transforms
        //
        //
        
        //position entity
        //this needs to be called at the end of this function.
        //Burried underneath this is function call to initialize a server side entity.
        //The server initialization logic checks what type of entity this is, if this function is called prior to its type being stored
        //the server will not be able to synchronize it properly.
        ServerEntityUtils.initiallyPositionEntity(realm,rVal,position);

        return rVal;
    }

    /**
     * Spawns a server-side object
     * @param type The type of object to spawn
     * @return The object
     */
    public static Entity serverSpawnTemplateObject(Realm realm, Vector3d position, String type, ObjectTemplate template){
        Entity rVal = CommonEntityUtils.serverSpawnBasicObject(realm, position, type);

        //apply inventory data
        if(template.getInventoryData() != null){
            ObjectInventoryData inventoryData = template.getInventoryData();
            if(inventoryData.getNaturalItems() != null && inventoryData.getNaturalItems().size() > 0){
                for(EntitySerialization serializedItem : inventoryData.getNaturalItems()){
                    ItemUtils.serverCreateContainerItem(rVal, Globals.gameConfigCurrent.getItemMap().getItem(serializedItem.getSubtype()));
                }
            }
            if(inventoryData.getEquipItems() != null && inventoryData.getEquipItems().size() > 0){
                throw new Error("Unsupported currently");
            }
            if(inventoryData.getToolbarItems() != null && inventoryData.getToolbarItems().size() > 0){
                throw new Error("Unsupported currently");
            }
        }

        return rVal;
    }

    /**
     * Sets the object to a given player
     * @param player The player
     * @param item The object entity
     */
    public static void sendEntityToPlayer(Player player, Entity object){
        int id = object.getId();
        String type = CommonEntityUtils.getEntitySubtype(object);
        Vector3d position = EntityUtils.getPosition(object);
        Quaterniond rotation = EntityUtils.getRotation(object);
        //construct the spawn message and attach to player
        NetworkMessage message = EntityMessage.constructCreateMessage(
            id,
            EntityType.COMMON.getValue(),
            type,
            "",
            position.x,
            position.y,
            position.z,
            rotation.x,
            rotation.y,
            rotation.z,
            rotation.w
        );
        player.addMessage(message);
    }

    /**
     * Sets the typing data
     * @param entity The entity
     * @param type The type
     */
    private static void setTyping(Entity entity, CommonEntityType type){
        if(type == null){
            throw new Error("Provided null typing!");
        }
        if(type instanceof CreatureData){
            CommonEntityUtils.setEntityType(entity, EntityType.CREATURE);
            CommonEntityUtils.setEntitySubtype(entity, type.getId());
        } else if(type instanceof Item){
            CommonEntityUtils.setEntityType(entity, EntityType.ITEM);
            CommonEntityUtils.setEntitySubtype(entity, type.getId());
        } else if(type instanceof FoliageType){
            CommonEntityUtils.setEntityType(entity, EntityType.FOLIAGE);
            CommonEntityUtils.setEntitySubtype(entity, type.getId());
        } else {
            CommonEntityUtils.setEntityType(entity, EntityType.COMMON);
            CommonEntityUtils.setEntitySubtype(entity, type.getId());
        }
    }

    /**
     * Attachs the entity to the specified tag
     * @param entity The entity
     * @param type The type
     */
    private static void serverAttachToTag(Entity entity, CommonEntityType type){
        if(type instanceof CreatureData){
            ServerEntityTagUtils.attachTagToEntity(entity, EntityTags.CREATURE);
        } else if(type instanceof Item){
            ServerEntityTagUtils.attachTagToEntity(entity, EntityTags.ITEM);
        } else if(type instanceof FoliageType){
            ServerEntityTagUtils.attachTagToEntity(entity, EntityTags.FOLIAGE);
        }
    }

    /**
     * Gets the type of the entity
     * @param entity The entity
     * @return The type
     */
    public static EntityType getEntityType(Entity entity){
        return (EntityType)entity.getData(EntityDataStrings.ENTITY_TYPE);
    }

    /**
     * Sets the entity type
     * @param entity the entity
     * @param type the type
     */
    public static void setEntityType(Entity entity, EntityType type){
        entity.putData(EntityDataStrings.ENTITY_TYPE, type);
    }

    /**
     * Gets the common data on the entity
     * @param entity The entity
     * @return The common data
     */
    public static CommonEntityType getCommonData(Entity entity){
        return (CommonEntityType)entity.getData(EntityDataStrings.COMMON_DATA);
    }

    /**
     * Sets the common data on the entity
     * @param entity The entity
     * @param data The common data
     */
    public static void setCommonData(Entity entity, CommonEntityType data){
        entity.putData(EntityDataStrings.COMMON_DATA, data);
    }

    /**
     * Gets the subtype of this entity
     * @param entity The entity
     * @return The subtype
     */
    public static String getEntitySubtype(Entity entity){
        return (String)entity.getData(EntityDataStrings.ENTITY_SUBTYPE);
    }

    /**
     * Sets the subtype of this entity
     * @param entity The entity
     * @param subtype The subtype
     */
    public static void setEntitySubtype(Entity entity, String subtype){
        entity.putData(EntityDataStrings.ENTITY_SUBTYPE, subtype);
    }

    /**
     * Gets the template for the creature
     * @param e The creature
     * @return The template
     */
    public static ObjectTemplate getObjectTemplate(Entity e){
        ObjectTemplate template = (ObjectTemplate)e.getData(EntityDataStrings.OBJECT_TEMPLATE);
        if(template == null){
            template = ObjectTemplate.create(CommonEntityUtils.getEntityType(e), CommonEntityUtils.getEntitySubtype(e));
        }
        ObjectInventoryData inventoryData = template.getInventoryData();
        inventoryData.clear();
        if(ServerEquipState.hasEquipState(e)){
            ServerEquipState serverEquipState = ServerEquipState.getEquipState(e);
            for(String point : serverEquipState.equippedPoints()){
                Entity item = serverEquipState.getEquippedItemAtPoint(point);
                EntitySerialization itemSerialized = ContentSerialization.constructEntitySerialization(item);
                inventoryData.addEquippedItem(point, itemSerialized);
                inventoryData.setEquippedId(point, item.getId());
            }
        }
        if(InventoryUtils.hasToolbarInventory(e)){
            RelationalInventoryState toolbarInventory = InventoryUtils.getToolbarInventory(e);
            for(String slot : toolbarInventory.getSlots()){
                Entity slotItem = toolbarInventory.getItemSlot(slot);
                if(slotItem != null){
                    EntitySerialization itemSerialized = ContentSerialization.constructEntitySerialization(slotItem);
                    inventoryData.addToolbarItem(slot, itemSerialized);
                    inventoryData.setToolbarId(slot, slotItem.getId());
                }
            }
        }
        if(InventoryUtils.hasNaturalInventory(e)){
            UnrelationalInventoryState toolbarInventory = InventoryUtils.getNaturalInventory(e);
            int i = 0;
            for(Entity item : toolbarInventory.getItems()){
                if(item != null){
                    EntitySerialization itemSerialized = ContentSerialization.constructEntitySerialization(item);
                    inventoryData.addNaturalItem(itemSerialized);
                    inventoryData.setNaturalId(i, item.getId());
                    i++;
                }
            }
        }
        if(StateCollection.hasStateCollection(e)){
            template.setStateCollection(StateCollection.getStateCollection(e));
        }
        return template;
    }

}

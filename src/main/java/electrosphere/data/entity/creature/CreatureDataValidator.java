package electrosphere.data.entity.creature;

import java.util.LinkedList;
import java.util.List;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.data.entity.creature.attack.AttackMove;
import electrosphere.data.entity.creature.bonegroups.BoneGroup;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.creature.movement.FallMovementSystem;
import electrosphere.data.entity.creature.movement.GroundMovementSystem;
import electrosphere.data.entity.creature.movement.JumpMovementSystem;
import electrosphere.data.entity.creature.movement.MovementSystem;
import electrosphere.logger.LoggerInterface;

/**
 * Validates a creature
 */
public class CreatureDataValidator {
    
    /**
     * Validates a creature's data
     * @param data The creature's data
     */
    public static void validate(CreatureData data){
        CreatureDataValidator.validateBoneGroups(data);
        CreatureDataValidator.validateAnimations(data);
    }


    /**
     * Validates the bone groups
     * @param data The creature data
     */
    static void validateBoneGroups(CreatureData data){
        List<String> bonesUsedFirstPerson = new LinkedList<String>();
        List<String> bonesUsedThirdPerson = new LinkedList<String>();
        List<BoneGroup> boneGroups = data.getBoneGroups();
        if(boneGroups != null){
            for(BoneGroup group : boneGroups){

                //check first person bones
                if(group.getBoneNamesFirstPerson() != null){
                    for(String boneName : group.getBoneNamesFirstPerson()){
                        if(bonesUsedFirstPerson.contains(boneName)){
                            //same bone used in multiple groups
                            String message = "Two bone groups have the same bone in them!\n" +
                            "Bone name: " + boneName + "\n" +
                            "Second group: " + group.getId() + "\n" +
                            "Creature name: " + data.getId()
                            ;
                            LoggerInterface.loggerEngine.WARNING(message);
                        } else {
                            bonesUsedFirstPerson.add(boneName);
                        }
                    }
                }

                //check third person bones
                if(group.getBoneNamesThirdPerson() != null){
                    for(String boneName : group.getBoneNamesThirdPerson()){
                        if(bonesUsedThirdPerson.contains(boneName)){
                            //same bone used in multiple groups
                            String message = "Two bone groups have the same bone in them!\n" +
                            "Bone name: " + boneName + "\n" +
                            "Second group: " + group.getId() + "\n" +
                            "Creature name: " + data.getId()
                            ;
                            LoggerInterface.loggerEngine.WARNING(message);
                        } else {
                            bonesUsedThirdPerson.add(boneName);
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates the bone groups
     * @param data The creature data
     */
    static void validateAnimations(CreatureData data){
        List<TreeDataAnimation> animations = new LinkedList<TreeDataAnimation>();
        if(data.getAttackMoves() != null){
            for(AttackMove move : data.getAttackMoves()){
                if(move.getWindupState() != null){
                    animations.add(move.getWindupState().getAnimation());
                }
                if(move.getHoldState() != null){
                    animations.add(move.getHoldState().getAnimation());
                }
                if(move.getAttackState() != null){
                    animations.add(move.getAttackState().getAnimation());
                }
                if(move.getCooldownState() != null){
                    animations.add(move.getCooldownState().getAnimation());
                }
            }
        }
        if(data.getEquipPoints() != null){
            for(EquipPoint point : data.getEquipPoints()){
                animations.add(point.getEquippedAnimation());
            }
        }
        if(data.getHealthSystem() != null && data.getHealthSystem().getDyingState() != null){
            animations.add(data.getHealthSystem().getDyingState().getAnimation());
        }
        for(MovementSystem system : data.getMovementSystems()){
            if(system instanceof GroundMovementSystem){
                GroundMovementSystem groundMovementSystem = (GroundMovementSystem)system;
                animations.add(groundMovementSystem.getAnimationLoop());
                animations.add(groundMovementSystem.getAnimationStartup());
                animations.add(groundMovementSystem.getAnimationWindDown());
            }
            if(system instanceof JumpMovementSystem){
                JumpMovementSystem jumpMovementSystem = (JumpMovementSystem)system;
                animations.add(jumpMovementSystem.getAnimationJump());
            }
            if(system instanceof FallMovementSystem){
                FallMovementSystem fallMovementSystem = (FallMovementSystem)system;
                if(fallMovementSystem.getFallState() != null){
                    animations.add(fallMovementSystem.getFallState().getAnimation());
                }
                if(fallMovementSystem.getLandState() != null){
                    animations.add(fallMovementSystem.getLandState().getAnimation());
                }
            }
        }
        for(TreeDataAnimation animation : animations){
            if(animation != null){
                if(animation.getPriority() == null && animation.getPriorityCategory() == null){
                    //same bone used in multiple groups
                    String message = "Animation does not have priority defined!\n" +
                    "Creature name: " + data.getId() + "\n" +
                    "Animation first person name: " + animation.getNameFirstPerson() + "\n" +
                    "Animation third person name: " + animation.getNameThirdPerson() + "\n"
                    ;
                    LoggerInterface.loggerEngine.WARNING(message);
                }
            }
        }
    }

}

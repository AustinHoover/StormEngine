package electrosphere.server.ai.trees.creature.melee;

import electrosphere.data.entity.creature.ai.AttackerTreeData;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementRelativeFacing;
import electrosphere.server.ai.blackboard.BlackboardKeys;
import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.AITreeNode.AITreeNodeResult;
import electrosphere.server.ai.nodes.actions.combat.AttackStartNode;
import electrosphere.server.ai.nodes.actions.combat.MeleeRangeCheckNode;
import electrosphere.server.ai.nodes.actions.combat.MeleeTargetingNode;
import electrosphere.server.ai.nodes.actions.combat.MeleeRangeCheckNode.MeleeRangeCheckType;
import electrosphere.server.ai.nodes.actions.move.FaceTargetNode;
import electrosphere.server.ai.nodes.actions.move.MoveStartNode;
import electrosphere.server.ai.nodes.actions.move.MoveStopNode;
import electrosphere.server.ai.nodes.actions.move.WalkStartNode;
import electrosphere.server.ai.nodes.actions.move.WalkStopNode;
import electrosphere.server.ai.nodes.checks.IsMovingNode;
import electrosphere.server.ai.nodes.checks.equip.HasWeaponNode;
import electrosphere.server.ai.nodes.meta.collections.RandomizerNode;
import electrosphere.server.ai.nodes.meta.collections.SelectorNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;
import electrosphere.server.ai.nodes.meta.decorators.FailerNode;
import electrosphere.server.ai.nodes.meta.decorators.InverterNode;
import electrosphere.server.ai.nodes.meta.decorators.SucceederNode;
import electrosphere.server.ai.nodes.meta.decorators.TimerNode;
import electrosphere.server.ai.nodes.meta.decorators.UntilNode;
import electrosphere.server.ai.nodes.meta.decorators.conditional.OnFailureNode;

/**
 * Creates a melee ai tree
 */
public class MeleeAITree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "Melee";

    /**
     * Creates a melee tree
     * @return The root node of the tree
     */
    public static AITreeNode create(AttackerTreeData attackerTreeData){

        return new SequenceNode(
            "MeleeAITree",
            //preconditions here
            new HasWeaponNode(),
            new MeleeTargetingNode(attackerTreeData.getAggroRange()),

            //perform different actions based on distance to target
            new SelectorNode(

                //in attack range
                new SequenceNode(
                    "MeleeAITree",
                    //check prior to performing action
                    new MeleeRangeCheckNode(attackerTreeData,MeleeRangeCheckType.ATTACK),

                    //set state
                    //stop walking now that we're in range
                    new PublishStatusNode("Slowing down"),
                    new MoveStopNode(),
                    new UntilNode(AITreeNodeResult.FAILURE, new IsMovingNode()),

                    //select action to perform
                    new RandomizerNode(
                        //wait
                        new SequenceNode(
                            "MeleeAITree",
                            new PublishStatusNode("Waiting"),
                            new FaceTargetNode(BlackboardKeys.ENTITY_TARGET),
                            new TimerNode(new SucceederNode(null), 600)
                        ),
                        //wait
                        new SequenceNode(
                            "MeleeAITree",
                            new PublishStatusNode("Waiting"),
                            new FaceTargetNode(BlackboardKeys.ENTITY_TARGET),
                            new TimerNode(new SucceederNode(null), 300)
                        ),
                        //attack
                        new SequenceNode(
                            "MeleeAITree",
                            new PublishStatusNode("Attacking"),
                            new FaceTargetNode(BlackboardKeys.ENTITY_TARGET),
                            new AttackStartNode(),
                            new TimerNode(new SucceederNode(null), 300)
                        )
                    )
                ),

                //in aggro range
                new SequenceNode(
                    "MeleeAITree",
                    //check prior to performing action
                    new MeleeRangeCheckNode(attackerTreeData,MeleeRangeCheckType.AGGRO),

                    //select action to perform
                    new RandomizerNode(

                        //wait
                        new SequenceNode(
                            "MeleeAITree",
                            new PublishStatusNode("Waiting"),
                            new FaceTargetNode(BlackboardKeys.ENTITY_TARGET),
                            new TimerNode(new SucceederNode(null), 1200)
                        ),

                        //strafe to the right
                        new SequenceNode(
                            "MeleeAITree",
                            new PublishStatusNode("Strafing right"),
                            new WalkStartNode(),
                            new InverterNode(new OnFailureNode(
                                new MoveStartNode(MovementRelativeFacing.RIGHT),
                                new FailerNode(null)
                            )),
                            new FaceTargetNode(BlackboardKeys.ENTITY_TARGET),
                            new TimerNode(new SucceederNode(null), 600),
                            new SucceederNode(new WalkStopNode()),
                            new SucceederNode(new MoveStopNode())
                        ),

                        //strafe to the left
                        new SequenceNode(
                            "MeleeAITree",
                            new PublishStatusNode("Strafing left"),
                            new WalkStartNode(),
                            new InverterNode(new OnFailureNode(
                                new MoveStartNode(MovementRelativeFacing.LEFT),
                                new FailerNode(null)
                            )),
                            new FaceTargetNode(BlackboardKeys.ENTITY_TARGET),
                            new TimerNode(new SucceederNode(null), 600),
                            new SucceederNode(new WalkStopNode()),
                            new SucceederNode(new MoveStopNode())
                        ),

                        //approach target
                        //move towards target and attack
                        new SequenceNode(
                            "MeleeAITree",
                            new PublishStatusNode("Move into attack range"),
                            new FaceTargetNode(BlackboardKeys.ENTITY_TARGET),
                            new SucceederNode(new MoveStartNode(MovementRelativeFacing.FORWARD)),
                            new TimerNode(new SucceederNode(null), 600)
                        )
                    )
                )
            )
            // //determine strategy
            // new RandomizerNode(
            //     //wait
            //     new SequenceNode(
            //         new PublishStatusNode("Waiting"),
            //         new FaceTargetNode(),
            //         new TimerNode(new SucceederNode(null), 1200)
            //     ),

            //     //move to hover distance
            //     new SequenceNode(
            //         new PublishStatusNode("Strafing right"),
            //         new InverterNode(new OnFailureNode(
            //             new WalkStartNode(MovementRelativeFacing.RIGHT),
            //             new FailerNode(null)
            //         )),
            //         new FaceTargetNode(),
            //         new TimerNode(new SucceederNode(null), 600),
            //         new SucceederNode(new WalkStopNode())
            //     ),

            //     //move from hover distance to melee range
            //     new SequenceNode(
            //         new PublishStatusNode("Strafing left"),
            //         new InverterNode(new OnFailureNode(
            //             new WalkStartNode(MovementRelativeFacing.LEFT),
            //             new FailerNode(null)
            //         )),
            //         new FaceTargetNode(),
            //         new TimerNode(new SucceederNode(null), 600),
            //         new SucceederNode(new WalkStopNode())
            //     ),

            //     //move towards target and attack
            //     new SequenceNode(
            //         new PublishStatusNode("Attack target"),
            //         //move towards target if its outside of melee range
            //         new UntilNode(AITreeNodeResult.SUCCESS,
            //             //or
            //             new SelectorNode(
            //                 //in range
            //                 new MeleeRangeCheckNode(attackerTreeData,MeleeRangeCheckType.ATTACK),
            //                 //approaching target
            //                 new SequenceNode(
            //                     new PublishStatusNode("Approaching target"),
            //                     new FaceTargetNode(),
            //                     new OnFailureNode(new IsMovingNode(), new WalkStartNode(MovementRelativeFacing.FORWARD)),
            //                     new MeleeRangeCheckNode(attackerTreeData,MeleeRangeCheckType.ATTACK)
            //                 )
            //             )
            //         ),
            //         //stop walking now that we're in range
            //         new PublishStatusNode("Slowing down"),
            //         new WalkStopNode(),
            //         new UntilNode(AITreeNodeResult.FAILURE, new IsMovingNode()),
            //         new PublishStatusNode("Attacking"),
            //         new AttackStartNode()
            //     )
            // )
        );
    }

}

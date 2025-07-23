package electrosphere.server.ai.trees.hierarchy.safety.shelter;

import electrosphere.server.ai.nodes.AITreeNode;
import electrosphere.server.ai.nodes.checks.macro.HasShelter;
import electrosphere.server.ai.nodes.checks.macro.IsCharacterNode;
import electrosphere.server.ai.nodes.meta.collections.SelectorNode;
import electrosphere.server.ai.nodes.meta.collections.SequenceNode;
import electrosphere.server.ai.nodes.meta.debug.PublishStatusNode;

/**
 * A tree that causes the entity to try to secure shelter
 */
public class ShelterTree {
    
    /**
     * Name of the tree
     */
    public static final String TREE_NAME = "ShelterTree";
    
    /**
     * Creates a shelter tree
     * @return The root node of the tree
     */
    public static AITreeNode create(){
        return new SequenceNode(
            "ShelterTree",
            new PublishStatusNode("Evaluate shelter"),
            //make sure that this entity actually cares about shelter
            new SequenceNode(
                "ShelterTree",
                //if this is a character
                new IsCharacterNode()
            ),
            //now that we know the entity cares about shelter, check if they have shelter
            new SequenceNode(
                "ShelterTree",
                //if has shelter..
                new SelectorNode(
                    new SequenceNode(
                        "ShelterTree",
                        new HasShelter()
                        //already has shelter
                        //TODO: check environment (ie time of day) to see if we should return to shelter
                    ),
                    new SequenceNode(
                        "ShelterTree",
                        //does not have shelter
                        ConstructShelterTree.create()
                    )
                )
            )
        );
    }

}

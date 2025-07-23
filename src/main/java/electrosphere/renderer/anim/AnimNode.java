package electrosphere.renderer.anim;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4d;
import org.lwjgl.assimp.AINode;

/**
 * A node that will be animated by an animation
 */
public class AnimNode {
    public String id;
    private Matrix4d transform;
    public AnimNode parent;
    public List<AnimNode> children;
    public boolean is_bone;
    public AINode raw_data;
    public AnimNode(String id, AnimNode parent, AINode raw_data){
        this.id = id;
        this.parent = parent;
        this.children = new ArrayList<AnimNode>();
        this.transform = new Matrix4d();
        is_bone = false;
        this.raw_data = raw_data;
    }

    public void getTransform(Matrix4d loc){
        loc.set(transform);
    }

    public void setTransform(Matrix4d transform){
        this.transform.set(transform);
    }
}

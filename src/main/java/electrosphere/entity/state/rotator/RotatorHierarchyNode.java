package electrosphere.entity.state.rotator;

import java.util.LinkedList;
import java.util.List;

public class RotatorHierarchyNode {
    
    String bone;
    List<RotatorConstraint> rotatorConstraints = new LinkedList<RotatorConstraint>();

    public String getBone(){
        return bone;
    }

    public List<RotatorConstraint> getRotatorContraints(){
        return rotatorConstraints;
    }

    public void addRotatorConstraint(RotatorConstraint contraint){
        this.rotatorConstraints.add(contraint);
    }

    public void setBone(String bone){
        this.bone = bone;
    }

}

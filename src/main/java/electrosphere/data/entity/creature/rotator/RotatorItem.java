package electrosphere.data.entity.creature.rotator;

import java.util.List;

public class RotatorItem {
    String boneName;
    List<RotatorConstraint> constraints;

    public String getBoneName(){
        return boneName;
    }

    public List<RotatorConstraint> getConstraints(){
        return constraints;
    }
}

package electrosphere.entity.state.view;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;

public class ViewUtils {
    
    public static void setPitch(Entity parent, float pitch){
        parent.putData(EntityDataStrings.VIEW_PITCH, pitch);
    }

    public static float getPitch(Entity parent){
        if(parent.containsKey(EntityDataStrings.VIEW_PITCH)){
            return (float)parent.getData(EntityDataStrings.VIEW_PITCH);
        } else {
            return 0;
        }
    }

}

package electrosphere.data.entity.projectile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectileTypeHolder {

    List<ProjectileType> projectiles;

    Map<String,ProjectileType> projectileTypeMap;
    
    public void init(){
        projectileTypeMap = new HashMap<String, ProjectileType>();
        for(ProjectileType type : projectiles){
            projectileTypeMap.put(type.getId(), type);
        }
    }

    public ProjectileType getType(String id){
        return projectileTypeMap.get(id);
    }
    
}

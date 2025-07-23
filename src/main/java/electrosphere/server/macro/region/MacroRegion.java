package electrosphere.server.macro.region;

import org.joml.AABBd;
import org.joml.Vector3d;

import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.spatial.MacroAreaObject;
import electrosphere.util.math.region.Region;

/**
 * A macro data spatial region
 */
public class MacroRegion implements MacroAreaObject {
    

    /**
     * The id of the region
     */
    private long id;

    /**
     * The region
     */
    private Region region;

    /**
     * Creates a macro region
     * @param macroData The macro data
     * @param region The region
     * @return The macro region
     */
    public static MacroRegion create(MacroData macroData, Region region){
        MacroRegion rVal = new MacroRegion();
        rVal.region = region;
        macroData.registerRegion(rVal);
        return rVal;
    }

    /**
     * Gets the id of the region
     * @return The id of the region
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id of the region
     * @param id The id of the region
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the spatial data of the region
     * @return The spatial data
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Sets the spatial data of the region
     * @param region The spatial data of the region
     */
    public void setRegion(Region region) {
        this.region = region;
    }

    @Override
    public Vector3d getPos() {
        return new Vector3d(this.region.getAABB().minX,this.region.getAABB().minY,this.region.getAABB().minZ);
    }

    @Override
    public void setPos(Vector3d pos) {
        throw new UnsupportedOperationException("Unimplemented method 'setPos'");
    }

    @Override
    public AABBd getAABB() {
        return this.region.getAABB();
    }

    

}

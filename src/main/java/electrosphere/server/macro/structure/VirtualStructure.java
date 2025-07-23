package electrosphere.server.macro.structure;

import org.joml.AABBd;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.controls.cursor.CursorState;
import electrosphere.data.block.fab.BlockFab;
import electrosphere.data.macro.struct.StructureData;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.spatial.MacroAreaObject;
import electrosphere.util.annotation.Exclude;

/**
 * Server representation of a structure
 */
public class VirtualStructure implements MacroAreaObject {

    /**
     * Rotates the structure to face east
     */
    public static final int ROT_FACE_EAST = 0;

    /**
     * Rotates the structure ot face north
     */
    public static final int ROT_FACE_NORTH = 1;

    /**
     * Rotates the structure to face west
     */
    public static final int ROT_FACE_WEST = 2;

    /**
     * Rotates the structure to face south
     */
    public static final int ROT_FACE_SOUTH = 3;

    /**
     * The id of the structure
     */
    private int id;

    /**
     * The position of the structure
     */
    private Vector3d position;

    /**
     * The rotation of the fab
     */
    private int rotation = 0;

    /**
     * The bounding box for the structure
     */
    private AABBd aabb;

    /**
     * The path to the block fab for the structure
     */
    private String fabPath;

    /**
     * The actual fab
     */
    @Exclude
    private BlockFab fab;
    
    /**
     * The type of the structure
     */
    private String type;

    /**
     * Tracks whether this structure needs repairs or not
     */
    private boolean repairable = false;

    /**
     * Creates a structure
     * @param macroData The macro data
     * @param data The data
     * @param position The position
     * @param rotation The rotation of the structure
     * @return The structure
     */
    public static VirtualStructure createStructure(MacroData macroData, StructureData data, Vector3d position, int rotation){
        VirtualStructure rVal = new VirtualStructure();
        rVal.fabPath = data.getFabPath();
        rVal.fab = data.getFab();
        rVal.type = data.getId();
        rVal.position = ServerWorldData.clampRealToBlock(position);
        rVal.rotation = rotation;
        rVal.aabb = new AABBd();
        VirtualStructure.setAABB(rVal.aabb, rVal.position, data.getDimensions(), rotation);
        macroData.addStructure(rVal);
        return rVal;
    }

    /**
     * Gets the type of the structure
     * @return The type
     */
    public String getType() {
        return type;
    }

    @Override
    public Vector3d getPos() {
        return this.position;
    }

    @Override
    public void setPos(Vector3d pos) {
        this.position = pos;
    }

    @Override
    public AABBd getAABB() {
        return this.aabb;
    }

    /**
     * Gets the path to the corresponding fab
     * @return The path
     */
    public String getFabPath(){
        return fabPath;
    }

    /**
     * Gets the fab object
     * @return The fab object
     */
    public BlockFab getFab() {
        return fab;
    }

    /**
     * Sets the fab object
     * @param fab The fab object
     */
    public void setFab(BlockFab fab) {
        this.fab = fab;
    }

    /**
     * Checks if the structure is repairable
     * @return true if it is repairable, false otherwise
     */
    public boolean isRepairable() {
        return repairable;
    }

    /**
     * Sets whether this structure is repairable or not
     * @param repairable true if it is repairable, false otherwise
     */
    public void setRepairable(boolean repairable) {
        this.repairable = repairable;
    }

    /**
     * Gets the id of the structure
     * @return The id
     */
    public int getId(){
        return id;
    }

    /**
     * Sets the id of this structure
     * @param id The id
     */
    public void setId(int id){
        this.id = id;
    }

    /**
     * Sets the rotation of the structure
     * @param rotation The rotation
     */
    public void setRotation(int rotation){
        this.rotation = rotation;
    }
    
    /**
     * Gets the rotation of the structure
     * @return The rotation of the structure
     */
    public int getRotation(){
        return this.rotation;
    }

    /**
     * Calculates the aabb of this structure given a rotation, dims, and start position
     * @param aabb The aabb to populate
     * @param startPos The start position
     * @param dims The dimensions of the structure
     * @param rotation The rotation
     */
    public static void setAABB(AABBd aabb, Vector3d startPos, Vector3d dims, int rotation){
        //construct aabb based on rotation
        Quaterniond rotationQuat = CursorState.getBlockRotation(rotation);
        Vector3d dimVec = new Vector3d(dims);
        rotationQuat.transform(dimVec);
        Vector3d startVec = startPos;
        Vector3d endVec = new Vector3d(startPos).add(dimVec);
        aabb.setMin(Math.min(startVec.x,endVec.x),Math.min(startVec.y,endVec.y),Math.min(startVec.z,endVec.z));
        aabb.setMax(Math.max(startVec.x,endVec.x),Math.max(startVec.y,endVec.y),Math.max(startVec.z,endVec.z));
    }
    
    
}

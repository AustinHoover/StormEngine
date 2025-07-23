package electrosphere.server.macro.civilization.road;

import org.joml.AABBd;
import org.joml.Vector3d;

import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.spatial.MacroAreaObject;

/**
 * A road
 */
public class Road implements MacroAreaObject {

    /**
     * The default radius
     */
    public static final double DEFAULT_RADIUS = 5;

    /**
     * The default material
     */
    public static final String defaultMaterial = "dirt";
    
    /**
     * The id of the road
     */
    int id;

    /**
     * The start position of the road segment
     */
    Vector3d startPos;

    /**
     * The end position of the road segment
     */
    Vector3d endPos;

    /**
     * The radius of the road
     */
    double radius = Road.DEFAULT_RADIUS;

    /**
     * The aabb of the road
     */
    AABBd aabb;

    /**
     * The material that the road is made out of
     */
    String roadMaterial = Road.defaultMaterial;

    /**
     * Private contructor
     */
    private Road(){ }

    /**
     * Creates a road
     * @param macroData The macro data
     * @param start The start position
     * @param end The end position
     * @return The road
     */
    public static Road createRoad(MacroData macroData, Vector3d start, Vector3d endPos){
        Road road = new Road();
        road.startPos = start;
        road.endPos = endPos;
        road.computeAABB();
        macroData.addRoad(road);
        return road;
    }

    /**
     * Gets the id of the road
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the road
     * @param id The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the radius of the road
     * @return The radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the road
     * @param radius The radius
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Gets the road material
     * @return The material
     */
    public String getRoadMaterial() {
        return roadMaterial;
    }

    /**
     * Sets the road material
     * @param roadMaterial The material
     */
    public void setRoadMaterial(String roadMaterial) {
        this.roadMaterial = roadMaterial;
    }

    /**
     * Computes the aabb of the road
     */
    private void computeAABB(){
        this.aabb = new AABBd();
        this.aabb.minX = Math.min(this.startPos.x,this.endPos.x) - radius;
        this.aabb.minY = Math.min(this.startPos.y,this.endPos.y) - radius;
        this.aabb.minZ = Math.min(this.startPos.z,this.endPos.z) - radius;
        this.aabb.maxX = Math.max(this.startPos.x,this.endPos.x) + radius;
        this.aabb.maxY = Math.max(this.startPos.y,this.endPos.y) + radius;
        this.aabb.maxZ = Math.max(this.startPos.z,this.endPos.z) + radius;
    }

    /**
     * Gets the first point of the road segment
     * @return The first point
     */
    public Vector3d getPoint1(){
        return startPos;
    }

    /**
     * Gets the send point of the road segment
     * @return The second point
     */
    public Vector3d getPoint2(){
        return endPos;
    }

    @Override
    public Vector3d getPos() {
        throw new UnsupportedOperationException("Unimplemented method 'getPos'");
    }

    @Override
    public void setPos(Vector3d pos) {
        throw new UnsupportedOperationException("Unimplemented method 'setPos'");
    }

    @Override
    public AABBd getAABB() {
        return aabb;
    }
    

}

package electrosphere.server.pathfinding.recast;

import java.util.List;

import org.joml.Vector3d;

/**
 * Data tracking moving along a solved path
 */
public class PathingProgressiveData {
    
    /**
     * The list of points that represent the path
     */
    private List<Vector3d> points;

    /**
     * The current point to move towards (ie all previous points have already been pathed to)
     */
    private int currentPoint;

    /**
     * The goal position
     */
    private Vector3d goal;

    /**
     * Tracks whether this data is ready to be used or not
     */
    private boolean ready = false;


    /**
     * Constructor
     * @param goal The goal point
     */
    public PathingProgressiveData(Vector3d goal){
        this.goal = goal;
        this.currentPoint = 0;
    }

    /**
     * Gets the points that define the path
     * @return The points
     */
    public List<Vector3d> getPoints() {
        return points;
    }

    /**
     * Sets the points that define the path
     * @param points The points
     */
    public void setPoints(List<Vector3d> points) {
        this.points = points;
    }

    /**
     * Gets the current point to move towards
     * @return The current point's index
     */
    public int getCurrentPoint() {
        return currentPoint;
    }

    /**
     * Sets the current point to move towards
     * @param currentPoint The current point's index
     */
    public void setCurrentPoint(int currentPoint) {
        this.currentPoint = currentPoint;
    }

    /**
     * Gets the goal point
     * @return The goal point
     */
    public Vector3d getGoal() {
        return goal;
    }

    /**
     * Sets the goal point
     * @param goal The goal point
     */
    public void setGoal(Vector3d goal) {
        this.goal = goal;
    }

    /**
     * Gets whether this data is ready or not
     * @return true if it is ready, false otherwise
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Sets the ready status of this data
     * @param ready true if the data is ready, false otherwise
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }
    

}

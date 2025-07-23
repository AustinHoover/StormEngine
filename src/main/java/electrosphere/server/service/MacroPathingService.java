package electrosphere.server.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;

import org.joml.Vector3d;

import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.engine.Globals;
import electrosphere.engine.signal.SignalServiceImpl;
import electrosphere.engine.threads.ThreadCounts;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.spatial.path.MacroPathCache;
import electrosphere.server.macro.spatial.path.MacroPathNode;
import electrosphere.server.pathfinding.recast.PathingProgressiveData;

/**
 * Service for solving pathing between macro area objects
 */
public class MacroPathingService extends SignalServiceImpl {

    /**
     * The executor service
     */
    final ExecutorService executorService;

    /**
     * Constructor
     */
    public MacroPathingService() {
        super("MacroPathingService", new SignalType[]{
        });
        this.executorService = Globals.engineState.threadManager.requestFixedThreadPool(ThreadCounts.MACRO_PATHING_THREADS);
    }

    /**
     * Simulates the macro pathing service
     */
    public void simulate(){
    }

    /**
     * Queues a pathfinding job
     * @param realm The realm
     * @param start The start point
     * @param end The end point
     * @return The object that will eventually hold the pathfinding data
     */
    public PathingProgressiveData queuePathfinding(Realm realm, MacroPathNode start, MacroPathNode end){
        PathingProgressiveData rVal = new PathingProgressiveData(end.getPosition());
        executorService.submit(() -> {
            try {
                List<Vector3d> points = this.findPath(realm.getMacroData(), start, end);
                rVal.setPoints(points);
                rVal.setReady(true);
            } catch(Throwable e){
                e.printStackTrace();
            }
        });
        return rVal;
    }

    /**
     * Halts all threads in the pathfinding service
     */
    public void haltThreads(){
        executorService.shutdownNow();
    }

    /**
     * Finds a path between macro area objects
     * @param macroData The macro data
     * @param start The start area
     * @param end The end area
     * @return The path
     */
    protected List<Vector3d> findPath(MacroData macroData, MacroPathNode start, MacroPathNode end){
        List<Vector3d> rVal = null;
        //tracks whether we've found the goal or not
        boolean foundGoal = false;
        int countConsidered = 0;
        MacroPathCache pathCache = macroData.getPathCache();

        //create sets
        PriorityQueue<PathfinderNode> openSet = new PriorityQueue<PathfinderNode>();
        Map<Long,PathfinderNode> openSetLookup = new HashMap<Long,PathfinderNode>();
        Map<Long,PathfinderNode> closetSet = new HashMap<Long,PathfinderNode>();

        //add start node
        PathfinderNode node = new PathfinderNode(start, 0, start.getId());
        openSet.add(node);
        openSetLookup.put(node.graphNode.getId(),node);

        //search
        while(openSet.size() > 0 && !foundGoal){

            //pull from open set
            PathfinderNode currentNode = openSet.poll();
            long currentCost = currentNode.cost;
            openSetLookup.remove(currentNode.graphNode.getId());
            closetSet.put(currentNode.graphNode.getId(), currentNode);
            countConsidered++;


            //iterate along neighbors
            for(Long neighborId : currentNode.graphNode.getNeighborIds()){
                //goal check
                if(end.getId() == neighborId){
                    foundGoal = true;
                    break;
                }
                //add-to-set check
                if(!closetSet.containsKey(neighborId) && !openSetLookup.containsKey(neighborId)){
                    MacroPathNode neighborGraphNode = pathCache.getNodeById(neighborId);
                    long newCost = currentCost + neighborGraphNode.getCost();
                    PathfinderNode newNode = new PathfinderNode(
                        neighborGraphNode,
                        newCost, currentNode.graphNode.getId()
                    );
                    openSet.add(newNode);
                    openSetLookup.put(neighborGraphNode.getId(), newNode);
                }
            }

            //if found goal
            if(foundGoal){
                //reverse up the chain from here
                rVal = new LinkedList<Vector3d>();
                rVal.add(end.getPosition());
                while(currentNode.prevNode != currentNode.graphNode.getId()){
                    rVal.add(0,currentNode.getPosition());
                    currentNode = closetSet.get(currentNode.prevNode);
                }
                rVal.add(0,start.getPosition());
                break;
            }

            //error check
            if(openSet.size() < 1){
                throw new Error("Open set ran out of nodes! " + countConsidered);
            }
        }

        if(!foundGoal){
            throw new Error("Failed to find goal " + countConsidered);
        }
        return rVal;
    }


    /**
     * A node to use during searching
     */
    protected static class PathfinderNode implements Comparable<PathfinderNode> {

        /**
         * The corresponding graph node
         */
        MacroPathNode graphNode;

        /**
         * Cost to get to this node
         */
        long cost = 0;

        /**
         * The previous node
         */
        long prevNode = 0;

        public PathfinderNode(
            MacroPathNode graphNode,
            long cost, long prevNode
        ){
            this.graphNode = graphNode;
            this.cost = cost;
            this.prevNode = prevNode;
        }

        @Override
        public int compareTo(PathfinderNode o) {
            return (int)(this.cost - o.cost);
        }

        /**
         * Gets the position of the node
         * @return The position of the node
         */
        public Vector3d getPosition(){
            return graphNode.getPosition();
        }
    }
    
}

package electrosphere.server.macro.civilization.town;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joml.AABBd;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.controls.cursor.CursorState;
import electrosphere.data.macro.struct.StructureData;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.Realm;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.character.race.Race;
import electrosphere.server.macro.civilization.Civilization;
import electrosphere.server.macro.civilization.road.Road;
import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.spatial.path.MacroPathCache;
import electrosphere.server.macro.spatial.path.MacroPathNode;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.util.math.HashUtils;
import electrosphere.util.math.VoronoiUtils;
import electrosphere.util.math.region.RegionPrism;

/**
 * Lays out town objects
 */
public class TownLayout {

    /**
     * Scaler for town layout nodes
     */
    public static final double TOWN_LAYOUT_SCALER = 64;

    /**
     * The maximum radius of towns
     */
    public static final double TOWN_MAX_RADIUS = 256;

    /**
     * Radius within which to place densly-packed buildings
     */
    public static final double TOWN_CENTER_RADIUS = 128;

    /**
     * Relaxation factor for regularizing placement of town center nodes
     */
    public static final double VORONOI_RELAXATION_FACTOR = 0.3;

    /**
     * Offset applied to breadth search hashing
     */
    private static final int HASH_OFFSET = 1000;

    /**
     * Default height of a farm plot
     */
    private static final float FARM_PLOT_DEFAULT_HEIGHT = 20.0f;

    /**
     * Offset used to search for town centers
     */
    static final int[] offsetX = new int[]{
        -1,1,0,0
    };

    /**
     * Offset used to search for town centers
     */
    static final int[] offsetZ = new int[]{
        0,0,-1,1
    };

    /**
     * Lays out structures for a town
     * @param macroData The macro data
     * @param town The town
     */
    public static void layoutTown(Realm realm, MacroData macroData, Town town){

        MacroPathCache pathCache = macroData.getPathCache();

        //
        //figure out what structures we're allowed to place
        Civilization parentCiv = town.getParent(macroData);
        List<String> raceIds = parentCiv.getRaceIds();
        List<Race> races = raceIds.stream().map((String raceId) -> Globals.gameConfigCurrent.getRaceMap().getRace(raceId)).filter((Race race) -> race != null).collect(Collectors.toList());
        if(races.size() == 0){
            throw new Error("No races found! " + raceIds);
        }
        List<StructureData> allowedStructures = new LinkedList<StructureData>();
        for(Race race : races){
            for(String structureId : race.getStructureIds()){
                StructureData structData = Globals.gameConfigCurrent.getStructureData().getType(structureId);
                if(!allowedStructures.contains(structData)){
                    allowedStructures.add(structData);
                }
            }
        }
        if(allowedStructures.size() < 1){
            throw new Error("No structures found! " + raceIds);
        }
        LoggerInterface.loggerEngine.DEBUG("Allowed structure count: " + allowedStructures.size());

        //
        //find the nodes to connect
        Vector3d townCenter = town.getPos();
        Vector3d scanPoint = new Vector3d();
        Vector3d currPoint = new Vector3d();
        Vector3d nearPoint = null;

        //get center loc
        scanPoint.set(townCenter);
        Vector3d centerNodeLoc = TownLayout.getTownCenter(realm, scanPoint);

        //get north node
        scanPoint.set(townCenter).add(0,0,TOWN_LAYOUT_SCALER);
        Vector3d upNodeLoc = TownLayout.getTownCenter(realm, scanPoint);

        //get left node
        scanPoint.set(townCenter).add(-TOWN_LAYOUT_SCALER,0,0);
        Vector3d leftNodeLoc = TownLayout.getTownCenter(realm, scanPoint);

        //get right node
        scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER,0,0);
        Vector3d rightNodeLoc = TownLayout.getTownCenter(realm, scanPoint);

        //get south node
        scanPoint.set(townCenter).add(0,0,-TOWN_LAYOUT_SCALER);
        Vector3d downNodeLoc = TownLayout.getTownCenter(realm, scanPoint);




        //
        //generate roads
        Road.createRoad(macroData, upNodeLoc, centerNodeLoc);
        Road.createRoad(macroData, rightNodeLoc, centerNodeLoc);
        Road.createRoad(macroData, leftNodeLoc, centerNodeLoc);
        Road.createRoad(macroData, downNodeLoc, centerNodeLoc);



        //
        //Breadth search other nodes to branch outwards
        //

        //
        //sets for breadth search
        LinkedList<Long> openSet = new LinkedList<Long>();
        LinkedList<Long> closedSet = new LinkedList<Long>();
        closedSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET - 1, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET + 1, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET - 1));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET + 1));
        while(openSet.size() > 0){
            long openHash = openSet.poll();
            int x = HashUtils.unhashIVec(openHash, HashUtils.UNHASH_COMPONENT_X) - HASH_OFFSET;
            int z = HashUtils.unhashIVec(openHash, HashUtils.UNHASH_COMPONENT_Z) - HASH_OFFSET;
            scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * x,0,TOWN_LAYOUT_SCALER * z);
            currPoint = TownLayout.getTownCenter(realm, scanPoint);

            //check below
            for(int i = 0; i < 4; i++){
                int oX = x + offsetX[i];
                int oZ = z + offsetZ[i];
                scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * oX,0,TOWN_LAYOUT_SCALER * oZ);
                nearPoint = TownLayout.getTownCenter(realm, scanPoint);
                long newHash = HashUtils.hashIVec(HASH_OFFSET + oX, 0, HASH_OFFSET + oZ);
                if(nearPoint.distance(townCenter) < TOWN_MAX_RADIUS){
                    if(!openSet.contains(newHash) && !closedSet.contains(newHash)){
                        openSet.add(newHash);
                    }

                    //build road and structures between curr and next node
                    if(closedSet.contains(newHash)){
                        Road.createRoad(macroData, nearPoint, currPoint);
                    }
                }
            }

            closedSet.add(openHash);
        }

        //
        // Construct pathfinding nodes for roads and link them
        //
        //
        //sets for breadth search
        Map<Long,MacroPathNode> positionNodeMap = new HashMap<Long,MacroPathNode>();
        openSet.clear();
        closedSet.clear();
        closedSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET - 1, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET + 1, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET - 1));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET + 1));
        //add pathing nodes for initial entries
        MacroPathNode currentPathingNode = MacroPathNode.createRoadNode(pathCache, new Vector3d(centerNodeLoc));
        positionNodeMap.put(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET), currentPathingNode);

        //up
        currentPathingNode = MacroPathNode.createRoadNode(pathCache, new Vector3d(upNodeLoc));
        positionNodeMap.put(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET + 1), currentPathingNode);

        //left
        currentPathingNode = MacroPathNode.createRoadNode(pathCache, new Vector3d(leftNodeLoc));
        positionNodeMap.put(HashUtils.hashIVec(HASH_OFFSET - 1, 0, HASH_OFFSET), currentPathingNode);

        //right
        currentPathingNode = MacroPathNode.createRoadNode(pathCache, new Vector3d(rightNodeLoc));
        positionNodeMap.put(HashUtils.hashIVec(HASH_OFFSET + 1, 0, HASH_OFFSET), currentPathingNode);

        //down
        currentPathingNode = MacroPathNode.createRoadNode(pathCache, new Vector3d(downNodeLoc));
        positionNodeMap.put(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET - 1), currentPathingNode);
        
        while(openSet.size() > 0){
            long openHash = openSet.poll();
            int x = HashUtils.unhashIVec(openHash, HashUtils.UNHASH_COMPONENT_X) - HASH_OFFSET;
            int z = HashUtils.unhashIVec(openHash, HashUtils.UNHASH_COMPONENT_Z) - HASH_OFFSET;
            scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * x,0,TOWN_LAYOUT_SCALER * z);
            currPoint = TownLayout.getTownCenter(realm, scanPoint);
            currentPathingNode = positionNodeMap.get(openHash);
            if(currentPathingNode == null){
                throw new Error("Failed to find pathing node for hash");
            }

            //check below
            for(int i = 0; i < 4; i++){
                int oX = x + offsetX[i];
                int oZ = z + offsetZ[i];
                scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * oX,0,TOWN_LAYOUT_SCALER * oZ);
                nearPoint = TownLayout.getTownCenter(realm, scanPoint);
                long newHash = HashUtils.hashIVec(HASH_OFFSET + oX, 0, HASH_OFFSET + oZ);
                if(nearPoint.distance(townCenter) < TOWN_MAX_RADIUS){
                    if(!openSet.contains(newHash) && !closedSet.contains(newHash)){
                        openSet.add(newHash);
                    }
                    
                    //link the neighbor to this node
                    MacroPathNode neighborPathingNode;
                    if(positionNodeMap.containsKey(newHash)){
                        neighborPathingNode = positionNodeMap.get(newHash);
                    } else {
                        neighborPathingNode = MacroPathNode.createRoadNode(pathCache, new Vector3d(nearPoint));
                        positionNodeMap.put(newHash,neighborPathingNode);
                    }
                    currentPathingNode.addNeighbor(neighborPathingNode);
                }
            }
            closedSet.add(openHash);
        }


        //
        //Breadth search other nodes to branch outwards
        //

        //
        //sets for breadth search
        openSet = new LinkedList<Long>();
        closedSet = new LinkedList<Long>();
        closedSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET - 1, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET + 1, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET - 1));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET + 1));
        while(openSet.size() > 0){
            long openHash = openSet.poll();
            int x = HashUtils.unhashIVec(openHash, HashUtils.UNHASH_COMPONENT_X) - HASH_OFFSET;
            int z = HashUtils.unhashIVec(openHash, HashUtils.UNHASH_COMPONENT_Z) - HASH_OFFSET;
            scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * x,0,TOWN_LAYOUT_SCALER * z);
            currPoint = TownLayout.getTownCenter(realm, scanPoint);

            //check below
            for(int i = 0; i < 4; i++){
                int oX = x + offsetX[i];
                int oZ = z + offsetZ[i];
                scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * oX,0,TOWN_LAYOUT_SCALER * oZ);
                nearPoint = TownLayout.getTownCenter(realm, scanPoint);
                long newHash = HashUtils.hashIVec(HASH_OFFSET + oX, 0, HASH_OFFSET + oZ);
                if(nearPoint.distance(townCenter) < TOWN_MAX_RADIUS){
                    if(!openSet.contains(newHash) && !closedSet.contains(newHash)){
                        openSet.add(newHash);
                    }

                    //build road and structures between curr and next node
                    if(closedSet.contains(newHash)){
                        if(nearPoint.distance(townCenter) < TOWN_CENTER_RADIUS && currPoint.distance(townCenter) < TOWN_CENTER_RADIUS){
                            MacroPathNode roadPoint1 = positionNodeMap.get(openHash);
                            MacroPathNode roadPoint2 = positionNodeMap.get(newHash);
                            if(roadPoint1 == null || roadPoint2 == null){
                                throw new Error("Failed to resolve road points! " + roadPoint1 + " " + roadPoint2);
                            }
                            TownLayout.generateStructuresAlongRoad(
                                realm, town,
                                roadPoint1, roadPoint2,
                                nearPoint, currPoint, Road.DEFAULT_RADIUS,
                                allowedStructures
                            );
                        }
                    }
                }
            }

            closedSet.add(openHash);
        }



        //
        //Place field plots
        //

        //points for defining farm plots
        Vector3d plotPoint1 = new Vector3d();
        Vector3d plotPoint2 = new Vector3d();
        Vector3d plotPoint3 = new Vector3d();
        Vector3d plotPoint4 = new Vector3d();

        //
        //sets for breadth search
        openSet = new LinkedList<Long>();
        closedSet = new LinkedList<Long>();
        closedSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET - 1, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET + 1, 0, HASH_OFFSET));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET - 1));
        openSet.add(HashUtils.hashIVec(HASH_OFFSET, 0, HASH_OFFSET + 1));
        while(openSet.size() > 0){
            long openHash = openSet.poll();
            int x = HashUtils.unhashIVec(openHash, HashUtils.UNHASH_COMPONENT_X) - HASH_OFFSET;
            int z = HashUtils.unhashIVec(openHash, HashUtils.UNHASH_COMPONENT_Z) - HASH_OFFSET;
            scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * x,0,TOWN_LAYOUT_SCALER * z);
            currPoint = TownLayout.getTownCenter(realm, scanPoint);

            //check below
            for(int i = 0; i < 4; i++){
                int oX = x + offsetX[i];
                int oZ = z + offsetZ[i];
                scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * oX,0,TOWN_LAYOUT_SCALER * oZ);
                nearPoint = TownLayout.getTownCenter(realm, scanPoint);
                long newHash = HashUtils.hashIVec(HASH_OFFSET + oX, 0, HASH_OFFSET + oZ);
                if(nearPoint.distance(townCenter) < TOWN_MAX_RADIUS){
                    if(!openSet.contains(newHash) && !closedSet.contains(newHash)){
                        openSet.add(newHash);
                    }
                }
            }

            //this is +0,+0
            plotPoint1.set(currPoint).add(Road.DEFAULT_RADIUS,0,Road.DEFAULT_RADIUS);
            scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * (x + 1),0,TOWN_LAYOUT_SCALER * (z + 0));
            plotPoint2 = TownLayout.getTownCenter(realm, scanPoint).add(-Road.DEFAULT_RADIUS,0,Road.DEFAULT_RADIUS);
            scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * (x + 1),0,TOWN_LAYOUT_SCALER * (z + 1));
            plotPoint3 = TownLayout.getTownCenter(realm, scanPoint).add(-Road.DEFAULT_RADIUS,0,-Road.DEFAULT_RADIUS);
            scanPoint.set(townCenter).add(TOWN_LAYOUT_SCALER * (x + 0),0,TOWN_LAYOUT_SCALER * (z + 1));
            plotPoint4 = TownLayout.getTownCenter(realm, scanPoint).add(Road.DEFAULT_RADIUS,0,-Road.DEFAULT_RADIUS);
            if(
                plotPoint1.distance(townCenter) > TOWN_CENTER_RADIUS &&
                plotPoint2.distance(townCenter) > TOWN_CENTER_RADIUS &&
                plotPoint3.distance(townCenter) > TOWN_CENTER_RADIUS &&
                plotPoint4.distance(townCenter) > TOWN_CENTER_RADIUS
            ){
                plotPoint1.y = realm.getServerWorldData().getServerTerrainManager().getElevation(plotPoint1);
                plotPoint2.y = plotPoint1.y;
                plotPoint3.y = plotPoint1.y;
                plotPoint4.y = plotPoint1.y;
                MacroPathNode roadPoint1 = positionNodeMap.get(openHash);
                MacroPathNode roadPoint2 = positionNodeMap.get(HashUtils.hashIVec(HASH_OFFSET + x + 1, 0, HASH_OFFSET + z + 0));
                MacroPathNode roadPoint3 = positionNodeMap.get(HashUtils.hashIVec(HASH_OFFSET + x + 0, 0, HASH_OFFSET + z + 1));
                MacroPathNode roadPoint4 = positionNodeMap.get(HashUtils.hashIVec(HASH_OFFSET + x + 1, 0, HASH_OFFSET + z + 1));
                if(roadPoint1 == null || roadPoint2 == null || roadPoint3 == null || roadPoint4 == null){
                    // throw new Error("Failed to resolve road points! " + roadPoint1 + " " + roadPoint2 + " " + roadPoint1 + " " + roadPoint2);
                } else {
                    //define a farm plot with these points
                    TownLayout.generateFarmPlot(
                        realm,macroData,town,
                        roadPoint1,roadPoint2,roadPoint3,roadPoint4,
                        plotPoint1,plotPoint2,plotPoint3,plotPoint4
                    );
                }
            }

            closedSet.add(openHash);
        }
    }

    /**
     * Generates structures along a road
     * @param realm The realm
     * @param town The town
     * @param road The road
     * @param allowedStructures The list of allowed structure types
     */
    private static void generateStructuresAlongRoad(
        Realm realm, Town town,
        MacroPathNode roadPoint1, MacroPathNode roadPoint2,
        Vector3d startPoint, Vector3d endPoint, double radius,
        List<StructureData> allowedStructures
    ){
        MacroData macroData = realm.getMacroData();
        MacroPathCache pathCache = macroData.getPathCache();

        //get values to scan along
        int len = (int)startPoint.distance(endPoint);

        //determine if it's primarily north-south or east-west
        boolean isNorthSouth = true;
        if(Math.abs(endPoint.x - startPoint.x) > Math.abs(endPoint.z - startPoint.z)){
            isNorthSouth = false;
        } else {
            isNorthSouth = true;
        }

        int roadRadiusOffsetRaw = (int)(radius + 4);

        //offset applied to the scan location to not place it on top of the road
        Vector3d roadOffset = null;
        int rotation1 = VirtualStructure.ROT_FACE_WEST;
        int rotation2 = VirtualStructure.ROT_FACE_EAST;
        Vector3d rotOffset = new Vector3d();
        Quaterniond rotQuat1 = null;
        Quaterniond rotQuat2 = null;
        if(isNorthSouth){
            roadOffset = new Vector3d(roadRadiusOffsetRaw,0,0);
            rotation1 = VirtualStructure.ROT_FACE_SOUTH;
            rotation2 = VirtualStructure.ROT_FACE_NORTH;
            rotQuat1 = CursorState.getBlockRotation(rotation1);
            rotQuat2 = CursorState.getBlockRotation(rotation2);
        } else {
            roadOffset = new Vector3d(0,0,roadRadiusOffsetRaw);
            rotation1 = VirtualStructure.ROT_FACE_EAST;
            rotation2 = VirtualStructure.ROT_FACE_WEST;
            rotQuat1 = CursorState.getBlockRotation(rotation1);
            rotQuat2 = CursorState.getBlockRotation(rotation2);
        }

        //the position to try at
        Vector3d currPos = new Vector3d();
        AABBd aabb = new AABBd();

        //the type of structure to place
        StructureData structureData = allowedStructures.get(0);

        //scan along the length of the road
        for(int i = roadRadiusOffsetRaw; i < len - roadRadiusOffsetRaw; i++){
            //update rotation spatial offset based on current struct
            rotOffset.set(structureData.getDimensions());
            rotQuat1.transform(rotOffset);
            rotOffset.x = Math.min(rotOffset.x,0);
            rotOffset.y = Math.min(rotOffset.y,0);
            rotOffset.z = Math.min(rotOffset.z,0);
            rotOffset.mul(-1);


            //solve terrain position to place
            currPos.set(startPoint).lerp(endPoint,i/(double)len).add(roadOffset);
            // currPos.set(dir).mul(i).add(startPoint).add(roadOffset);
            currPos.y = realm.getServerWorldData().getServerTerrainManager().getElevation(currPos);
            //apply structure placement offset
            currPos.add(structureData.getPlacementOffset());
            //add offset to re-align after rotation
            currPos.add(rotOffset);

            //update aabb
            VirtualStructure.setAABB(aabb, currPos, structureData.getDimensions(), rotation1);

            if(!macroData.intersectsStruct(aabb)){
                VirtualStructure struct = VirtualStructure.createStructure(macroData, structureData, currPos, rotation1);
                town.addStructure(struct);
                //create pathing node for structure and link it to nearest town centers
                MacroPathNode structNode = MacroPathNode.create(pathCache, struct, new Vector3d(currPos));
                structNode.addNeighbor(roadPoint1);
                structNode.addNeighbor(roadPoint2);
                //TODO: once have successfully placed this structure type, pick a new one to place
            }
        }

        //scan along the length of the road
        for(int i = roadRadiusOffsetRaw; i < len - roadRadiusOffsetRaw; i++){
            //update rotation spatial offset based on current struct
            rotOffset.set(structureData.getDimensions());
            rotQuat2.transform(rotOffset);
            rotOffset.x = Math.max(rotOffset.x,0);
            rotOffset.y = Math.max(rotOffset.y,0);
            rotOffset.z = Math.max(rotOffset.z,0);
            rotOffset.mul(-1);

            //solve terrain position to place
            currPos.set(startPoint).lerp(endPoint,i/(double)len).sub(roadOffset);//.sub(structureData.getDimensions());
            currPos.y = realm.getServerWorldData().getServerTerrainManager().getElevation(currPos);
            //apply structure placement offset
            currPos.add(structureData.getPlacementOffset());
            //add offset to re-align after rotation
            currPos.add(rotOffset.x,0,rotOffset.z);

            //update aabb
            VirtualStructure.setAABB(aabb, currPos, structureData.getDimensions(), rotation2);

            if(!macroData.intersectsStruct(aabb)){
                VirtualStructure struct = VirtualStructure.createStructure(macroData, structureData, currPos, rotation2);
                town.addStructure(struct);
                //create pathing node for structure and link it to nearest town centers
                MacroPathNode structNode = MacroPathNode.create(pathCache, struct, new Vector3d(currPos));
                structNode.addNeighbor(roadPoint1);
                structNode.addNeighbor(roadPoint2);
                //TODO: once have successfully placed this structure type, pick a new one to place
            }
        }
    }

    /**
     * Creates a farm plot in the town at a given set of points
     * @param realm The realm the town is in
     * @param macroData The macro data
     * @param town The town
     * @param point1 The first point
     * @param point2 The second point
     * @param point3 The third point
     * @param point4 The fourth point
     */
    private static void generateFarmPlot(
        Realm realm, MacroData macroData, Town town,
        MacroPathNode roadPoint1, MacroPathNode roadPoint2, MacroPathNode roadPoint3, MacroPathNode roadPoint4,
        Vector3d point1, Vector3d point2, Vector3d point3, Vector3d point4
    ){
        RegionPrism region = RegionPrism.create(new Vector3d[]{
            new Vector3d(point1).sub(0,FARM_PLOT_DEFAULT_HEIGHT/2.0f,0),
            new Vector3d(point2).sub(0,FARM_PLOT_DEFAULT_HEIGHT/2.0f,0),
            new Vector3d(point3).sub(0,FARM_PLOT_DEFAULT_HEIGHT/2.0f,0),
            new Vector3d(point4).sub(0,FARM_PLOT_DEFAULT_HEIGHT/2.0f,0),
        }, FARM_PLOT_DEFAULT_HEIGHT);
        MacroRegion macroRegion = MacroRegion.create(macroData, region);
        town.addFarmPlot(macroRegion);
        //find center point of region
        Vector3d centerPoint = new Vector3d();
        centerPoint.add(point1);
        centerPoint.add(point2);
        centerPoint.add(point3);
        centerPoint.add(point4);
        centerPoint.mul(0.25);
        centerPoint.y = realm.getServerWorldData().getServerTerrainManager().getElevation(centerPoint);
        //create pathing node for region and link it to nearest town centers
        MacroPathNode structNode = MacroPathNode.create(macroData.getPathCache(), macroRegion, centerPoint);
        structNode.addNeighbor(roadPoint1);
        structNode.addNeighbor(roadPoint2);
        structNode.addNeighbor(roadPoint3);
        structNode.addNeighbor(roadPoint4);
    }

    /**
     * Clamps the scan point to the closest town center point
     * @param realm The realm
     * @param scanPoint The scan point
     * @return The closest town center point
     */
    private static Vector3d getTownCenter(Realm realm, Vector3d scanPoint){
        Vector3d solved = VoronoiUtils.solveClosestVoronoiNode(
            scanPoint.x / TOWN_LAYOUT_SCALER,
            0,
            scanPoint.z / TOWN_LAYOUT_SCALER,
            VORONOI_RELAXATION_FACTOR
        );
        solved.mul(TOWN_LAYOUT_SCALER,0,TOWN_LAYOUT_SCALER);
        Vector3i cell = VoronoiUtils.solveVoronoiCell(
            scanPoint.x / TOWN_LAYOUT_SCALER,
            0,
            scanPoint.z / TOWN_LAYOUT_SCALER
        );
        solved.add(
            cell.x * TOWN_LAYOUT_SCALER,
            0,
            cell.z * TOWN_LAYOUT_SCALER
        );
        solved.y = realm.getServerWorldData().getServerTerrainManager().getElevation(solved);
        return solved;
    }

}

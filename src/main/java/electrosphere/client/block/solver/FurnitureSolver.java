package electrosphere.client.block.solver;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.joml.Sphered;
import org.joml.Vector3d;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.interact.select.AreaSelection;
import electrosphere.client.interact.select.AreaSelection.AreaSelectionType;
import electrosphere.data.block.fab.FurnitureSlotMetadata;
import electrosphere.data.block.fab.RoomMetadata;

/**
 * Solves for placement of furniture
 */
public class FurnitureSolver {

    /**
     * Size of a furniture slot in blocks
     */
    public static final int FURNITURE_SIZE_DIM = 4;

    /**
     * Types of layouts for furniture
     */
    public static enum LayoutType {
        /**
         * Place furniture along the walls, skipping where there are entrances to the room
         */
        WALL_ALIGNED,
        /**
         * Place furniture in rows organized through the room, like a warehouse
         */
        WAREHOUSE,
    }
    
    /**
     * Solves for furniture placement spots for the given room
     */
    public static List<FurnitureSlotMetadata> solveFurnitureSpots(RoomMetadata room, LayoutType layout){
        if(layout == LayoutType.WAREHOUSE){
            throw new Error("Unsupported layout type " + layout);
        }
        List<FurnitureSlotMetadata> rVal = null;

        switch(layout){
            case WALL_ALIGNED: {
                rVal = FurnitureSolver.solveWallAlignedLayout(room);
            } break;
            default: {
                throw new Error("Unsupported layout type! " + layout);
            }
        }
        

        return rVal;
    }

    /**
     * Solves a wall-aligned furniture layout
     * @param room The room
     * @return The furniture slots
     */
    public static List<FurnitureSlotMetadata> solveWallAlignedLayout(RoomMetadata room){
        if(room.getArea().getType() != AreaSelectionType.RECTANGULAR){
            throw new Error("Unsupported room area type! " + room.getArea().getType());
        }
        List<FurnitureSlotMetadata> rVal = new LinkedList<FurnitureSlotMetadata>();

        int floorDimX = (int)((room.getArea().getRectEnd().x - room.getArea().getRectStart().x) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
        int floorDimZ = (int)((room.getArea().getRectEnd().z - room.getArea().getRectStart().z) * BlockChunkData.BLOCKS_PER_UNIT_DISTANCE);
        Vector3d roomStart = room.getArea().getRectStart();

        //map the entrances to a list of spheres to intersection check against
        List<Sphered> entranceColliders = room.getEntryPoints().stream().map((Vector3d entryPoint) -> {
            return new Sphered(entryPoint, RoomSolver.DOORWAY_MIN_WIDTH / 2);
        }).collect(Collectors.toList());

        //scan negative z dir
        for(int x = 0; x < floorDimX - FURNITURE_SIZE_DIM; x++){
            AreaSelection selection = AreaSelection.createRect(
                new Vector3d(roomStart).add(
                    x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    0 * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    0 * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                ),
                new Vector3d(roomStart).add(
                    (x + FurnitureSolver.FURNITURE_SIZE_DIM) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    FurnitureSolver.FURNITURE_SIZE_DIM * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    FurnitureSolver.FURNITURE_SIZE_DIM * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                )
            );

            //verify another furniture slot doesn't already contain this one
            boolean contained = false;
            for(FurnitureSlotMetadata furnitureSlot : rVal){
                if(furnitureSlot.getArea().intersects(selection)){
                    contained = true;
                    break;
                }
            }

            //verify isn't blocking an entryway
            for(Sphered entryCollider : entranceColliders){
                if(selection.intersects(entryCollider)){
                    contained = true;
                    break;
                }
            }

            if(!contained){
                FurnitureSlotMetadata newSlot = new FurnitureSlotMetadata(selection);
                rVal.add(newSlot);
            }
        }

        //scan positive z dir
        for(int x = 0; x < floorDimX - FURNITURE_SIZE_DIM; x++){
            AreaSelection selection = AreaSelection.createRect(
                new Vector3d(roomStart).add(
                    x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    0 * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    (floorDimZ - FurnitureSolver.FURNITURE_SIZE_DIM) * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                ),
                new Vector3d(roomStart).add(
                    (x + FurnitureSolver.FURNITURE_SIZE_DIM) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    FurnitureSolver.FURNITURE_SIZE_DIM * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    floorDimZ * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                )
            );

            //verify another furniture slot doesn't already contain this one
            boolean contained = false;
            for(FurnitureSlotMetadata furnitureSlot : rVal){
                if(furnitureSlot.getArea().intersects(selection)){
                    contained = true;
                    break;
                }
            }

            //verify isn't blocking an entryway
            for(Sphered entryCollider : entranceColliders){
                if(selection.intersects(entryCollider)){
                    contained = true;
                    break;
                }
            }

            if(!contained){
                FurnitureSlotMetadata newSlot = new FurnitureSlotMetadata(selection);
                rVal.add(newSlot);
            }
        }

        //scan negative x dir
        for(int z = 0; z < floorDimZ - FURNITURE_SIZE_DIM; z++){
            AreaSelection selection = AreaSelection.createRect(
                new Vector3d(roomStart).add(
                    0 * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    0 * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                ),
                new Vector3d(roomStart).add(
                    FurnitureSolver.FURNITURE_SIZE_DIM * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    FurnitureSolver.FURNITURE_SIZE_DIM * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    (z + FurnitureSolver.FURNITURE_SIZE_DIM) * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                )
            );

            //verify another furniture slot doesn't already contain this one
            boolean contained = false;
            for(FurnitureSlotMetadata furnitureSlot : rVal){
                if(furnitureSlot.getArea().intersects(selection)){
                    contained = true;
                    break;
                }
            }

            //verify isn't blocking an entryway
            for(Sphered entryCollider : entranceColliders){
                if(selection.intersects(entryCollider)){
                    contained = true;
                    break;
                }
            }

            if(!contained){
                FurnitureSlotMetadata newSlot = new FurnitureSlotMetadata(selection);
                rVal.add(newSlot);
            }
        }

        //scan positive x dir
        for(int z = 0; z < floorDimZ - FURNITURE_SIZE_DIM; z++){
            AreaSelection selection = AreaSelection.createRect(
                new Vector3d(roomStart).add(
                    (floorDimX - FurnitureSolver.FURNITURE_SIZE_DIM) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    0 * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                ),
                new Vector3d(roomStart).add(
                    floorDimX * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    FurnitureSolver.FURNITURE_SIZE_DIM * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                    (z + FurnitureSolver.FURNITURE_SIZE_DIM) * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                )
            );

            //verify another furniture slot doesn't already contain this one
            boolean contained = false;
            for(FurnitureSlotMetadata furnitureSlot : rVal){
                if(furnitureSlot.getArea().intersects(selection)){
                    contained = true;
                    break;
                }
            }

            //verify isn't blocking an entryway
            for(Sphered entryCollider : entranceColliders){
                if(selection.intersects(entryCollider)){
                    contained = true;
                    break;
                }
            }

            if(!contained){
                FurnitureSlotMetadata newSlot = new FurnitureSlotMetadata(selection);
                rVal.add(newSlot);
            }
        }

        return rVal;
    }

}

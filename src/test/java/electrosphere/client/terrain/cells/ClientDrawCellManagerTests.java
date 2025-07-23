package electrosphere.client.terrain.cells;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.extension.ExtendWith;

import electrosphere.client.scene.ClientWorldData;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import electrosphere.test.annotations.UnitTest;
import electrosphere.test.template.extensions.StateCleanupCheckerExtension;
import electrosphere.test.testutils.EngineInit;
import electrosphere.test.testutils.TestEngineUtils;
import electrosphere.util.ds.octree.WorldOctTree.WorldOctTreeNode;

/**
 * Tests for the client draw cell manager
 */
@ExtendWith(StateCleanupCheckerExtension.class)
public class ClientDrawCellManagerTests {
    

    /**
     * Test creating a manager
     */
    @UnitTest
    public void testCreation(){
        assertDoesNotThrow(() -> {
            new ClientDrawCellManager(null, 64);
        });
        Main.shutdown();
    }

    @UnitTest
    public void testJoinCase(){
        
        int worldDiscreteSize = 64;
        EngineInit.initGraphicalEngine();
        TestEngineUtils.flush();
        Globals.clientState.clientWorldData = new ClientWorldData(new Vector3f(0), new Vector3f(worldDiscreteSize * ServerTerrainChunk.CHUNK_DIMENSION), worldDiscreteSize);
        ClientDrawCellManager manager = new ClientDrawCellManager(null, 64);
        Vector3i playerPos = new Vector3i(0,0,0);
        WorldOctTreeNode<DrawCell> node = WorldOctTreeNode.constructorForTests(manager.chunkTree, 1, new Vector3i(16,0,0), new Vector3i(32,16,16));
        node.setLeaf(true);
        node.setData(DrawCell.generateTerrainCell(new Vector3i(16,0,0),3));

        boolean shouldBeTrue = node.getParent() != null;

        assertEquals(shouldBeTrue,manager.shouldSplit(playerPos, node, 0));

        //cleanup
        Globals.clientState = null;
        Main.shutdown();
    }

}

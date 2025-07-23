package electrosphere.util.ds.octree;

import static org.junit.jupiter.api.Assertions.*;

import electrosphere.test.annotations.UnitTest;

/**
 * Unit testing for the chunk octree implementation
 */
public class ChunkTreeTests {
    
    /**
     * Creates a chunk tree
     */
    @UnitTest
    public void testCreateChunkTree(){
        ChunkTree<String> tree = new ChunkTree<String>();
        assertNotNull(tree);
    }

}

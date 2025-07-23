package electrosphere.util.ds;

import static org.junit.jupiter.api.Assertions.*;

import electrosphere.test.annotations.FastTest;
import electrosphere.test.annotations.UnitTest;
import electrosphere.util.ds.DirectedGraph.GraphNode;

/**
 * Unit tests for the directed graph implementation
 */
public class DirectedGraphUnitTests {


    //
    //Graph-specific tests
    //

    @UnitTest
    @FastTest
    public void testCreateGraph(){
        DirectedGraph graph = new DirectedGraph();
        assertNotNull(graph);
    }

    @UnitTest
    @FastTest
    public void testCreateEntry(){
        DirectedGraph graph = new DirectedGraph();
        GraphNode node = graph.createNode(null);
        assertNotNull(node);
    }

    @UnitTest
    @FastTest
    public void testContainsEntryNotNull(){
        DirectedGraph graph = new DirectedGraph();
        graph.createNode(null);
        assertNotNull(graph.getNodes());
    }

    @UnitTest
    @FastTest
    public void testContainsEntryHasEntry(){
        DirectedGraph graph = new DirectedGraph();
        graph.createNode(null);
        assertEquals(1,graph.getNodes().size());
    }

    @UnitTest
    @FastTest
    public void testDeleteNode(){
        DirectedGraph graph = new DirectedGraph();
        GraphNode node = graph.createNode(null);
        graph.destroyNode(node);
        assertEquals(0,graph.getNodes().size());
    }


    //
    //Node-specific tests
    //
    
    @UnitTest
    @FastTest
    public void testCreateNode(){
        GraphNode node = new GraphNode();
        assertNotNull(node);
    }

    @UnitTest
    @FastTest
    public void testNodeGetData(){
        GraphNode node = new GraphNode("some data");
        assertEquals("some data", node.getData());
    }

    @UnitTest
    @FastTest
    public void testAddNeighbor(){
        GraphNode node = new GraphNode();
        GraphNode neighbor = new GraphNode();
        node.addNeighbor(neighbor);
        assertEquals(neighbor, node.getNeighbors().get(0));
    }

    @UnitTest
    @FastTest
    public void testRemoveNeighbor(){
        GraphNode node = new GraphNode();
        GraphNode neighbor = new GraphNode();
        node.addNeighbor(neighbor);
        node.removeNeighbor(neighbor);
        assertEquals(0, node.getNeighbors().size());
    }

    @UnitTest
    @FastTest
    public void testGetNeighbors(){
        GraphNode node = new GraphNode();
        GraphNode neighbor = new GraphNode();
        node.addNeighbor(neighbor);
        assertEquals(1, node.getNeighbors().size());
    }

    @UnitTest
    @FastTest
    public void testContainsNeighbor(){
        GraphNode node = new GraphNode();
        GraphNode neighbor = new GraphNode();
        node.addNeighbor(neighbor);
        assertEquals(true, node.containsNeighbor(neighbor));
    }

}

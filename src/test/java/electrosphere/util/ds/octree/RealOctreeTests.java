package electrosphere.util.ds.octree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.joml.Vector3d;

import electrosphere.test.annotations.UnitTest;
import electrosphere.util.ds.octree.RealOctree.RealOctreeNode;

/**
 * Unit testing for the octree implementation
 */
public class RealOctreeTests {
    
    /**
     * Creates an octree
     */
    @UnitTest
    public void testCreateOctree(){
        new RealOctree<Integer>(new Vector3d(0,0,0), new Vector3d(32,32,32));
    }

    /**
     * Add a single leaf
     */
    @UnitTest
    public void testAddNode(){
        RealOctree<Integer> octree = new RealOctree<Integer>(new Vector3d(0,0,0), new Vector3d(32,32,32));
        octree.addLeaf(new Vector3d(1,1,1), 1);
        //
        //validate the tree
        //
        List<RealOctreeNode<Integer>> openSet = new LinkedList<RealOctreeNode<Integer>>();
        openSet.add(octree.getRoot());
        while(openSet.size() > 0){
            RealOctreeNode<Integer> current = openSet.remove(0);
            if(current.isLeaf()){
                assertNotNull(current.getData());
            } else {
                assertNull(current.getData());
                for(OctreeNode<Integer> child : current.getChildren()){
                    if(child != null && child.getParent() != current){
                        fail("Child not attached to parent!");
                    }
                    if(child != null){
                        openSet.add((RealOctreeNode<Integer>)child);
                    }
                }
            }
        }
    }

    /**
     * Add two leaves in a line
     */
    @UnitTest
    public void testAddTwo(){
        RealOctree<Integer> octree = new RealOctree<Integer>(new Vector3d(0,0,0), new Vector3d(32,32,32));
        octree.addLeaf(new Vector3d(1,1,1), 1);
        octree.addLeaf(new Vector3d(2,1,1), 2);
        //
        //validate the tree
        //
        List<RealOctreeNode<Integer>> openSet = new LinkedList<RealOctreeNode<Integer>>();
        openSet.add(octree.getRoot());
        while(openSet.size() > 0){
            RealOctreeNode<Integer> current = openSet.remove(0);
            if(current.isLeaf()){
                assertNotNull(current.getData());
            } else {
                assertNull(current.getData());
                for(OctreeNode<Integer> child : current.getChildren()){
                    if(child != null && child.getParent() != current){
                        fail("Child not attached to parent!");
                    }
                    if(child != null){
                        openSet.add((RealOctreeNode<Integer>)child);
                    }
                }
            }
        }
        //
        //Specific, expected values to check. Verifies that the octree construct itself correctly
        //
        RealOctreeNode<Integer> current = (RealOctreeNode<Integer>)octree.getRoot().getChildren().get(0);
        assertEquals(8, current.getLocation().x);
        assertEquals(8, current.getLocation().y);
        assertEquals(8, current.getLocation().z);
        assertTrue(!current.isLeaf());
        assertEquals(1, ((RealOctreeNode<Integer>)current).getNumChildren());

        current = (RealOctreeNode<Integer>)current.getChildren().get(0);
        assertEquals(4, current.getLocation().x);
        assertEquals(4, current.getLocation().y);
        assertEquals(4, current.getLocation().z);
        assertTrue(!current.isLeaf());
        assertEquals(1, ((RealOctreeNode<Integer>)current).getNumChildren());

        current = (RealOctreeNode<Integer>)current.getChildren().get(0);
        assertEquals(2, current.getLocation().x);
        assertEquals(2, current.getLocation().y);
        assertEquals(2, current.getLocation().z);
        assertTrue(!current.isLeaf());
        assertEquals(2, ((RealOctreeNode<Integer>)current).getNumChildren());

        OctreeNode<Integer> leaf1 = current.getChildren().get(0);
        OctreeNode<Integer> leaf2 = current.getChildren().get(1);
        assertTrue(leaf1.isLeaf());
        assertTrue(leaf2.isLeaf());
    }

    /**
     * Adds a whole bunch of nodes in a line
     */
    @UnitTest
    public void testAddLine(){
        RealOctree<Integer> octree = new RealOctree<Integer>(new Vector3d(0,0,0), new Vector3d(32,32,32));
        for(int i = 0; i < 31; i++){
            octree.addLeaf(new Vector3d(i,1,0), i);
        }
        //
        //validate the tree
        //
        List<RealOctreeNode<Integer>> openSet = new LinkedList<RealOctreeNode<Integer>>();
        openSet.add(octree.getRoot());
        while(openSet.size() > 0){
            RealOctreeNode<Integer> current = openSet.remove(0);
            if(current.isLeaf()){
                assertNotNull(current.getData());
            } else {
                assertNull(current.getData());
                for(OctreeNode<Integer> child : current.getChildren()){
                    if(child != null && child.getParent() != current){
                        fail("Child not attached to parent!");
                    }
                    if(child != null){
                        openSet.add(((RealOctreeNode<Integer>)child));
                    }
                }
            }
        }
    }

    /**
     * Get a single leaf
     */
    @UnitTest
    public void testGetLeaf(){
        RealOctree<Integer> octree = new RealOctree<Integer>(new Vector3d(0,0,0), new Vector3d(32,32,32));
        octree.addLeaf(new Vector3d(1,1,1), 1);
        RealOctreeNode<Integer> leaf = octree.getLeaf(new Vector3d(1,1,1));
        assertNotNull(leaf);
        //
        //validate the tree
        //
        List<RealOctreeNode<Integer>> openSet = new LinkedList<RealOctreeNode<Integer>>();
        openSet.add(octree.getRoot());
        while(openSet.size() > 0){
            RealOctreeNode<Integer> current = openSet.remove(0);
            if(current.isLeaf()){
                assertNotNull(current.getData());
            } else {
                assertNull(current.getData());
                for(OctreeNode<Integer> child : current.getChildren()){
                    if(child != null && child.getParent() != current){
                        fail("Child not attached to parent!");
                    }
                    if(child != null){
                        openSet.add((RealOctreeNode<Integer>)child);
                    }
                }
            }
        }
    }

    /**
     * Remove a single leaf
     */
    @UnitTest
    public void testRemoveLeaf(){
        RealOctree<Integer> octree = new RealOctree<Integer>(new Vector3d(0,0,0), new Vector3d(32,32,32));
        octree.addLeaf(new Vector3d(1,1,1), 1);
        RealOctreeNode<Integer> leaf = octree.getLeaf(new Vector3d(1,1,1));
        assertNotNull(leaf);
        assertNotNull(leaf.getParent());
        octree.removeNode(leaf);
        //
        //validate the tree
        //
        List<RealOctreeNode<Integer>> openSet = new LinkedList<RealOctreeNode<Integer>>();
        openSet.add(octree.getRoot());
        while(openSet.size() > 0){
            RealOctreeNode<Integer> current = openSet.remove(0);
            if(current.isLeaf()){
                assertNotNull(current.getData());
            } else {
                assertNull(current.getData());
                for(OctreeNode<Integer> child : current.getChildren()){
                    if(child != null && child.getParent() != current){
                        fail("Child not attached to parent!");
                    }
                    if(child != null){
                        openSet.add((RealOctreeNode<Integer>)child);
                    }
                }
            }
        }
    }

    /**
     * Remove a leaf from a more complex tree
     */
    @UnitTest
    public void testRemoveLeaf2(){
        RealOctree<Integer> octree = new RealOctree<Integer>(new Vector3d(0,0,0), new Vector3d(32,32,32));
        octree.addLeaf(new Vector3d(1,1,1), 1);
        octree.addLeaf(new Vector3d(2,1,1), 2);
        octree.addLeaf(new Vector3d(3,1,1), 3);
        RealOctreeNode<Integer> leaf = octree.getLeaf(new Vector3d(2,1,1));
        assertNotNull(leaf);
        assertNotNull(leaf.getParent());

        //removes 2 & 3, but not 1
        octree.removeNode(leaf.getParent());
        assertEquals(1, octree.getNumLeaves());

        //remove 1
        leaf = octree.getLeaf(new Vector3d(1,1,1));
        assertNotNull(leaf);
        assertNotNull(leaf.getParent());
        octree.removeNode(leaf.getParent());
        assertEquals(0, octree.getNumLeaves());

        //
        //validate the tree
        //
        List<RealOctreeNode<Integer>> openSet = new LinkedList<RealOctreeNode<Integer>>();
        openSet.add(octree.getRoot());
        while(openSet.size() > 0){
            RealOctreeNode<Integer> current = openSet.remove(0);
            if(current.isLeaf()){
                assertNotNull(current.getData());
            } else {
                assertNull(current.getData());
                for(OctreeNode<Integer> child : current.getChildren()){
                    if(child != null && child.getParent() != current){
                        fail("Child not attached to parent!");
                    }
                    if(child != null){
                        openSet.add((RealOctreeNode<Integer>)child);
                    }
                }
            }
        }
    }
    
    /**
     * Adds lots of (random) points
     */
    @UnitTest
    public void testAddManyPoints(){
        RealOctree<Integer> octree = new RealOctree<Integer>(new Vector3d(0,0,0), new Vector3d(256,256,256));


        //
        //add points
        Random rand = new Random();
        for(int i = 0; i < 1000; i++){
            Vector3d loc = new Vector3d(
                rand.nextInt(0, 256),
                rand.nextInt(0, 256),
                rand.nextInt(0, 256)
            );
            if(!octree.containsLeaf(loc)){
                octree.addLeaf(loc, i);
            }
        }

        //
        //verify at least one point was added
        assertEquals(true, octree.getNumLeaves() > 0);
    }

    

}

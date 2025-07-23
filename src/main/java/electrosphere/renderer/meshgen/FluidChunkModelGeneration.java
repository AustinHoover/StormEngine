package electrosphere.renderer.meshgen;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import electrosphere.engine.Globals;
import electrosphere.entity.types.fluid.FluidChunkModelData;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

public class FluidChunkModelGeneration {
    
    //http://paulbourke.net/geometry/polygonise/

    static int edgeTable[]={
        0x0  , 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c,
        0x80c, 0x905, 0xa0f, 0xb06, 0xc0a, 0xd03, 0xe09, 0xf00,
        0x190, 0x99 , 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c,
        0x99c, 0x895, 0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90,
        0x230, 0x339, 0x33 , 0x13a, 0x636, 0x73f, 0x435, 0x53c,
        0xa3c, 0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30,
        0x3a0, 0x2a9, 0x1a3, 0xaa , 0x7a6, 0x6af, 0x5a5, 0x4ac,
        0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0,
        0x460, 0x569, 0x663, 0x76a, 0x66 , 0x16f, 0x265, 0x36c,
        0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60,
        0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0xff , 0x3f5, 0x2fc,
        0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0,
        0x650, 0x759, 0x453, 0x55a, 0x256, 0x35f, 0x55 , 0x15c,
        0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950,
        0x7c0, 0x6c9, 0x5c3, 0x4ca, 0x3c6, 0x2cf, 0x1c5, 0xcc ,
        0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0,
        0x8c0, 0x9c9, 0xac3, 0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc,
        0xcc , 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0,
        0x950, 0x859, 0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c,
        0x15c, 0x55 , 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650,
        0xaf0, 0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc,
        0x2fc, 0x3f5, 0xff , 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0,
        0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c,
        0x36c, 0x265, 0x16f, 0x66 , 0x76a, 0x663, 0x569, 0x460,
        0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac,
        0x4ac, 0x5a5, 0x6af, 0x7a6, 0xaa , 0x1a3, 0x2a9, 0x3a0,
        0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c,
        0x53c, 0x435, 0x73f, 0x636, 0x13a, 0x33 , 0x339, 0x230,
        0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c,
        0x69c, 0x795, 0x49f, 0x596, 0x29a, 0x393, 0x99 , 0x190,
        0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c,
        0x70c, 0x605, 0x50f, 0x406, 0x30a, 0x203, 0x109, 0x0   
    };

    //256 by 16
    static int triTable[][] = {
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 8, 3, 9, 8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 2, 10, 0, 2, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 8, 3, 2, 10, 8, 10, 9, 8, -1, -1, -1, -1, -1, -1, -1},
        {3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 11, 2, 8, 11, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 9, 0, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 11, 2, 1, 9, 11, 9, 8, 11, -1, -1, -1, -1, -1, -1, -1},
        {3, 10, 1, 11, 10, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 10, 1, 0, 8, 10, 8, 11, 10, -1, -1, -1, -1, -1, -1, -1},
        {3, 9, 0, 3, 11, 9, 11, 10, 9, -1, -1, -1, -1, -1, -1, -1},
        {9, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 3, 0, 7, 3, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 9, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 1, 9, 4, 7, 1, 7, 3, 1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 4, 7, 3, 0, 4, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1},
        {9, 2, 10, 9, 0, 2, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
        {2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, -1, -1, -1, -1},
        {8, 4, 7, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {11, 4, 7, 11, 2, 4, 2, 0, 4, -1, -1, -1, -1, -1, -1, -1},
        {9, 0, 1, 8, 4, 7, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
        {4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1, -1, -1, -1, -1},
        {3, 10, 1, 3, 11, 10, 7, 8, 4, -1, -1, -1, -1, -1, -1, -1},
        {1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4, -1, -1, -1, -1},
        {4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3, -1, -1, -1, -1},
        {4, 7, 11, 4, 11, 9, 9, 11, 10, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 4, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 5, 4, 1, 5, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {8, 5, 4, 8, 3, 5, 3, 1, 5, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 8, 1, 2, 10, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
        {5, 2, 10, 5, 4, 2, 4, 0, 2, -1, -1, -1, -1, -1, -1, -1},
        {2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8, -1, -1, -1, -1},
        {9, 5, 4, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 11, 2, 0, 8, 11, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
        {0, 5, 4, 0, 1, 5, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
        {2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5, -1, -1, -1, -1},
        {10, 3, 11, 10, 1, 3, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1},
        {4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10, -1, -1, -1, -1},
        {5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3, -1, -1, -1, -1},
        {5, 4, 8, 5, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1},
        {9, 7, 8, 5, 7, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 3, 0, 9, 5, 3, 5, 7, 3, -1, -1, -1, -1, -1, -1, -1},
        {0, 7, 8, 0, 1, 7, 1, 5, 7, -1, -1, -1, -1, -1, -1, -1},
        {1, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 7, 8, 9, 5, 7, 10, 1, 2, -1, -1, -1, -1, -1, -1, -1},
        {10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, -1, -1, -1, -1},
        {8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2, -1, -1, -1, -1},
        {2, 10, 5, 2, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1},
        {7, 9, 5, 7, 8, 9, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11, -1, -1, -1, -1},
        {2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7, -1, -1, -1, -1},
        {11, 2, 1, 11, 1, 7, 7, 1, 5, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11, -1, -1, -1, -1},
        {5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0, -1},
        {11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0, -1},
        {11, 10, 5, 7, 11, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 0, 1, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 8, 3, 1, 9, 8, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
        {1, 6, 5, 2, 6, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 6, 5, 1, 2, 6, 3, 0, 8, -1, -1, -1, -1, -1, -1, -1},
        {9, 6, 5, 9, 0, 6, 0, 2, 6, -1, -1, -1, -1, -1, -1, -1},
        {5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, -1, -1, -1, -1},
        {2, 3, 11, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {11, 0, 8, 11, 2, 0, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 9, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
        {5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11, -1, -1, -1, -1},
        {6, 3, 11, 6, 5, 3, 5, 1, 3, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6, -1, -1, -1, -1},
        {3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9, -1, -1, -1, -1},
        {6, 5, 9, 6, 9, 11, 11, 9, 8, -1, -1, -1, -1, -1, -1, -1},
        {5, 10, 6, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 3, 0, 4, 7, 3, 6, 5, 10, -1, -1, -1, -1, -1, -1, -1},
        {1, 9, 0, 5, 10, 6, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
        {10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, -1, -1, -1, -1},
        {6, 1, 2, 6, 5, 1, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7, -1, -1, -1, -1},
        {8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, -1, -1, -1, -1},
        {7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9, -1},
        {3, 11, 2, 7, 8, 4, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
        {5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11, -1, -1, -1, -1},
        {0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1},
        {9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6, -1},
        {8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6, -1, -1, -1, -1},
        {5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11, -1},
        {0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7, -1},
        {6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9, -1, -1, -1, -1},
        {10, 4, 9, 6, 4, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 10, 6, 4, 9, 10, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1},
        {10, 0, 1, 10, 6, 0, 6, 4, 0, -1, -1, -1, -1, -1, -1, -1},
        {8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10, -1, -1, -1, -1},
        {1, 4, 9, 1, 2, 4, 2, 6, 4, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, -1, -1, -1, -1},
        {0, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {8, 3, 2, 8, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1},
        {10, 4, 9, 10, 6, 4, 11, 2, 3, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6, -1, -1, -1, -1},
        {3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10, -1, -1, -1, -1},
        {6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1, -1},
        {9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3, -1, -1, -1, -1},
        {8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1, -1},
        {3, 11, 6, 3, 6, 0, 0, 6, 4, -1, -1, -1, -1, -1, -1, -1},
        {6, 4, 8, 11, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {7, 10, 6, 7, 8, 10, 8, 9, 10, -1, -1, -1, -1, -1, -1, -1},
        {0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10, -1, -1, -1, -1},
        {10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0, -1, -1, -1, -1},
        {10, 6, 7, 10, 7, 1, 1, 7, 3, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7, -1, -1, -1, -1},
        {2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, -1},
        {7, 8, 0, 7, 0, 6, 6, 0, 2, -1, -1, -1, -1, -1, -1, -1},
        {7, 3, 2, 6, 7, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7, -1, -1, -1, -1},
        {2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7, -1},
        {1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11, -1},
        {11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1, -1, -1, -1, -1},
        {8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6, -1},
        {0, 9, 1, 11, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0, -1, -1, -1, -1},
        {7, 11, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 8, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 9, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {8, 1, 9, 8, 3, 1, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
        {10, 1, 2, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 3, 0, 8, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
        {2, 9, 0, 2, 10, 9, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
        {6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8, -1, -1, -1, -1},
        {7, 2, 3, 6, 2, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {7, 0, 8, 7, 6, 0, 6, 2, 0, -1, -1, -1, -1, -1, -1, -1},
        {2, 7, 6, 2, 3, 7, 0, 1, 9, -1, -1, -1, -1, -1, -1, -1},
        {1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, -1, -1, -1, -1},
        {10, 7, 6, 10, 1, 7, 1, 3, 7, -1, -1, -1, -1, -1, -1, -1},
        {10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8, -1, -1, -1, -1},
        {0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7, -1, -1, -1, -1},
        {7, 6, 10, 7, 10, 8, 8, 10, 9, -1, -1, -1, -1, -1, -1, -1},
        {6, 8, 4, 11, 8, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 6, 11, 3, 0, 6, 0, 4, 6, -1, -1, -1, -1, -1, -1, -1},
        {8, 6, 11, 8, 4, 6, 9, 0, 1, -1, -1, -1, -1, -1, -1, -1},
        {9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6, -1, -1, -1, -1},
        {6, 8, 4, 6, 11, 8, 2, 10, 1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6, -1, -1, -1, -1},
        {4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9, -1, -1, -1, -1},
        {10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3, -1},
        {8, 2, 3, 8, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1},
        {0, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, -1, -1, -1, -1},
        {1, 9, 4, 1, 4, 2, 2, 4, 6, -1, -1, -1, -1, -1, -1, -1},
        {8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1, -1, -1, -1, -1},
        {10, 1, 0, 10, 0, 6, 6, 0, 4, -1, -1, -1, -1, -1, -1, -1},
        {4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3, -1},
        {10, 9, 4, 6, 10, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 9, 5, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 4, 9, 5, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
        {5, 0, 1, 5, 4, 0, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
        {11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, -1, -1, -1, -1},
        {9, 5, 4, 10, 1, 2, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
        {6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5, -1, -1, -1, -1},
        {7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2, -1, -1, -1, -1},
        {3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6, -1},
        {7, 2, 3, 7, 6, 2, 5, 4, 9, -1, -1, -1, -1, -1, -1, -1},
        {9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7, -1, -1, -1, -1},
        {3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, -1, -1, -1, -1},
        {6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8, -1},
        {9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7, -1, -1, -1, -1},
        {1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, -1},
        {4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10, -1},
        {7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10, -1, -1, -1, -1},
        {6, 9, 5, 6, 11, 9, 11, 8, 9, -1, -1, -1, -1, -1, -1, -1},
        {3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5, -1, -1, -1, -1},
        {0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11, -1, -1, -1, -1},
        {6, 11, 3, 6, 3, 5, 5, 3, 1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6, -1, -1, -1, -1},
        {0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10, -1},
        {11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5, -1},
        {6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3, -1, -1, -1, -1},
        {5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, -1, -1, -1, -1},
        {9, 5, 6, 9, 6, 0, 0, 6, 2, -1, -1, -1, -1, -1, -1, -1},
        {1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, -1},
        {1, 5, 6, 2, 1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6, -1},
        {10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0, -1, -1, -1, -1},
        {0, 3, 8, 5, 6, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {10, 5, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {11, 5, 10, 7, 5, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {11, 5, 10, 11, 7, 5, 8, 3, 0, -1, -1, -1, -1, -1, -1, -1},
        {5, 11, 7, 5, 10, 11, 1, 9, 0, -1, -1, -1, -1, -1, -1, -1},
        {10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1, -1, -1, -1, -1},
        {11, 1, 2, 11, 7, 1, 7, 5, 1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11, -1, -1, -1, -1},
        {9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7, -1, -1, -1, -1},
        {7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2, -1},
        {2, 5, 10, 2, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1},
        {8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5, -1, -1, -1, -1},
        {9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2, -1, -1, -1, -1},
        {9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2, -1},
        {1, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 7, 0, 7, 1, 1, 7, 5, -1, -1, -1, -1, -1, -1, -1},
        {9, 0, 3, 9, 3, 5, 5, 3, 7, -1, -1, -1, -1, -1, -1, -1},
        {9, 8, 7, 5, 9, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {5, 8, 4, 5, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1},
        {5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0, -1, -1, -1, -1},
        {0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5, -1, -1, -1, -1},
        {10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4, -1},
        {2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8, -1, -1, -1, -1},
        {0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11, -1},
        {0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5, -1},
        {9, 4, 5, 2, 11, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4, -1, -1, -1, -1},
        {5, 10, 2, 5, 2, 4, 4, 2, 0, -1, -1, -1, -1, -1, -1, -1},
        {3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9, -1},
        {5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2, -1, -1, -1, -1},
        {8, 4, 5, 8, 5, 3, 3, 5, 1, -1, -1, -1, -1, -1, -1, -1},
        {0, 4, 5, 1, 0, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, -1, -1, -1, -1},
        {9, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 11, 7, 4, 9, 11, 9, 10, 11, -1, -1, -1, -1, -1, -1, -1},
        {0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11, -1, -1, -1, -1},
        {1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11, -1, -1, -1, -1},
        {3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4, -1},
        {4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2, -1, -1, -1, -1},
        {9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3, -1},
        {11, 7, 4, 11, 4, 2, 2, 4, 0, -1, -1, -1, -1, -1, -1, -1},
        {11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4, -1, -1, -1, -1},
        {2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9, -1, -1, -1, -1},
        {9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7, -1},
        {3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10, -1},
        {1, 10, 2, 8, 7, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 9, 1, 4, 1, 7, 7, 1, 3, -1, -1, -1, -1, -1, -1, -1},
        {4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, -1, -1, -1, -1},
        {4, 0, 3, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {4, 8, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {9, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 9, 3, 9, 11, 11, 9, 10, -1, -1, -1, -1, -1, -1, -1},
        {0, 1, 10, 0, 10, 8, 8, 10, 11, -1, -1, -1, -1, -1, -1, -1},
        {3, 1, 10, 11, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 2, 11, 1, 11, 9, 9, 11, 8, -1, -1, -1, -1, -1, -1, -1},
        {3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9, -1, -1, -1, -1},
        {0, 2, 11, 8, 0, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {3, 2, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 3, 8, 2, 8, 10, 10, 8, 9, -1, -1, -1, -1, -1, -1, -1},
        {9, 10, 2, 0, 9, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8, -1, -1, -1, -1},
        {1, 10, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {1, 3, 8, 9, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 9, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {0, 3, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
    };

    // private static final byte[] BLOCK_PICK_BITS_BY_VERT_INDEX = new byte[]{
    //     (byte)0x67,
    //     (byte)0x26,
    //     (byte)0x23,
    //     (byte)0x37,
    //     (byte)0x45,
    //     (byte)0x04,
    //     (byte)0x01,
    //     (byte)0x15,
    //     (byte)0x57,
    //     (byte)0x46,
    //     (byte)0x02,
    //     (byte)0x13
    // };

    static class Triangle {
        int[] indices = new int[3]; //array of size 3

        public Triangle(int index0, int index1, int index2){
            indices[0] = index0;
            indices[1] = index1;
            indices[2] = index2;
        }
    }

    static class GridCell {
        Vector3f[] points = new Vector3f[8]; //array of size 8
        double[] val = new double[8]; //array of size 8
        public void setValues(
            Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4,
            Vector3f p5, Vector3f p6, Vector3f p7, Vector3f p8,
            double val1, double val2, double val3, double val4,
            double val5, double val6, double val7, double val8
        ){
            points[0] = p1; points[1] = p2; points[2] = p3; points[3] = p4;
            points[4] = p5; points[5] = p6; points[6] = p7; points[7] = p8;
            val[0] = val1; val[1] = val2; val[2] = val3; val[3] = val4;
            val[4] = val5; val[5] = val6; val[6] = val7; val[7] = val8;
        }
    }







    public static VisualShader fluidChunkShaderProgram = null;













    protected static int polygonize(
            GridCell grid,
            double isolevel,
            List<Triangle> triangles,
            Map<String,Integer> vertMap,
            List<Vector3f> verts,
            List<Vector3f> normals,
            List<Integer> trianglesSharingVert
        ){
        int i;
        int ntriang;
        int cubeIndex = 0;
        Vector3f[] vertList = new Vector3f[12];

        //get lookup key (index) for edge table
        //edge table tells us which vertices are inside of the surface
        if (grid.val[0] < isolevel) cubeIndex |= 1;
        if (grid.val[1] < isolevel) cubeIndex |= 2;
        if (grid.val[2] < isolevel) cubeIndex |= 4;
        if (grid.val[3] < isolevel) cubeIndex |= 8;
        if (grid.val[4] < isolevel) cubeIndex |= 16;
        if (grid.val[5] < isolevel) cubeIndex |= 32;
        if (grid.val[6] < isolevel) cubeIndex |= 64;
        if (grid.val[7] < isolevel) cubeIndex |= 128;

        //Cube is entirely in/out of the surface
        if (edgeTable[cubeIndex] == 0)
            return(0);

        //instead of having all intersections be perfectly at the midpoint,
        //for each edge this code calculates where along the edge to place the vertex
        //this should dramatically smooth the surface
        if ((edgeTable[cubeIndex] & 1) > 0)
            vertList[0] =
                VertexInterp(isolevel,grid.points[0],grid.points[1],grid.val[0],grid.val[1]);
        if ((edgeTable[cubeIndex] & 2) > 0)
            vertList[1] =
                VertexInterp(isolevel,grid.points[1],grid.points[2],grid.val[1],grid.val[2]);
        if ((edgeTable[cubeIndex] & 4) > 0)
            vertList[2] =
                VertexInterp(isolevel,grid.points[2],grid.points[3],grid.val[2],grid.val[3]);
        if ((edgeTable[cubeIndex] & 8) > 0)
            vertList[3] =
                VertexInterp(isolevel,grid.points[3],grid.points[0],grid.val[3],grid.val[0]);
        if ((edgeTable[cubeIndex] & 16) > 0)
            vertList[4] =
                VertexInterp(isolevel,grid.points[4],grid.points[5],grid.val[4],grid.val[5]);
        if ((edgeTable[cubeIndex] & 32) > 0)
            vertList[5] =
                VertexInterp(isolevel,grid.points[5],grid.points[6],grid.val[5],grid.val[6]);
        if ((edgeTable[cubeIndex] & 64) > 0)
            vertList[6] =
                VertexInterp(isolevel,grid.points[6],grid.points[7],grid.val[6],grid.val[7]);
        if ((edgeTable[cubeIndex] & 128) > 0)
            vertList[7] =
                VertexInterp(isolevel,grid.points[7],grid.points[4],grid.val[7],grid.val[4]);
        if ((edgeTable[cubeIndex] & 256) > 0)
            vertList[8] =
                VertexInterp(isolevel,grid.points[0],grid.points[4],grid.val[0],grid.val[4]);
        if ((edgeTable[cubeIndex] & 512) > 0)
            vertList[9] =
                VertexInterp(isolevel,grid.points[1],grid.points[5],grid.val[1],grid.val[5]);
        if ((edgeTable[cubeIndex] & 1024) > 0)
            vertList[10] =
                VertexInterp(isolevel,grid.points[2],grid.points[6],grid.val[2],grid.val[6]);
        if ((edgeTable[cubeIndex] & 2048) > 0)
            vertList[11] =
                VertexInterp(isolevel,grid.points[3],grid.points[7],grid.val[3],grid.val[7]);

        //Create the triangle
        ntriang = 0;
        for (i=0; triTable[cubeIndex][i]!=-1; i+=3) {
            //
            // Triangles calculation
            //
            //get indices
            Vector3f vert0 = vertList[triTable[cubeIndex][i+0]];
            Vector3f vert1 = vertList[triTable[cubeIndex][i+1]];
            Vector3f vert2 = vertList[triTable[cubeIndex][i+2]];
            int index0 = getVertIndex(vert0,vertMap,verts);
            int index1 = getVertIndex(vert1,vertMap,verts);
            int index2 = getVertIndex(vert2,vertMap,verts);

            //add 0's to normals until it matches vert count
            while(trianglesSharingVert.size() < verts.size()){
                trianglesSharingVert.add(0);
                normals.add(new Vector3f());
            }


            //add new triangle
            Triangle newTriangle = new Triangle(index0,index1,index2);
            triangles.add(newTriangle);
            ntriang++;



            //
            // Normals calculation
            //


            //calculate normal for new triangle
            Vector3f u = verts.get(index1).sub(verts.get(index0), new Vector3f());
            Vector3f v = verts.get(index2).sub(verts.get(index1), new Vector3f());
            Vector3f n = new Vector3f(u.y * v.z - u.z * v.y, u.z * v.x - u.x * v.z, u.x * v.y - u.y * v.x).normalize();



            //for each vertex, average the new normal with the normals that are already there
            int trianglesSharingIndex0 = trianglesSharingVert.get(index0);
            //calculate proportion of each normal
            float oldProportion = trianglesSharingIndex0 / (float)(trianglesSharingIndex0 + 1);
            float newProportion = 1.0f / (float)(trianglesSharingIndex0 + 1);
            //increment number of triangles sharing vert
            trianglesSharingVert.set(index0, trianglesSharingIndex0 + 1);

            Vector3f currentNormal = normals.get(index0);
            currentNormal = averageNormals(currentNormal,oldProportion,n,newProportion);
            normals.get(index0).set(currentNormal);





            int trianglesSharingIndex1 = trianglesSharingVert.get(index1);
            //calculate proportion of each normal
            oldProportion = trianglesSharingIndex1 / (float)(trianglesSharingIndex1 + 1);
            newProportion = 1.0f / (float)(trianglesSharingIndex1 + 1);
            //increment number of triangles sharing vert
            trianglesSharingVert.set(index1, trianglesSharingIndex1 + 1);

            currentNormal = normals.get(index1);
            currentNormal = averageNormals(currentNormal,oldProportion,n,newProportion);
            normals.get(index1).set(currentNormal);








            int trianglesSharingIndex2 = trianglesSharingVert.get(index2);
            //calculate proportion of each normal
            oldProportion = trianglesSharingIndex2 / (float)(trianglesSharingIndex2 + 1);
            newProportion = 1.0f / (float)(trianglesSharingIndex2 + 1);
            //increment number of triangles sharing vert
            trianglesSharingVert.set(index2, trianglesSharingIndex2 + 1);

            currentNormal = normals.get(index2);
            currentNormal = averageNormals(currentNormal,oldProportion,n,newProportion);
            normals.get(index2).set(currentNormal);

        }

        return(ntriang);
    }

    //interpolates the location that the edge gets cut based on the magnitudes of the scalars of the vertices at either end of the edge
    static Vector3f VertexInterp(double isolevel, Vector3f p1, Vector3f p2, double valp1, double valp2){
        double mu;
        float x, y, z;

        if (Math.abs(isolevel-valp1) < 0.00001)
            return(p1);
        if (Math.abs(isolevel-valp2) < 0.00001)
            return(p2);
        if (Math.abs(valp1-valp2) < 0.00001)
            return(p1);
        mu = (isolevel - valp1) / (valp2 - valp1);
        x = (float)(p1.x + mu * (p2.x - p1.x));
        y = (float)(p1.y + mu * (p2.y - p1.y));
        z = (float)(p1.z + mu * (p2.z - p1.z));

        return new Vector3f(x,y,z);
    }

    public static FluidChunkModelData generateFluidChunkData(float[][][] weightGrid){

        //            5             6
        //            +-------------+               +-----5-------+     ^ Y                
        //          / |           / |             / |            /|     |    _             
        //        /   |         /   |           4   9         6   10    |    /\  Z         
        //    4 +-----+-------+  7  |         +-----+7------+     |     |   /              
        //      |   1 +-------+-----+ 2       |     +-----1-+-----+     |  /               
        //      |   /         |   /           8   0        11   2       | /                
        //      | /           | /             | /           | /         |/                 
        //    0 +-------------+ 3             +------3------+           +---------------> X

        //the current grid cell
        GridCell currentCell = new GridCell();
        //the list of all triangles
        List<Triangle> triangles = new LinkedList<Triangle>();
        //the map of vertex to index
        Map<String,Integer> vertMap = new HashMap<String,Integer>();
        //the list of all verts
        List<Vector3f> verts = new LinkedList<Vector3f>();
        //the list of all normals
        List<Vector3f> normals = new LinkedList<Vector3f>();
        //the list of number of triangles that share a vert
        List<Integer> trianglesSharingVert = new LinkedList<Integer>();
        //List of UVs
        List<Float> UVs = new LinkedList<Float>();

 
 
        for(int x = 0; x < weightGrid.length - 1; x++){
            for(int y = 0; y < weightGrid[0].length - 1; y++){
                for(int z = 0; z < weightGrid[0][0].length - 1; z++){
                    //push the current cell's values into the gridcell
                    currentCell.setValues(
                        new Vector3f(x+0,y+0,z+0), new Vector3f(x+0,y+0,z+1), new Vector3f(x+1,y+0,z+1), new Vector3f(x+1,y+0,z+0),
                        new Vector3f(x+0,y+1,z+0), new Vector3f(x+0,y+1,z+1), new Vector3f(x+1,y+1,z+1), new Vector3f(x+1,y+1,z+0),
                        weightGrid[x+0][y+0][z+0], weightGrid[x+0][y+0][z+1], weightGrid[x+1][y+0][z+1], weightGrid[x+1][y+0][z+0],
                        weightGrid[x+0][y+1][z+0], weightGrid[x+0][y+1][z+1], weightGrid[x+1][y+1][z+1], weightGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    polygonize(currentCell, 0.01f, triangles, vertMap, verts, normals, trianglesSharingVert);
                }
            }
        }

        //all verts in order, flattened as an array of floats instead of vecs
        List<Float> vertsFlat = new LinkedList<Float>();
        //all normals in order, flattened as an array of floats instead of vecs
        List<Float> normalsFlat = new LinkedList<Float>();
        //all elements of faces in order
        List<Integer> elementsFlat = new LinkedList<Integer>();

        //flatten verts + normals
        for(Vector3f vert : verts){
            vertsFlat.add(vert.x);
            vertsFlat.add(vert.y);
            vertsFlat.add(vert.z);
        }

        for(Vector3f normal : normals){
            normalsFlat.add(normal.x);
            normalsFlat.add(normal.y);
            normalsFlat.add(normal.z);
        }


        for(Triangle triangle : triangles){
            elementsFlat.add(triangle.indices[0]);
            elementsFlat.add(triangle.indices[1]);
            elementsFlat.add(triangle.indices[2]);
        }

        float[] temp = new float[3];
        int i = 0;
        for(Vector3f normal : normals){
            Vector3f vert = verts.get(i);

            float absX = Math.abs(normal.x);
            float absY = Math.abs(normal.y);
            float absZ = Math.abs(normal.z);

            float uvX = vert.z * absX + vert.x * absY + vert.x * absZ;
            float uvY = vert.y * absX + vert.z * absY + vert.y * absZ;
            temp[0] = uvX;
            temp[1] = uvY;

            // if(absX >= absZ && absX >= absY){
            //     temp[0] = normal.z / 2.0f + 0.5f + vert.z * (absX / (absX + absZ)) + vert.x * (absZ / (absX + absZ));
            //     temp[1] = normal.y / 2.0f + 0.5f + vert.x * (absY / (absX + absY)) + vert.y * (absX / (absX + absY));
            // } else if(absZ >= absX && absZ >= absY){
            //     temp[0] = normal.x / 2.0f + 0.5f + vert.z * (absX / (absX + absZ)) + vert.x * (absZ / (absX + absZ));
            //     temp[1] = normal.y / 2.0f + 0.5f + vert.z * (absY / (absZ + absY)) + vert.y * (absZ / (absZ + absY));
            // } else if(absY >= absX && absY >= absZ){
            //     temp[0] = normal.x / 2.0f + 0.5f + vert.y * (absX / (absX + absY)) + vert.x * (absY / (absX + absY));
            //     temp[1] = normal.z / 2.0f + 0.5f + vert.y * (absZ / (absZ + absY)) + vert.z * (absY / (absZ + absY));
            // } else {
            //     temp[0] = vert.x / 1.5f + vert.z / 1.5f;
            //     temp[1] = vert.y / 1.5f + vert.z / 1.5f;
            // }
            i++;
            UVs.add(temp[0]);
            UVs.add(temp[1]);
        }

        //List<Float> vertices, List<Float> normals, List<Integer> faceElements, List<Float> uvs
        FluidChunkModelData rVal = new FluidChunkModelData(vertsFlat, normalsFlat, elementsFlat, UVs);
        return rVal;
    }
    
    /**
     * Generates a mesh based on a fluidchunkdata object
     * @param data The fluid chunk data object
     * @return The mesh
     */
    protected static Mesh generateFluidMesh(OpenGLState openGLState, FluidChunkModelData data){
        
        Mesh mesh = new Mesh("fluidChunk");


        
        //
        //  VAO
        //
        mesh.generateVAO(openGLState);
        
        
        
        
        
        
        //
        //Buffer data to GPU
        //
        
        try {
            int vertexCount = data.getVertices().size() / 3;
            FloatBuffer vertexArrayBufferData = BufferUtils.createFloatBuffer(vertexCount * 3);
            for(float vertValue : data.getVertices()){
                vertexArrayBufferData.put(vertValue);
            }
            if(vertexArrayBufferData.position() > 0){
                vertexArrayBufferData.flip();
                mesh.bufferVertices(vertexArrayBufferData, 3);
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
        

        
        //
        //  FACES
        //
        int elementCount = data.getFaceElements().size();
        try {
            IntBuffer elementArrayBufferData = BufferUtils.createIntBuffer(elementCount);
            for(int element : data.getFaceElements()){
                elementArrayBufferData.put(element);
            }
            if(elementArrayBufferData.position() > 0){
                elementArrayBufferData.flip();
                mesh.bufferFaces(elementArrayBufferData,elementCount);
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
        
        
        
        
        //
        //  NORMALS
        //
        try {
            int normalCount = data.getNormals().size() / 3;
            FloatBuffer normalArrayBufferData;
            if(normalCount > 0){
                normalArrayBufferData = BufferUtils.createFloatBuffer(normalCount * 3);
                for(float normalValue : data.getNormals()){
                    normalArrayBufferData.put(normalValue);
                }
                if(normalArrayBufferData.position() > 0){
                    normalArrayBufferData.flip();
                    mesh.bufferNormals(normalArrayBufferData, 3);
                }
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }

        //
        //  TEXTURE COORDINATES
        //
        try {
            int textureCoordCount = data.getUVs().size() / 2;
            FloatBuffer textureArrayBufferData;
            if(textureCoordCount > 0){
                textureArrayBufferData = BufferUtils.createFloatBuffer(textureCoordCount * 2);
                for(float uvValue : data.getUVs()){
                    textureArrayBufferData.put(uvValue);
                }
                if(textureArrayBufferData.position() > 0){
                    textureArrayBufferData.flip();
                    mesh.bufferTextureCoords(textureArrayBufferData, 2);
                }
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
        
        float halfChunk = ServerTerrainChunk.CHUNK_DIMENSION / 2.0f;
        mesh.updateBoundingSphere(
            halfChunk,
            halfChunk,
            halfChunk,
            (float)Math.sqrt(halfChunk * halfChunk + halfChunk * halfChunk + halfChunk * halfChunk)
            );
        
        
        openGLState.glBindVertexArray(0);
        return mesh;
    }


    /**
     * Generates a model based on a fluidchunkdata object
     * @param data The fluid chunk data object
     * @return The model
     */
    public static Model generateFluidModel(FluidChunkModelData data){
        Model rVal = new Model();
        Mesh m = generateFluidMesh(Globals.renderingEngine.getOpenGLState(),data);
        
        
        Material groundMat = Material.create("/Textures/Ground/Dirt1.png");
        m.setMaterial(groundMat);

        m.setShader(FluidChunkModelGeneration.fluidChunkShaderProgram);
        
        rVal.addMesh(m);

        return rVal;
    }

    private static String getVertKeyFromPoints(float x, float y, float z){
        return x + "_" + y + "_" + z;
    }

    private static int getVertIndex(Vector3f vert, Map<String,Integer> vertMap, List<Vector3f> verts){
        int rVal = -1;
        String vertKey = getVertKeyFromPoints(vert.x,vert.y,vert.z);
        if(vertMap.containsKey(vertKey)){
            return vertMap.get(vertKey);
        } else {
            rVal = verts.size();
            verts.add(vert);
            vertMap.put(vertKey,rVal);
            return rVal;
        }
    }

    private static Vector3f averageNormals(Vector3f normal0, float proportion0, Vector3f normal1, float proportion1){
        Vector3f rVal = new Vector3f(normal0);
        rVal = rVal.mul(proportion0).add(new Vector3f(normal1).mul(proportion1));
        return rVal;
    }

}

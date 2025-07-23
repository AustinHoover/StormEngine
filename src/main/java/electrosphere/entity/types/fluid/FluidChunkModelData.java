package electrosphere.entity.types.fluid;

import java.util.List;

public class FluidChunkModelData {
    
    List<Float> vertices;
    List<Float> normals;
    List<Integer> faceElements;
    List<Float> uvs;

    public FluidChunkModelData(List<Float> vertices, List<Float> normals, List<Integer> faceElements, List<Float> uvs){
        this.vertices = vertices;
        this.normals = normals;
        this.faceElements = faceElements;
        this.uvs = uvs;
    }

    public List<Float> getVertices(){
        return vertices;
    }

    public List<Float> getNormals(){
        return normals;
    }

    public List<Integer> getFaceElements(){
        return faceElements;
    }

    public List<Float> getUVs(){
        return uvs;
    }

}

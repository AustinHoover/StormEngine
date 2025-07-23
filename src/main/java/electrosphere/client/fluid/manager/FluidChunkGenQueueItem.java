package electrosphere.client.fluid.manager;

import electrosphere.entity.types.fluid.FluidChunkModelData;

public class FluidChunkGenQueueItem {
    
    FluidChunkModelData data;
    String promisedHash;

    public FluidChunkGenQueueItem(FluidChunkModelData data, String promisedHash){
        this.data = data;
        this.promisedHash = promisedHash;
    }

    public FluidChunkModelData getData(){
        return data;
    }

    public String getPromisedHash(){
        return this.promisedHash;
    }

}

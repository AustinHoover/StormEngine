package electrosphere.client.entity.particle;

import electrosphere.data.entity.particle.ParticleData;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.DrawableUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.client.particle.ClientParticleTree;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.mask.ActorTextureMask;

import java.util.Arrays;

import org.joml.Vector3d;

/**
 * Particle utility functions
 */
public class ParticleUtils {
    
    
    


    // public static Entity clientSpawnStaticBillboardParticle(){
    //     Entity rVal = EntityCreationUtils.createClientSpatialEntity();
    //     EntityCreationUtils.makeEntityDrawable(rVal, Globals.particleBillboardModel);
    //     ClientParticleTree particleTree = new ClientParticleTree(rVal, 10, new Vector3f(0,0,0), 0, 0, false);
    //     rVal.putData(EntityDataStrings.TREE_CLIENTPARTICLETREE, particleTree);
    //     rVal.putData(EntityDataStrings.IS_PARTICLE, true);
    //     Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(rVal, EntityTags.PARTICLE);
    //     return rVal;
    // }

    /**
     * Spawns a billboard particle
     * @param data The particle definition
     * @param destination the destination of the particle
     * @return The particle entity
     */
    public static Entity clientSpawnBillboardParticle(ParticleData data, Vector3d destination){
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(rVal, AssetDataStrings.MODEL_PARTICLE);

        //actor logic
        Actor particleActor = EntityUtils.getActor(rVal);
        DrawableUtils.makeEntityTransparent(rVal);
        particleActor.addTextureMask(new ActorTextureMask("particleBillboard", Arrays.asList(data.getTexture())));

        //behavior tree attachments
        ClientParticleTree.attachTree(rVal, data, destination);
        rVal.putData(EntityDataStrings.IS_PARTICLE, true);
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(rVal, EntityTags.PARTICLE);
        return rVal;
    }

    /**
     * Checks if the provided entity is a particle or not
     * @param entity The entity
     * @return true if it is a particle, false otherwise
     */
    public static boolean isParticle(Entity entity){
        return entity.containsKey(EntityDataStrings.IS_PARTICLE);
    }

}

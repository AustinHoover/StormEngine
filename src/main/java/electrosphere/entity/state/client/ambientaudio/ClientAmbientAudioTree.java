package electrosphere.entity.state.client.ambientaudio;

import org.joml.Vector3d;

import electrosphere.audio.VirtualAudioSource;
import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.data.entity.foliage.AmbientAudio;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;

/**
 * 
 * A behavior tree encapsulating 
 */
public class ClientAmbientAudioTree implements BehaviorTree {

    //the parent entity to this ambient audio tree
    Entity parent;
    //the virtual audio source that is emitting audio
    VirtualAudioSource virtualAudioSource;
    //the offset of the sound relative to the parent entity origin point
    Vector3d offset = new Vector3d(0,0,0);

    private ClientAmbientAudioTree(Entity parent){
        this.parent = parent;
    }

    @Override
    public void simulate(float deltaTime) {
        //TODO: eventually swap to pushing down entity position from move methods when they mode
        Vector3d position = EntityUtils.getPosition(parent);
        virtualAudioSource.setPosition(new Vector3d(position).add(offset));
    }

    /**
     * Attaches this tree to the entity.
     * @param entity The entity to attach to
     * @param ambientAudio The ambient audio model
     */
    public static ClientAmbientAudioTree attachTree(Entity parent, AmbientAudio ambientAudio){
        ClientAmbientAudioTree rVal = new ClientAmbientAudioTree(parent);
    
        if(ambientAudio.getResponseWindAudioFilePath()!=null){
            Globals.assetManager.addAudioPathToQueue(ambientAudio.getResponseWindAudioFilePath());
            rVal.virtualAudioSource = Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(
                ambientAudio.getResponseWindAudioFilePath(),
                VirtualAudioSourceType.ENVIRONMENT_LONG,
                ambientAudio.getResponseWindLoops(),
                new Vector3d(0,0,0)
            );
            if(ambientAudio.getRandomizeOffset()){
                rVal.virtualAudioSource.setTotalTimePlayed((float)(Math.random() * 100));
            }
            if(ambientAudio.getEmitterSpatialOffset()!=null){
                rVal.offset.set(
                    ambientAudio.getEmitterSpatialOffset()[0],
                    ambientAudio.getEmitterSpatialOffset()[1],
                    ambientAudio.getEmitterSpatialOffset()[2]
                );
            }
            rVal.virtualAudioSource.setGain(ambientAudio.getGainMultiplier());
        }
    
        //!!WARNING!! THIS WAS MANUALLY MODIFIED OH GOD
        parent.putData(EntityDataStrings.CLIENT_AMBIENT_AUDIO_TREE, rVal);
        Globals.clientState.clientScene.registerBehaviorTree(rVal);
        return rVal;
    }

    
    
}

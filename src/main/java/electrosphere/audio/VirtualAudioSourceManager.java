package electrosphere.audio;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.joml.Vector3d;
import org.joml.Vector3f;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;

/**
 * Manages all the virtual audio sources in the engine.
 * Divides all virtual sources into buckets of different types: ui, environment, creatures, etc.
 * Then sorts them by priority and phases them in or out based on priority.
 */
public class VirtualAudioSourceManager {
    
    /**
     * Types of virtual audio sources
     */
    public enum VirtualAudioSourceType {
        UI,
        ENVIRONMENT_SHORT,
        ENVIRONMENT_LONG,
        CREATURE,
    }

    //the list of categories of virtual audio sources
    List<VirtualAudioSourceCategory> categories = new LinkedList<VirtualAudioSourceCategory>();

    //The list of all virtual sources
    List<VirtualAudioSource> virtualSourceQueue = new CopyOnWriteArrayList<VirtualAudioSource>();

    //the map of virtual source to active source for all active sources
    Map<VirtualAudioSource,AudioSource> virtualActiveMap = new HashMap<VirtualAudioSource,AudioSource>();

    //Temporary list used to store sources that need to be destroyed
    List<VirtualAudioSource> sourcesToKill = new LinkedList<VirtualAudioSource>();


    /**
     * Creates the manager
     */
    public VirtualAudioSourceManager(){
        //add all categories
        categories.add(new VirtualAudioSourceCategory(VirtualAudioSourceType.UI,4,-0.1f,1.0f));
        categories.add(new VirtualAudioSourceCategory(VirtualAudioSourceType.ENVIRONMENT_SHORT,6,-0.1f,1.0f));
        categories.add(new VirtualAudioSourceCategory(VirtualAudioSourceType.ENVIRONMENT_LONG,8,-0.05f,0.05f));
        categories.add(new VirtualAudioSourceCategory(VirtualAudioSourceType.CREATURE,8,-0.1f,1.0f));
    }

    /**
     * Creates a spatial virtual audio source
     * @param filePath The file path for the audio source
     * @param type The type of audio source (ui, environment, etc)
     * @param loops If true, loops the audio source
     * @param position The position of the audio source
     */
    public VirtualAudioSource createVirtualAudioSource(String filePath, VirtualAudioSourceType type, boolean loops, Vector3d position){
        VirtualAudioSource source = new VirtualAudioSource(filePath, type, loops, position);
        Globals.assetManager.addAudioPathToQueue(filePath);
        LoggerInterface.loggerAudio.DEBUG("Create virtual audio source " + filePath);
        this.virtualSourceQueue.add(source);
        return source;
    }

    /**
     * Creates a non-spatial virtual audio source
     * @param filePath The file path for the audio source
     * @param type The type of audio source (ui, environment, etc)
     * @param loops If true, loops the audio source
     */
    public VirtualAudioSource createVirtualAudioSource(String filePath, VirtualAudioSourceType type, boolean loops){
        VirtualAudioSource source = new VirtualAudioSource(filePath, type, loops);
        Globals.assetManager.addAudioPathToQueue(filePath);
        LoggerInterface.loggerAudio.DEBUG("Create virtual audio source " + filePath);
        this.virtualSourceQueue.add(source);
        return source;
    }

    /**
     * Creates a non-spatial virtual audio source
     * @param filePath The file path for the audio source
     */
    public VirtualAudioSource createUI(String filePath){
        VirtualAudioSource source = new VirtualAudioSource(filePath, VirtualAudioSourceType.UI, false);
        Globals.assetManager.addAudioPathToQueue(filePath);
        LoggerInterface.loggerAudio.DEBUG("Create virtual audio source " + filePath);
        this.virtualSourceQueue.add(source);
        return source;
    }

    /**
     * Updates all virtual audio sources this frame
     */
    public void update(float deltaTime){
        //update priority of all virtual audio sources based on distance from camera position
        if(Globals.clientState.playerCamera!=null){
            Vector3d cameraEarPos = new Vector3d(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera)).add(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera));
            for(VirtualAudioSource source : virtualSourceQueue){
                if(source.position!=null){
                    source.setPriority((int)cameraEarPos.distance(source.position));
                }
            }
        }
        //go through each audio source and destroy ones that are no longer virtually playing
        sourcesToKill.clear();
        for(VirtualAudioSource source : virtualSourceQueue){
            boolean stillActive = source.update(deltaTime);
            if(!stillActive){
                LoggerInterface.loggerAudio.DEBUG("Kill Virtual Audio Source");
                sourcesToKill.add(source);
            }
        }
        for(VirtualAudioSource source : sourcesToKill){
            AudioSource realSource = virtualActiveMap.remove(source);
            if(realSource != null){
                realSource.stop();
            }
            virtualSourceQueue.remove(source);
            for(VirtualAudioSourceCategory category : categories){
                category.activeVirtualSources.remove(source);
            }
        }
        //sort audio sources
        virtualSourceQueue.sort(Comparator.naturalOrder());
        LoggerInterface.loggerAudio.DEBUG_LOOP("[VirtualAudioSourceManager] Virtual audio source count: " + virtualSourceQueue.size());
        //for each bucket that has capacity, start available sources
        for(VirtualAudioSourceCategory category : categories){
            LoggerInterface.loggerAudio.DEBUG_LOOP("[VirtualAudioSourceManager] Audio category: " + category.type + "  Active Virtual Sources: " + category.activeVirtualSources.size());
            //
            for(VirtualAudioSource source : virtualSourceQueue){
                if(source.type != category.type){
                    continue;
                }
                //if it is an active source, set its gain to the virtual source's gain
                if(virtualActiveMap.containsKey(source)){
                    AudioSource realSource = virtualActiveMap.get(source);
                    realSource.setGain(source.getGain());
                }
                //if there is a currently active source in this category that is lower priority than this source
                //tell the engine to start fading out that lower priority source
                if(!category.activeVirtualSources.contains(source)){
                    for(VirtualAudioSource activeSource : category.activeVirtualSources){
                        if(activeSource.priority > source.priority){
                            activeSource.setFadeRate(category.fadeOutRate);
                            break;
                        }
                    }
                }
                //add virtual source if necessary
                if(category.activeVirtualSources.size() < category.capacity && !category.activeVirtualSources.contains(source)){
                    //activate source here
                    AudioSource realSource = null;
                    LoggerInterface.loggerAudio.DEBUG("[VirtualAudioSourceManager] MAP Audio to real source! ");
                    if(source.position == null){
                        realSource = AudioUtils.playAudio(source.filePath,source.loops);
                    } else {
                        realSource = AudioUtils.playAudioAtLocation(source.filePath, new Vector3f((float)source.position.x,(float)source.position.y,(float)source.position.z),source.loops);
                    }
                    if(realSource != null){
                        source.setFadeRate(category.fadeInRate);
                        realSource.setGain(source.gain);
                        realSource.setOffset(source.totalTimePlayed);
                        virtualActiveMap.put(source, realSource);
                        category.activeVirtualSources.add(source);
                    }
                }
            }
        }
    }

    /**
     * Gets the queue of all currently active sources
     * @return The queue
     */
    public List<VirtualAudioSource> getSourceQueue(){
        return virtualSourceQueue;
    }

    /**
     * Gets the list of all real sources
     * @return The list
     */
    List<VirtualAudioSource> virtualSourcesMappedToRealSources = new LinkedList<VirtualAudioSource>();
    public List<VirtualAudioSource> getMappedSources(){
        virtualSourcesMappedToRealSources.clear();
        for(VirtualAudioSourceCategory category : categories){
            for(VirtualAudioSource activeSource : category.activeVirtualSources){
                virtualSourcesMappedToRealSources.add(activeSource);
            }
        }
        virtualSourcesMappedToRealSources.sort(Comparator.naturalOrder());
        return virtualSourcesMappedToRealSources;
    }


    /**
     * A category of virtual audio sources
     */
    private static class VirtualAudioSourceCategory {

        //the type of the category
        VirtualAudioSourceType type;

        //the number of real audio sources that can be playing for this category at a given time
        int capacity;

        //the rate to fade out sources in this category by when they become lower priority
        float fadeOutRate;
        //the rate to fade in sources in this category by when they become higher priority
        float fadeInRate = 0;

        //the list of virtual audio sources that are currently being played for this category
        List<VirtualAudioSource> activeVirtualSources = new LinkedList<VirtualAudioSource>();

        /**
         * Constructor
         * @param type
         * @param capacity
         */
        public VirtualAudioSourceCategory(VirtualAudioSourceType type, int capacity, float fadeOutRate, float fadeInRate){
            this.type = type;
            this.capacity = capacity;
            this.fadeOutRate = fadeOutRate;
            this.fadeInRate = fadeInRate;
        }

    }
}

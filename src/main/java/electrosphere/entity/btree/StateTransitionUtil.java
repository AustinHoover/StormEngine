package electrosphere.entity.btree;

import java.util.function.Supplier;

import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.data.entity.common.treedata.TreeDataAudio;
import electrosphere.data.entity.common.treedata.TreeDataState;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.actor.Actor;
import electrosphere.server.entity.poseactor.PoseActor;

/**
 * For a lot of behavior trees, there are states where we simply want to play an animation (ie a cooldown)
 * Those states can be registered with this utility object
 * When you simulate that specific state in the tree, instead ccall the simulation function from this handler
 * It will handle playing the animation in first and third person, playing audio, and transitioning to the next state once the animation has completed
 */
public class StateTransitionUtil {
    
    //The list of simplified states within this util object
    StateTransitionUtilItem[] states;

    //The parent entity
    Entity parent;

    //tracks if this is the server or not
    boolean isServer;

    //If set to true on client, will account for delay between client and server when starting animations
    boolean accountForSync = false;

    /**
     * Private constructor
     * @param states
     */
    private StateTransitionUtil(Entity entity, boolean isServer, StateTransitionUtilItem[] states){
        this.states = states;
        this.parent = entity;
        this.isServer = isServer;
    }

    /**
     * Creates a state transition util
     * @param entity The entity
     * @param states The states
     * @return The util object
     */
    public static StateTransitionUtil create(Entity entity, boolean isServer, StateTransitionUtilItem[] states){
        StateTransitionUtil rVal = new StateTransitionUtil(entity,isServer,states);

        //error checking
        if(!isServer && Globals.clientState.clientSceneWrapper.getScene().getEntityFromId(entity.getId()) == null){
            throw new Error("Tried to create state transition util with isServer=false and passed in a server entity");
        } else if(isServer && Globals.clientState.clientSceneWrapper.getScene().getEntityFromId(entity.getId()) != null){
            throw new Error("Tried to create state transition util with isServer=true and passed in a client entity");
        }


        return rVal;
    }

    /**
     * Should be called every time the natural state progression is interrupted (ie if you call start() or stop())
     */
    public void reset(){
        for(StateTransitionUtilItem state : states){
            state.startedAnimation = false;
        }
    }

    /**
     * Sets whether the client will account for delay over network or not
     * @param accountForSync true if account for delay, false otherwise
     */
    public void setAccountForSync(boolean accountForSync){
        this.accountForSync = accountForSync;
    }

    /**
     * Simulates a given state
     * @param stateEnum The enum for the state
     */
    public void simulate(Object stateEnum){
        StateTransitionUtilItem state = null;
        for(StateTransitionUtilItem targetState : states){
            if(targetState != null && targetState.stateEnum == stateEnum){
                state = targetState;
                break;
            }
        }
        if(state == null){
            LoggerInterface.loggerEngine.DEBUG("Skipping state " + stateEnum + " because there is not a state registered to that enum value!");
        } else {
            if(this.isServer){
                this.simulateServerState(this.parent,state);
            } else {
                this.simulateClientState(this.parent,state);
            }
        }
    }

    /**
     * Runs animation logic for client tree
     * @param parent The parent entity
     * @param state The state
     */
    private void simulateClientState(Entity parent, StateTransitionUtilItem state){
        boolean shouldPlayFirstPerson = false;
        Actor actor = EntityUtils.getActor(parent);
        if(actor != null){

            //get the animation to play
            TreeDataAnimation animation = state.animation;
            if(state.getAnimation != null && state.getAnimation.get() != null){
                animation = state.getAnimation.get();
            }

            TreeDataAudio audioData = state.audioData;
            if(state.getAudio != null && state.getAudio.get() != null){
                audioData = state.getAudio.get();
            }

            //
            //Calculate offset to start the animation at
            double animationOffset = 0.0001;
            if(this.accountForSync){
                int delay = Globals.clientState.clientConnection.getDelay();
                double simFrameTime = Globals.engineState.timekeeper.getSimFrameTime();
                animationOffset = delay * simFrameTime;
            }


            //
            //Play main animation
            //
            if(!actor.getAnimationData().isPlayingAnimation() && state.onComplete != null && state.startedAnimation == true){
                //state transition if this isn't set to loop
                state.onComplete.run();
                state.startedAnimation = false;
            } else if(!actor.getAnimationData().isPlayingAnimation() || !actor.getAnimationData().isPlayingAnimation(animation)){

                //
                //if it isn't looping, only play on first go around
                if(state.loop || !state.startedAnimation){
                    
                    
                    //
                    //play audio
                    if(parent == Globals.clientState.playerEntity && !Globals.controlHandler.cameraIsThirdPerson() && animation != null){
                        //first person
                        //play first person audio
                        if(Globals.audioEngine.initialized() && audioData != null && audioData.getAudioPath() != null){
                            Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(audioData.getAudioPath(), VirtualAudioSourceType.CREATURE, false);
                        }
                    } else {
                        //play third person audio
                        if(Globals.audioEngine.initialized() && audioData != null && audioData.getAudioPath() != null){
                            Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource(audioData.getAudioPath(), VirtualAudioSourceType.CREATURE, false, EntityUtils.getPosition(parent));
                        }
                    }

                    //
                    //play animation
                    if(animation != null){
                        actor.getAnimationData().playAnimation(animation,true);
                        shouldPlayFirstPerson = true;
                    }
                    actor.getAnimationData().incrementAnimationTime(animationOffset);
                }
                state.startedAnimation = true;
            } else if(state.animation == null && state.onComplete != null){
                state.onComplete.run();
                state.startedAnimation = false;
            }

            //
            //Play animation in first person
            //
            if(shouldPlayFirstPerson && animation != null){
                FirstPersonTree.conditionallyPlayAnimation(parent, animation, animationOffset);
            }
        }
    }

    /**
     * Runs animation logic for server tree
     * @param parent The parent entity
     * @param state The state
     */
    private void simulateServerState(Entity parent, StateTransitionUtilItem state){
        PoseActor poseActor = EntityUtils.getPoseActor(parent);
        if(poseActor != null){

            //get the animation to play
            TreeDataAnimation animation = state.animation;
            if(state.getAnimation != null && state.getAnimation.get() != null){
                animation = state.getAnimation.get();
            }



            //
            //Play main animation
            //
            if(animation == null){
                state.startedAnimation = true;
                if(state.onComplete != null){
                    state.onComplete.run();
                }
            } else if(!poseActor.isPlayingAnimation(animation) && state.onComplete != null && state.startedAnimation == true){
                //state transition if this isn't set to loop
                state.onComplete.run();
                state.startedAnimation = false;
            } else if(!poseActor.isPlayingAnimation() || !poseActor.isPlayingAnimation(animation)){
                //if it isn't looping, only play on first go around
                if(state.loop || !state.startedAnimation){
                    //play animation for state
                    if(animation != null){
                        poseActor.playAnimation(animation);
                    }
                    poseActor.incrementAnimationTime(0.0001);
                }
                state.startedAnimation = true;
            }
        }
    }

    /**
     * Interrupts a given state
     * @param stateEnum The state enum
     */
    public void interrupt(Object stateEnum){
        StateTransitionUtilItem state = null;
        for(StateTransitionUtilItem targetState : states){
            if(targetState.stateEnum == stateEnum){
                state = targetState;
                break;
            }
        }
        if(state == null){
            LoggerInterface.loggerEngine.DEBUG("Skipping state " + stateEnum + " because there is not a state registered to that enum value!");
        } else {
            if(this.isServer){
                interruptServerState(this.parent,state);
            } else {
                interruptClientState(this.parent,state);
            }
        }
    }

    /**
     * Interrupts animation logic for client tree
     * @param parent The parent entity
     * @param state The state
     */
    private static void interruptClientState(Entity parent, StateTransitionUtilItem state){
        Actor actor = EntityUtils.getActor(parent);
        if(actor != null){

            //get the animation to play
            TreeDataAnimation animation = state.animation;
            if(state.getAnimation != null && state.getAnimation.get() != null){
                animation = state.getAnimation.get();
            }


            //
            //Interrupt main animation
            //
            if(animation != null && actor.getAnimationData().isPlayingAnimation() && actor.getAnimationData().isPlayingAnimation(animation)){
                actor.getAnimationData().interruptAnimation(animation, true);
            }

            //
            //Interrupt animation in first person
            //
            if(animation != null){
                FirstPersonTree.conditionallyInterruptAnimation(Globals.clientState.firstPersonEntity, animation);
            }
        }
    }

    /**
     * Interrupts animation logic for server tree
     * @param parent The parent entity
     * @param state The state
     */
    private static void interruptServerState(Entity parent, StateTransitionUtilItem state){
        PoseActor poseActor = EntityUtils.getPoseActor(parent);
        if(poseActor != null){

            //get the animation to play
            TreeDataAnimation animation = state.animation;
            if(state.getAnimation != null && state.getAnimation.get() != null){
                animation = state.getAnimation.get();
            }



            //
            //Interrupt main animation
            //
            if(animation != null && poseActor.isPlayingAnimation() && poseActor.isPlayingAnimation(animation)){
                poseActor.interruptAnimation(animation, true);
            }
        }
    }

    /**
     * A parameter used to construct a StateTransitionUtil
     */
    public static class StateTransitionUtilItem {

        /**
         * The enum value for this state
         */
        Object stateEnum;

        /**
         * The animation to play
         */
        TreeDataAnimation animation;

        /**
         * Gets the first person animation
         */
        Supplier<TreeDataAnimation> getAnimation;

        /**
         * The audio data
         */
        TreeDataAudio audioData;

        /**
         * Gets the audio to play
         */
        Supplier<TreeDataAudio> getAudio;

        /**
         * The function to fire on completion (ie to transition to the next state)
         */
        Runnable onComplete;

        /**
         * Controls whether the state transition util should loop or not
         */
        boolean loop = true;

        /**
         * Tracks whether the animation has been played or not
         */
        boolean startedAnimation = false;

        /**
         * Constructor
         */
        private StateTransitionUtilItem(
            Object stateEnum,
            TreeDataAnimation animation,
            TreeDataAudio audioData,
            Runnable onComplete
        ){
            this.stateEnum = stateEnum;
            this.animation = animation;
            this.audioData = audioData;
            this.onComplete = onComplete;
            if(this.onComplete != null){
                this.loop = false;
            }
        }

        /**
         * Constructor
         */
        private StateTransitionUtilItem(
            Object stateEnum,
            TreeDataAnimation animation,
            TreeDataAudio audioData,
            boolean loop
        ){
            this.stateEnum = stateEnum;
            this.animation = animation;
            this.audioData = audioData;
            this.loop = loop;
        }

        /**
         * Constructor for supplier type
         */
        private StateTransitionUtilItem(
            Object stateEnum,
            Supplier<TreeDataAnimation> getAnimation,
            Supplier<TreeDataAudio> getAudio,
            Runnable onComplete
        ){
            this.stateEnum = stateEnum;
            this.getAnimation = getAnimation;
            this.getAudio = getAudio;
            this.onComplete = onComplete;
            if(this.onComplete != null){
                this.loop = false;
            }
        }

        /**
         * Constructor for supplier type
         */
        private StateTransitionUtilItem(
            Object stateEnum,
            Supplier<TreeDataAnimation> getAnimation,
            Supplier<TreeDataAudio> getAudio,
            boolean loop
        ){
            this.stateEnum = stateEnum;
            this.getAnimation = getAnimation;
            this.getAudio = getAudio;
            this.loop = loop;
        }

        /**
         * Constructor for a supplier-based approach. This takes suppliers that will provide animation data on demand.
         * This decouples the animations from the initialization of the tree.
         * The intended usecase is if the animation could change based on some state in the tree.
         * @param stateEnum The enum value for this state
         * @param getAnimationData The supplier for the animation data. If it is null, it will not play any animation
         * @param getAudio The supplier for path to an audio file to play on starting the animation. If null, no audio will be played
         * @param onComplete !!Must transition to the next state!! Fires when the animation completes. If not supplied, animations and autio will loop
         */
        public static StateTransitionUtilItem create(
            Object stateEnum,
            Supplier<TreeDataAnimation> getAnimation,
            Supplier<TreeDataAudio> getAudio,
            Runnable onComplete
            ){
            return new StateTransitionUtilItem(
                stateEnum,
                getAnimation,
                getAudio,
                onComplete
            );
        }

        /**
         * Constructor for a supplier-based approach. This takes suppliers that will provide animation data on demand.
         * This decouples the animations from the initialization of the tree.
         * The intended usecase is if the animation could change based on some state in the tree.
         * @param stateEnum The enum value for this state
         * @param getAnimationData The supplier for the animation data. If it is null, it will not play any animation
         * @param getAudio The supplier for path to an audio file to play on starting the animation. If null, no audio will be played
         * @param loop Sets whether the animation should loop or not
         * @return
         */
        public static StateTransitionUtilItem create(
            Object stateEnum,
            Supplier<TreeDataAnimation> getAnimation,
            Supplier<TreeDataAudio> getAudio,
            boolean loop
            ){
            return new StateTransitionUtilItem(
                stateEnum,
                getAnimation,
                getAudio,
                loop
            );
        }

        /**
         * Creates a state transition based on tree data for the state
         * @param stateEnum The enum value for this state in particular in the tree
         * @param treeData The tree data for this state
         * @return The item for the transition util
         */
        public static StateTransitionUtilItem create(
            Object stateEnum,
            TreeDataState treeData,
            Runnable onComplete
        ){
            StateTransitionUtilItem rVal = null;
            if(treeData != null){
                rVal = new StateTransitionUtilItem(
                    stateEnum,
                    treeData.getAnimation(),
                    treeData.getAudioData(),
                    onComplete
                );
            } else {
                rVal = new StateTransitionUtilItem(
                    stateEnum,
                    (TreeDataAnimation)null,
                    null,
                    onComplete
                );
            }
            return rVal;
        }

        /**
         * Creates a state transition based on tree data for the state
         * @param stateEnum The enum value for this state in particular in the tree
         * @param treeData The tree data for this state
         * @param loop Controls whether the state loops its animation or not
         * @return The item for the transition util
         */
        public static StateTransitionUtilItem create(
            Object stateEnum,
            TreeDataState treeData,
            boolean loop
        ){
            StateTransitionUtilItem rVal = null;
            if(treeData != null){
                rVal = new StateTransitionUtilItem(
                    stateEnum,
                    treeData.getAnimation(),
                    treeData.getAudioData(),
                    loop
                );
            } else {
                rVal = new StateTransitionUtilItem(
                    stateEnum,
                    (TreeDataAnimation)null,
                    null,
                    loop
                );
            }
            return rVal;
        }


    }

}

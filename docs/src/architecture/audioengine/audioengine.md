@page audioengine Audio Engine

[TOC]

## High Level Overview
The main class that you should interact with directly is the VirtualAudioSourceManager.
It is what creates virtual audio sources and those are what you should be working with directly.
Under the hood, the virtual audio sources are dynamically mapped to real audio sources by the VirtualAudioSourceManager.
The real audio sources are what are actually playing audio to the user.
![](/docs/src/images/archaudio/virtualAudioSourceManagerArch.png)

## Major Usage Notes
 - OpenAL will not play stereo audio spatially. It must be converted to mono before openal will follow location.


## Main Classes
[VirtualAudioSourceManager.java](@ref #electrosphere.audio.VirtualAudioSourceManager) - The manager of all virtual audio sources

[VirtualAudioSource.java](@ref #electrosphere.audio.VirtualAudioSource) - An audio source being tracked in data by the engine

[AudioEngine.java](@ref #electrosphere.audio.AudioEngine) - A manager class for direct openal calls from things like main loop and renderer

[AudioSource.java](@ref #electrosphere.audio.AudioSource) - A wrapper around an openAL audio source

[AudioListener.java](@ref #electrosphere.audio.AudioListener) - A wrapper around the openAL listener

[AudioBuffer.java](@ref #electrosphere.audio.AudioBuffer) - A wrapper around an openAL buffer

[AudioUtils.java](@ref #electrosphere.audio.AudioUtils) - Utility functions for creating audio sources (protected to just this package)

[ClientAmbientAudioTree.java](@ref #electrosphere.entity.state.ambientaudio.ClientAmbientAudioTree) - A client-side behavior tree for emitting audio ambiently from an entity

[AmbientAudio.java](@ref #electrosphere.game.data.foliage.type.AmbientAudio) - Ambient audio data about a given type of entity from the entity description in data


## Library Explanation


#### OpenAL Context Creation
OpenAL supports numerous extensions that add things like effects, HRTF, etc.
These extensions must be manually enabled when the openAL context creation is done.
This should happen in [AudioEngine.java](@ref #electrosphere.audio.AudioEngine) when it is initializing openAL.


## Code Organization and Best Practices
#### Startup
TODO

#### Usage

Creating a virtual audio source -- this is analogous to saying "Play this audio here"
```
Globals.assetManager.addAudioPathToQueue(ambientAudio.getResponseWindAudioFilePath());
VirtualAudioSoruce virtualAudioSource = Globals.virtualAudioSourceManager.createVirtualAudioSource(
    ambientAudio.getResponseWindAudioFilePath(),
    VirtualAudioSourceType.ENVIRONMENT_LONG,
    ambientAudio.getResponseWindLoops(),
    new Vector3d(0,0,0)
);
virtualAudioSource.setGain(ambientAudio.getGainMultiplier());
```

Creating a ui audio effect
```
Globals.virtualAudioSourceManager.createVirtualAudioSource("/Audio/openMenu.ogg", VirtualAudioSourceType.UI, false);
```



## Terminology
 - Virtual Audio Source - An entity or empty source of audio in the game space that may or may not actually be emitting audio to the user.
 - Real Audio Source - An OpenAL audio source that is definitely emitting audio to the user.
 - Virtual Audio Source Manager - The manager of all virtual audio sources that handles creating, queueing, and destroying audio sources.
 - HRTF - Head-related transfer function, fancy math to make the audio sound better by modeling how it enters your ear









## Known Bugs To Fix
 - ClientAmbientAudioTree does not destroy itself if the audio does not loop and closes
 - AudioBuffer can hard crash if no file is found



## Future Goals
 
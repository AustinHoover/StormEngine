package electrosphere.client.ui.menu.debug.audio;

import org.joml.Vector3d;

import electrosphere.audio.AudioBuffer;
import electrosphere.audio.AudioListener;
import electrosphere.audio.VirtualAudioSource;
import electrosphere.audio.VirtualAudioSourceManager.VirtualAudioSourceType;
import electrosphere.engine.Globals;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import imgui.ImGui;
import imgui.type.ImString;

/**
 * Audio debug menus
 */
public class ImGuiAudio {

    //audio debug menu
    public static ImGuiWindow audioDebugMenu;

    //the audio buffer list filter
    private static ImString bufferListFilter = new ImString();

    /**
     * Create audio debug menu
     */
    public static void createAudioDebugMenu(){
        audioDebugMenu = new ImGuiWindow("Audio");
        audioDebugMenu.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //audio engine details
                ImGui.text("Audio Engine Details");
                ImGui.text("Virtual Audio Source Manager Details");
                ImGui.text("Total number active virtual sources: " + Globals.audioEngine.virtualAudioSourceManager.getSourceQueue().size());
                if(ImGui.collapsingHeader("Engine Configuration")){
                    ImGui.text("Current audio device: " + Globals.audioEngine.getDevice());
                    ImGui.text("Default audio device: " + Globals.audioEngine.getDefaultDevice());
                    ImGui.text("Has HRTF: " + Globals.audioEngine.getHRTFStatus());
                    ImGui.text("Listener location: " + Globals.audioEngine.getListener().getPosition());
                    ImGui.text("Listener eye vector: " + Globals.audioEngine.getListener().getEyeVector());
                    ImGui.text("Listener up vector: " + Globals.audioEngine.getListener().getUpVector());
                    if(ImGui.collapsingHeader("Supported (Java-loaded) formats")){
                        for(String extension : Globals.audioEngine.getSupportedFormats()){
                            ImGui.text(extension);
                        }
                    }
                }
                //only active children
                if(ImGui.collapsingHeader("Mapped Virtual Sources")){
                    for(VirtualAudioSource source : Globals.audioEngine.virtualAudioSourceManager.getMappedSources()){
                        ImGui.text("Source " + source.getPriority());
                        ImGui.text(" - Position " + source.getPosition());
                        ImGui.text(" - Gain " + source.getGain());
                        ImGui.text(" - Type " + source.getType());
                        ImGui.text(" - Total time played " + source.getTotalTimePlayed());
                        ImGui.text(" - Buffer Lenth " + source.getBufferLength());
                    }
                }
                //all virtual children
                if(ImGui.collapsingHeader("All Virtual Sources")){
                    for(VirtualAudioSource source : Globals.audioEngine.virtualAudioSourceManager.getSourceQueue()){
                        ImGui.text("Position " + source.getPosition());
                    }
                }
                //all buffers loaded into memory and stats about them
                if(ImGui.collapsingHeader("All Loaded Audio Buffers")){
                    if(ImGui.inputText("Filter", bufferListFilter)){
                        
                    }
                    for(AudioBuffer buffer : Globals.assetManager.getAllAudio()){
                        if(buffer.getFilePath().contains(bufferListFilter.get()) && ImGui.collapsingHeader(buffer.getFilePath())){
                            ImGui.text("Length: " + buffer.getLength());
                            ImGui.text("Channels: " + buffer.getChannels());
                        }
                    }
                }
                //testing
                if(ImGui.collapsingHeader("Spatial Testing")){
                    if(ImGui.button("Front")){
                        AudioListener listener = Globals.audioEngine.getListener();
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource("/Audio/movement/surface/dirt/Bare Step Gravel Medium A.wav", VirtualAudioSourceType.UI, false, new Vector3d(listener.getPosition()).add(listener.getEyeVector()));
                    }
                    if(ImGui.button("Back")){
                        AudioListener listener = Globals.audioEngine.getListener();
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource("/Audio/movement/surface/dirt/Bare Step Gravel Medium A.wav", VirtualAudioSourceType.UI, false, new Vector3d(listener.getPosition()).sub(listener.getEyeVector()));
                    }
                    if(ImGui.button("Above")){
                        AudioListener listener = Globals.audioEngine.getListener();
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource("/Audio/movement/surface/dirt/Bare Step Gravel Medium A.wav", VirtualAudioSourceType.UI, false, new Vector3d(listener.getPosition()).add(listener.getUpVector()));
                    }
                    if(ImGui.button("Below")){
                        AudioListener listener = Globals.audioEngine.getListener();
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource("/Audio/movement/surface/dirt/Bare Step Gravel Medium A.wav", VirtualAudioSourceType.UI, false, new Vector3d(listener.getPosition()).sub(listener.getUpVector()));
                    }
                    if(ImGui.button("Left")){
                        AudioListener listener = Globals.audioEngine.getListener();
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource("/Audio/movement/surface/dirt/Bare Step Gravel Medium A.wav", VirtualAudioSourceType.UI, false, new Vector3d(listener.getPosition()).add(listener.getEyeVector().cross(listener.getUpVector())));
                    }
                    if(ImGui.button("Right")){
                        AudioListener listener = Globals.audioEngine.getListener();
                        Globals.audioEngine.virtualAudioSourceManager.createVirtualAudioSource("/Audio/movement/surface/dirt/Bare Step Gravel Medium A.wav", VirtualAudioSourceType.UI, false, new Vector3d(listener.getPosition()).sub(listener.getEyeVector().cross(listener.getUpVector())));
                    }
                }
            }
        });
        audioDebugMenu.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(audioDebugMenu);
    }
    
}

package electrosphere.audio;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.util.FileUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL11;

import static org.lwjgl.openal.AL10.*;

import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * An audio buffer
 */
public class AudioBuffer {
    
    /**
     * the id of the buffer
     */
    private int bufferId;

    /**
     * The number of channels for the audio
     */
    private int channels = 0;

    /**
     * The sample rate of the audio
     */
    private float sampleRate = 0;

    /**
     * The size of a single sample in bits
     */
    private int sampleSize = 0;

    /**
     * The framerate
     */
    private float frameRate = 0;

    /**
     * the length of a frame
     */
    private long frameLength = 0;

    /**
     * The size of a single frame
     */
    private int frameSize = 0;
    
    /**
     * the length of the audio source in milliseconds
     */
    private float length = 0;

    /**
     * whether this buffer has created an al buffer object or not
     */
    private boolean isBuffered = false;

    /**
     * The filepath associated with this buffer
     */
    private String filePath = null;
    
    /**
     * Creates the audio buffer object
     * @param fileNameRaw The path for the audio file
     */
    public AudioBuffer(String fileNameRaw) {
        String fileNameSanitized = FileUtils.sanitizeFilePath(fileNameRaw);
        this.filePath = fileNameSanitized;
        bufferId = alGenBuffers();
        Globals.audioEngine.checkError();

        //read vorbis
        if(!isBuffered && fileNameSanitized.contains(".ogg")){
            //create buffer to store vorbis data
            try(STBVorbisInfo info = STBVorbisInfo.malloc()){
                readVorbis(fileNameSanitized, bufferId, 32 * 1024, info);
            } catch (Exception e){
                LoggerInterface.loggerAudio.ERROR("Failed to load audio", e);
            }
        }


        //read wav
        try {
            if(!isBuffered && AudioSystem.getAudioFileFormat(FileUtils.getAssetFile(fileNameSanitized)) != null){
                readJava(fileNameSanitized, bufferId, true);
            }
        } catch(UnsupportedAudioFileException ex){
            LoggerInterface.loggerAudio.ERROR(ex);
        } catch (IOException ex){
            LoggerInterface.loggerAudio.ERROR(ex);
        }


        LoggerInterface.loggerAudio.DEBUG("Created audio buffer(" + fileNameRaw + ") with length " + length);
    }
    
    /**
     * Reads vorbis data. Constructs a buffer that contains vorbis metadata as well as reading the main audio data into a buffer that is returned.
     * @param filepath The filepath to read
     * @param bufferSize The size of the buffer
     * @param info The vorbis info buffer to be filled
     * @return The main audio data buffer
     * @throws Exception Throws an exception if the decoder fails
     */
    private void readVorbis(String filepath, int bufferId, int bufferSize, STBVorbisInfo info) throws Exception {
        //buffer containing vorbis metadata
        ByteBuffer vorbis = null;
        try(MemoryStack stack = MemoryStack.stackPush()){

            //read the vorbis data from disk
            vorbis = AudioBuffer.readFilepathToByteBuffer(filepath);

            //decode the vorbis data
            IntBuffer error = stack.mallocInt(1);
            long decoder = STBVorbis.stb_vorbis_open_memory(vorbis, error, null);
            if (decoder == NULL) {
                throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
            }

            //creates the vorbis metadata object and grabs information about the audio file
            STBVorbis.stb_vorbis_get_info(decoder, info);
            this.channels = info.channels();
            int lengthSamples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            this.length = STBVorbis.stb_vorbis_stream_length_in_seconds(decoder) * 1;

            //reads the main audio data
            ShortBuffer pcm = MemoryUtil.memAllocShort(lengthSamples);
            pcm.limit(STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, this.channels, pcm) * this.channels);

            //close decoder and return
            STBVorbis.stb_vorbis_close(decoder);

            // Copy to buffer
            alBufferData(bufferId, this.channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
            Globals.audioEngine.checkError();
            isBuffered = true;
        }
    }

    /**
     * Tries reading an audio file using java built in audio processing
     * @param filepath The filepath to the wav
     * @param bufferId The id of the buffer
     * @param forceMono Forces the audio to load as a mono channel source
     */
    private void readJava(String filepath, int bufferId, boolean forceMono){
        try(MemoryStack stack = MemoryStack.stackPush()){
            //get raw file objects
            AudioInputStream inputStreamRaw = AudioSystem.getAudioInputStream(FileUtils.getAssetFile(filepath));
            AudioFormat format = inputStreamRaw.getFormat();
            AudioFormat.Encoding encoding = format.getEncoding();
            AudioInputStream inputStreamEncoded = AudioSystem.getAudioInputStream(encoding, inputStreamRaw);

            //get current format values
            float currentFormatSampleRate = format.getSampleRate();
            int desiredSampleSizeInBits = 16;
            int desiredChannels = format.getChannels() > 2 ? 2 : format.getChannels();
            if(forceMono){
                desiredChannels = 1;
            }
            boolean desiredSigned = true;
            boolean currentFormatIsBigEndian = format.isBigEndian();

            //get the desired format
            AudioFormat desiredFormat = new AudioFormat(currentFormatSampleRate, desiredSampleSizeInBits, desiredChannels, desiredSigned, currentFormatIsBigEndian);
            AudioInputStream finalStream = AudioSystem.getAudioInputStream(desiredFormat, inputStreamEncoded);


            //get data about specific file
            boolean isBigEndian = desiredFormat.isBigEndian();
            this.channels = desiredFormat.getChannels();
            this.sampleRate = desiredFormat.getSampleRate();
            this.sampleSize = desiredFormat.getSampleSizeInBits();
            this.frameLength = finalStream.getFrameLength();
            this.frameSize = desiredFormat.getFrameSize();
            this.frameRate = desiredFormat.getFrameRate();
            this.length = frameLength / frameRate;

            LoggerInterface.loggerAudio.INFO(
                "filepath: " + filepath + "\n" +
                "encoding: " + encoding + "\n" +
                "isBigEndian: " + isBigEndian + "\n" +
                "channels: " + this.channels + "\n" +
                "sampleRate: " + this.sampleRate + "\n" +
                "sampleSize: " + this.sampleSize + "\n" +
                "frameLength: " + this.frameLength + "\n" +
                "frameSize: " + this.frameSize + "\n" +
                "frameRate: " + this.frameRate + "\n" +
                "length: " + this.length + "\n"
            );

            //read data
            byte[] dataRaw = finalStream.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(dataRaw.length);
            buffer.order(isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            buffer.put(dataRaw);
            if(buffer.position() > 0){
                buffer.flip();
            }

            //sanity check
            if(this.length > 10 * 60 * 60 * 1000){
                String message = "Audio file has length greater than 10 hours!\n" +
                "filepath: " + filepath + "\n" +
                "channels: " + channels + "\n" +
                "sampleRate: " + sampleRate + "\n" +
                "length: " + this.length + "\n" +
                "frame length: " + finalStream.getFrameLength() + "\n" +
                "frame rate: " + format.getFrameRate() + "\n"
                ;
                LoggerInterface.loggerAudio.ERROR(new IllegalStateException(message));
            }
            if(sampleRate == AudioSystem.NOT_SPECIFIED){
                String message = "Sample rate not specified!\n" +
                "filepath: " + filepath + "\n"  
                ;
                LoggerInterface.loggerAudio.ERROR(new IllegalStateException(message));
            }
            if(format.getFrameRate() == AudioSystem.NOT_SPECIFIED){
                String message = "Frame rate not specified!\n" +
                "filepath: " + filepath + "\n"
                ;
                LoggerInterface.loggerAudio.ERROR(new IllegalStateException(message));
            }/* */
            if(channels == AudioSystem.NOT_SPECIFIED){
                String message = "Channels not specified!\n" +
                "filepath: " + filepath + "\n"
                ;
                LoggerInterface.loggerAudio.ERROR(new IllegalStateException(message));
            }
            if(channels > 2){
                String message = "More than two channels defined in audio file! The engine will only use the first two!\n" +
                "filepath: " + filepath + "\n"
                ;
                LoggerInterface.loggerAudio.ERROR(new IllegalStateException(message));
            }

            //buffer to openal
            AL11.alBufferData(bufferId, channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, buffer, (int)sampleRate);
            Globals.audioEngine.checkError();
            isBuffered = true;

            //cleanup
            MemoryUtil.memFree(buffer);

        } catch (UnsupportedAudioFileException e) {
            LoggerInterface.loggerAudio.ERROR(e);
        } catch (IOException e) {
            LoggerInterface.loggerAudio.ERROR(e);
        }
    }
    
    /**
     * Reads the filepath into a byte buffer
     * @param filepath The filepath
     * @return The byte buffer if the file was read successfully, null otherwise
     * @throws IOException Thrown if the file was not readable
     */
    private static ByteBuffer readFilepathToByteBuffer(String filepath) throws IOException {
        ByteBuffer buffer = null;

        Path path = FileUtils.getAssetFile(filepath).toPath();
        if(Files.isReadable(path)){
            try(SeekableByteChannel fc = Files.newByteChannel(path)){
                buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
                while(fc.read(buffer) != -1){
                    
                }
                if(buffer.position() > 0){
                    buffer.flip();
                }
            }
        } else {
            LoggerInterface.loggerFileIO.ERROR("Failed to create audio, file is not readable: " + filepath, new IOException("File access error!"));
        }
        
        return buffer;
    }
    
    
    /**
     * Gets the id of this buffer
     * @return The id
     */
    public int getBufferId() {
        return bufferId;
    }

    /**
     * Gets the file path of the buffer
     * @return The file path
     */
    public String getFilePath(){
        return this.filePath;
    }

    /**
     * Gets the length of this audio buffer
     * @return The length
     */
    public float getLength(){
        return this.length;
    }

    /**
     * Gets the number of channels for the audio
     * @return The number of channels
     */
    public int getChannels(){
        return this.channels;
    }

    /**
     * Cleans up this audio buffer
     */
    public void cleanup() {
        alDeleteBuffers(bufferId);
        Globals.audioEngine.checkError();
    }
    
}

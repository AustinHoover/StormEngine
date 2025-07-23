package electrosphere.engine.assetmanager.queue;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.lwjgl.BufferUtils;

import electrosphere.engine.Globals;
import electrosphere.renderer.texture.Texture;

/**
 * A texture queued to be sent to the gpu
 */
public class QueuedTexture implements QueuedAsset<Texture> {

    /**
     * The type of loading to perform with this queued texture
     */
    public static enum QueuedTextureType {
        /**
         * Loading raw data as a texture
         */
        DATA_BUFF,
        /**
         * Loading an actual image as a texture
         */
        IMG_BUFF,
    }

    /**
     * The type of queued texture
     */
    private QueuedTextureType type;

    /**
     * True if loaded
     */
    private boolean hasLoaded = false;

    /**
     * The resuling texture object
     */
    private Texture texture = null;

    /**
     * The data to be loaded
     */
    private BufferedImage data;

    /**
     * The byte buffer
     */
    private ByteBuffer buffer;

    /**
     * Width of the image
     */
    private int width = -1;

    /**
     * Height of the image
     */
    private int height = -1;

    /**
     * The path the asset manager promises this texture will be stored at
     */
    private String promisedPath;

    /**
     * true if the path to register this asset was supplied while it was being queued, false if the promised path should be generated when it is placed in queue
     */
    private boolean suppliedPath = false;

    /**
     * The runnable to invoke to actually load the model
     */
    private Consumer<Texture> loadFunc;

    /**
     * Constructor
     */
    private QueuedTexture(){ }

    /**
     * Creates the queued texture object
     * @param buffer The data to buffer
     * @param width The width of the buffer
     * @param height The height of the buffer
     */
    private QueuedTexture(QueuedTextureType type, ByteBuffer buffer, int width, int height){
        this.type = type;
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }

    /**
     * Creates a queued texture from a buffer
     * @param buffer The buffer
     * @param width The width of the buffer
     * @param height The height of the buffer
     * @return The queued texture
     */
    public static QueuedTexture createFromBuffer(ByteBuffer buffer, int width, int height){
        QueuedTexture rVal = new QueuedTexture();
        rVal.type = QueuedTextureType.DATA_BUFF;
        rVal.buffer = buffer;
        rVal.width = width;
        rVal.height = height;
        return rVal;
    }

    /**
     * Creates the queued texture object
     * @param image the image to load to gpu
     */
    public static QueuedTexture createFromImage(BufferedImage bufferedImage){
        ByteBuffer data;
        int width = 1;
        int height = 1;
        BufferedImage image_data = bufferedImage;
        boolean hasTransparency = false;
        if (
                image_data.getType() == BufferedImage.TYPE_3BYTE_BGR ||
                image_data.getType() == BufferedImage.TYPE_INT_RGB
                ){
            hasTransparency = false;
        } else if(
                image_data.getType() == BufferedImage.TYPE_4BYTE_ABGR ||
                image_data.getType() == BufferedImage.TYPE_INT_ARGB
                ){
            hasTransparency = true;
        }
        width = image_data.getWidth();
        height = image_data.getHeight();
        if(hasTransparency){
            data = BufferUtils.createByteBuffer(width * height * 4);
        } else {
            data = BufferUtils.createByteBuffer(width * height * 3);
        }
        for(int y = height - 1; y > -1; y--){
            for(int x = 0; x < width; x++){
                int color = image_data.getRGB(x, y);
                
                // data.put((byte)temp.getRed());
                // data.put((byte)temp.getGreen());
                // data.put((byte)temp.getBlue());
                if(hasTransparency){
                    int blue = color & 0xff;
                    int green = (color & 0xff00) >> 8;
                    int red = (color & 0xff0000) >> 16;
                    int alpha = (color & 0xff000000) >>> 24;
                    data.put((byte)red);
                    data.put((byte)green);
                    data.put((byte)blue);
                    data.put((byte)alpha);
                } else {
                    int blue = color & 0xff;
                    int green = (color & 0xff00) >> 8;
                    int red = (color & 0xff0000) >> 16;
                    data.put((byte)red);
                    data.put((byte)green);
                    data.put((byte)blue);
                }
            }
        }
        if(data.position() > 0){
            data.flip();
        }
        QueuedTexture rVal = new QueuedTexture(QueuedTextureType.IMG_BUFF, data, width, height);
        rVal.data = bufferedImage;
        return rVal;
    }

    /**
     * Creates the queued texture object
     * @param image the image to load to gpu
     */
    public static QueuedTexture createFromImage(BufferedImage bufferedImage, Consumer<Texture> loadFunc){
        QueuedTexture rVal = QueuedTexture.createFromImage(bufferedImage);
        rVal.loadFunc = loadFunc;
        return rVal;
    }

    /**
     * Creates the queued texture object
     * @param path The path to register this texture to
     * @param image the image to load to gpu
     */
    public static QueuedTexture createFromImage(String path, BufferedImage bufferedImage){
        QueuedTexture rVal = QueuedTexture.createFromImage(bufferedImage);
        rVal.promisedPath = path;
        rVal.suppliedPath = true;
        return rVal;
    }

    /**
     * Creates the queued texture object
     * @param path The path to the texture file
     * @param loadFunc The function to invoke when the texture loads
     */
    public static QueuedTexture createFromPath(String path, Consumer<Texture> loadFunc){
        QueuedTexture rVal = new QueuedTexture();
        rVal.type = QueuedTextureType.IMG_BUFF;
        rVal.promisedPath = path;
        rVal.loadFunc = loadFunc;
        rVal.suppliedPath = true;
        return rVal;
    }

    @Override
    public void load() {
        switch(this.type){
            case DATA_BUFF: {
                texture = new Texture(Globals.renderingEngine.getOpenGLState(),buffer,width,height);
                this.buffer = null;
            } break;
            case IMG_BUFF: {
                if(data != null){
                    texture = new Texture(Globals.renderingEngine.getOpenGLState(), data, buffer);
                } else if(promisedPath != null){
                    texture = new Texture(Globals.renderingEngine.getOpenGLState(), promisedPath);
                }
            } break;
        }
        if(this.loadFunc != null){
            this.loadFunc.accept(texture);
        }
        hasLoaded = true;
    }

    @Override
    public boolean hasLoaded() {
        return hasLoaded;
    }

    /**
     * Gets the texture object
     * @return The texture if it has been loaded, otherwise null
     */
    public Texture getTexture(){
        return texture;
    }

    /**
     * Gets the buffer data
     * @return The buffer data
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Gets the width of the buffer
     * @return The width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the buffer
     * @return The height
     */
    public int getHeight() {
        return height;
    }
    
    @Override
    public void setPromisedPath(String promisedPath) {
        this.promisedPath = promisedPath;
    }

    @Override
    public String getPromisedPath(){
        return this.promisedPath;
    }

    @Override
    public Texture get(){
        return texture;
    }

    @Override
    public boolean suppliedPath() {
        return suppliedPath;
    }

}

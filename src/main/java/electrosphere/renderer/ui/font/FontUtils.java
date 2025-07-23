package electrosphere.renderer.ui.font;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.queue.QueuedTexture;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.texture.Texture;
import electrosphere.util.FileUtils;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;


/**
 * Utilities for loading fonts
 */
public class FontUtils {

    public static final int TTF_CHAR_WIDTH = 32;
    public static final int TTF_CHAR_HEIGHT = 24;
    static final char FIRST_CHAR_TO_BAKE = 32;
    static final char LAST_CHAR_TO_BAKE = 127;
    static final int CHAR_COUNT = LAST_CHAR_TO_BAKE - FIRST_CHAR_TO_BAKE;
    static final int TTF_BITMAP_WIDTH = 512;
    static final int TTF_BITMAP_HEIGHT = 512;
    static final int SPACE_WIDTH = 8;

    /**
     * Manual offset to align them
     */
    static final int MANUAL_OFFSET = -2;

    /**
     * The factor to adjust the descent by. Just intuiting this from how the data looks, not guaranteed to be correct
     */
    static final int DESCENT_DIVISOR = 100;

    static float[] scale = new float[]{
        TTF_CHAR_HEIGHT,
    };


    /**
     * Renders a single character to a buffered image
     * @param font The java font object
     * @param c the character
     * @param antiAlias whether to antialias the font or not
     * @return The buffered image with the rendered font
     */
    private static BufferedImage createCharImage(java.awt.Font font, char c, boolean antiAlias) {
        
        //get the size of the character
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        if(antiAlias){
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.dispose();
        int charWidth = metrics.charWidth(c);
        int charHeight = metrics.getHeight();

        //return 0 width characters
        if (charWidth == 0) {
            return null;
        }

        //render character to a properly sized buffered image
        image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        g.setPaint(java.awt.Color.WHITE);
        g.drawString(String.valueOf(c), 0, metrics.getAscent());
        g.dispose();
        return image;
    }

    /**
     * Loads a java font object into an engine font object
     * @param font The java font object
     * @param antiAlias if true, antialias, otherwise dont
     * @return The engine font object
     */
    protected static Font loadFont(OpenGLState openGLState, java.awt.Font font, boolean antiAlias) {
        int imageWidth = 0;
        int imageHeight = 0;


        //data for parsing characters out of the font image
        List<Font.Glyph> glyphs = new LinkedList<Font.Glyph>();

        //iterate through ascii codes
        for(int i = 32; i < 256; i++){
            if(i == 127){
                //skip del character
                continue;
            }
            char c = (char)i;
            BufferedImage ch = createCharImage(font, c, antiAlias);
            if(ch == null){
                //skip characters with no images
                continue;
            }

            imageWidth += ch.getWidth();
            imageHeight = Math.max(imageHeight, ch.getHeight());
        }

        // int fontHeight = imageHeight;

        //create font bitmap
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        int x = 0;

        //loop through ascii codes
        for (int i = 32; i < 256; i++) {
            if (i == 127) {
                //skip del character command
                continue;
            }
            char c = (char) i;
            BufferedImage charImage = createCharImage(font, c, antiAlias);
            if (charImage == null) {
                //don't render if the character is blank
                continue;
            }

            int charWidth = charImage.getWidth();
            int charHeight = charImage.getHeight();

            //draw the glyph to the image
            g.drawImage(charImage, x, 0, null);

            //create glyph and push it into the array
            Font.Glyph glyph = new Font.Glyph();
            glyph.height = charHeight;
            glyph.width = charWidth;
            glyph.startX = x;
            glyph.startY = 0;
            glyph.symbol = (char)i + "";
            glyphs.add(glyph);

            //increment image
            x += charWidth;
        }

        //uncomment if you need to flip the font
        // AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
        // transform.translate(0, -image.getHeight());
        // AffineTransformOp operation = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        // image = operation.filter(image, null);

        // try {
        //     File imgFile = new File("./.testcache/testimg.png");
        //     imgFile.delete();
        //     imgFile.getParentFile().mkdirs();
        //     ImageIO.write(image, "png", Files.newOutputStream(imgFile.toPath()));
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }


        //create material with new font image
        Material uiMat = new Material();
        Globals.assetManager.queuedAsset(QueuedTexture.createFromImage(image, (Texture tex) -> {
            uiMat.setDiffuse(tex);
        }));


        //construct final font object and return
        Font rVal = new Font(uiMat,glyphs,imageWidth,imageHeight);
        rVal.process();
        return rVal;
    }
    
    /**
     * Loads the TTF font
     * @param openGLState The opengl state
     * @param path The path to the ttf font file
     * @return The font if it loaded successfully, null otherwise
     */
    protected static Font loadTTF(OpenGLState openGLState, String path){

        //used for cosntructing the font object to return
        List<Font.Glyph> glyphs = new LinkedList<Font.Glyph>();
        Material uiMat = new Material();

        //read the file
        ByteBuffer fileContent = null;
        try {
            fileContent = FileUtils.getAssetFileAsByteBuffer(path);
        } catch (IOException e) {
            LoggerInterface.loggerFileIO.ERROR("Failed to read font file", e);
        }
        if(fileContent == null){
            return null;
        }

        //load info about the file
        STBTTFontinfo info = STBTTFontinfo.create();
        if(!STBTruetype.stbtt_InitFont(info, fileContent)){
            throw new Error("Failed to init font info!");
        }

        int descent = 0;
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ascentBuff = stack.mallocInt(1);
            IntBuffer descentBuff = stack.mallocInt(1);
            IntBuffer lineGapBuff = stack.mallocInt(1);

            //get data on the font
            STBTruetype.stbtt_GetFontVMetrics(info, ascentBuff, descentBuff, lineGapBuff);

            // int ascent = ascentBuff.get(0);
            descent = descentBuff.get(0);
            // int lineGap = lineGapBuff.get(0);
        }

        try(STBTTPackContext packContext = STBTTPackContext.malloc()){

            //get char data
            STBTTPackedchar.Buffer charData = STBTTPackedchar.malloc(6 * 128);
            ByteBuffer bakedTextureData = BufferUtils.createByteBuffer(TTF_BITMAP_WIDTH * TTF_BITMAP_HEIGHT);
            STBTruetype.stbtt_PackBegin(packContext, bakedTextureData, TTF_BITMAP_WIDTH, TTF_BITMAP_HEIGHT, 0, 1);

            //make image
            for(int i = 0; i < scale.length; i++){
                int p = (i * 3 + 0) * CHAR_COUNT + FIRST_CHAR_TO_BAKE;
                charData.limit(p + 95);
                charData.position(p);
                STBTruetype.stbtt_PackSetOversampling(packContext, 1, 1);
                STBTruetype.stbtt_PackFontRange(packContext, fileContent, 0, scale[i], FIRST_CHAR_TO_BAKE, charData);

                p = (i * 3 + 1) * CHAR_COUNT + FIRST_CHAR_TO_BAKE;
                charData.limit(p + 95);
                charData.position(p);
                STBTruetype.stbtt_PackSetOversampling(packContext, 2, 2);
                STBTruetype.stbtt_PackFontRange(packContext, fileContent, 0, scale[i], FIRST_CHAR_TO_BAKE, charData);

                p = (i * 3 + 2) * CHAR_COUNT + FIRST_CHAR_TO_BAKE;
                charData.limit(p + 95);
                charData.position(p);
                STBTruetype.stbtt_PackSetOversampling(packContext, 3, 1);
                STBTruetype.stbtt_PackFontRange(packContext, fileContent, 0, scale[i], FIRST_CHAR_TO_BAKE, charData);
            }
            charData.clear();
            STBTruetype.stbtt_PackEnd(packContext);

            //create the bitmap image off of the raw texture data
            Texture texture = Texture.createBitmap(openGLState, bakedTextureData, TTF_BITMAP_WIDTH, TTF_BITMAP_HEIGHT);
            uiMat.setDiffuse(texture);

            //parse the glyphs
            try(MemoryStack stack = MemoryStack.stackPush()){
                STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
                FloatBuffer xPos = stack.floats(0.0f);
                FloatBuffer yPos = stack.floats(0.0f);
                for (int i = FIRST_CHAR_TO_BAKE; i < LAST_CHAR_TO_BAKE; i++) {
                    xPos.put(0,0);
                    yPos.put(0,0);
                    if (i == 127) {
                        //skip del character command
                        continue;
                    }
                    STBTruetype.stbtt_GetPackedQuad(charData, TTF_BITMAP_WIDTH, TTF_BITMAP_HEIGHT, i, xPos, yPos, quad, false);
                    // float charX = xPos.get(0);
                    // float charY = yPos.get(0);

                    // float x0 = quad.x0();
                    // float x1 = quad.x1();
                    // float y0 = quad.y0();
                    // float y1 = quad.y1();
                    // float s0 = quad.s0();
                    // float s1 = quad.s1();
                    // float t0 = quad.t0();
                    // float t1 = quad.t1();


                    //create glyph and push it into the array
                    Font.Glyph glyph = new Font.Glyph();
                    glyph.width = (int)((quad.s1() - quad.s0()) * TTF_BITMAP_WIDTH);
                    glyph.height = (int)Math.ceil((quad.t1() - quad.t0()) * TTF_BITMAP_HEIGHT);
                    glyph.startX = (int)(quad.s0() * TTF_BITMAP_WIDTH);
                    glyph.startY = (int)Math.ceil((quad.t0() * TTF_BITMAP_HEIGHT));
                    glyph.offsetY = (int)(TTF_CHAR_HEIGHT + quad.y0()) + (descent / DESCENT_DIVISOR) + MANUAL_OFFSET;
                    glyph.quadScalingY = (quad.y1() - quad.y0()) / (float)TTF_CHAR_HEIGHT;
                    glyph.symbol = (char)i + "";
                    if(i == 32){
                        glyph.width = SPACE_WIDTH;
                    }
                    glyphs.add(glyph);

                    // if(i == 110 || i == 100 || i == 76 || i == 82){
                    //     System.out.println((char)i + "");
                    // }

                }
            }
        }

        //construct final font object and return
        Font rVal = new Font(uiMat,glyphs,TTF_BITMAP_WIDTH,TTF_BITMAP_HEIGHT);
        rVal.process();

        return rVal;
    }
    
}

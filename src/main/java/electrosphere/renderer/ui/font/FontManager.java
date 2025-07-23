package electrosphere.renderer.ui.font;

import java.util.HashMap;
import java.util.Map;

import electrosphere.engine.Globals;

/**
 * Manages all fonts loaded into the engine
 */
public class FontManager {
    
    //the default font
    Font defaultFont;

    //maps names of fonts to font objects
    Map<String,Font> fontMap = new HashMap<String,Font>();


    /**
     * Gets a font based on an identifying string
     * @param identifier the identifying string
     * @return The font if it exists or null
     */
    public Font getFont(String identifier){
        return fontMap.get(identifier);
    }

    /**
     * Loads fonts at engine startup during the init graphics resource phase
     */
    public void loadFonts(){
        // java.awt.Font font = null;
        // try {
        //     font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, FileUtils.getAssetFileAsStream("Fonts/Tuffy_Bold.ttf"));
        //     font = font.deriveFont(24f);
        // } catch (FontFormatException e) {
        //     LoggerInterface.loggerEngine.ERROR("Failed to load a font!", e);
        // } catch (IOException e) {
        //     LoggerInterface.loggerEngine.ERROR("Failed to load a font!", e);
        // }
        // if(font==null){
        //     font = new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 16);
        // }
        // if(font != null){
        //     defaultFont = FontUtils.loadFont(Globals.renderingEngine.getOpenGLState(), font, true);
        //     fontMap.put("default",defaultFont);
        // }
        defaultFont = FontUtils.loadTTF(Globals.renderingEngine.getOpenGLState(), "Fonts/Tuffy_Bold.ttf");
        fontMap.put("default",defaultFont);
    }

}

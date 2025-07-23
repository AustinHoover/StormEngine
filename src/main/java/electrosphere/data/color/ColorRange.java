package electrosphere.data.color;

/**
 * Defines a range of colors
 */
public class ColorRange {
    
    /**
     * The minimum chroma value
     */
    float chromaMin;

    /**
     * The range of allowed chroma values
     */
    float chromaRange;

    /**
     * The minimum hue value
     */
    float hueMin;

    /**
     * The range of allowed hue values
     */
    float hueRange;

    /**
     * The minimum lightness
     */
    float lightMin;

    /**
     * The range of allowed lightness values
     */
    float lightRange;

    public float getChromaMin() {
        return chromaMin;
    }

    public void setChromaMin(float chromaMin) {
        this.chromaMin = chromaMin;
    }

    public float getChromaRange() {
        return chromaRange;
    }

    public void setChromaRange(float chromaRange) {
        this.chromaRange = chromaRange;
    }

    public float getHueMin() {
        return hueMin;
    }

    public void setHueMin(float hueMin) {
        this.hueMin = hueMin;
    }

    public float getHueRange() {
        return hueRange;
    }

    public void setHueRange(float hueRange) {
        this.hueRange = hueRange;
    }

    public float getLightMin() {
        return lightMin;
    }

    public void setLightMin(float lightMin) {
        this.lightMin = lightMin;
    }

    public float getLightRange() {
        return lightRange;
    }

    public void setLightRange(float lightRange) {
        this.lightRange = lightRange;
    }

    

}

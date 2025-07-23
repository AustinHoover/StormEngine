package electrosphere.renderer.ui.elementtypes;

import java.util.List;

/**
 * An element that can contain other elements (label, button, div, etc)
 */
public interface ContainerElement extends Element {

    /**
     * Options for flex directions
     */
    public static enum YogaFlexDirection {
        Column,
        Column_Reverse,
        Row,
        Row_Reverse,
    };

    /**
     * Options for aligning items in containers
     */
    public static enum YogaAlignment {
        Auto,
        Baseline,
        Center,
        End,
        Start,
        Around,
        Between,
        Stretch,
    };

    /**
     * Options for justifying items in containers
     */
    public static enum YogaJustification {
        Center,
        End,
        Start,
        Around,
        Between,
        Evenly,
    }

    /**
     * Different wrap strategies
     */
    public static enum YogaWrap {
        WRAP,
        NO_WRAP,
        REVERSE,
    }

    /**
     * Overflow strategies
     */
    public static enum YogaOverflow {
        Visible,
        Hidden,
        Scroll,
    }

    /**
     * The position type of a yoga node
     */
    public static enum YogaPositionType {
        /**
         * Relative to the nearest positioned ancestor
         */
        Absolute,
        /**
         * Relative to its normal position
         */
        Relative,
        /**
         * Not affected by top/bottom/left/right properties
         */
        Static,
    }

    /**
     * Add a child element to this element
     * @param child The child element
     */
    public void addChild(Element child);

    /**
     * Gets the list of children elements
     * @return The list of child elements
     */
    public List<Element> getChildren();

    /**
     * Removes a child from this element
     * @param child The child
     */
    public void removeChild(Element child);

    /**
     * Clears all children attached to this element
     */
    public void clearChildren();

    //gets the offset applied to all children
    //ie if you scrolled up, how much are the children offset by that
    public int getChildOffsetX();
    public int getChildOffsetY();

    //Gets the scaling applied to all children
    public float getChildScaleX();
    public float getChildScaleY();

    /**
     * Sets the flex layout order of this component
     * @param layout The order
     */
    public void setDirection(int layout);

    /**
     * Sets the flex direction
     * @param layout the flex direction
     */
    public void setFlexDirection(YogaFlexDirection layout);

    /**
     * Sets the content justification of the container
     * @param justification The spacing value
     */
    public void setJustifyContent(YogaJustification justification);

    /**
     * Sets the item alignment
     * @param alignment The alignment style
     */
    public void setAlignItems(YogaAlignment alignment);

    /**
     * Sets the content alignment
     * @param alignment the alignment style
     */
    public void setAlignContent(YogaAlignment alignment);

    /**
     * Sets the wrap strategy
     * @param wrap The wrap strategy
     */
    public void setWrap(YogaWrap wrap);

    /**
     * Sets the overflow strategy
     * @param overflow The overflow strategy
     */
    public void setOverflow(YogaOverflow overflow);
    
}

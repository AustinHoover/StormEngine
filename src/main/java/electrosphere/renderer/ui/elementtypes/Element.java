package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaPositionType;
import electrosphere.renderer.ui.events.Event;

public interface Element {

    /**
     * An uninitialized yoga node
     */
    public static final long UNINITIALIZED_ID = -1;

    /**
     * A yoga element that either hasn't been created or has already been destroyed
     */
    public static final int NULL_YOGA_ELEMENT = -1;

    //
    //dimensions-related
    //

    /**
     * Gets the width of the element
     * @return The width
     */
    public int getWidth();

    /**
     * Gets the height of the element
     * @return The height
     */
    public int getHeight();

    /**
     * Sets the width of the eleement
     * @param width The width
     */
    public void setWidth(int width);

    /**
     * Sets the width as a percentage
     * @param width The percentage
     */
    public void setWidthPercent(float width);

    /**
     * Sets the height of the element
     * @param height The height
     */
    public void setHeight(int height);

    /**
     * Sets the height as a percentage
     * @param height The percentage
     */
    public void setHeightPercent(float height);

    /**
     * Sets the max width of the element
     * @param width The max width
     */
    public void setMaxWidth(int width);

    /**
     * Sets the max width as a percentage
     * @param percent The width as a percentage
     */
    public void setMaxWidthPercent(float percent);

    /**
     * Sets the max height
     * @param height The height
     */
    public void setMaxHeight(int height);

    /**
     * Sets the max height as a percentage
     * @param percent The max height
     */
    public void setMaxHeightPercent(float percent);

    /**
     * Sets the min width
     * @param width The min width
     */
    public void setMinWidth(int width);

    /**
     * Sets the min width as a percentage
     * @param percent The min width as a percentage
     */
    public void setMinWidthPercent(float percent);

    /**
     * Sets the min height
     * @param height The min height
     */
    public void setMinHeight(int height);

    /**
     * Sets the min height s a percentage
     * @param percent The min height as a percentage
     */
    public void setMinHeightPercent(float percent);

    //
    //position
    //

    /**
     * Gets the relative x
     * @return The x position
     */
    public int getRelativeX();

    /**
     * Gets the relative y
     * @return The y position
     */
    public int getRelativeY();

    /**
     * Gets the absolute x
     * @return The absolute x
     */
    public int getAbsoluteX();

    /**
     * Gets the absolute y
     * @return The absolute y
     */
    public int getAbsoluteY();

    /**
     * Sets the x position
     * @param positionX The x position
     */
    public void setPositionX(int positionX);

    /**
     * Sets the y position
     * @param positionY The y position
     */
    public void setPositionY(int positionY);

    /**
     * Sets whether should use absolute position or not
     * @param useAbsolutePosition true to use absolute position, false otherwise
     */
    public void setAbsolutePosition(boolean useAbsolutePosition);

    //
    //parent data
    //

    /**
     * Gets the parent element
     * @return The parent element
     */
    public ContainerElement getParent();

    /**
     * Sets the parent element
     * @param parent THe parent element
     */
    public void setParent(ContainerElement parent);

    //
    //margin
    //

    /**
     * Sets the top margin
     * @param marginTop The top margin
     */
    public void setMarginTop(int marginTop);

    /**
     * Sets the right margin
     * @param marginRight The right margin
     */
    public void setMarginRight(int marginRight);

    /**
     * Sets the bottom margin
     * @param marginBottom The bottom margin
     */
    public void setMarginBottom(int marginBottom);

    /**
     * Sets the left margin
     * @param marginLeft The left margin
     */
    public void setMarginLeft(int marginLeft);

    //
    //padding
    //

    /**
     * Sets the top padding
     * @param paddingTop The top padding
     */
    public void setPaddingTop(int paddingTop);

    /**
     * Sets the right padding
     * @param paddingRight The right padding
     */
    public void setPaddingRight(int paddingRight);

    /**
     * Sets the bottom padding
     * @param paddingBottom The bottom padding
     */
    public void setPaddingBottom(int paddingBottom);

    /**
     * Sets the left padding
     * @param paddingLeft The left padding
     */
    public void setPaddingLeft(int paddingLeft);

    //
    //positioning
    //

    /**
     * Sets the position type
     * @param positionType The position type
     */
    public void setPositionType(YogaPositionType positionType);

    /**
     * Gets the position type
     * @return The position type
     */
    public YogaPositionType getPositionType();

    /**
     * Sets the self alignment
     * @param alignment the alignment style
     */
    public void setAlignSelf(YogaAlignment alignment);


    //
    //Maintenance related
    //
    /**
     * Destroys the element
     */
    public void destroy();


    //
    // Y O G A
    //

    /**
     * Gets the yoga node id
     * @return the yoga node id
     */
    public long getYogaNode();

    /**
     * Applies the yoga values to this component
     */
    public void applyYoga(int parentX, int parentY);


    //
    // E V E N T S
    //
    /**
     * 
     * @param event the even to handle
     * @return true if the event should continue to propagate
     */
    public boolean handleEvent(Event event);

}

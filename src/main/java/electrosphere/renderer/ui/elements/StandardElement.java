package electrosphere.renderer.ui.elements;

import org.lwjgl.util.yoga.Yoga;

import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaPositionType;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.events.Event;

/**
 * An implementation of element
 */
public class StandardElement implements Element {

    //these are set by the 
    private int width = -1;
    private int height = -1;

    
    private int relativeX;
    private int relativeY;
    private int absoluteX;
    private int absoluteY;
    boolean useAbsolutePosition = false;
    
    ContainerElement parent = null;

    public boolean visible = false;

    //the yoga node id
    long yogaNode = -1;

    /**
     * Constructor
     */
    protected StandardElement(){
        this.yogaNode = Yoga.YGNodeNew();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getRelativeX() {
        return relativeX;
    }

    @Override
    public int getRelativeY() {
        return relativeY;
    }

    @Override
    public int getAbsoluteX(){
        return absoluteX;
    }

    @Override
    public int getAbsoluteY(){
        return absoluteY;
    }

    @Override
    public void setAbsolutePosition(boolean useAbsolutePosition){
        if(useAbsolutePosition){
            Yoga.YGNodeStyleSetPositionType(yogaNode, Yoga.YGPositionTypeAbsolute);
        } else {
            Yoga.YGNodeStyleSetPositionType(yogaNode, Yoga.YGPositionTypeRelative);
        }
        this.useAbsolutePosition = useAbsolutePosition;
    }

    @Override
    public void setWidth(int width) {
        Yoga.YGNodeStyleSetWidth(this.yogaNode, width);
    }

    @Override
    public void setWidthPercent(float width) {
        Yoga.YGNodeStyleSetWidthPercent(yogaNode, width);
    }

    @Override
    public void setHeight(int height) {
        Yoga.YGNodeStyleSetHeight(this.yogaNode, height);
    }

    @Override
    public void setHeightPercent(float height) {
        Yoga.YGNodeStyleSetHeightPercent(yogaNode, height);
    }

    @Override
    public void setPositionX(int posX) {
        Yoga.YGNodeStyleSetPosition(this.yogaNode, Yoga.YGEdgeLeft, posX);
    }

    @Override
    public void setPositionY(int posY) {
        Yoga.YGNodeStyleSetPosition(this.yogaNode, Yoga.YGEdgeTop, posY);
    }

    public void setMarginTop(int marginTop){
        Yoga.YGNodeStyleSetMargin(this.yogaNode, Yoga.YGEdgeTop, marginTop);
    }

    public void setMarginRight(int marginRight){
        Yoga.YGNodeStyleSetMargin(this.yogaNode, Yoga.YGEdgeRight, marginRight);
    }

    public void setMarginBottom(int marginBottom){
        Yoga.YGNodeStyleSetMargin(this.yogaNode, Yoga.YGEdgeBottom, marginBottom);
    }

    public void setMarginLeft(int marginLeft){
        Yoga.YGNodeStyleSetMargin(this.yogaNode, Yoga.YGEdgeLeft, marginLeft);
    }

    public ContainerElement getParent(){
        return this.parent;
    }

    public void setParent(ContainerElement parent){
        this.parent = parent;
    }

    @Override
    public void destroy(){
        if(this.yogaNode != Element.NULL_YOGA_ELEMENT){
            Yoga.YGNodeFree(this.yogaNode);
            this.yogaNode = Element.NULL_YOGA_ELEMENT;
        }
    }

    @Override
    public long getYogaNode() {
        return yogaNode;
    }

    @Override
    public void applyYoga(int parentX, int parentY) {
        if(this.yogaNode != Element.NULL_YOGA_ELEMENT){
            //get the values from yoga
            float leftRaw = Yoga.YGNodeLayoutGetLeft(yogaNode);
            float topRaw = Yoga.YGNodeLayoutGetTop(yogaNode);
            float widthRaw = Yoga.YGNodeLayoutGetWidth(yogaNode);
            float heightRaw = Yoga.YGNodeLayoutGetHeight(yogaNode);
            //apply the values to this component
            this.relativeX = (int)leftRaw;
            this.relativeY = (int)topRaw;
            this.width = (int)widthRaw;
            this.height = (int)heightRaw;
            //calculate absolute values
            this.absoluteX = parentX + this.relativeX;
            this.absoluteY = parentY + this.relativeY;
        }
    }





    @Override
    public boolean handleEvent(Event event) {
        boolean propagate = true;
        return propagate;
    }

    @Override
    public void setMaxWidth(int width) {
        Yoga.YGNodeStyleSetMaxWidth(yogaNode, width);
    }

    @Override
    public void setMaxWidthPercent(float percent) {
        Yoga.YGNodeStyleSetMaxWidthPercent(yogaNode, percent);
    }

    @Override
    public void setMaxHeight(int height) {
        Yoga.YGNodeStyleSetMaxHeight(yogaNode, height);
    }

    @Override
    public void setMaxHeightPercent(float percent) {
        Yoga.YGNodeStyleSetMaxHeight(yogaNode, percent);
    }

    @Override
    public void setMinWidth(int width) {
        Yoga.YGNodeStyleSetMinWidth(yogaNode, width);
    }

    @Override
    public void setMinWidthPercent(float percent) {
        Yoga.YGNodeStyleSetMinWidthPercent(yogaNode, percent);
    }

    @Override
    public void setMinHeight(int height) {
        Yoga.YGNodeStyleSetMinHeight(yogaNode, height);
    }

    @Override
    public void setMinHeightPercent(float percent) {
        Yoga.YGNodeStyleSetMinHeightPercent(yogaNode, percent);
    }

    /**
     * Converts an absolute (to the screen) position to a position within a framebuffer
     * @param absolutePos The absolute position
     * @param framebufferPos The position of the framebuffer on the screen
     * @return The position within the framebuffer
     */
    public int absoluteToFramebuffer(int absolutePos, int framebufferPos){
        return absolutePos - framebufferPos;
    }

    @Override
    public void setAlignSelf(YogaAlignment alignment){
        int alignmentInteger = Yoga.YGAlignAuto;
        switch(alignment){
            case Auto:
                alignmentInteger = Yoga.YGAlignAuto;
                break;
            case Start:
                alignmentInteger = Yoga.YGAlignFlexStart;
                break;
            case End:
                alignmentInteger = Yoga.YGAlignFlexEnd;
                break;
            case Around:
                alignmentInteger = Yoga.YGAlignSpaceAround;
                break;
            case Between:
                alignmentInteger = Yoga.YGAlignSpaceBetween;
                break;
            case Stretch:
                alignmentInteger = Yoga.YGAlignStretch;
                break;
            case Baseline:
                alignmentInteger = Yoga.YGAlignBaseline;
                break;
            case Center:
                alignmentInteger = Yoga.YGAlignCenter;
                break;
        }
        Yoga.YGNodeStyleSetAlignSelf(this.yogaNode, alignmentInteger);
    }

    /**
     * The value of the grow property
     * @param grow The grow value
     */
    public void setFlexGrow(float grow){
        Yoga.YGNodeStyleSetFlexGrow(yogaNode, grow);
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean draw) {
        this.visible = draw;
    }

    public void setDisplay(int value){
        Yoga.YGNodeStyleSetDisplay(this.yogaNode, value);
    }

    @Override
    public void setPaddingTop(int paddingTop) {
        Yoga.YGNodeStyleSetPadding(this.yogaNode, Yoga.YGEdgeTop, paddingTop);
    }

    @Override
    public void setPaddingRight(int paddingRight) {
        Yoga.YGNodeStyleSetPadding(this.yogaNode, Yoga.YGEdgeRight, paddingRight);
    }

    @Override
    public void setPaddingBottom(int paddingBottom) {
        Yoga.YGNodeStyleSetPadding(this.yogaNode, Yoga.YGEdgeBottom, paddingBottom);
    }

    @Override
    public void setPaddingLeft(int paddingLeft) {
        Yoga.YGNodeStyleSetPadding(this.yogaNode, Yoga.YGEdgeLeft, paddingLeft);
    }

    @Override
    public void setPositionType(YogaPositionType positionType) {
        switch(positionType){
            case Absolute:{
                Yoga.YGNodeStyleSetPositionType(yogaNode, Yoga.YGPositionTypeAbsolute);
            } break;
            case Relative: {
                Yoga.YGNodeStyleSetPositionType(yogaNode, Yoga.YGPositionTypeRelative);
            } break;
            case Static: {
                Yoga.YGNodeStyleSetPositionType(yogaNode, Yoga.YGPositionTypeStatic);
            } break;
        }
    }

    @Override
    public YogaPositionType getPositionType() {
        int type = Yoga.YGNodeStyleGetPositionType(yogaNode);
        switch(type){
            case Yoga.YGPositionTypeAbsolute: {
                return YogaPositionType.Absolute;
            }
            case Yoga.YGPositionTypeRelative: {
                return YogaPositionType.Relative;
            }
            case Yoga.YGPositionTypeStatic: {
                return YogaPositionType.Static;
            }
            default: {
                throw new Error("Unsupported position type! " + type);
            }
        }
    }

}

package electrosphere.renderer.ui.elements;

import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL40;
import org.lwjgl.util.yoga.Yoga;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.framebuffer.FramebufferUtils;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.texture.Texture;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.NavigableElement;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.NavigationEvent;
import electrosphere.renderer.ui.frame.UIFrameUtils;

/**
 * A window
 */
public class Window implements DrawableElement, ContainerElement, NavigableElement {

    /**
     * The color of the window
     */
    Vector4f color = new Vector4f(0.5f);

    /**
     * The child elements of this window
     */
    List<Element> childList = new LinkedList<Element>();

    /**
     * The buffer to draw the contents of the window to
     */
    Framebuffer widgetBuffer;

    /**
     * The material for the window
     */
    Material customMat = new Material();

    /**
     * The position of buffer texture within the render call for the window itself
     */
    Vector3f texPosition = new Vector3f(0,0,0);

    /**
     * The scale of buffer texture within the render call for the window itself
     */
    Vector3f texScale = new Vector3f(1,1,0);

    /**
     * The navigation callback for the window
     */
    NavigationEventCallback navCallback;

    /**
     * Default width of popups
     */
    static final int DEFAULT_POPUP_WIDTH = 1000;

    /**
     * Default height of popups
     */
    static final int DEFAULT_POPUP_HEIGHT = 1000;

    /**
     * Controls whether the decorations for the window draw or not
     */
    boolean showDecorations = true;

    /**
     * Margin size for decorations
     */
    static final int DECORATION_MARGIN = 25;

    /**
     * Yoga node for controlling placement of the window on the screen
     * IE, if you want to place a window in the upper right hand side of the screen,
     * this node can be used to control placement alignment to accomplish that
     * NOTE: It should always be set to the current size of the window (width, height)
     * NOTE: It is updated every time the applyYoga function is called
     */
    long parentWindowYogaNode = Element.UNINITIALIZED_ID;

    /**
     * The frame decoration texture path
     */
    String frameDecoration = AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_1;

    public int width = 1;
    public int height = 1;
    
    public int absoluteX = 0;
    public int absoluteY = 0;
    
    public boolean visible = false;

    //the yoga node id
    long yogaNode = Element.UNINITIALIZED_ID;

    /**
     * Constructor
     * @param showDecorations
     * @param positionX
     * @param positionY
     * @param width
     * @param height
     */
    @Deprecated
    public Window(OpenGLState openGLState, int positionX, int positionY, int width, int height, boolean showDecorations){
        //yoga node for the actually visible part
        this.yogaNode = Yoga.YGNodeNew();
        //yoga node for placement
        this.parentWindowYogaNode = Yoga.YGNodeNew();
        Yoga.YGNodeInsertChild(this.parentWindowYogaNode, this.yogaNode, 0);
        this.setParentAlignContent(YogaAlignment.Start);
        this.setParentAlignItem(YogaAlignment.Start);
        this.setParentJustifyContent(YogaJustification.Start);
        this.setFlexDirection(YogaFlexDirection.Column);
        this.setWidth(width);
        this.setHeight(height);
        this.setShowDecorations(showDecorations);
        this.reallocateBuffer(width, height);
    }

    /**
     * Private constructor
     * @param openGLState
     */
    private Window(OpenGLState openGLState){
        //yoga node for the actually visible part
        this.yogaNode = Yoga.YGNodeNew();
        //yoga node for placement
        this.parentWindowYogaNode = Yoga.YGNodeNew();
        Yoga.YGNodeInsertChild(this.parentWindowYogaNode, this.yogaNode, 0);
        this.setParentAlignContent(YogaAlignment.Start);
        this.setParentAlignItem(YogaAlignment.Start);
        this.setParentJustifyContent(YogaJustification.Start);
        this.setFlexDirection(YogaFlexDirection.Column);
        this.setMinWidth(DEFAULT_POPUP_WIDTH);
        this.setMinHeight(DEFAULT_POPUP_HEIGHT);
        this.setShowDecorations(showDecorations);
        this.reallocateBuffer(DEFAULT_POPUP_WIDTH, DEFAULT_POPUP_HEIGHT);
    }

    /**
     * Creates a window
     * @param openGLState The opengl state
     * @param posX The x position of the window
     * @param posY The y position of the window
     * @param width The width of the window
     * @param height The height of the window
     * @param showDecorations true to show window decorations, false otherwise
     * @return The window element
     */
    public static Window create(OpenGLState openGLState, int posX, int posY, int width, int height, boolean showDecorations){
        Window rVal = new Window(openGLState, posX, posY, width, height, showDecorations);
        return rVal;
    }

    /**
     * Creates an expandable window
     * @param openGLState The opengl state
     * @return The window element
     */
    public static Window createExpandable(OpenGLState openGLState){
        Window rVal = new Window(openGLState);
        return rVal;
    }

    /**
     * Creates an expandable window
     * @param openGLState The opengl state
     * @return The window element
     */
    public static Window createExpandableCenterAligned(OpenGLState openGLState){
        Window rVal = new Window(openGLState);
        rVal.setParentAlignItem(YogaAlignment.Center);
        rVal.setParentJustifyContent(YogaJustification.Center);
        return rVal;
    }

    /**
     * Creates an expandable window
     * @param openGLState The opengl state
     * @param showDecorations true to show decorations for window, false to not draw them
     * @return The window element
     */
    public static Window createExpandableCenterAligned(OpenGLState openGLState, boolean showDecorations){
        Window rVal = new Window(openGLState);
        rVal.setParentAlignItem(YogaAlignment.Center);
        rVal.setParentJustifyContent(YogaJustification.Center);
        rVal.showDecorations = showDecorations;
        return rVal;
    }

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
    ) {
        int absoluteX = this.getAbsoluteX();
        int absoluteY = this.getAbsoluteY();
        
        widgetBuffer.bind(openGLState);
        openGLState.glViewport(width, height);
        
        GL40.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);

        //grab assets required to render window
        Model planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);
        Texture windowFrame = Globals.assetManager.fetchTexture(AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_1);
        
        for(Element child : childList){
            if(child instanceof DrawableElement){
                DrawableElement drawableChild = (DrawableElement) child;
                drawableChild.draw(renderPipelineState,openGLState,widgetBuffer,absoluteX,absoluteY);
            }
        }


        //
        //Actually draw to screen
        framebuffer.bind(openGLState);
        openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());

        //error if assets are null
        if(planeModel == null || windowFrame == null){
            LoggerInterface.loggerRenderer.ERROR("Window unable to find plane model or window frame!!", new Exception());
        }

        //render background of window
        if(planeModel != null && windowFrame != null && this.showDecorations){
            UIFrameUtils.drawFrame(
                openGLState,
                AssetDataStrings.UI_FRAME_TEXTURE_DEFAULT_3, color, 48, 12,
                this.getAbsoluteX(), this.getAbsoluteY(), this.getWidth(), this.getHeight(), 
                framebuffer, framebufferPosX, framebufferPosY
            );
        }
        
        //render content of window
        float ndcWidth =  (float)this.widgetBuffer.getWidth()/framebuffer.getWidth();
        float ndcHeight = (float)this.widgetBuffer.getHeight()/framebuffer.getHeight();
        float ndcX =      (float)this.absoluteToFramebuffer(absoluteX,framebufferPosX)/framebuffer.getWidth();
        float ndcY =      (float)this.absoluteToFramebuffer(absoluteY,framebufferPosY)/framebuffer.getHeight();
        Vector3f boxPosition = new Vector3f(ndcX,ndcY,0);
        Vector3f boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);
        if(planeModel != null){
            planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
            planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
            planeModel.pushUniformToMesh("plane", "tPosition", texPosition);
            planeModel.pushUniformToMesh("plane", "tDimension", texScale);
            planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", new Vector4f(1.0f));
            customMat.setDiffuse(widgetBuffer.getTexture());
            planeModel.getMeshes().get(0).setMaterial(customMat);
            planeModel.drawUI();
        }
    }

    /**
     * Destroys the element
     */
    public void destroy(){
        this.yogaNode = Element.NULL_YOGA_ELEMENT;
        for(Element el : this.getChildren()){
            Globals.engineState.signalSystem.post(SignalType.YOGA_DESTROY, el);
        }
        this.clearChildren();
        if(this.yogaNode != Element.NULL_YOGA_ELEMENT){
            Yoga.YGNodeFree(this.yogaNode);
        }
    }

    /**
     * clears all children
     */
    public void clear(){
        for(Element el : this.getChildren()){
            Globals.engineState.signalSystem.post(SignalType.YOGA_DESTROY, el);
        }
        this.clearChildren();
    }

    /**
     * Sets the show decoration value
     * @param decoration The show decoration value
     */
    private void setShowDecorations(boolean decoration){
        this.showDecorations = decoration;
    }

    /**
     * Reallocates the render buffer
     */
    private void reallocateBuffer(int width, int height){
        int finalWidth = width - this.getPaddingLeft() - this.getPaddingRight();
        int finalHeight = height - this.getPaddingTop() - this.getPaddingBottom();
        try {
            if(this.widgetBuffer != null){
                widgetBuffer.free();
            }
            widgetBuffer = FramebufferUtils.generateTextureFramebuffer(Globals.renderingEngine.getOpenGLState(), finalWidth, finalHeight);
        } catch(Exception e){
            LoggerInterface.loggerRenderer.ERROR(e);
        }
        customMat.setDiffuse(widgetBuffer.getTexture());
    }

    @Override
    public void setWidth(int width){
        this.width = width;
        Yoga.YGNodeStyleSetWidth(this.yogaNode, width);
    }

    @Override
    public void setWidthPercent(float width) {
        Yoga.YGNodeStyleSetWidthPercent(yogaNode, width);
    }
    
    @Override
    public void setHeight(int height){
        this.height = height;
        Yoga.YGNodeStyleSetHeight(this.yogaNode, height);
    }

    @Override
    public void setHeightPercent(float height) {
        Yoga.YGNodeStyleSetHeightPercent(yogaNode, height);
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean draw) {
        this.visible = draw;
    }

    public int getMarginTop(){
        return (int)Yoga.YGNodeLayoutGetMargin(this.yogaNode, Yoga.YGEdgeTop);
    }

    public int getMarginRight(){
        return (int)Yoga.YGNodeLayoutGetMargin(this.yogaNode, Yoga.YGEdgeRight);
    }

    public int getMarginBottom(){
        return (int)Yoga.YGNodeLayoutGetMargin(this.yogaNode, Yoga.YGEdgeBottom);
    }

    public int getMarginLeft(){
        return (int)Yoga.YGNodeLayoutGetMargin(this.yogaNode, Yoga.YGEdgeLeft);
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

    @Override
    public long getYogaNode() {
        return yogaNode;
    }

    @Override
    public void applyYoga(int parentX, int parentY) {
        if(this.yogaNode != Element.NULL_YOGA_ELEMENT && parentWindowYogaNode != Element.NULL_YOGA_ELEMENT){
            if(
                Globals.WINDOW_WIDTH <= 0 ||
                Globals.WINDOW_HEIGHT <= 0 ||
                width <= 0 ||
                height <= 0
            ){
                String message = "Window has invalid dimensions!\n" +
                "Globals.WINDOW_WIDTH: " + Globals.WINDOW_WIDTH + "\n" +
                "Globals.WINDOW_HEIGHT: " + Globals.WINDOW_HEIGHT + "\n" +
                "width: " + width + "\n" +
                "height: " + height + "\n"
                ;
                throw new IllegalStateException(message);
            }
            Yoga.YGNodeStyleSetWidth(parentWindowYogaNode, Globals.WINDOW_WIDTH);
            Yoga.YGNodeStyleSetHeight(parentWindowYogaNode, Globals.WINDOW_HEIGHT);
            //calculate yoga layout
            Yoga.YGNodeCalculateLayout(parentWindowYogaNode, width, height, Yoga.YGDirectionInherit);
            //get the values from yoga
            float leftRaw = Yoga.YGNodeLayoutGetLeft(yogaNode);
            float topRaw = Yoga.YGNodeLayoutGetTop(yogaNode);
            float widthRaw = Yoga.YGNodeLayoutGetWidth(yogaNode);
            float heightRaw = Yoga.YGNodeLayoutGetHeight(yogaNode);
            if(this.width != (int)widthRaw || this.height != (int)heightRaw){
                this.reallocateBuffer((int)widthRaw,(int)heightRaw);
            }
            //apply the values to this component
            this.absoluteX = (int)leftRaw;
            this.absoluteY = (int)topRaw;
            this.width = (int)widthRaw;
            this.height = (int)heightRaw;
            //apply yoga values to all children
            LoggerInterface.loggerUI.DEBUG("==Apply yoga to windoow==");
            for(Element child : this.getChildren()){
                child.applyYoga(this.absoluteX,this.absoluteY);
            }
        }
    }

    @Override
    public void setDirection(int layout) {
        Yoga.YGNodeStyleSetDirection(yogaNode, Yoga.YGFlexDirectionColumn);
    }

    @Override
    public void setFlexDirection(YogaFlexDirection layout){
        int directionInteger = Yoga.YGFlexDirectionColumn;
        switch(layout){
            case Column:
                directionInteger = Yoga.YGFlexDirectionColumn;
                break;
            case Column_Reverse:
                directionInteger = Yoga.YGFlexDirectionColumnReverse;
                break;
            case Row:
                directionInteger = Yoga.YGFlexDirectionRow;
                break;
            case Row_Reverse:
                directionInteger = Yoga.YGFlexDirectionRowReverse;
                break;
        }
        Yoga.YGNodeStyleSetFlexDirection(yogaNode, directionInteger);
    }

    @Override
    public void setJustifyContent(YogaJustification justification){
        int justificationInteger = Yoga.YGJustifyFlexStart;
        switch(justification){
            case Center:
                justificationInteger = Yoga.YGJustifyCenter;
                break;
            case Start:
                justificationInteger = Yoga.YGJustifyFlexStart;
                break;
            case End:
                justificationInteger = Yoga.YGJustifyFlexEnd;
                break;
            case Around:
                justificationInteger = Yoga.YGJustifySpaceAround;
                break;
            case Between:
                justificationInteger = Yoga.YGJustifySpaceBetween;
                break;
            case Evenly:
                justificationInteger = Yoga.YGJustifySpaceEvenly;
                break;
        }
        Yoga.YGNodeStyleSetJustifyContent(this.yogaNode, justificationInteger);
    }

    @Override
    public void setAlignItems(YogaAlignment alignment){
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
        Yoga.YGNodeStyleSetAlignItems(this.yogaNode, alignmentInteger);
    }

    @Override
    public void setAlignContent(YogaAlignment alignment){
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
        Yoga.YGNodeStyleSetAlignContent(this.yogaNode, alignmentInteger);
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

    @Override
    public void addChild(Element child) {
        if(child.getParent() != null){
            throw new Error("Child has a parent!");
        }
        if(parentWindowYogaNode == Element.UNINITIALIZED_ID){
            throw new Error("parent id undefined! " + parentWindowYogaNode);
        }
        if(yogaNode == Element.UNINITIALIZED_ID){
            throw new Error("window yoga id undefined! " + yogaNode);
        }
        childList.add(child);
        child.setParent(this);
        if(child instanceof DrawableElement){
            DrawableElement drawableChild = (DrawableElement) child;
            drawableChild.setVisible(false);
            if(drawableChild.getYogaNode() == Element.UNINITIALIZED_ID){
                throw new Error("Drawable child is uninitialized!");
            }
            Yoga.YGNodeInsertChild(yogaNode, drawableChild.getYogaNode(), childList.size() - 1);
        }
    }

    @Override
    public List<Element> getChildren() {
        return childList;
    }

    @Override
    public void removeChild(Element child) {
        if(childList.contains(child)){
            Yoga.YGNodeRemoveChild(yogaNode, child.getYogaNode());
        }
        childList.remove(child);
        child.setParent(null);
    }

    @Override
    public void clearChildren(){
        Yoga.YGNodeRemoveAllChildren(yogaNode);
        childList.clear();
    }

    public boolean handleEvent(Event event){
        boolean propagate = true;
        if(event instanceof NavigationEvent && navCallback != null){
            if(!navCallback.execute((NavigationEvent)event)){
                propagate = false;
            }
        }
        return propagate;
    }

    @Override
    public void setOnNavigationCallback(NavigationEventCallback callback) {
        navCallback = callback;
    }

    public ContainerElement getParent(){
        return null;
    }

    @Override
    public int getChildOffsetX(){
        return 0;
    }

    @Override
    public int getChildOffsetY(){
        return 0;
    }

    @Override
    public float getChildScaleX(){
        return 1;
    }

    @Override
    public float getChildScaleY(){
        return 1;
    }

    /**
     * Sets the alignment of items on the parent to the window yoga node
     * @param alignment The alignment value
     */
    public void setParentAlignItem(YogaAlignment alignment){
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
        Yoga.YGNodeStyleSetAlignItems(this.parentWindowYogaNode, alignmentInteger);
    }

    /**
     * Sets the alignment of content on the parent to the window yoga node
     * @param alignment The alignment
     */
    public void setParentAlignContent(YogaAlignment alignment){
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
        Yoga.YGNodeStyleSetAlignContent(this.parentWindowYogaNode, alignmentInteger);
    }

    /**
     * Sets the justification of the parent yoga node containing this window
     * @param justification The justification mode
     */
    public void setParentJustifyContent(YogaJustification justification){
        int justificationInteger = Yoga.YGJustifyFlexStart;
        switch(justification){
            case Center:
                justificationInteger = Yoga.YGJustifyCenter;
                break;
            case Start:
                justificationInteger = Yoga.YGJustifyFlexStart;
                break;
            case End:
                justificationInteger = Yoga.YGJustifyFlexEnd;
                break;
            case Around:
                justificationInteger = Yoga.YGJustifySpaceAround;
                break;
            case Between:
                justificationInteger = Yoga.YGJustifySpaceBetween;
                break;
            case Evenly:
                justificationInteger = Yoga.YGJustifySpaceEvenly;
                break;
        }
        Yoga.YGNodeStyleSetJustifyContent(this.parentWindowYogaNode, justificationInteger);
    }

    @Override
    public void setWrap(YogaWrap wrap) {
        switch(wrap){
            case WRAP:
                Yoga.YGNodeStyleSetFlexWrap(yogaNode, Yoga.YGWrapWrap);
                break;
            case NO_WRAP:
                Yoga.YGNodeStyleSetFlexWrap(yogaNode, Yoga.YGWrapNoWrap);
                break;
            case REVERSE:
                Yoga.YGNodeStyleSetFlexWrap(yogaNode, Yoga.YGWrapReverse);
                break;
        }
    }

    @Override
    public void setOverflow(YogaOverflow overflow){
        switch(overflow){
            case Visible:
                Yoga.YGNodeStyleSetOverflow(yogaNode, Yoga.YGOverflowVisible);
                break;
            case Hidden:
                Yoga.YGNodeStyleSetOverflow(yogaNode, Yoga.YGOverflowHidden);
                break;
            case Scroll:
                Yoga.YGNodeStyleSetOverflow(yogaNode, Yoga.YGOverflowScroll);
                break;
        }
    }

    @Override
    public void setAbsolutePosition(boolean useAbsolutePosition) {
        //not implemented
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParent(ContainerElement parent) {
        //not implemented
        throw new UnsupportedOperationException();
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
    public int getRelativeX() {
        return absoluteX;
    }

    @Override
    public int getRelativeY() {
        return absoluteY;
    }

    @Override
    public int getAbsoluteX() {
        return absoluteX;
    }

    @Override
    public int getAbsoluteY() {
        return absoluteY;
    }

    @Override
    public void setPositionX(int positionX) {
        Yoga.YGNodeStyleSetPosition(this.yogaNode, Yoga.YGEdgeLeft, positionX);
    }

    @Override
    public void setPositionY(int positionY) {
        Yoga.YGNodeStyleSetPosition(this.yogaNode, Yoga.YGEdgeTop, positionY);
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

    public int getPaddingTop(){
        return (int)Yoga.YGNodeLayoutGetPadding(this.yogaNode, Yoga.YGEdgeTop);
    }

    public int getPaddingRight(){
        return (int)Yoga.YGNodeLayoutGetPadding(this.yogaNode, Yoga.YGEdgeRight);
    }

    public int getPaddingBottom(){
        return (int)Yoga.YGNodeLayoutGetPadding(this.yogaNode, Yoga.YGEdgeBottom);
    }

    public int getPaddingLeft(){
        return (int)Yoga.YGNodeLayoutGetPadding(this.yogaNode, Yoga.YGEdgeLeft);
    }

    /**
     * Gets the frame decoration texture path
     * @return The frame decoration texture path
     */
    public String getFrameDecoration() {
        return frameDecoration;
    }

    /**
     * Sets the frame decoration texture path
     * @param frameDecoration The frame decoration texture path
     */
    public void setFrameDecoration(String frameDecoration) {
        this.frameDecoration = frameDecoration;
    }

    /**
     * Gets the color of the window decorations
     * @return The color of the decorations
     */
    public Vector4f getColor() {
        return color;
    }

    /**
     * Sets the color of the window decorations
     * @param color The color of the decorations
     */
    public void setColor(Vector4f color) {
        this.color = color;
    }

    @Override
    public void setPositionType(YogaPositionType positionType) {
        throw new UnsupportedOperationException("Unimplemented method 'setPositionType'");
    }

    @Override
    public YogaPositionType getPositionType() {
        return YogaPositionType.Absolute;
    }

    

}

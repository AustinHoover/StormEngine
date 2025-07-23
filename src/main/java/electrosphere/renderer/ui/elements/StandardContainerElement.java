package electrosphere.renderer.ui.elements;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.yoga.Yoga;

import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * An element that contains other elements
 */
public class StandardContainerElement extends StandardElement implements ContainerElement {

    List<Element> childList = new LinkedList<Element>();

    public StandardContainerElement(){
        super();
    }

    @Override
    public void setDirection(int layout) {
        Yoga.YGNodeStyleSetDirection(yogaNode, layout);
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
    public void addChild(Element child) {
        if(child.getParent() != null){
            String message = "Tried adding a child to a that already has a parent!\n" +
            "this: " + this + "\n" +
            "child: " + child + "\n" + 
            "child parent: " + child.getParent() + "\n";
            throw new Error(message);
        }
        childList.add(child);
        child.setParent(this);
        if(child instanceof DrawableElement){
            DrawableElement drawableChild = (DrawableElement) child;
            drawableChild.setVisible(true);
            Yoga.YGNodeInsertChild(yogaNode, drawableChild.getYogaNode(), childList.size() - 1);
        }
    }

    @Override
    public List<Element> getChildren() {
        return childList;
    }

    @Override
    public void removeChild(Element child) {
        childList.remove(child);
        child.setParent(null);
        Yoga.YGNodeRemoveChild(this.yogaNode, child.getYogaNode());
    }

    @Override
    public void clearChildren(){
        Yoga.YGNodeRemoveAllChildren(yogaNode);
        childList.clear();
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

    @Override
    public void destroy(){
        for(Element child : childList){
            Globals.engineState.signalSystem.post(SignalType.YOGA_DESTROY, child);
        }
        if(this.yogaNode != Element.NULL_YOGA_ELEMENT){
            Yoga.YGNodeFree(this.yogaNode);
            this.yogaNode = Element.NULL_YOGA_ELEMENT;
        }
    }

    @Override
    public void applyYoga(int parentX, int parentY) {
        if(this.yogaNode != Element.NULL_YOGA_ELEMENT){
            //get the values from yoga
            super.applyYoga(parentX, parentY);
            //apply yoga values to all children
            for(Element child : this.getChildren()){
                child.applyYoga(this.getAbsoluteX(),this.getAbsoluteY());
            }
        }
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
    
    

    
}

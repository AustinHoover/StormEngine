package electrosphere.client.ui.parsing;

import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaOverflow;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaPositionType;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaWrap;

/**
 * Contains the styling data for an element
 */
public class ElementStyling {
    
    /**
     * Width in pixels
     */
    Integer width;

    /**
     * Height in pixels
     */
    Integer height;

    /**
     * Margin
     */
    Integer marginTop;
    Integer marginBottom;
    Integer marginLeft;
    Integer marginRight;


    /**
     * Padding
     */
    Integer paddingTop;
    Integer paddingRight;
    Integer paddingBottom;
    Integer paddingLeft;

    /**
     * Position type (static, relative, absolute)
     */
    YogaPositionType position;


    /**
     * Alignment self type
     */
    YogaAlignment alignSelf;


    /**
     * Flex direction
     */
    YogaFlexDirection flexDirection;

    /**
     * Justification
     */
    YogaJustification justification;

    /**
     * Item alignment
     */
    YogaAlignment alignItems;

    /**
     * Content alignment
     */
    YogaAlignment alignContent;

    /**
     * Wrap behavior
     */
    YogaWrap wrap;

    /**
     * Overflow behavior
     */
    YogaOverflow overflow;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(Integer marginTop) {
        this.marginTop = marginTop;
    }

    public Integer getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(Integer marginBottom) {
        this.marginBottom = marginBottom;
    }

    public Integer getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(Integer marginLeft) {
        this.marginLeft = marginLeft;
    }

    public Integer getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(Integer marginRight) {
        this.marginRight = marginRight;
    }

    public Integer getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(Integer paddingTop) {
        this.paddingTop = paddingTop;
    }

    public Integer getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(Integer paddingRight) {
        this.paddingRight = paddingRight;
    }

    public Integer getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(Integer paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public Integer getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(Integer paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public YogaPositionType getPosition() {
        return position;
    }

    public void setPosition(YogaPositionType position) {
        this.position = position;
    }

    public YogaFlexDirection getFlexDirection() {
        return flexDirection;
    }

    public void setFlexDirection(YogaFlexDirection flexDirection) {
        this.flexDirection = flexDirection;
    }

    public YogaJustification getJustification() {
        return justification;
    }

    public void setJustification(YogaJustification justification) {
        this.justification = justification;
    }

    public YogaAlignment getAlignItems() {
        return alignItems;
    }

    public void setAlignItems(YogaAlignment alignItems) {
        this.alignItems = alignItems;
    }

    public YogaWrap getWrap() {
        return wrap;
    }

    public void setWrap(YogaWrap wrap) {
        this.wrap = wrap;
    }

    public YogaOverflow getOverflow() {
        return overflow;
    }

    public void setOverflow(YogaOverflow overflow) {
        this.overflow = overflow;
    }

    public YogaAlignment getAlignSelf() {
        return alignSelf;
    }

    public void setAlignSelf(YogaAlignment alignSelf) {
        this.alignSelf = alignSelf;
    }

    public YogaAlignment getAlignContent() {
        return alignContent;
    }

    public void setAlignContent(YogaAlignment alignContent) {
        this.alignContent = alignContent;
    }


    


}

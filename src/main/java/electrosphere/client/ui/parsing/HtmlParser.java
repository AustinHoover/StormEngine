package electrosphere.client.ui.parsing;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.nodes.Node;

import electrosphere.client.script.ClientScriptUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.ui.elements.Button;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.renderer.ui.elements.Label;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * Converts HTML to engine elements
 */
public class HtmlParser {

    /**
     * Parses a jsoup node into in-engine ui elements
     * @param jsoupNode The jsoup node
     * @return The in-engine ui stack
     */
    public static Element parseJSoup(Node jsoupNode){
        StyleAccumulator styleAccumulator = HtmlParser.parseStyles(jsoupNode);
        Element rVal = HtmlParser.recursivelyParseChildren(jsoupNode, styleAccumulator);
        return rVal;
    }
    
    /**
     * Recursively parses a jsoup node into engine ui elements
     * @param jsoupNode The jsoup node
     * @return The engine ui elements
     */
    private static Element recursivelyParseChildren(Node jsoupNode, StyleAccumulator styleAccumulator){
        String tag = jsoupNode.nodeName();

        Element rVal = null;
        switch(tag){
            case "p": {
                rVal = Div.createDiv();
                for(Node child : jsoupNode.childNodes()){
                    Element childEl = HtmlParser.recursivelyParseChildren(child, styleAccumulator);
                    if(childEl != null){
                        ((Div)rVal).addChild(childEl);
                    }
                }
            } break;
            case "#text": {
                String content = jsoupNode.outerHtml();
                rVal = Label.createLabel(content);
            } break;
            case "div": {
                rVal = Div.createDiv();
                for(Node child : jsoupNode.childNodes()){
                    Element childEl = HtmlParser.recursivelyParseChildren(child, styleAccumulator);
                    if(childEl != null){
                        ((Div)rVal).addChild(childEl);
                    }
                }
            } break;
            case "body": {
                rVal = Div.createDiv();
                for(Node child : jsoupNode.childNodes()){
                    Element childEl = HtmlParser.recursivelyParseChildren(child, styleAccumulator);
                    if(childEl != null){
                        ((Div)rVal).addChild(childEl);
                    }
                }
            } break;
            case "button": {
                String onClick = jsoupNode.attr("onclick");
                Runnable callback = null;
                if(onClick == null){
                    LoggerInterface.loggerUI.WARNING("Button with undefined onclick " + jsoupNode);
                    callback = () -> {};
                } else {
                    callback = () -> {
                        ClientScriptUtils.eval(onClick);
                    };
                }
                rVal = Button.createEmptyButton(callback);
                for(Node child : jsoupNode.childNodes()){
                    Element childEl = HtmlParser.recursivelyParseChildren(child, styleAccumulator);
                    if(childEl != null){
                        ((Button)rVal).addChild(childEl);
                    }
                }
            } break;
            case "script": {
                LoggerInterface.loggerUI.WARNING("Script tag not implemented yet");
            } break;
            case "style":
            //silently ignore
            break;
            default: {
                throw new Error("Unsupported element type " + tag);
            }
        }

        //figure out the styling applied to this element
        String classData = jsoupNode.attr("class");
        List<ElementStyling> styles = new LinkedList<ElementStyling>();
        if(classData.length() > 0){
            String[] cssClasses = classData.split(" ");
            styles = Arrays.asList(cssClasses).stream().map((String cssClassName) -> {return styleAccumulator.getStyle(cssClassName);}).filter((ElementStyling elStyling) -> {return elStyling != null;}).collect(Collectors.toList());
        }
        HtmlParser.applyStyling(rVal, styles);

        return rVal;
    }

    /**
     * Determines all css styles to apply
     * @param root The root node
     * @return The style accumulator
     */
    private static StyleAccumulator parseStyles(Node root){
        StyleAccumulator rVal = new StyleAccumulator();
        HtmlParser.recursivelyParseStyles(root, rVal);
        return rVal;
    }

    /**
     * Recurses through the html tree to calculate styles to apply
     * @param root The root node
     * @param styleAccumulator The style accumulator
     */
    private static void recursivelyParseStyles(Node root, StyleAccumulator styleAccumulator){
        String tag = root.nodeName();
        switch(tag){
            case "style": {
                if(root.childNodes() == null || root.childNodes().size() < 1){
                    throw new Error("Style tag has no content! " + root.outerHtml());
                }
                styleAccumulator.parseCssString(root.childNodes().get(0).outerHtml());
            } break;
        }
        for(Node child : root.childNodes()){
            HtmlParser.recursivelyParseStyles(child, styleAccumulator);
        }
    }

    /**
     * Applies a set of styles to an element
     * @param el The element
     * @param styles The styles
     */
    private static void applyStyling(Element el, List<ElementStyling> styles){
        for(ElementStyling styleCurr : styles){

            //width + height
            if(styleCurr.getWidth() != null){
                el.setWidth(styleCurr.getWidth());
            }
            if(styleCurr.getHeight() != null){
                el.setHeight(styleCurr.getHeight());
            }

            //margin
            if(styleCurr.getMarginTop() != null){
                el.setMarginTop(styleCurr.getMarginTop());
            }
            if(styleCurr.getMarginRight() != null){
                el.setMarginRight(styleCurr.getMarginRight());
            }
            if(styleCurr.getMarginBottom() != null){
                el.setMarginBottom(styleCurr.getMarginBottom());
            }
            if(styleCurr.getMarginLeft() != null){
                el.setMarginLeft(styleCurr.getMarginLeft());
            }

            //padding
            if(styleCurr.getPaddingTop() != null){
                el.setPaddingTop(styleCurr.getPaddingTop());
            }
            if(styleCurr.getPaddingRight() != null){
                el.setPaddingRight(styleCurr.getPaddingRight());
            }
            if(styleCurr.getPaddingBottom() != null){
                el.setPaddingBottom(styleCurr.getPaddingBottom());
            }
            if(styleCurr.getPaddingLeft() != null){
                el.setPaddingLeft(styleCurr.getPaddingLeft());
            }

            //align self
            if(styleCurr.getAlignSelf() != null){
                el.setAlignSelf(styleCurr.getAlignSelf());
            }

            //flex direction
            if(styleCurr.getFlexDirection() != null && el instanceof ContainerElement){
                ((ContainerElement)el).setFlexDirection(styleCurr.getFlexDirection());
            }

            //positioning
            if(styleCurr.getPosition() != null){
                el.setPositionType(styleCurr.getPosition());
            }

            //justification
            if(styleCurr.getJustification() != null && el instanceof ContainerElement){
                ((ContainerElement)el).setJustifyContent(styleCurr.getJustification());
            }

            //align items
            if(styleCurr.getAlignItems() != null && el instanceof ContainerElement){
                ((ContainerElement)el).setAlignItems(styleCurr.getAlignItems());
            }

            //wrap
            if(styleCurr.getWrap() != null && el instanceof ContainerElement){
                ((ContainerElement)el).setWrap(styleCurr.getWrap());
            }

            //overflow
            if(styleCurr.getOverflow() != null && el instanceof ContainerElement){
                ((ContainerElement)el).setOverflow(styleCurr.getOverflow());
            }


        }
    }

}

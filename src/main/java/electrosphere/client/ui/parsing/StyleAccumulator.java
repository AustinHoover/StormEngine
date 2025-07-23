package electrosphere.client.ui.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaAlignment;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaFlexDirection;
import electrosphere.renderer.ui.elementtypes.ContainerElement.YogaJustification;

/**
 * Accumulates css
 */
public class StyleAccumulator {

    /**
     * The css capturing pattern
     * Group 1 is the class name
     * Group 2 is the styling on the class
     */
    static Pattern cssPattern = Pattern.compile("([a-zA-Z0-9]+)[ ]*\\{([^{}]*)\\}");
    
    /**
     * Maps a css class to its corresponding style
     */
    Map<String,ElementStyling> classStyleMap = new HashMap<String,ElementStyling>();

    /**
     * The name of the css class
     * @param className The name of the css class
     * @return The element styling for that class if it exists
     */
    public ElementStyling getStyle(String className){
        return classStyleMap.get(className);
    }

    /**
     * Parses a css string
     * @param content The string
     */
    public void parseCssString(String content){
        if(content == null || content.equals("")){
            throw new Error("String is empty");
        }
        Matcher matcher = cssPattern.matcher(content);
        while(matcher.find()){
            String className = matcher.group(1);
            String styleContent = matcher.group(2);
            ElementStyling elementStyling = new ElementStyling();

            //parse individual lines of the class
            String[] styleElements = styleContent.split(";");
            for(String styleString : styleElements){
                String[] pair = styleString.split(":");

                //
                //error checking
                if(pair.length == 1 && pair[0].trim().length() == 0){
                    continue;
                }
                if(pair.length != 2){
                    System.out.println("Skipping style pair that was parsed as " + pair.length);
                    if(pair.length > 0){
                        int i = 0;
                        for(String pairVal : pair){
                            System.out.println(i + ": " + pairVal);
                            i++;
                        }
                    }
                    System.out.println("Root line: " + styleString);
                    continue;
                }

                //
                //parse css data
                String styleType = pair[0].trim();
                String styleValue = pair[1].trim();
                switch(styleType){
                    case "height": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setHeight(intVal);
                    } break;
                    case "width": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setWidth(intVal);
                    } break;
                    case "margin-top": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setMarginTop(intVal);
                    } break;
                    case "margin-right": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setMarginRight(intVal);
                    } break;
                    case "margin-bottom": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setMarginBottom(intVal);
                    } break;
                    case "margin-left": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setMarginLeft(intVal);
                    } break;
                    case "padding-top": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setPaddingTop(intVal);
                    } break;
                    case "padding-right": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setPaddingRight(intVal);
                    } break;
                    case "padding-bottom": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setPaddingBottom(intVal);
                    } break;
                    case "padding-left": {
                        String shortened = styleValue.replace("px", "").replace("\"", "");
                        int intVal = Integer.parseInt(shortened);
                        elementStyling.setPaddingLeft(intVal);
                    } break;
                    case "align-items": {
                        switch(styleValue){
                            case "auto": {
                                elementStyling.setAlignItems(YogaAlignment.Auto);
                            } break;
                            case "baseline": {
                                elementStyling.setAlignItems(YogaAlignment.Baseline);
                            } break;
                            case "center": {
                                elementStyling.setAlignItems(YogaAlignment.Center);
                            } break;
                            case "flex-start": {
                                elementStyling.setAlignItems(YogaAlignment.Start);
                            } break;
                            case "flex-end": {
                                elementStyling.setAlignItems(YogaAlignment.End);
                            } break;
                            case "around": {
                                elementStyling.setAlignItems(YogaAlignment.Around);
                            } break;
                            case "between": {
                                elementStyling.setAlignItems(YogaAlignment.Between);
                            } break;
                            case "stretch": {
                                elementStyling.setAlignItems(YogaAlignment.Stretch);
                            } break;
                            default: {
                                LoggerInterface.loggerUI.WARNING("Unsupported align-items type " + styleValue);
                            } break;
                        }
                    } break;
                    case "flex-direction": {
                        switch(styleValue){
                            case "column": {
                                elementStyling.setFlexDirection(YogaFlexDirection.Column);
                            } break;
                            case "column-reverse": {
                                elementStyling.setFlexDirection(YogaFlexDirection.Column_Reverse);
                            } break;
                            case "row": {
                                elementStyling.setFlexDirection(YogaFlexDirection.Row);
                            } break;
                            case "row-reverse": {
                                elementStyling.setFlexDirection(YogaFlexDirection.Row_Reverse);
                            } break;
                            default: {
                                LoggerInterface.loggerUI.WARNING("Unsupported flex-direction type " + styleValue);
                            } break;
                        }
                    } break;
                    case "justify-content": {
                        switch(styleValue){
                            case "center": {
                                elementStyling.setJustification(YogaJustification.Center);
                            } break;
                            case "flex-start": {
                                elementStyling.setJustification(YogaJustification.Start);
                            } break;
                            case "flex-end": {
                                elementStyling.setJustification(YogaJustification.End);
                            } break;
                            case "space-around": {
                                elementStyling.setJustification(YogaJustification.Around);
                            } break;
                            case "space-between": {
                                elementStyling.setJustification(YogaJustification.Between);
                            } break;
                            case "space-evenly": {
                                elementStyling.setJustification(YogaJustification.Evenly);
                            } break;
                            default: {
                                LoggerInterface.loggerUI.WARNING("Unsupported justify-content type " + styleValue);
                            } break;
                        }
                    } break;
                    default: {
                        LoggerInterface.loggerUI.WARNING("Unsupported style type " + styleType);
                    } break;
                }
            }
            this.classStyleMap.put(className,elementStyling);
        }
    }

}

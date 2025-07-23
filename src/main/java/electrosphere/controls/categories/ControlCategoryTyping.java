package electrosphere.controls.categories;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import electrosphere.controls.Control;
import electrosphere.controls.ControlHandler;
import electrosphere.controls.Control.ControlMethod;
import electrosphere.controls.Control.ControlType;
import electrosphere.controls.MouseState;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.ui.events.KeyboardEvent;

public class ControlCategoryTyping {

    public static final String MENU_TYPE_BACKSPACE = "menuTypeBackspace";
    public static final String MENU_TYPE_0 = "menuType0";
    public static final String MENU_TYPE_1 = "menuType1";
    public static final String MENU_TYPE_2 = "menuType2";
    public static final String MENU_TYPE_3 = "menuType3";
    public static final String MENU_TYPE_4 = "menuType4";
    public static final String MENU_TYPE_5 = "menuType5";
    public static final String MENU_TYPE_6 = "menuType6";
    public static final String MENU_TYPE_7 = "menuType7";
    public static final String MENU_TYPE_8 = "menuType8";
    public static final String MENU_TYPE_9 = "menuType9";
    public static final String MENU_TYPE_PERIOD = "menuType.";
    public static final String MENU_TYPE_A = "menuTypeA";
    public static final String MENU_TYPE_B = "menuTypeB";
    public static final String MENU_TYPE_C = "menuTypeC";
    public static final String MENU_TYPE_D = "menuTypeD";
    public static final String MENU_TYPE_E = "menuTypeE";
    public static final String MENU_TYPE_F = "menuTypeF";
    public static final String MENU_TYPE_G = "menuTypeG";
    public static final String MENU_TYPE_H = "menuTypeH";
    public static final String MENU_TYPE_I = "menuTypeI";
    public static final String MENU_TYPE_J = "menuTypeJ";
    public static final String MENU_TYPE_K = "menuTypeK";
    public static final String MENU_TYPE_L = "menuTypeL";
    public static final String MENU_TYPE_M = "menuTypeM";
    public static final String MENU_TYPE_N = "menuTypeN";
    public static final String MENU_TYPE_O = "menuTypeO";
    public static final String MENU_TYPE_P = "menuTypeP";
    public static final String MENU_TYPE_Q = "menuTypeQ";
    public static final String MENU_TYPE_R = "menuTypeR";
    public static final String MENU_TYPE_S = "menuTypeS";
    public static final String MENU_TYPE_T = "menuTypeT";
    public static final String MENU_TYPE_U = "menuTypeU";
    public static final String MENU_TYPE_V = "menuTypeV";
    public static final String MENU_TYPE_W = "menuTypeW";
    public static final String MENU_TYPE_X = "menuTypeX";
    public static final String MENU_TYPE_Y = "menuTypeY";
    public static final String MENU_TYPE_Z = "menuTypeZ";
    public static final String MENU_TYPE_FORWARD_SLASH = "menuTypeForwardSlash";
    

    public static void mapControls(ControlHandler handler){
        /*
        Map the typing controls
        */
        handler.addControl(MENU_TYPE_BACKSPACE, new Control(ControlType.KEY,GLFW.GLFW_KEY_BACKSPACE,false,"",""));
        handler.addControl(MENU_TYPE_0, new Control(ControlType.KEY,GLFW.GLFW_KEY_0,false,"",""));
        handler.addControl(MENU_TYPE_1, new Control(ControlType.KEY,GLFW.GLFW_KEY_1,false,"",""));
        handler.addControl(MENU_TYPE_2, new Control(ControlType.KEY,GLFW.GLFW_KEY_2,false,"",""));
        handler.addControl(MENU_TYPE_3, new Control(ControlType.KEY,GLFW.GLFW_KEY_3,false,"",""));
        handler.addControl(MENU_TYPE_4, new Control(ControlType.KEY,GLFW.GLFW_KEY_4,false,"",""));
        handler.addControl(MENU_TYPE_5, new Control(ControlType.KEY,GLFW.GLFW_KEY_5,false,"",""));
        handler.addControl(MENU_TYPE_6, new Control(ControlType.KEY,GLFW.GLFW_KEY_6,false,"",""));
        handler.addControl(MENU_TYPE_7, new Control(ControlType.KEY,GLFW.GLFW_KEY_7,false,"",""));
        handler.addControl(MENU_TYPE_8, new Control(ControlType.KEY,GLFW.GLFW_KEY_8,false,"",""));
        handler.addControl(MENU_TYPE_9, new Control(ControlType.KEY,GLFW.GLFW_KEY_9,false,"",""));
        handler.addControl(MENU_TYPE_PERIOD, new Control(ControlType.KEY,GLFW.GLFW_KEY_PERIOD,false,"",""));
        handler.addControl(MENU_TYPE_A, new Control(ControlType.KEY,GLFW.GLFW_KEY_A,false,"",""));
        handler.addControl(MENU_TYPE_B, new Control(ControlType.KEY,GLFW.GLFW_KEY_B,false,"",""));
        handler.addControl(MENU_TYPE_C, new Control(ControlType.KEY,GLFW.GLFW_KEY_C,false,"",""));
        handler.addControl(MENU_TYPE_D, new Control(ControlType.KEY,GLFW.GLFW_KEY_D,false,"",""));
        handler.addControl(MENU_TYPE_E, new Control(ControlType.KEY,GLFW.GLFW_KEY_E,false,"",""));
        handler.addControl(MENU_TYPE_F, new Control(ControlType.KEY,GLFW.GLFW_KEY_F,false,"",""));
        handler.addControl(MENU_TYPE_G, new Control(ControlType.KEY,GLFW.GLFW_KEY_G,false,"",""));
        handler.addControl(MENU_TYPE_H, new Control(ControlType.KEY,GLFW.GLFW_KEY_H,false,"",""));
        handler.addControl(MENU_TYPE_I, new Control(ControlType.KEY,GLFW.GLFW_KEY_I,false,"",""));
        handler.addControl(MENU_TYPE_J, new Control(ControlType.KEY,GLFW.GLFW_KEY_J,false,"",""));
        handler.addControl(MENU_TYPE_K, new Control(ControlType.KEY,GLFW.GLFW_KEY_K,false,"",""));
        handler.addControl(MENU_TYPE_L, new Control(ControlType.KEY,GLFW.GLFW_KEY_L,false,"",""));
        handler.addControl(MENU_TYPE_M, new Control(ControlType.KEY,GLFW.GLFW_KEY_M,false,"",""));
        handler.addControl(MENU_TYPE_N, new Control(ControlType.KEY,GLFW.GLFW_KEY_N,false,"",""));
        handler.addControl(MENU_TYPE_O, new Control(ControlType.KEY,GLFW.GLFW_KEY_O,false,"",""));
        handler.addControl(MENU_TYPE_P, new Control(ControlType.KEY,GLFW.GLFW_KEY_P,false,"",""));
        handler.addControl(MENU_TYPE_Q, new Control(ControlType.KEY,GLFW.GLFW_KEY_Q,false,"",""));
        handler.addControl(MENU_TYPE_R, new Control(ControlType.KEY,GLFW.GLFW_KEY_R,false,"",""));
        handler.addControl(MENU_TYPE_S, new Control(ControlType.KEY,GLFW.GLFW_KEY_S,false,"",""));
        handler.addControl(MENU_TYPE_T, new Control(ControlType.KEY,GLFW.GLFW_KEY_T,false,"",""));
        handler.addControl(MENU_TYPE_U, new Control(ControlType.KEY,GLFW.GLFW_KEY_U,false,"",""));
        handler.addControl(MENU_TYPE_V, new Control(ControlType.KEY,GLFW.GLFW_KEY_V,false,"",""));
        handler.addControl(MENU_TYPE_W, new Control(ControlType.KEY,GLFW.GLFW_KEY_W,false,"",""));
        handler.addControl(MENU_TYPE_X, new Control(ControlType.KEY,GLFW.GLFW_KEY_X,false,"",""));
        handler.addControl(MENU_TYPE_Y, new Control(ControlType.KEY,GLFW.GLFW_KEY_Y,false,"",""));
        handler.addControl(MENU_TYPE_Z, new Control(ControlType.KEY,GLFW.GLFW_KEY_Z,false,"",""));
        handler.addControl(MENU_TYPE_FORWARD_SLASH, new Control(ControlType.KEY,GLFW.GLFW_KEY_SLASH,false,"",""));
    }

    /**
     * Populates the typing controls list
     * @param controlMap
     */
    public static void setCallbacks(
        HashMap<String, Control> controlMap,
        List<Control> typingControlList
    ){
        String[] typeKeybinds = {
            MENU_TYPE_BACKSPACE,
            MENU_TYPE_0,
            MENU_TYPE_1,
            MENU_TYPE_2,
            MENU_TYPE_3,
            MENU_TYPE_4,
            MENU_TYPE_5,
            MENU_TYPE_6,
            MENU_TYPE_7,
            MENU_TYPE_8,
            MENU_TYPE_9,
            MENU_TYPE_PERIOD,
            MENU_TYPE_A,
            MENU_TYPE_B,
            MENU_TYPE_C,
            MENU_TYPE_D,
            MENU_TYPE_E,
            MENU_TYPE_F,
            MENU_TYPE_G,
            MENU_TYPE_H,
            MENU_TYPE_I,
            MENU_TYPE_J,
            MENU_TYPE_K,
            MENU_TYPE_L,
            MENU_TYPE_M,
            MENU_TYPE_N,
            MENU_TYPE_O,
            MENU_TYPE_P,
            MENU_TYPE_Q,
            MENU_TYPE_R,
            MENU_TYPE_S,
            MENU_TYPE_T,
            MENU_TYPE_U,
            MENU_TYPE_V,
            MENU_TYPE_W,
            MENU_TYPE_X,
            MENU_TYPE_Y,
            MENU_TYPE_Z,
            MENU_TYPE_FORWARD_SLASH,
        };
        for(String currentKey : typeKeybinds){
            typingControlList.add(controlMap.get(currentKey));
            controlMap.get(currentKey).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
                Globals.elementService.fireEventNoPosition(
                    new KeyboardEvent(convertKeycodeToName(controlMap.get(currentKey).getKeyValue())),
                    Globals.elementService.getFocusedElement()
                );
            }});
        }
    }

    /**
     * Converts a keycode to a string containing a code related to the keycode (ie "A" for 65, "Escape" for 256, etc)
     * @param code The keycode
     * @return The corresponding string code
     */
    public static String convertKeycodeToName(int code){
        String rVal = "";
        switch(code){
            case 46:
            rVal = ".";
            break;
            case 47:
            rVal = "/";
            break;
            case 48:
                rVal = "0";
                break;
            case 49:
                rVal = "1";
                break;
            case 50:
                rVal = "2";
                break;
            case 51:
                rVal = "3";
                break;
            case 52:
                rVal = "4";
                break;
            case 53:
                rVal = "5";
                break;
            case 54:
                rVal = "6";
                break;
            case 55:
                rVal = "7";
                break;
            case 56:
                rVal = "8";
                break;
            case 57:
                rVal = "9";
                break;
            case 65:
                rVal = "A";
                break;
            case 66:
                rVal = "B";
                break;
            case 67:
                rVal = "C";
                break;
            case 68:
                rVal = "D";
                break;
            case 69:
                rVal = "E";
                break;
            case 70:
                rVal = "F";
                break;
            case 71:
                rVal = "G";
                break;
            case 72:
                rVal = "H";
                break;
            case 73:
                rVal = "I";
                break;
            case 74:
                rVal = "J";
                break;
            case 75:
                rVal = "K";
                break;
            case 76:
                rVal = "L";
                break;
            case 77:
                rVal = "M";
                break;
            case 78:
                rVal = "N";
                break;
            case 79:
                rVal = "O";
                break;
            case 80:
                rVal = "P";
                break;
            case 81:
                rVal = "Q";
                break;
            case 82:
                rVal = "R";
                break;
            case 83:
                rVal = "S";
                break;
            case 84:
                rVal = "T";
                break;
            case 85:
                rVal = "U";
                break;
            case 86:
                rVal = "V";
                break;
            case 87:
                rVal = "W";
                break;
            case 88:
                rVal = "X";
                break;
            case 89:
                rVal = "Y";
                break;
            case 90:
                rVal = "Z";
                break;
            case 259:
            rVal = "bs"; //backspace
            break;
            case 256:
                rVal = "Escape";
                break;
            default:
            LoggerInterface.loggerEngine.WARNING("Unable to convert keycode " + code + " in control handler.");
            break;
        }
        return rVal;
    }

}

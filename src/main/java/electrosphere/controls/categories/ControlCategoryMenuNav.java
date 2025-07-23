package electrosphere.controls.categories;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import electrosphere.controls.Control;
import electrosphere.controls.Control.ControlMethod;
import electrosphere.controls.Control.ControlType;
import electrosphere.controls.ControlHandler;
import electrosphere.controls.MouseState;
import electrosphere.engine.Globals;
import electrosphere.engine.Main;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.MenuEvent;
import electrosphere.renderer.ui.events.MenuEvent.MenuEventType;
import electrosphere.renderer.ui.events.MouseEvent;
import electrosphere.renderer.ui.events.ScrollEvent;
import electrosphere.util.FileUtils;

public class ControlCategoryMenuNav {

    public static final String DATA_STRING_INPUT_CODE_MENU_NAVIGATE_FORWARD = "menuNavigateForward";
    public static final String DATA_STRING_INPUT_CODE_MENU_NAVIGATE_BACKWARDS = "menuNavigateBackwards";
    public static final String DATA_STRING_INPUT_CODE_MENU_INCREMENT = "menuIncrement";
    public static final String DATA_STRING_INPUT_CODE_MENU_DECREMENT = "menuDecrement";
    public static final String DATA_STRING_INPUT_CODE_MENU_SELECT = "menuSelect";
    public static final String INPUT_CODE_MENU_MOUSE_PRIMARY = "menuMousePrimary";
    public static final String DATA_STRING_INPUT_CODE_MENU_BACKOUT = "menuBackout";
    public static final String MENU_MOUSE_MOVE = "menuMouseMove";
    public static final String MENU_SCROLL = "menuScroll";
    public static final String MENU_DRAG_MANIPULATE = "menuDragManipulate";
    public static final String MENU_DRAG_START = "menuDragStart";
    public static final String MENU_CAPTURE_SCREEN = "menuCaptureScreen";

    /**
     * Maps the controls
     * @param handler
     */
    public static void mapControls(ControlHandler handler){
        handler.addControl(DATA_STRING_INPUT_CODE_MENU_NAVIGATE_FORWARD, new Control(ControlType.KEY,GLFW.GLFW_KEY_DOWN,false,"",""));
        handler.addControl(DATA_STRING_INPUT_CODE_MENU_NAVIGATE_BACKWARDS, new Control(ControlType.KEY,GLFW.GLFW_KEY_UP,false,"",""));
        handler.addControl(DATA_STRING_INPUT_CODE_MENU_INCREMENT, new Control(ControlType.KEY,GLFW.GLFW_KEY_RIGHT,false,"",""));
        handler.addControl(DATA_STRING_INPUT_CODE_MENU_DECREMENT, new Control(ControlType.KEY,GLFW.GLFW_KEY_LEFT,false,"",""));
        handler.addControl(DATA_STRING_INPUT_CODE_MENU_SELECT, new Control(ControlType.KEY,GLFW.GLFW_KEY_ENTER,false,"",""));
        handler.addControl(DATA_STRING_INPUT_CODE_MENU_BACKOUT, new Control(ControlType.KEY,GLFW.GLFW_KEY_ESCAPE,false,"",""));
        handler.addControl(MENU_MOUSE_MOVE, new Control(ControlType.MOUSE_MOVEMENT,0,false,"",""));
        handler.addControl(INPUT_CODE_MENU_MOUSE_PRIMARY, new Control(ControlType.MOUSE_BUTTON,GLFW.GLFW_MOUSE_BUTTON_LEFT,false,"Mouse primary",""));
        handler.addControl(MENU_SCROLL, new Control(ControlType.MOUSE_SCROLL,0,false,"",""));
        handler.addControl(MENU_DRAG_START, new Control(ControlType.MOUSE_MOVEMENT,0,false,"",""));
        handler.addControl(MENU_CAPTURE_SCREEN, new Control(ControlType.KEY,GLFW.GLFW_KEY_F12,true,"Screenshot","Takes a screenshot of the engine"));
        handler.addControl(MENU_DRAG_MANIPULATE, new Control(ControlType.MOUSE_BUTTON,GLFW.GLFW_MOUSE_BUTTON_1,false,"",""));
    }
    
    /**
     * Populates the menu navigation controls list
     * @param controlMap
     */
    public static void setCallbacks(
        HashMap<String, Control> controlMap,
        List<Control> menuNavigationControlList,
        List<Control> mainGameDebugControlList,
        List<Control> inventoryControlList
    ){
        menuNavigationControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MENU_NAVIGATE_FORWARD));
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_NAVIGATE_FORWARD).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            // Globals.currentMenu.incrementMenuOption();
            Globals.elementService.focusNextElement();
        }});

        menuNavigationControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MENU_NAVIGATE_BACKWARDS));
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_NAVIGATE_BACKWARDS).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            // Globals.currentMenu.decrementMenuOption();
            Globals.elementService.focusPreviousElement();
        }});



        //Incrementing a menu element
        menuNavigationControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MENU_INCREMENT));
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_INCREMENT).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            Globals.elementService.fireEvent(
                new MenuEvent(MenuEventType.INCREMENT),
                Globals.elementService.getFocusedElement().getAbsoluteX(),
                Globals.elementService.getFocusedElement().getAbsoluteY()
            );
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_INCREMENT).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            Globals.elementService.fireEvent(
                new MenuEvent(MenuEventType.INCREMENT),
                Globals.elementService.getFocusedElement().getAbsoluteX(),
                Globals.elementService.getFocusedElement().getAbsoluteY()
            );
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_INCREMENT).setRepeatTimeout(0.5f * Main.targetFrameRate);

        //moving the mouse
        inventoryControlList.add(controlMap.get(MENU_MOUSE_MOVE));
        menuNavigationControlList.add(controlMap.get(MENU_MOUSE_MOVE));
        controlMap.get(MENU_MOUSE_MOVE).setOnMove(new Control.MouseCallback(){public void execute(MouseState mouseState, MouseEvent mouseEvent){
            Globals.elementService.updateHover(mouseEvent.getCurrentX(), mouseEvent.getCurrentY());
        }});

        //scrolling the mouse
        menuNavigationControlList.add(controlMap.get(MENU_SCROLL));
        controlMap.get(MENU_SCROLL).setOnScroll(new Control.ScrollCallback() {public void execute(MouseState mouseState, ScrollEvent scrollEvent){
            Globals.elementService.fireEvent(scrollEvent, (int)scrollEvent.getMouseX(), (int)scrollEvent.getMouseY());
        }});

        //dragging the cursor
        menuNavigationControlList.add(controlMap.get(MENU_DRAG_MANIPULATE));
        controlMap.get(MENU_DRAG_MANIPULATE).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
        }});
        controlMap.get(MENU_DRAG_MANIPULATE).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
            if(mouseState.isDragging()){
                mouseState.setDragging(false);
                //fire dragrelease event to elementmanager
                Globals.elementService.dragRelease(
                    (int)mouseState.getCurrentX(),
                    (int)mouseState.getCurrentY(),
                    (int)mouseState.getLastX(),
                    (int)mouseState.getLastY(),
                    (int)(mouseState.getDeltaX()),
                    (int)(mouseState.getDeltaY())
                );
            }
        }});
        /*
        item dragging
        */
        menuNavigationControlList.add(controlMap.get(MENU_DRAG_START));
        controlMap.get(MENU_DRAG_START).setOnMove(new Control.MouseCallback(){public void execute(MouseState mouseState, MouseEvent event){
            if(!mouseState.isDragging() && event.getButton1()){
                mouseState.setDragging(true);
                //fire dragstart event to elementmanager
                Globals.elementService.dragStart(
                    (int)mouseState.getCurrentX(),
                    (int)mouseState.getCurrentY(),
                    (int)mouseState.getLastX(),
                    (int)mouseState.getLastY(),
                    (int)(mouseState.getDeltaX()),
                    (int)(mouseState.getDeltaY())
                );
            }
            if(mouseState.isDragging()){
                //fire drag event to elementmanager
                Globals.elementService.drag(
                    (int)mouseState.getCurrentX(),
                    (int)mouseState.getCurrentY(),
                    (int)mouseState.getLastX(),
                    (int)mouseState.getLastY(),
                    (int)(mouseState.getDeltaX()),
                    (int)(mouseState.getDeltaY())
                );
            }
        }});
        
        /**
        Screenshots
        */
        menuNavigationControlList.add(controlMap.get(MENU_CAPTURE_SCREEN));
        controlMap.get(MENU_CAPTURE_SCREEN).setOnPress(new Control.ControlMethod() {public void execute(MouseState mouseState){
            FileUtils.writeBufferedImage(
                Globals.renderingEngine.defaultFramebuffer.getPixels(Globals.renderingEngine.getOpenGLState()),
                "Screenshots/" + System.currentTimeMillis() + ".png"
            );
        }});
        



        //Decrementing a menu element
        menuNavigationControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MENU_DECREMENT));
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_DECREMENT).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            Globals.elementService.fireEvent(
                new MenuEvent(MenuEventType.DECREMENT),
                Globals.elementService.getFocusedElement().getAbsoluteX(),
                Globals.elementService.getFocusedElement().getAbsoluteY()
            );
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_DECREMENT).setOnRepeat(new ControlMethod(){public void execute(MouseState mouseState){
            Globals.elementService.fireEvent(
                new MenuEvent(MenuEventType.DECREMENT),
                Globals.elementService.getFocusedElement().getAbsoluteX(),
                Globals.elementService.getFocusedElement().getAbsoluteY()
            );
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_DECREMENT).setRepeatTimeout(0.5f * Main.targetFrameRate);




        menuNavigationControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MENU_SELECT));
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_SELECT).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
            Globals.elementService.click(new ClickEvent(
                Globals.elementService.getFocusedElement().getAbsoluteX() + 1,
                Globals.elementService.getFocusedElement().getAbsoluteY() + 1,
                true,
                Globals.mouseCallback.getButton(GLFW.GLFW_MOUSE_BUTTON_2)
            ));
        }});

        menuNavigationControlList.add(controlMap.get(INPUT_CODE_MENU_MOUSE_PRIMARY));
        mainGameDebugControlList.add(controlMap.get(INPUT_CODE_MENU_MOUSE_PRIMARY));
        controlMap.get(INPUT_CODE_MENU_MOUSE_PRIMARY).setOnClick(new ControlMethod(){public void execute(MouseState mouseState){
            Globals.elementService.click(new ClickEvent(
                (int)mouseState.getCurrentX(),
                (int)mouseState.getCurrentY(),
                true,
                Globals.mouseCallback.getButton(GLFW.GLFW_MOUSE_BUTTON_2)
            ));
        }});
        controlMap.get(INPUT_CODE_MENU_MOUSE_PRIMARY).setRepeatTimeout(0.5f * Main.targetFrameRate);

        menuNavigationControlList.add(controlMap.get(DATA_STRING_INPUT_CODE_MENU_BACKOUT));
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_BACKOUT).setOnClick(new ControlMethod(){public void execute(MouseState mouseState){
            Globals.elementService.navigateBackwards();
        }});
        controlMap.get(DATA_STRING_INPUT_CODE_MENU_BACKOUT).setRepeatTimeout(0.5f * Main.targetFrameRate);
    }
    

}

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
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.MouseEvent;

/**
 * Inventory control callbacks
 */
public class ControlCategoryInventory {

    //inventory
    public static final String INPUT_CODE_INVENTORY_ITEM_MANIPULATE = "inventoryItemManipulate";
    public static final String INPUT_CODE_INVENTORY_ITEM_DRAG = "inventoryDrag";

    /**
     * Maps the controls
     * @param handler
     */
    public static void mapControls(ControlHandler handler){
        handler.addControl(INPUT_CODE_INVENTORY_ITEM_MANIPULATE, new Control(ControlType.MOUSE_BUTTON,GLFW.GLFW_MOUSE_BUTTON_1,false,"",""));
        handler.addControl(INPUT_CODE_INVENTORY_ITEM_DRAG, new Control(ControlType.MOUSE_MOVEMENT,0,false,"",""));
    }
    
    /**
     * Populates the in-game debug controls list
     * @param controlMap
     */
    public static void setCallbacks(
        HashMap<String, Control> controlMap,
        List<Control> inventoryControlList
    ){
        /*
        Item manipulation
        */
        inventoryControlList.add(controlMap.get(INPUT_CODE_INVENTORY_ITEM_MANIPULATE));
        controlMap.get(INPUT_CODE_INVENTORY_ITEM_MANIPULATE).setOnPress(new ControlMethod(){public void execute(MouseState mouseState){
        }});
        controlMap.get(INPUT_CODE_INVENTORY_ITEM_MANIPULATE).setOnRelease(new ControlMethod(){public void execute(MouseState mouseState){
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
            } else {
                //fire onclick event to elementmanager
                Globals.elementService.click(new ClickEvent((int)mouseState.getCurrentX(),(int)mouseState.getCurrentY(),true,Globals.mouseCallback.getButton(GLFW.GLFW_MOUSE_BUTTON_2)));
            }
        }});
        /*
        item dragging
        */
        inventoryControlList.add(controlMap.get(INPUT_CODE_INVENTORY_ITEM_DRAG));
        controlMap.get(INPUT_CODE_INVENTORY_ITEM_DRAG).setOnMove(new Control.MouseCallback(){public void execute(MouseState mouseState, MouseEvent event){
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
    }

}

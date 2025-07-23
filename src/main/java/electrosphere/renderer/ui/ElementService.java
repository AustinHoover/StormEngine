package electrosphere.renderer.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Vector2i;

import electrosphere.engine.Globals;
import electrosphere.engine.signal.Signal;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.LoggerInterface;
import electrosphere.engine.signal.SignalServiceImpl;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.ContainerElement;
import electrosphere.renderer.ui.elementtypes.DraggableElement;
import electrosphere.renderer.ui.elementtypes.Element;
import electrosphere.renderer.ui.elementtypes.FocusableElement;
import electrosphere.renderer.ui.elementtypes.HoverableElement;
import electrosphere.renderer.ui.events.ClickEvent;
import electrosphere.renderer.ui.events.DragEvent;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.FocusEvent;
import electrosphere.renderer.ui.events.HoverEvent;
import electrosphere.renderer.ui.events.NavigationEvent;
import electrosphere.renderer.ui.events.ScrollEvent;
import electrosphere.renderer.ui.events.DragEvent.DragEventType;
import electrosphere.renderer.ui.events.NavigationEvent.NavigationEventType;

/**
 * The main interface for working with the ui systemd
 */
public class ElementService extends SignalServiceImpl {
    
    
    /**
     * Constructor
     */
    public ElementService() {
        super(
            "ElementService",
            new SignalType[]{
                SignalType.YOGA_APPLY,
                SignalType.YOGA_APPLY_ROOT,
                SignalType.YOGA_DESTROY,
                SignalType.UI_MODIFICATION,
            }
        );
    }

    Map<String,Element> elementMap = new ConcurrentHashMap<String,Element>();
    List<Element> elementList = new CopyOnWriteArrayList<Element>();
    FocusableElement currentFocusedElement = null;
    DraggableElement currentDragElement = null;

    // the element currently hovered over
    HoverableElement currentHoveredElement = null;

    /**
     * Lock for thread-safing the structure
     */
    ReentrantLock lock = new ReentrantLock();
    
    /**
     * Registers a window
     * @param name the name associated with the window
     * @param w The window element
     */
    public void registerWindow(String name, Element w){
        lock.lock();
        if(elementMap.containsKey(name)){
            LoggerInterface.loggerUI.ERROR(new Error("Registering element to existing window string " + name));
        }
        elementMap.put(name,w);
        if(!elementList.contains(w)){
            elementList.add(w);
        } else {
            LoggerInterface.loggerUI.ERROR(new Error("Registering element to existing window string " + name));
        }
        if(elementList.size() < 2){
            focusFirstElement();
        }
        lock.unlock();
    }

    /**
     * Gets a window element by the name associated with it
     * @param name The associated name
     * @return The window element if it exists, null otherwise
     */
    public Element getWindow(String name){
        Element rVal = null;
        lock.lock();
        rVal = elementMap.get(name);
        lock.unlock();
        return rVal;
    }
    
    /**
     * Gets the list of all registered windows
     * @return The list of all registered windows
     */
    public List<Element> getWindowList(){
        lock.lock();
        List<Element> rVal = new LinkedList<Element>(this.elementList);
        lock.unlock();
        return rVal;
    }
    
    /**
     * Unregisters a window stored at a given window string
     * @param name The window string
     */
    public void unregisterWindow(String name){
        lock.lock();
        Element w = elementMap.remove(name);
        while(elementList.contains(w)){
            elementList.remove(w);
        }
        if(elementList.size() > 0){
            focusFirstElement();
        } else {
            this.currentFocusedElement = null;
        }
        lock.unlock();
    }

    /**
     * Gets the id of a window
     * @param window The window
     * @return The id if it has an assigned id, null otherwise
     */
    public String getWindowId(Element window){
        for(Entry<String,Element> windowEntry : this.elementMap.entrySet()){
            if(windowEntry.getValue().equals(window)){
                return windowEntry.getKey();
            }
        }
        return null;
    }

    /**
     * Checks if the element service contains a window
     * @param name The name of the window
     * @return true if the service contains the window, false otheriwse
     */
    public boolean containsWindow(String name){
        lock.lock();
        boolean rVal = elementMap.containsKey(name);
        lock.unlock();
        return rVal;
    }

    /**
     * Pushes a window to the front of the drawing stack
     * @param window The window
     */
    public void pushWindowToFront(Window window){
        lock.lock();
        elementList.remove(window);
        elementList.add(window);
        lock.unlock();
    }

    /**
     * Gets the currently open window's ids
     * @return The set of ids
     */
    public Set<String> getCurrentWindowIds(){
        lock.lock();
        Set<String> rVal = elementMap.keySet();
        lock.unlock();
        return rVal;
    }

    /**
     * Tries to navigate-close the window at a window string
     * @param windowString The window string
     */
    public void closeWindow(String windowString){
        Element windowEl = Globals.elementService.getWindow(windowString);
        if(windowEl instanceof Window){
            ((Window)windowEl).handleEvent(new NavigationEvent(NavigationEventType.BACKWARD));
        }
    }

    /**
     * Gets the list of focusable elements recursively
     * @param topLevel The current top level element
     * @param input The list to append to
     * @return The list to append to
     */
    List<FocusableElement> getFocusableList(Element topLevel, List<FocusableElement> input){
        if(topLevel instanceof FocusableElement){
            input.add((FocusableElement)topLevel);
        }
        if(topLevel instanceof ContainerElement){
            ContainerElement container = (ContainerElement) topLevel;
            for(Element child : container.getChildren()){
                getFocusableList(child,input);
            }
        }
        return input;
    }

    public FocusableElement getFocusedElement(){
        return currentFocusedElement;
    }

    public void focusFirstElement(){
        lock.lock();
        if(elementList.size() > 0){
            List<FocusableElement> focusables = getFocusableList(elementList.get(elementList.size() - 1),new LinkedList<FocusableElement>());
            if(focusables.size() > 0){
                if(currentFocusedElement != null){
                    currentFocusedElement.handleEvent(new FocusEvent(false));
                }
                currentFocusedElement = focusables.get(0);
                currentFocusedElement.handleEvent(new FocusEvent(true));
            } else {
                if(currentFocusedElement != null){
                    currentFocusedElement.handleEvent(new FocusEvent(false));
                }
                currentFocusedElement = null;
            }
        }
        lock.unlock();
    }

    public void focusNextElement(){
        lock.lock();
        List<FocusableElement> focusables = getFocusableList(elementList.get(elementList.size() - 1),new LinkedList<FocusableElement>());
        lock.unlock();
        if(focusables.contains(currentFocusedElement)){
            int index = focusables.indexOf(currentFocusedElement);
            if(index + 1 >= focusables.size()){
                index = 0;
            } else {
                index++;
            }
            if(currentFocusedElement != null){
                currentFocusedElement.handleEvent(new FocusEvent(false));
            }
            currentFocusedElement = focusables.get(index);
            currentFocusedElement.handleEvent(new FocusEvent(true));
        }
    }

    public void focusPreviousElement(){
        lock.lock();
        List<FocusableElement> focusables = getFocusableList(elementList.get(elementList.size() - 1),new LinkedList<FocusableElement>());
        lock.unlock();
        if(focusables.contains(currentFocusedElement)){
            int index = focusables.indexOf(currentFocusedElement);
            if(index - 1 < 0){
                index = focusables.size() - 1;
            } else {
                index--;
            }
            if(currentFocusedElement != null){
                currentFocusedElement.handleEvent(new FocusEvent(false));
            }
            currentFocusedElement = focusables.get(index);
            currentFocusedElement.handleEvent(new FocusEvent(true));
        }
    }

    /**
     * Sets a specific element to be focused
     * @param focusableElement The focusable element
     */
    public void focusElement(FocusableElement focusableElement){
        if(currentFocusedElement != null){
            currentFocusedElement.handleEvent(new FocusEvent(false));
        }
        this.currentFocusedElement = focusableElement;
        focusableElement.handleEvent(new FocusEvent(true));
    }

    /**
     * Fires an event at a given position
     * @param event The event
     * @param x the x coordinate of the position
     * @param y the y coordinate of the position
     */
    public void fireEvent(Event event, int x, int y){
        boolean propagate = true;
        lock.lock();
        ListIterator<Element> windowIterator = elementList.listIterator(elementList.size());
        lock.unlock();
        while(windowIterator.hasPrevious()){
            Element currentWindow = windowIterator.previous();
            Stack<Element> elementStack = this.buildElementPositionalStack(new Stack<Element>(), currentWindow, x, y);
            Element currentElement = null;
            while(elementStack.size() > 0 && propagate == true){
                currentElement = elementStack.pop();
                if(event instanceof ClickEvent){
                    ClickEvent clickEvent = (ClickEvent)event;
                    Vector2i absPos = getAbsolutePosition(currentElement);
                    clickEvent.setRelativeX(clickEvent.getCurrentX() - absPos.x);
                    clickEvent.setRelativeY(clickEvent.getCurrentY() - absPos.y);
                } else if(event instanceof DragEvent){
                    DragEvent dragEvent = (DragEvent)event;
                    Vector2i absPos = getAbsolutePosition(currentElement);
                    dragEvent.setRelativeX(dragEvent.getCurrentX() - absPos.x);
                    dragEvent.setRelativeY(dragEvent.getCurrentY() - absPos.y);
                } else if(event instanceof ScrollEvent){
                    ScrollEvent scrollEvent = (ScrollEvent)event;
                    Vector2i absPos = getAbsolutePosition(currentElement);
                    scrollEvent.setRelativeX((int)(scrollEvent.getMouseX() - absPos.x));
                    scrollEvent.setRelativeY((int)(scrollEvent.getMouseY() - absPos.y));
                }
                propagate = currentElement.handleEvent(event);
            }
            if(!propagate){
                currentWindow.applyYoga(0,0);
                break;
            }
        }
    }

    /**
     * Fires an event that does not have a screen position on a given element. Event propagation works by element ancestry regardless of position.
     * @param event The event
     * @param el The element to handle the event
     */
    public boolean fireEventNoPosition(Event event, Element el){
        List<Element> ancestryList = constructNonPositionalAncestryList(el);
        boolean propagate = true;
        while(ancestryList.size() > 0 && propagate == true){
            Element currentElement = ancestryList.remove(0);
            propagate = currentElement.handleEvent(event);
        }
        return propagate;
    }

    /**
     * Fires a top level event (ie on all window elements registered)
     * @param event The top level event
     */
    public void fireTopLevelEvent(Event event){
        boolean propagate = true;
        lock.lock();
        for(Element topLevelEl : this.elementList){
            propagate = fireEventNoPosition(event, topLevelEl);
            if(!propagate){
                break;
            }
        }
        lock.unlock();
    }

    /**
     * Constructs a list of elements starting with el and containing all its ancestor elements, parent, grandparent, etc
     * @param el The target element
     * @return The ancestry list
     */
    private List<Element> constructNonPositionalAncestryList(Element el){
        //a list of elements with 0 being the target of the event, 1 being the parent of the target, 2 being the parent of 1, etc
        List<Element> elementPropagation = new LinkedList<Element>();
        
        //if we are calling this on the current focused element, it could be null
        if(el != null){
            elementPropagation.add(el);
        }
        Element targetElement = el;
        while(targetElement != null && targetElement.getParent() != null){
            targetElement = targetElement.getParent();
            elementPropagation.add(targetElement);
        }
        return elementPropagation;
    }

    /**
     * Recursively searched for a target element starting at the searchRoot
     * @param searchRoot The root element to search from propagating down the tree
     * @param target The target to search for
     * @return The parent of target if it exists, null otherwise
     */
    public Element recursivelySearchParent(ContainerElement searchRoot, Element target){
        if(searchRoot instanceof ContainerElement){
            for(Element child : searchRoot.getChildren()){
                if(child == target){
                    return searchRoot;
                } else if(child instanceof ContainerElement){
                    Element result = recursivelySearchParent((ContainerElement)child, target);
                    if(result != null){
                        return result;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Recursively uilds a stack of elements at a given position based on their depth into the tree
     * @param inputStack The empty stack to fill
     * @param current The current element
     * @param x the x position to query
     * @param y the y position to query
     * @param offsetX the x offset accumulated
     * @param offsetY the y offset accumulated
     * @return the stack, filled with all relevant elements
     */
    private Stack<Element> buildElementPositionalStack(Stack<Element> inputStack, Element current, int x, int y){
        //if contains x,y, call function on el
        if(this.elementContainsPoint(current,x,y)){
            inputStack.push(current);
        }
        if(current instanceof ContainerElement){
            for(Element el : ((ContainerElement)current).getChildren()){
                this.buildElementPositionalStack(inputStack, el, x, y);
            }
        }
        return inputStack;
    }

    public Element resolveFirstDraggable(DragEvent event){
        lock.lock();
        ListIterator<Element> windowIterator = elementList.listIterator(elementList.size());
        while(windowIterator.hasPrevious()){
            Element currentWindow = windowIterator.previous();
            Stack<Element> elementStack = this.buildElementPositionalStack(new Stack<Element>(), currentWindow, event.getCurrentX(), event.getCurrentY());
            Element currentElement = null;
            while(elementStack.size() > 0){
                currentElement = elementStack.pop();
                if(currentElement instanceof DraggableElement){
                    lock.unlock();
                    return currentElement;
                }
            }
        }
        lock.unlock();
        return null;
    }

    /**
     * Gets the first hoverable element in the view stack
     * @param currentX the current x position of the mouse
     * @param currentY the current y position of the mouse
     * @return The first hoverable element if it exists, null otherwise
     */
    public Element resolveFirstHoverable(int currentX, int currentY){
        lock.lock();
        ListIterator<Element> windowIterator = elementList.listIterator(elementList.size());
        while(windowIterator.hasPrevious()){
            Element currentWindow = windowIterator.previous();
            Stack<Element> elementStack = buildElementPositionalStack(new Stack<Element>(), currentWindow, currentX, currentY);
            Element currentElement = null;
            while(elementStack.size() > 0){
                currentElement = elementStack.pop();
                if(currentElement instanceof HoverableElement){
                    lock.unlock();
                    return currentElement;
                }
            }
        }
        lock.unlock();
        return null;
    }

    /**
     * Checks if an element contains a given screen coordinate
     * @param el The element
     * @param x the x component of the coordinate
     * @param y the y component of the coordinate
     * @return True if it contains that point, false otherwise
     */
    boolean elementContainsPoint(Element el, int x, int y){
        return
        x >= el.getAbsoluteX() &&
        x <= el.getAbsoluteX() + el.getWidth() &&
        y >= el.getAbsoluteY() &&
        y <= el.getAbsoluteY() + el.getHeight();
    }

    /**
     * Fires an event where the mouse presses a button and clicks
     * @param event The event to fire
     */
    public void click(ClickEvent event){
        this.fireEvent(event,event.getCurrentX(),event.getCurrentY());
    }

    /**
     * Fires an event where the mouse presses down a button and begins to drag an element
     * @param x
     * @param y
     * @param lastX
     * @param lastY
     * @param deltaX
     * @param deltaY
     */
    public void dragStart(int x, int y, int lastX, int lastY, int deltaX, int deltaY){
        DragEvent event = new DragEvent(x, y, lastX, lastY, deltaX, deltaY, DragEventType.START, null);
        currentDragElement = (DraggableElement)resolveFirstDraggable(event);
        event.setTarget(currentDragElement);
        this.fireEvent(event,x,y);
    }

    /**
     * Fires an event where the mouse is dragging an element
     * @param x
     * @param y
     * @param lastX
     * @param lastY
     * @param deltaX
     * @param deltaY
     */
    public void drag(int x, int y, int lastX, int lastY, int deltaX, int deltaY){
        if(currentDragElement != null){
            DragEvent event = new DragEvent(x, y, lastX, lastY, deltaX, deltaY, DragEventType.DRAG, currentDragElement);
            Vector2i absPos = this.getAbsolutePosition(currentDragElement);
            event.setRelativeX(x - absPos.x);
            event.setRelativeY(y - absPos.y);
            currentDragElement.handleEvent(event);
        }
        // fireEvent(event,event.getCurrentX(),event.getCurrentY());
    }

    /**
     * Fires an event where the mouse is dragging an element and releases the mouse button
     * @param x
     * @param y
     * @param lastX
     * @param lastY
     * @param deltaX
     * @param deltaY
     */
    public void dragRelease(int x, int y, int lastX, int lastY, int deltaX, int deltaY){
        if(currentDragElement != null){
            DragEvent event = new DragEvent(x, y, lastX, lastY, deltaX, deltaY, DragEventType.RELEASE, currentDragElement);
            Vector2i absPos = this.getAbsolutePosition(currentDragElement);
            event.setRelativeX(x - absPos.x);
            event.setRelativeY(y - absPos.y);
            currentDragElement.handleEvent(event);
            currentDragElement = null;
        }
        // fireEvent(event,event.getCurrentX(),event.getCurrentY());
    }

    /**
     * Gets the absolute position of an element
     * @param target the element
     * @return The absolute position
     */
    private Vector2i getAbsolutePosition(Element target){
        List<Element> ancestryList = constructNonPositionalAncestryList(target);
        ancestryList.remove(target);
        int relX = 0;
        int relY = 0;
        for(Element el : ancestryList){
            relX = relX + el.getRelativeX();
            relY = relY + el.getRelativeY();
        }
        return new Vector2i(relX,relY);
    }

    /**
     * Navigates backwards
     */
    public void navigateBackwards(){
        NavigationEvent event = new NavigationEvent(NavigationEventType.BACKWARD);
        lock.lock();
        int elListSize = this.elementList.size();
        lock.unlock();
        if(currentFocusedElement != null){
            //fires on the currently focused element
            fireEventNoPosition(event, currentFocusedElement);
        } else if(elListSize > 0){
            fireTopLevelEvent(event);
        }
    }

    /**
     * Updates the hover state
     * @param currentX The current mouse X
     * @param currentY The current mouse Y
     */
    public void updateHover(int currentX, int currentY){
        Element newHoverableElement = resolveFirstHoverable(currentX,currentY);
        if(currentHoveredElement != newHoverableElement){
            if(currentHoveredElement != null){
                currentHoveredElement.handleEvent(new HoverEvent(false,currentX,currentY));
            }
            if(newHoverableElement != null){
                newHoverableElement.handleEvent(new HoverEvent(true,currentX,currentY));
            }
            currentHoveredElement = (HoverableElement)newHoverableElement;
        }
    }
    
    @Override
    public boolean handle(Signal signal){
        boolean rVal = false;
        switch(signal.getType()){
            case YOGA_APPLY: {
                Element target = (Element)signal.getData();
                if(target == null){
                    throw new Error("You forgot to include an element with the signal!");
                }
                target.applyYoga(0, 0);
                rVal = true;
            } break;
            case YOGA_APPLY_ROOT: {
                Element target = (Element)signal.getData();
                if(target == null){
                    throw new Error("You forgot to include an element with the signal!");
                }
                //find the root node
                while(target.getParent() != null){
                    target = target.getParent();
                }
                target.applyYoga(0, 0);
                rVal = true;
            } break;
            case YOGA_DESTROY: {
                Element target = (Element)signal.getData();
                target.destroy();
                rVal = true;
            } break;
            case UI_MODIFICATION: {
                Runnable modificationCallback = (Runnable)signal.getData();
                modificationCallback.run();
                rVal = true;
            } break;
            default: {
            } break;
        }
        return rVal;
    }
    
}

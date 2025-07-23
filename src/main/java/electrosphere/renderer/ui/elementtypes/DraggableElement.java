package electrosphere.renderer.ui.elementtypes;

import electrosphere.renderer.ui.events.DragEvent;

public interface DraggableElement extends Element {
    
    public void setOnDragStart(DragEventCallback callback);

    public void setOnDrag(DragEventCallback callback);

    public void setOnDragRelease(DragEventCallback callback);


    public interface DragEventCallback{

        public boolean execute(DragEvent event);

    }

}

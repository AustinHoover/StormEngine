@page imgui ImGUI

ImGui is a library for drawing immediate mode debug ui elements that are extremely pretty and quick to work with.
It has been included with this engine to facilitate faster debugging of all parts of the engine.


# Architecture
## Library Structure
Almost all functions called from the library are static calls from specific classes like ImPlot.

## Engine Structure
The principle class is ImGuiWindow which acts as a container for other ui elements.
These windows are registered to the RenderingEngine via addImGuiWindow().
The RenderingEngine loops through every window it has registered and renders them one after another.

## Initialization
The imgui library is initialized at the same time as the glfw window and is attached to it by the window pointer.

## Main Loop
The main logic for imgui is at the tail and of the main render function in RenderEngine.
It loops through every in-engine top level imgui container and calls immediate mode functions to construct the ui.


# Usage Examples
## Creating a window
As soon as you call this, it should immediately start rendering this window.
```
ImGuiWindow imGuiWindow = new ImGuiWindow("Frametime Graph");
RenderingEngine.addImGuiWindow(imGuiWindow);
```

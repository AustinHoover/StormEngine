@page scriptengine Script Engine


# Potential problems with integrating with the overall game engine
A chief problem we want to avoid is allowing people to 'escape' the scripting engine and get access to all objects in the overall engine

# Roadmap for integrating with the overall game engine



# Startup
On startup, the script engine loads a number of files required to polyfill basic javascript ecosystem functionality.
Once these are integrated, the engine loads typescript and the compiler.
The compiler is used to translate scene files and mod files into javascript that the runtime environment can actually execute.

# On loading a scene
When a scene is loaded, the scene and all dependent files are compiled from typescript to javascript.
Then, an instance of this scene is created which will store values set by the scene.
This instance is returned to the java side where it is associated with the java instance of the scene.

# On firing a hook
When a hook fires, java side calls a JS-side function that supplies the hook, values provided alongside the hook, and the scene which the hook was fired within.
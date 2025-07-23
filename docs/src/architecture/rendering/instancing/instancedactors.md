@page instancedactors Instanced Actors


Instanced Actors Documentation







Terminology

"Attribute" is a term that is used a lot in this implementation. An attribute is a value that will show up for a given instance in a shader.
For instance, aPos is an attribute that shows up in a lot of shaders that represents the position of the vertex in model-space.
They have a layout location in the shader and require special logic for making sure there is a 1-1 relation of buffer index to each instance.
These are different from "Uniforms" which you should be familiar with.























Goals of the implementation

The main goal of the implementation was to provide an interface to create a single instanced entity and have it have two main functions:
 - Update a value that the shader uses to draw this entity
 - Call a draw function and have it appear on the screen with whatever its current values are

Behind the scenes there's a lot of complicated logic to handle actually queueing these entities so that buffers aren't overflowing and values end up in the right spots.
























Restrictions

For the moment, the instanced actors only support having values of the HomogenousUniformBuffer type. Eg, you can only have mat4s, vec4s, vec3, etc. No strings.
Furthermore, there's a requirement to declare a capacity, or maximum number of instances, of the model you're about to draw.
EG you have to declare that there will only be 1000 leaf objects. You can still create more than 1000 and call draw more than 1000 times per frame, but only 1000 will draw.
 ^ This is for buffer-related reasons. GPU and CPU preallocate the buffers for all attributes





























Code layout


There are three main components to working with an instanced actor
 - InstancedActor.java
 - InstanceManager.java
 - InstanceData.java

InstanceManager.java provides the constructor for creating an instanced actor. It handles registration of the actor behind the scenes and queueing logic for drawing individual instances.

InstanceActor.java provides most of the publicly facing interface for instanced things that you want to draw. For instance, handles setting values of attributes for the single instance.

InstanceData.java used primarily behind the scenes to handle queueing and buffering data to the gpu based on what instances have drawn this frame.





























Basic flow for the parts you should be concerned with:

Have a managing class for whatever type of instance you want to draw. For instance, "GrassManager".

(1) "GrassManager" should loop through each instance and update any attributes by calling setAttribute() on InstancedActor.java for each instance.
    - This updates the values for that instance prior to draw call.
    - This does NOT immediately buffer these values to the gpu. That happens in step 3

(2) Every frame, while iterating through entities to draw, call the draw() method on the InstancedActor.java object.
    - This adds that instance to the queue to be drawn by the InstanceManager.java

(3) At the end of looping through normal entities in a RenderingEngine.java render pass, call draw() on InstanceManager.java.
    - This will draw every model that has instanced queued to be drawn.



@page firstpersonviewmodel First Person Viewmodel

# The pipeline

There is a separate render pipeline for first person elements. It is composited ontop the main render in the composite pipeline.

# The actor

There is a global entity, firstPersonEntity, that is rendered in the first person pipeline. This is the source of the visuals in that render.
The animations for this actor are controlled via the `FirstPersonTree`. It provides a convenient function where you give it the entity and the name of an animation and it will play it.
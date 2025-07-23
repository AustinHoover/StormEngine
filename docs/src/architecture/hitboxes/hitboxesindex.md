@page hitboxesindex Hitboxes






# Architecture Highest Level
New architecture needs to be something like

Per realm/client scene, we have a collision engine that has a whole bunch of capsules.
We update those collision engines at the top of the frame.
On collision, run collision logic






# Design Notes
We want to have one collision per "object" per frame. IE, lets say you have a sword with two hitboxes on it that both collide with an enemy.
This would technically generate two collision events per frame. Need to condense this to one collision "event".
Going to have one body per object, ie one body per sword, but then multiple shapes per body.
Then drop the collision engine down to 1 collision per body per frame.






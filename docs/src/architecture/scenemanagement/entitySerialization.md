@page entitySerialization Entity Serialization

How are entities load from disk/sent to player

Entities have a top level 'type', ie 'creature', 'object', 'foliage', etc
Beneath that is the specific subtype, ie 'human', 'elf', 'woodenCrate'


Lets say when the client receives the request 'spawn human' it runs the full human-creation macro. What then needs to be updated from the default for the macro?
Visual attributes
Equip state
current animation
movement state
idle state
gravity state
jump state
attack state


Specifically when loading files, also need to store server data
Which entity is the player's entity

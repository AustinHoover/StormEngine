@page equipstate Equip State


# Relevant Classes





# Data Explanation
## Item-side definitions

Items must define a property `equipClass`. This determines what type of item it is.
The equip points on the creature then whitelist equipClass variants.

IE, I would define a sword as a "weapon", then assign the item type "weapon" to a given equippoint (ie the person's hand).

Furthermore, a creature can define a `equipWhitelist` array. This allows you to define per-item what creatures can equip it.
The goal with this is to limit item equipping when we haven't modeled out a variant for a given creature.

IE, if you make a cool helmet for a human and don't bother to make one for dwarves, you could equipWhitelist it to just humans.

## Creature-side definitions

Creatures define an array `equipPoints`. This contains a list of points that the creature can equip items at.
A given equipPoint controls the bone the item is attached to, offsets/rotations from that bone, and the types of equipment that can be attached at that point.


## Equip Classes

They're basically category strings. There isn't any data intrinsic to an equipClass yet.
Potentially these will eventually be broken out into a dedicated data object to support things like tooltips.

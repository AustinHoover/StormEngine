@page BasicClothingGuide Basic Clothing Guide



## High Level Overview
The idea of this page is to track notes about creating simple clothing items that work with existing creature models.




## Setting Up The File
Create a new blender file.

Link the armature from the existing creature model.

![](/docs/src/images/blender/basicClothing/clothingLinkOption.png)

![](/docs/src/images/blender/basicClothing/clothingSelectLinkArmature.png)




Append the relevant meshes from the existing creature model.

![](/docs/src/images/blender/basicClothing/clothingAppend.png)

![](/docs/src/images/blender/basicClothing/clothingSelectRightMeshImport.png)



Clean up the existing mesh

![](/docs/src/images/blender/basicClothing/clothingDeletingVertices.png)

You can use the circle select mode to significantly speed up selection of vertices to delete

![](/docs/src/images/blender/basicClothing/clothingSwitchToAreaSelect.png)



## Attach Armature

Add the armature modifier to the mesh you have created

![](/docs/src/images/blender/basicClothing/clothingAddArmatureModifier.png)

Select the dropper icon for Object

![](/docs/src/images/blender/basicClothing/clothingArmatureModifierSelectArmatureEyedrop.png)

And click on the armature object in the tree view in the top right

![](/docs/src/images/blender/basicClothing/clothingClickOnArmature.png)

You can validate that it added the modifier correctly by selecting the bones, going into pose mode, and setting the pose to watch the mesh move.

![](/docs/src/images/blender/basicClothing/clothingTestWithPose.png)




## Solidify Modifier

Add the solidify modifier to make the clothing have depth

![](/docs/src/images/blender/basicClothing/clothingSolidifyModifier.png)

![](/docs/src/images/blender/basicClothing/clothingSolidifyModifierApply.png)

@page treesystem Tree System


### The Concept

The purpose of the tree is to take a branch model and leaf models and construct interesting, complex plants that can be animated and drawn cheaply. An example of a branch might look like the following.

![Some caption](/docs/src/images/basicbranch.png)


 * The red gismo is the origin point of the entity for the actual model of the branch
 * The blue arrow is the "branch bone" that controls where the branch model points
 * The green gizmo is the point that further branches will be placed

The idea is to attach multiple of these branches together one after another such that the red points of the children coincide with the green points of the parents and furthermore than the rotation of the green points of the parents coincide with the rotation of the red points of the children.

![Some caption](/docs/src/images/branchconnectionexample.png)

### How the branches actually branch

First the algorithm begins with the vertical line as shown by the purple line in diagram 1. It calculates a "peel" off the vertical line by a certain amount, as shown with the dark blue arc in diagram 2. It then calculates a rotation around the purple vertical as demonstrated by the red arc in diagram 3.

The branches wouldn't line up if the second rotation was done directly. Instead, the algorithm both rolls and pitches the branch bone to rotate it without any yaw. It then sets the child branch rotations to be the same as this branch bone.

![](/docs/src/images/branchpeelexplanation.png)

### Placing leaves delicately

The leaf placement algorithm creates sets of rings around the branch bone and places leaf entities evenly spaced along the circles

![](/docs/src/images/branchleafplacementconcept.png)

### Wind Behavior Tree

Todo

### Explanation of Config Parameters

`limbScalarFalloffFactor`: how quickly do the limbs shrink

![](/docs/src/images/branchfalloff.png)

`minimumLimbScalar`: How small are the terminal limbs, basically how small can it get before it stops generating

![](/docs/src/images/branchminimumscalar.png)

`maximumLimbDispersion`: The maximum a single branch can disperse from the current line

`minimumLimbDispersion`: The minimum a single branch must disperse from the current line

![](/docs/src/images/branchdispersion.png)

`minimumNumberForks`: The minimum number of branch forks per iteration

`maximumNumberForks`: The maximum number of branch forks per iteration

`branchHeight`: The height of a single branch, should be the height of the model

Or, how high is the green above the red

![Some caption](/docs/src/images/basicbranch.png)

`centralTrunk`: if true, always generates a central trunk

`maximumTrunkSegments`: The maximum number of linear segments for the trunk (ie how many times can a function recurse)

`maximumBranchSegments`: The maximum number of linear segments for the branch (ie how many times can a function recurse)

`maxBranchSegmentFalloffFactor`: The rate at which number of branch segments from the current trunk falls off over time

`minimumSegmentToSpawnLeaves`: The minimum segment number required to start spawning leaves

`minBranchHeightToStartSpawningLeaves`: the minimum distance along a given segment to start spawning leaves at

`maxBranchHeightToStartSpawningLeaves`: the maximum distance along a given segment to start spawning leaves at

`leafIncrement`: The increment along the branch segment to spawn leaves at

`minLeavesToSpawnPerPoint`: the minimum leaves to spawn per leaf point

`maxLeavesToSpawnPerPoint`: the maximum leaves to spawn per leaf point

`leafDistanceFromCenter`: The distance from the central line of a branch to spawn a leaf at

`peelVariance`: How much can the peel vary hypothetically while it's swinging

`peelMinimum`: a minimum amount of peel (For instance forcing weather to cause large motions in the branches)

`swaySigmoidFactor`: The value of the sigmoid controlling branch way speed over time (check branch btree for details)

`minimumSwayTime`: The minimum number of frames that a branch should sway for

`swayTimeVariance`: The maximum amount of frames that can be added to minimumSwayTime to increase the time of a single sway

`yawVariance`: How much can the yaw vary hypothetically while it's swinging

`yawMinimum`: a minimum amount of yaw (For instance forcing weather to cause large motions in the branches)

`minimumScalarToGenerateSwayTree`: The minimum scalar of a branch to generate a sway behavior tree

`maximumScalarToGenerateSwayTree`: The maximum scalar of a branch to generate a sway behavior tree


### Art Direction Ideas

* High dispersion, low limb count is likely to generate alien plants
* High dispersion, high limb count will generate bush-like plants
* Low dispersion, high limb count will give good trees
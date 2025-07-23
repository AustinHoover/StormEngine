@page jenkins Jenkins

Jenkins is used as the CI server for the engine. It automatically runs testing every time a new commit is uploaded to the git server.




# Stages

## Setup
Sets up the build environment. This is where file permissions are set and tools are downloaded.

## Check Environment
Gathers information about the build environment. This gets versions of build tools.

## Build (Engine)
Builds the core game engine.

## Build (Documentation)
Builds the documentation.

## Test
Performs all automated tests for the game engine. If this step fails, it triggers the test debugging step.

## DebugTests
Re-tests the engine with lots of helpful tools turn on. The memory-tracing jar is turned on. The logging is also set to maximum.






# Xvfb

The environment uses [Xvfb](https://en.wikipedia.org/wiki/Xvfb) to render the engine on headless servers. This allows for fully end-to-end testing of the graphical side of the engine.


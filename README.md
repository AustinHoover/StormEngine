# Storm Engine

A multiplayer-focused game engine


This is a mirror of the codebase stored on my private git server.
It does not include the assets required for the engine to actually run.
It is uploaded here to be a showcase of the code for the engine, not a complete product.



## Codebase Guide
 - [First File To Run](https://github.com/AustinHoover/StormEngine/blob/master/src/main/java/electrosphere/engine/Main.java)
 - [Core Runtime Code](https://github.com/AustinHoover/StormEngine/tree/master/src/main/java/electrosphere)
 - [Native Library](https://github.com/AustinHoover/StormEngine/tree/master/src/main/c)
 - [Assets Bunbled With Release](https://github.com/AustinHoover/StormEngine/tree/master/assets)
 - [Documentation Source Files](https://github.com/AustinHoover/StormEngine/tree/master/docs)






## Languages
 - [Java](https://github.com/AustinHoover/StormEngine/tree/master/src/main/java/electrosphere)
 - [Typescript](https://github.com/AustinHoover/StormEngine/tree/master/assets/Scripts)
 - [SQL](https://github.com/AustinHoover/StormEngine/tree/master/assets/DB)
 - [GLSL](https://github.com/AustinHoover/StormEngine/tree/master/assets/Shaders)
 - [C](https://github.com/AustinHoover/StormEngine/tree/master/src/main/c)




## Support Tooling
 - [Jenkins](https://github.com/AustinHoover/StormEngine/blob/master/Jenkinsfile)
 - [Docker](https://gist.github.com/AustinHoover/c5a1799b6f42a410f6db3bfcbaaaff68)
 - [NetArranger](https://github.com/StudioRailgun/NetArranger)
 - [Remotery](https://github.com/Celtoys/Remotery)
 - [RenderDoc](https://renderdoc.org/)






## Building


### Windows
1. Install
 - [gitbash](https://git-scm.com/downloads)
 - [choco](https://chocolatey.org/install)
 - [Eclipse Temurin 17](https://adoptium.net/temurin/releases/)
 - [maven](https://maven.apache.org/download.cgi)
 - [7zip](https://www.7-zip.org/)

2. From choco install
 - [mingw](https://community.chocolatey.org/packages/mingw)
 - [make](https://community.chocolatey.org/packages/make)
 - [cmake](https://community.chocolatey.org/packages/cmake)
 - [ninja](https://community.chocolatey.org/packages/ninja)

3. Run build.sh

The build will be in `<Project Directory>/build`

### Alternate Build Profiles

Several build profiles are defined in maven to support different functions
 - fast - Only runs the fast unit tests
 - integration - Runs the integration tests
 - integrationDebug - Runs the integration tests with the memory-debug jar linked






## Documentation

### Building
The documentation uses [Doxygen](https://github.com/doxygen/doxygen) to build.

On windows, it's recommended to use [Doxywizard](https://www.doxygen.nl/manual/doxywizard_usage.html) to build the documentation.

To build
 - Open the file `<Project Directory>/docs/Doxyfile`
 - Navigate to the `Run` tab
 - Click `Run doxygen`
 - Click `Show HTML output`

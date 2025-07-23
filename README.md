# Storm Engine

A multiplayer-focused game engine


This is a mirror of the codebase stored on my private git server.
It does not include the assets required for the engine to actually run.
It is uploaded here to be a showcase of the code for the engine, not a complete product.




## Building


### Cloning
When cloning the repo, make sure to grab all submodules with `git clone --recurse-submodules git@git.austinwhoover.com:studiorailgun/Renderer.git`

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

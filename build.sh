#DEPENDENCIES
#Windows: JDK17, Maven, GitBash, 7zip, choco, GCC, Make
#Linux:   JDK17, Maven, git, bash, unzip, GCC, Make

BUILD_VER=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

JRE_URL=""
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    JRE_URL=""
    echo "Must specify url to pull jre from!"
    exit 1
elif [[ "$OSTYPE" == "msys" ]]; then
    JRE_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jre_x64_windows_hotspot_17.0.12_7.zip"
else
    echo "Unsupported operating system!"
    exit 1
fi

##
## INIT RELEASE FOLDER
##
rm -rf ./build
mkdir build
mkdir ./build/assets
mkdir ./build/shared-folder

##
## BUILD NATIVE CODE
##
#completely clear native code build directory
rm -rf ./out
rm -rf ./shared-folder
#build native code
mkdir shared-folder
mkdir out
mkdir ./out/build
mkdir ./out/build/default
cmake --preset=default
cmake --build ./out/build/default


#compile project and copy into build dir
mvn clean package
cp ./target/Renderer-${BUILD_VER}.jar ./build/engine.jar
#build launcher
cd ./src/launcher/
make clean
make build
cd ../..
#copy launcher, jdk, and assets into build dir
mv ./src/launcher/launcher.exe ./build/
curl -L $JRE_URL >> jdk.zip
unzip ./jdk.zip -d ./build/
mv ./build/jdk-* ./build/jdk
rm -f ./jdk.zip
cp -r ./assets/* ./build/assets/
cp -r ./shared-folder/* ./build/shared-folder/
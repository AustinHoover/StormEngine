
# determine environment variables
LIB_ENDING=".so"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    #linux
    LIB_ENDING=".so"
elif [[ "$OSTYPE" == "cygwin" ]]; then
    # POSIX compatibility layer and Linux environment emulation for Windows
    LIB_ENDING=".dll"
elif [[ "$OSTYPE" == "msys" ]]; then
    # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
    LIB_ENDING=".dll"
elif [[ "$OSTYPE" == "win32" ]]; then
    # I'm not sure this can happen.
    LIB_ENDING=".dll"
fi


# Builds the native code
cmake --build ${PWD}/out/build --target clean
cmake -DCMAKE_BUILD_TYPE=Debug -DCMAKE_TOOLCHAIN_FILE= -D CMAKE_INSTALL_PREFIX=${PWD}/out/install -S ${PWD} -B ${PWD}/out/build -G Ninja
cmake --build ${PWD}/out/build

#copy to expected folder
mkdir -p ${PWD}/shared-folder
rm ${PWD}/shared-folder/libStormEngine${LIB_ENDING}
cp ${PWD}/out/build/libStormEngine${LIB_ENDING} ${PWD}/shared-folder/

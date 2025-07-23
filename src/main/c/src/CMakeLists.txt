
# Find sources
file(GLOB_RECURSE SOURCES CONFIGURE_DEPENDS "*.c")

# include jni
set(JAVA_AWT_LIBRARY NotNeeded)
if(EXISTS "/tmp/jni/jdk/include")
  include_directories(/tmp/jni/jdk/include)
  include_directories(/tmp/jni/jdk/include/linux)
else()
  find_package(JNI REQUIRED)
  include_directories(${JNI_INCLUDE_DIRS})
endif()

# Create shared library
add_library(StormEngine SHARED ${SOURCES})

# include public dirs
target_include_directories(StormEngine PUBLIC ${PROJECT_SOURCE_DIR}/src/main/c/includes)
# include public dirs
target_include_directories(StormEngine PUBLIC ${PROJECT_SOURCE_DIR}/src/main/c/lib)

# set props for the lib
target_compile_options(StormEngine PRIVATE -m64 -mavx -mavx2)


if (WIN32)
  # define so public header knows to export
  add_definitions(-DWIN32)
  # define so library knows to export, not import
  add_definitions(-DLIBRARY_EXPORTS)
endif ()
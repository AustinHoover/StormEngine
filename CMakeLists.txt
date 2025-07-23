cmake_minimum_required(VERSION 3.20.0)
project(StormEngine VERSION 0.1.0 LANGUAGES C)

# include and enable testing
include(CTest)
enable_testing()

# set shared library output dir
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/out/build)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/out/build)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/out/build)

#include project folders
add_subdirectory(src/main/c/src)
add_subdirectory(src/test/c)

# Find sources
# file(GLOB_RECURSE SOURCES src/main/c/src/**.c)

# # include jni
# set(JAVA_AWT_LIBRARY NotNeeded)
# if(EXISTS "/tmp/jni/jdk/include")
#   include_directories(/tmp/jni/jdk/include)
#   include_directories(/tmp/jni/jdk/include/linux)
# else()
#   find_package(JNI REQUIRED)
#   include_directories(${JNI_INCLUDE_DIRS})
# endif()

# # Create shared library
# add_library(StormEngine SHARED ${SOURCES})

# # include public dirs
# target_include_directories(StormEngine PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/src/main/c/includes)

# # set props for the lib
# target_compile_options(StormEngine PRIVATE -m64 -mavx -mavx2)

# set shared library output dir
# set_target_properties(StormEngine PROPERTIES
#   CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/out/build
# )

# # include and enable testing
# include(CTest)
# enable_testing()

# # Grab test files
# file(GLOB_RECURSE TEST_FILES ${CMAKE_SOURCE_DIR}/test/c/**.c)
# set(TEST_DRIVER test_driver.c)

# # preprocess the test files to use relative paths
# set(TEST_SOURCES "")
# foreach(TEST_FILE ${TEST_FILES})
#     file(RELATIVE_PATH REL_TEST_FILE ${CMAKE_SOURCE_DIR} ${TEST_FILE})
#     # get_filename_component (TEST_NAME ${TEST_FILE} NAME)
#     list(APPEND TEST_SOURCES ${REL_TEST_FILE})
# endforeach()

# # Add test sources
# create_test_sourcelist(TEST_SOURCE_LIST ${TEST_DRIVER} ${TEST_SOURCES})

# # Create test executable
# add_executable(test_runner ${TEST_SOURCE_LIST})


# # emit test driver to outdir
# set_target_properties(test_runner PROPERTIES
#   CMAKE_RUNTIME_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/out/build
# )

# # include header files
# target_include_directories(test_runner PRIVATE ${PROJECT_SOURCE_DIR}/src/main/c/includes)

# # link the library to the test executable
# target_link_libraries(test_runner PRIVATE StormEngine)
# # find_library(STORM_ENGINE_NAME StormEngine PATHS ${PROJECT_SOURCE_DIR}/shared-folder)
# # if(STORM_ENGINE_NAME)
# #     target_link_libraries(test_runner PRIVATE ${STORM_ENGINE_NAME})
# #     target_include_directories(test_runner PRIVATE src/main/c/includes)
# # else()
# #     message("Library not found!")
# #     message(${STORM_ENGINE_NAME})
# #     message(FATAL_ERROR ${PROJECT_SOURCE_DIR}/shared-folder)
# # endif()

# # add tests for each test source file
# foreach (TEST_FILE ${TEST_SOURCES})
#   get_filename_component (TEST_NAME ${TEST_FILE} NAME_WE)
#   get_filename_component (TEST_PATH ${TEST_FILE} DIRECTORY)
#   add_test(NAME ${TEST_NAME} COMMAND test_runner ${TEST_PATH}/${TEST_NAME})
# endforeach ()

# # make test runner depend on library
# add_dependencies(test_runner StormEngine)

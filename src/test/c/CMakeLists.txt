# include jni
set(JAVA_AWT_LIBRARY NotNeeded)
if(EXISTS "/tmp/jni/jdk/include")
  include_directories(/tmp/jni/jdk/include)
  include_directories(/tmp/jni/jdk/include/linux)
else()
  find_package(JNI REQUIRED)
  include_directories(${JNI_INCLUDE_DIRS})
endif()

# Grab test files
file(GLOB_RECURSE TEST_FILES CONFIGURE_DEPENDS "*.c")
set(TEST_DRIVER test_driver.c)

# include and enable testing
# include(CTest)
# enable_testing()

# preprocess the test files to use relative paths
set(TEST_SOURCES "")
foreach(TEST_FILE ${TEST_FILES})
    file(RELATIVE_PATH REL_TEST_FILE ${CMAKE_CURRENT_SOURCE_DIR} ${TEST_FILE})
    # get_filename_component (TEST_NAME ${TEST_FILE} NAME)
    list(APPEND TEST_SOURCES ${REL_TEST_FILE})
endforeach()

# Add test sources
create_test_sourcelist(TEST_SOURCE_LIST ${TEST_DRIVER} ${TEST_SOURCES})

# Create test executable
add_executable(test_runner ${TEST_SOURCE_LIST})

# include header files
# target_include_directories(test_runner PRIVATE ${PROJECT_SOURCE_DIR}/src/main/c/includes)

# link the library to the test executable
# target_link_libraries(test_runner PRIVATE -L${PROJECT_SOURCE_DIR}/out/build/)
# target_link_libraries(test_runner PRIVATE -lStormEngine)
target_link_libraries(test_runner PRIVATE StormEngine)
# find_library(STORM_ENGINE_NAME StormEngine PATHS ${PROJECT_SOURCE_DIR}/out/build)
# if(STORM_ENGINE_NAME)
#     target_link_libraries(test_runner PRIVATE ${STORM_ENGINE_NAME})
# else()
#     message("Library not found!")
#     message(${STORM_ENGINE_NAME})
#     message(FATAL_ERROR ${PROJECT_SOURCE_DIR}/out/build)
# endif()

# add tests for each test source file
foreach (TEST_FILE ${TEST_SOURCES})
  get_filename_component (TEST_NAME ${TEST_FILE} NAME_WE)
  get_filename_component (TEST_PATH ${TEST_FILE} DIRECTORY)
  if(TEST_PATH)
    add_test(NAME ${TEST_NAME} COMMAND test_runner ${TEST_PATH}/${TEST_NAME})
  else()
    message(FATAL_ERROR ${TEST_NAME})
    add_test(NAME ${TEST_NAME} COMMAND test_runner ${TEST_NAME})
  endif()
endforeach ()

target_compile_options(test_runner PRIVATE -m64 -mavx -mavx2)

#portably link math library
find_library(MATH_LIBRARY m)
if(MATH_LIBRARY)
    target_link_libraries(test_runner PUBLIC ${MATH_LIBRARY})
endif()

# make test runner depend on library
add_dependencies(test_runner StormEngine)


if (WIN32)
  # define so public header knows to export
  add_definitions(-DWIN32)
endif ()
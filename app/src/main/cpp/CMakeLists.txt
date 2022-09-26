# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)


set (SRC_LIST hook.cpp)
add_library(
        arthook_native
        SHARED
        ${SRC_LIST})

find_library(
        log-lib
        log )

target_link_libraries(
        arthook_native
        ${log-lib} )

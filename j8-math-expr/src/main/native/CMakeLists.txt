cmake_minimum_required(VERSION 3.22)
project(native)

set(CMAKE_CXX_STANDARD 14)

include_directories(.)

add_library(native SHARED
        util.cpp
        util.h
        vm.cpp
        vm.h)

cmake_minimum_required(VERSION 3.22)
project(native)

set(CMAKE_CXX_STANDARD 14)

add_library(native SHARED vm.cpp vm.h util.cpp util.h)

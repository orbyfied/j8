/*
 * By orbyfied (2022) 
 * License can be found at https://github.com/orbyfied
 */


#ifndef NATIVE_UTIL_H
#define NATIVE_UTIL_H

#include <cstdint>
#include <stdexcept>

// typedefs
typedef unsigned char byte;

template<typename ... Args>
static char* string_format( const std::string& format, Args ... args ) {
    int size_s = std::snprintf( nullptr, 0, format.c_str(), args ... ) + 1; // Extra space for '\0'
    if(size_s <= 0) { throw std::runtime_error( "Error during formatting." ); }
    auto size = static_cast<size_t>(size_s);
    char* buf = new char[size];
    std::snprintf(buf, size, format.c_str(), args...);
    return buf;
}

#endif //NATIVE_UTIL_H

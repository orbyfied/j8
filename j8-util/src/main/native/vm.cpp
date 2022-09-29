/*
 * By orbyfied (2022) 
 * License can be found at https://github.com/orbyfied
 */

#define m_min(a, b) (a > b ? b : a)

#include <memory.h>
#include "vm.h"

VmStackVal* ThreadValueStack::get_data() {
    return _data;
}

int ThreadValueStack::get_top() {
    return _top;
}

unsigned int ThreadValueStack::get_allocated() {
    return _alloc;
}

void ThreadValueStack::push(VmStackVal val) {
    // advance top
    _top++;
    // resize if necessary
    if (_top >= _alloc)
        resize((int)(_alloc * 1.5));

    // place data
    _data[_top] = val;
}

VmStackVal ThreadValueStack::pop() {
    // check if we have data
    if (_top == -1)
        return NIL_VALUE;

    // get data
    VmStackVal data = _data[_top];

    // reduce top
    _top--;

    // return data
    return data;
}

VmStackVal ThreadValueStack::peek() {
    return _data[_top];
}

// index format -> negative is from top of stack, positive is from bottom
VmStackVal ThreadValueStack::at(int idx) {
    if (idx < 0) {
        // return relative to top
        return _data[_top - idx];
    } else {
        // return relative to bottom
        return _data[idx];
    }
}

void ThreadValueStack::resize(unsigned int len) {
    // allocate new data
    auto* new_data = new VmStackVal[len];

    // copy old data into new buffer
    if (_data != nullptr) {
        memcpy(_data, new_data, m_min(_top, len) * sizeof(long long));
    }

    // replace old data
    this->_alloc = len;
    _data = new_data;
}

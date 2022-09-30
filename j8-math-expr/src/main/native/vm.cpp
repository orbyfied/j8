/*
 * By orbyfied (2022) 
 * License can be found at https://github.com/orbyfied
 */

#define m_min(a, b) (a > b ? b : a)

#include <memory.h>
#include "vm.h"
#include <exception>

/*
 * Value Stack
 */

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

void ThreadValueStack::expect_size(int size) {
    if (_top != size - 1)
        throw std::runtime_error(string_format("internal: expected value stack size %d", size));
}

void ThreadValueStack::resize(unsigned int len) {
    // allocate new data
    auto* new_data = new VmStackVal[len];

    // copy old data into new buffer
    if (_data != nullptr) {
        memcpy(new_data, _data, m_min(_top, len) * sizeof(VmStackVal));
    }

    // replace old data
    this->_alloc = len;
    _data = new_data;
}

/*
 * Call Stack
 */

VmCallFrame* ThreadCallStack::get_data() {
    return _data;
}

int ThreadCallStack::get_top() {
    return _top;
}

unsigned int ThreadCallStack::get_allocated() {
    return _alloc;
}

void ThreadCallStack::push(VmCallFrame val) {
    // advance top
    _top++;
    // resize if necessary
    if (_top >= _alloc)
        resize((int)(_alloc * 1.5));

    // place data
    _data[_top] = val;
}

VmCallFrame ThreadCallStack::pop() {
    // check if we have data
    if (_top == -1)
        throw std::runtime_error("internal: can't return, no call frame");

    // get data
    VmCallFrame data = _data[_top];

    // reduce top
    _top--;

    // return data
    return data;
}

VmCallFrame ThreadCallStack::peek() {
    return _data[_top];
}

// index format -> negative is from top of stack, positive is from bottom
VmCallFrame ThreadCallStack::at(int idx) {
    if (idx < 0) {
        // return relative to top
        return _data[_top - idx];
    } else {
        // return relative to bottom
        return _data[idx];
    }
}

void ThreadCallStack::resize(unsigned int len) {
    // allocate new data
    auto* new_data = (VmCallFrame*)malloc(sizeof(VmCallFrame) * len);

    // copy old data into new buffer
    if (_data != nullptr) {
        memcpy(new_data, _data, m_min(_top, len) * sizeof(VmCallFrame));
    }

    // replace old data
    this->_alloc = len;
    _data = new_data;
}

VmCallFrame new_call_frame(std::string* scope_name, uint16_t local_data_size) {
    return VmCallFrame { 0, scope_name, new uint64_t[local_data_size], local_data_size };
}

/*
 * Program Reader
 */

VmProgramReader::VmProgramReader(uint8_t* data) {
    this->_data = data;
}

uint8_t* VmProgramReader::get_data() {
    return _data;
}

uint64_t VmProgramReader::get_program_counter() const {
    return _pc;
}

template<typename T>
T VmProgramReader::read() {
    // read value
    T t = *(T*)(_data + _pc);

    // increment program counter
    _pc += sizeof(T);

    // return
    return t;
}

/*
 * VM
 */

ExprVmThread::ExprVmThread() {
    this->_call_stack  = new ThreadCallStack();
    this->_value_stack = new ThreadValueStack();
}

void ExprVmThread::set_program_data(VmProgramReader* reader) {
    this->_program_reader = reader;
}

void ExprVmThread::allocate_call_stack(unsigned int size) {
    this->_call_stack->resize(size);
}

void ExprVmThread::allocate_value_stack(unsigned int size) {
    this->_value_stack->resize(size);
}

ThreadCallStack* ExprVmThread::get_call_stack() {
    return this->_call_stack;
}

ThreadValueStack* ExprVmThread::get_value_stack() {
    return this->_value_stack;
}

VmProgramReader* ExprVmThread::get_program_reader() {
    return this->_program_reader;
}

inline void O_push_number(ExprVmThread* thread) {
    thread->get_value_stack()->push({ T_NUM,static_cast<uint64_t>(thread->get_program_reader()->read<double>()) });
}

inline void O_add(ExprVmThread* thread) {
    ThreadValueStack* stack = thread->get_value_stack();
    double a = static_cast<double>(stack->pop().value);
    double b = static_cast<double>(stack->pop().value);
    double v = a + b;
    stack->push({ T_NIL, static_cast<uint64_t>(v) });
}

void ExprVmThread::run() {
    // while running
    while (_running) {
        // get opcode
        auto opcode = _program_reader->read<short>();
        switch (opcode) {

            case OP_PUSH_NUMBER:
                O_push_number(this);
                break;

            case OP_ADD:
                O_add(this);
                break;

            default:
                throw std::runtime_error(string_format("internal: unknown opcode 0x%04x", opcode));

        }
    }
}

void ExprVmThread::interrupt(uint64_t pc) {
    // TODO
}

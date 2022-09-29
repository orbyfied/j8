/*
 * By orbyfied (2022) 
 * License can be found at https://github.com/orbyfied
 */


#ifndef NATIVE_VM_H
#define NATIVE_VM_H

#include <cstdint>
#include <string>
#include <atomic>

#include "util.h"

/*
 * Opcodes
 */

// stack operations
static constexpr uint16_t OP_PUSH_NUMBER = 0x0110;
// pops an item off the stack and discards it
static constexpr uint16_t OP_VOID_POP = 0x012;

// arithmetic
static constexpr uint16_t OP_ADD = 0x10;
static constexpr uint16_t OP_SUB = 0x11;
static constexpr uint16_t OP_MUL = 0x12;
static constexpr uint16_t OP_DIV = 0x13;
static constexpr uint16_t OP_POW = 0x14;

/*
 * Value Stack
 */

// stack value types
static constexpr uint8_t T_NIL = 0x0;
static constexpr uint8_t T_NUM = 0x1;

struct VmStackVal {
    uint8_t  type;
    uint64_t value;
};

// a nil stack value
static const VmStackVal NIL_VALUE = { T_NIL, 0 };

class ThreadValueStack {
private:
    // the pointer to the start of
    // the actual data on the stack
    VmStackVal* _data = new VmStackVal[0];
    // the index into the stack
    int _top = -1;
    // the space allocated
    unsigned int _alloc = 0;

public:
    /* getters */
    VmStackVal* get_data();
    int get_top();
    unsigned int get_allocated();

    /* operations */
    void push(VmStackVal val);
    VmStackVal pop();
    VmStackVal peek();
    // index format -> negative is from top of stack, positive is from bottom
    VmStackVal at(int idx);

    void expect_size(int size);

    /* internal */
    void resize(unsigned int len);
};

/*
 * Call Stack
 */

struct VmCallFrame {
    // the PC position
    // only updated when an interrupt
    // like a function call is triggered
    uint8_t pc = 0;
    // the function or scope name
    std::string* scope_name;

    // the local data
    uint64_t* local_data;
    // the local data size
    uint16_t  local_data_size;
};

VmCallFrame new_call_frame(std::string& scope_name, uint16_t local_data_size);

class ThreadCallStack {
private:
    // the pointer to the start of
    // the actual data on the stack
    VmCallFrame* _data = nullptr;
    // the index into the stack
    int _top = -1;
    // the space allocated
    unsigned int _alloc = 0;

public:
    /* getters */
    VmCallFrame* get_data();
    int get_top();
    unsigned int get_allocated();

    /* operations */
    void push(VmCallFrame val);
    VmCallFrame pop();
    VmCallFrame peek();
    // index format -> negative is from top of stack, positive is from bottom
    VmCallFrame at(int idx);

    /* internal */
    void resize(unsigned int len);
};

/*
 * Program
 */

class VmProgramReader {
private:
    // the data
    uint8_t* _data;
    // the program counter in bytes
    uint64_t _pc = 0;

public:
    VmProgramReader(uint8_t* data);

    uint8_t* get_data();
    uint64_t get_program_counter() const;

    template<typename T>
    T read();
};

/*
 * VM
 */

class ExprVmThread {
private:
    // the call stack
    ThreadCallStack* _call_stack;
    // the value stack
    ThreadValueStack* _value_stack;

    // the program reader
    VmProgramReader* _program_reader;

    // running flag
    std::atomic_bool _running;

public:
    ExprVmThread();

    void set_program_data(VmProgramReader* reader);
    void allocate_call_stack(unsigned int size);
    void allocate_value_stack(unsigned int size);

    ThreadCallStack*  get_call_stack();
    ThreadValueStack* get_value_stack();

    VmProgramReader* get_program_reader();

    void run();
    void interrupt(uint64_t pc);
};

#endif //NATIVE_VM_H

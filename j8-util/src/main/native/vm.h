/*
 * By orbyfied (2022) 
 * License can be found at https://github.com/orbyfied
 */


#ifndef NATIVE_VM_H
#define NATIVE_VM_H

#include <cstdint>
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
 * Stack
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
    VmStackVal* _data = nullptr;
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

    /* internal */
    void resize(unsigned int len);
};

#endif //NATIVE_VM_H

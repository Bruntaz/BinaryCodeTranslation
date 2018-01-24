package Jorvik5.Groups;

public enum J5InstructionSet {
    // Assignment
//    SET,	    // Set top of stack (long) // Not currently supported to make stack all 8 bit
    SSET,	    // Set top of stack (short)

    // Logical
    AND,        // And together top 2 (destructive)
    OR,         // Or together top 2 (destructive)
    XOR,        // Xor together top 2 (destructive)
    NOT,        // Not the top (destructive)

    // Arithmetic
    ADD,		// Add top 2
    SUB,		// Subtract top 2 (next - top)
    INC,		// Increment top
    DEC,		// Decrement top

    // Test and Compare
    TGT,		// Test greater than
    TLT,		// Test less than
    TEQ,		// Test equal
    TSZ,		// Test stack zero

    // Branching
    BRANCH, 	// Unconditional jump
    BRZERO, 	// Jump if zero flag is set
    SBRANCH,    // Short branch (unconditional)
    SBRZERO,    // Short branch if zero flag is set
    LBRANCH,    // Long branch (absolute address, 16 bits)
    IBRANCH,    // Indirect branch (branch to address at top of stack)
    CALL,		// Add old address+1 to program stack and jump to new address
    RETURN,	    // Pop top off program stack and add jump to it

    // Misc
    NOP,		// No operation (could be SKIP)
    STOP,		// Halt execution

    // J5Stack Management
    DROP,		// Remove top item
    SWAP,		// Swap top 2 on stack
    ROT,		// Rotate top 3 items in stack (XYZ -> YZX)
    RROT,		// Reverse ROT (XYZ -> ZXY)
    DUP,		// Duplicate top of stack

    // Scratch Pad Memory
    FETCH,      // Load value from J5ScratchPad memory to stack
    IFETCH,     // Indirect fetch (location from TOS)
    STORE,      // Write top of stack to memory
    ISTORE,     // Indirect store (location from TOS)
}

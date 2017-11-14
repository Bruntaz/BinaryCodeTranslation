package Jorvik5.Groups;

public enum InstructionSet {
    // Assignment
//    SET,	    // Set top of stack (long) // Not currently supported to make stack all 8 bit
    SSET,	    // Set top of stack (short)

    // ALU
    ADD,		// Add top 2
    SUB,		// Subtract top 2 (next - top)
    INC,		// Increment top
    DEC,		// Decrement top

    // Branching
    BRANCH, 	// Unconditional jump
    BRZERO, 	// Jump if top is 0
    SBRANCH,
    SBRZERO,
    IBRANCH,
    CALL,		// Add old address+1 to program stack and jump to new address
    RETURN,	    // Pop top off program stack and add jump to it

    // Misc
    NOP,		// No operation (could be SKIP)
    STOP,		// Halt execution

    // Stack management
    DROP,		// Remove top item
    SWAP,		// Swap top 2 on stack
    ROT,		// Rotate top 3 items in stack
    RROT,		// Reverse ROT
    DUP,		// Duplicate top of stack

    // Test and compare
    TGT,		// Test greater than
    TLT,		// Test less than
    TEQ,		// Test equal
    TSZ,		// Test stack zero
}

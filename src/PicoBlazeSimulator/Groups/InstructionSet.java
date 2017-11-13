package PicoBlazeSimulator.Groups;

public enum InstructionSet {
    // Register loading
    LOAD,
    STAR,

    // Logical
    AND,
    OR,
    XOR,

    // Arithmetic
    ADD,
    ADDCY,
    SUB,
    SUBCY,

    // Test and Compare
    TEST,
    TESTCY,
    COMPARE,
    COMPARECY,

    // Shift and Rotate
    SL0,
    SL1,
    SLX,
    SLA,
    RL,
    SR0,
    SR1,
    SRX,
    SRA,
    RR,

    // Register Bank Selection
    REGBANK,

    // Scratch Pad Memory
    STORE,
    FETCH,

    // Jump
    JUMP,
    JUMPAT,

    // Subroutines
    CALL,
    CALLAT,
    RETURN,
    LOADANDRETURN,

    // Version Control
    HWBUILD,

    // Directives
    CONSTANT,
}

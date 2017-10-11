public enum InstructionSet {
    // Register loading
    LOAD,

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
}

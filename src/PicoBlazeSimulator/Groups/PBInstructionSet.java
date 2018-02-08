package PicoBlazeSimulator.Groups;

public enum PBInstructionSet {
    // PBRegister loading
    LOAD,       // sX, kk/sY    Load value into register
    STAR,       // sX, sY       Send to alternate register (load register to alternate register)

    // Logical
    AND,        // sX, kk/sY    Logical and arg0 and arg1 and store in arg0
    OR,         // sX, kk/sY    Logical or arg0 and arg1 and store in arg0
    XOR,        // sX, kk/sY    Logical xor arg0 and arg1 and store in arg0

    // Arithmetic
    ADD,        // sX, kk/sY    Add arg0 and arg1 and store in arg0
    ADDCY,      // sX, kk/sY    Add arg0, arg1 and possible carry bit and store in arg0
    SUB,        // sX, kk/sY    Add arg0 and arg1 and store in arg0
    SUBCY,      // sX, kk/sY    Add arg0, arg1 and possible carry bit and store in arg0

    // Test and Compare
    TEST,       // sX, kk/sY    AND arg0 and arg1 discarding result. Z updated normally, C set if odd number of 1s
    TESTCY,     // sX, kk/sY    Same as TEST but includes carry. Previous state of flags taken into account for flags
    COMPARE,    // sX, kk/sY    SUB arg1 from arg0 discarding result. Z updated normally, C set if result is negative
    COMPARECY,  // sX, kk/sY    Same as COMPARE but includes carry. Previous Z taken into account. C same as COMPARE

    // Shift and Rotate
    SL0,        // sX           Shift bits left, spilling overflow into carry and inserting 0 at LSB
    SL1,        // sX           Shift bits left, spilling overflow into carry and inserting 1 at LSB
    SLX,        // sX           Shift bits left, spilling overflow into carry and keeping LSB the same
    SLA,        // sX           Shift bits left, spilling overflow into carry and inserting C at LSB
    RL,         // sX           Barrel shift bits left, updating C if an overflow occurs
    SR0,        // sX           Shift bits right, spilling overflow into carry and inserting 0 at MSB
    SR1,        // sX           Shift bits right, spilling overflow into carry and inserting 1 at MSB
    SRX,        // sX           Shift bits right, spilling overflow into carry and keeping MSB the same
    SRA,        // sX           Shift bits right, spilling overflow into carry and inserting C at MSB
    RR,         // sX           Barrel shift bits right, updating C if an overflow occurs

    // Register Bank Selection
    REGBANK,    // A/B          Set the active bank of registers

    // Scratch Pad Memory
    STORE,      // sX, ss/(sY)  Store the value of sX in address defined in arg1
    FETCH,      // sX, ss/(sY)  Load the value at address defined in arg1 into sX

    // Jump
    JUMP,       // aaa / Z/C/NZ/NZ, aaa     Jump to absolute address aaa. Conditional on any passed flag
    JUMPAT,     // (sX, sY)     Unconditionally jump to value defined in low 4 bits of sX concatenated to all 8 of sY

    // Subroutines
    CALL,       // aaa / Z/C/NZ/NZ, aaa     Same as JUMP but adds previous PC value to program stack
    CALLAT,     // (sX, sY)     Same as JUMP@ but adds previous PC value to program stack
    RETURN,     //  /Z/C/NZ/NC  Pop value off program stack and jump to it. Can be conditional
    LOADANDRETURN,// sX, kk     Combination of a LOAD and an unconditional RETURN. Takes 2 clock cycles

    // Version Control
    HWBUILD,    // sX           Loads sX with 8 bit value set within hardware design. Z is set normally and C = 1

    // Directives
    CONSTANT,   // name, kk     Allows named constants to be used in assembly

    // Misc
    NOP,        //              No operation
}

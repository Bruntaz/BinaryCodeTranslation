package PicoBlazeSimulator;

import PicoBlazeSimulator.Groups.InstructionSet;
import PicoBlazeSimulator.InstructionArguments.InstructionArgument;
import PicoBlazeSimulator.InstructionArguments.Literal;
import PicoBlazeSimulator.InstructionArguments.Register;

class ALU {
    private static ALU ourInstance = new ALU();
    static ALU getInstance() {
        return ourInstance;
    }

    private Registers registers = Registers.getInstance();

    // Logical
    void AND(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg0.getIntValue() & arg1.getIntValue());

        registers.setCarry(false);
        registers.setZero(arg0.getIntValue() == Register.MIN_VALUE);
    }

    void OR(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg0.getIntValue() | arg1.getIntValue());

        registers.setCarry(false);
        registers.setZero(arg0.getIntValue() == Register.MIN_VALUE);
    }

    void XOR(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg0.getIntValue() ^ arg1.getIntValue());

        registers.setCarry(false);
        registers.setZero(arg0.getIntValue() == Register.MIN_VALUE);
    }

    // Arithmetic
    void ADD(InstructionArgument arg0, InstructionArgument arg1, boolean includeCarry) {
        boolean beforeZ = registers.Z;

        int result = arg0.getIntValue() + arg1.getIntValue();

        if (includeCarry && registers.C) {
            result += 1;
        }

        if (result > Register.MAX_VALUE) {
            registers.setCarry(true);
            result = result % (Register.MAX_VALUE + 1);
        } else {
            registers.setCarry(false);
        }

        if (beforeZ) {
            registers.setZero(result == Register.MIN_VALUE);
        } else {
            registers.setZero(false);
        }

        arg0.setValue(result);
    }

    void SUB(InstructionArgument arg0, InstructionArgument arg1, boolean includeCarry) {
        boolean beforeZ = registers.Z;

        int result = arg0.getIntValue() - arg1.getIntValue();

        if (includeCarry && registers.C) {
            result -= 1;
        }

        if (result < Register.MIN_VALUE) {
            result += Register.MAX_VALUE + 1;
            registers.setCarry(true);
        } else {
            registers.setCarry(false);
        }

        if (beforeZ) {
            registers.setZero(result == Register.MIN_VALUE);
        } else {
            registers.setZero(false);
        }

        arg0.setValue(result);
    }

    // Test and Compare
    void TEST(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getIntValue() & arg1.getIntValue();

        // Carry bit true if odd number of 1 bits
        boolean carry = false;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE);
    }

    void TESTCY(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getIntValue() & arg1.getIntValue();

        // Carry bit true if odd number of 1 bits including carry bit
        boolean carry = registers.C;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    void COMPARE(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getIntValue() - arg1.getIntValue();

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE);
    }

    void COMPARECY(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getIntValue() - arg1.getIntValue();

        if (registers.C) {
            result -= 1;
        }

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    // Shift and Rotate
    void SL(InstructionSet instruction, InstructionArgument arg0) {
        int leastSignificantBit;
        switch (instruction) {
            case SL0:
                leastSignificantBit = 0;
                break;
            case SL1:
                leastSignificantBit = 1;
                break;
            case SLX:
                leastSignificantBit = arg0.getIntValue() & 0b00000001;
                break;
            default:
                leastSignificantBit = registers.C ? 1 : 0;
                break;
        }

        registers.setCarry((arg0.getIntValue() & 0b10000000) != 0);

        int newValue = (arg0.getIntValue() << 1) + leastSignificantBit;
        arg0.setValue(newValue & Register.MAX_VALUE);

        registers.setZero(arg0.getIntValue() == 0);
    }

    void SR(InstructionSet instruction, InstructionArgument arg0) {
        int mostSignificantBit;
        switch (instruction) {
            case SR0:
                mostSignificantBit = 0;
                break;
            case SR1:
                mostSignificantBit = 0b10000000;
                break;
            case SRX:
                mostSignificantBit = arg0.getIntValue() & 0b10000000;
                break;
            default:
                mostSignificantBit = registers.C ? 0b10000000 : 0;
                break;
        }

        registers.setCarry((arg0.getIntValue() & 0b00000001) != 0);

        int newValue = (arg0.getIntValue() >> 1) + mostSignificantBit;
        arg0.setValue(newValue & Register.MAX_VALUE);

        registers.setZero(arg0.getIntValue() == 0);
    }

    void RL(InstructionArgument arg0) {
        registers.setCarry((arg0.getIntValue() & 0b10000000) != 0);
        SL(InstructionSet.SLA, arg0);
    }

    void RR(InstructionArgument arg0) {
        registers.setCarry((arg0.getIntValue() & 0b00000001) != 0);
        SR(InstructionSet.SRA, arg0);
    }
}

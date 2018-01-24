package PicoBlazeSimulator;

import PicoBlazeSimulator.Groups.PBInstructionSet;
import PicoBlazeSimulator.InstructionArguments.PBInstructionArgument;
import PicoBlazeSimulator.InstructionArguments.PBRegister;

class PBALU {
    private static PBALU ourInstance = new PBALU();
    static PBALU getInstance() {
        return ourInstance;
    }

    private PBRegisters registers = PBRegisters.getInstance();

    // Logical
    void AND(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        arg0.setValue(arg0.getIntValue() & arg1.getIntValue());

        registers.setCarry(false);
        registers.setZero(arg0.getIntValue() == PBRegister.MIN_VALUE);
    }

    void OR(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        arg0.setValue(arg0.getIntValue() | arg1.getIntValue());

        registers.setCarry(false);
        registers.setZero(arg0.getIntValue() == PBRegister.MIN_VALUE);
    }

    void XOR(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        arg0.setValue(arg0.getIntValue() ^ arg1.getIntValue());

        registers.setCarry(false);
        registers.setZero(arg0.getIntValue() == PBRegister.MIN_VALUE);
    }

    // Arithmetic
    void ADD(PBInstructionArgument arg0, PBInstructionArgument arg1, boolean includeCarry) {
        boolean beforeZ = registers.Z;

        int result = arg0.getIntValue() + arg1.getIntValue();

        if (includeCarry && registers.C) {
            result += 1;
        }

        if (result > PBRegister.MAX_VALUE) {
            registers.setCarry(true);
            result = result % (PBRegister.MAX_VALUE + 1);
        } else {
            registers.setCarry(false);
        }

        if (includeCarry && !beforeZ) {
            registers.setZero(false);
        } else {
            registers.setZero(result == PBRegister.MIN_VALUE);
        }

        arg0.setValue(result);
    }

    void SUB(PBInstructionArgument arg0, PBInstructionArgument arg1, boolean includeCarry) {
        boolean beforeZ = registers.Z;

        int result = arg0.getIntValue() - arg1.getIntValue();

        if (includeCarry && registers.C) {
            result -= 1;
        }

        if (result < PBRegister.MIN_VALUE) {
            result += PBRegister.MAX_VALUE + 1;
            registers.setCarry(true);
        } else {
            registers.setCarry(false);
        }


        if (includeCarry && !beforeZ) {
            registers.setZero(false);
        } else {
            registers.setZero(result == PBRegister.MIN_VALUE);
        }

        arg0.setValue(result);
    }

    // Test and Compare
    void TEST(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        int result = arg0.getIntValue() & arg1.getIntValue();

        // Carry bit true if odd number of 1 bits
        boolean carry = false;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == PBRegister.MIN_VALUE);
    }

    void TESTCY(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        int result = arg0.getIntValue() & arg1.getIntValue();

        // Carry bit true if odd number of 1 bits including carry bit
        boolean carry = registers.C;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == PBRegister.MIN_VALUE && registers.Z);
    }

    void COMPARE(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        int result = arg0.getIntValue() - arg1.getIntValue();

        registers.setCarry(result < 0);
        registers.setZero(result == PBRegister.MIN_VALUE);
    }

    void COMPARECY(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        int result = arg0.getIntValue() - arg1.getIntValue();

        if (registers.C) {
            result -= 1;
        }

        registers.setCarry(result < 0);
        registers.setZero(result == PBRegister.MIN_VALUE && registers.Z);
    }

    // Shift and Rotate
    void SL(PBInstructionSet instruction, PBInstructionArgument arg0) {
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
        arg0.setValue(newValue & PBRegister.MAX_VALUE);

        registers.setZero(arg0.getIntValue() == 0);
    }

    void SR(PBInstructionSet instruction, PBInstructionArgument arg0) {
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
        arg0.setValue(newValue & PBRegister.MAX_VALUE);

        registers.setZero(arg0.getIntValue() == 0);
    }

    void RL(PBInstructionArgument arg0) {
        registers.setCarry((arg0.getIntValue() & 0b10000000) != 0);
        SL(PBInstructionSet.SLA, arg0);
    }

    void RR(PBInstructionArgument arg0) {
        registers.setCarry((arg0.getIntValue() & 0b00000001) != 0);
        SR(PBInstructionSet.SRA, arg0);
    }
}

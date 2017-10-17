public class ALU {
    private static ALU ourInstance = new ALU();
    public static ALU getInstance() {
        return ourInstance;
    }

    private Registers registers = Registers.getInstance();

    // Logical
    public void AND(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg0.getValue() & arg1.getValue());

        registers.setCarry(false);
        registers.setZero(arg0.getValue() == Register.MIN_VALUE);
    }

    public void OR(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg0.getValue() | arg1.getValue());

        registers.setCarry(false);
        registers.setZero(arg0.getValue() == Register.MIN_VALUE);
    }

    public void XOR(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg0.getValue() ^ arg1.getValue());

        registers.setCarry(false);
        registers.setZero(arg0.getValue() == Register.MIN_VALUE);
    }

    // Arithmetic
    public void ADD(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() + arg1.getValue();

        if (result > Register.MAX_VALUE) {
            registers.setCarry(true);
            result = result % (Register.MAX_VALUE + 1);
        } else {
            registers.setCarry(false);
        }

        registers.setZero(result == Register.MIN_VALUE);

        arg0.setValue(result);
    }

    /*
      This was just copied from the Python implementation. It could likely be implemented better.
     */
    public void ADDCY(InstructionArgument arg0, InstructionArgument arg1) {
        boolean beforeZ = registers.Z;

        if (registers.C) {
            ADD(arg0, new Constant(1));
        }

        ADD(arg0, arg1);

        if (!beforeZ) {
            registers.setZero(false);
        }
    }

    public void SUB(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() - arg1.getValue();

        if (result < Register.MIN_VALUE) {
            result += Register.MAX_VALUE + 1;
            registers.setCarry(true);
        } else {
            registers.setCarry(false);
        }

        registers.setZero(result == Register.MIN_VALUE);

        arg0.setValue(result);
    }

    /*
      This was just copied from the Python implementation. It could likely be implemented better.
     */
    public void SUBCY(InstructionArgument arg0, InstructionArgument arg1) {
        boolean beforeZ = registers.Z;

        if (registers.C) {
            SUB(arg0, new Constant(1));
        }

        SUB(arg0, arg1);

        if (!beforeZ) {
            registers.setZero(false);
        }
    }

    // Test and Compare
    public void TEST(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() & arg1.getValue();

        // Carry bit true if odd number of 1 bits
        boolean carry = false;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE);
    }

    public void TESTCY(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() & arg1.getValue();

        // Carry bit true if odd number of 1 bits including carry bit
        boolean carry = registers.C;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    public void COMPARE(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() - arg1.getValue();

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE);
    }

    public void COMPARECY(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() - arg1.getValue();

        if (registers.C) {
            result -= 1;
        }

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    // Shift and Rotate
    public void SL(InstructionSet instruction, InstructionArgument arg0) {
        int leastSignificantBit;
        switch (instruction) {
            case SL0:
                leastSignificantBit = 0;
                break;
            case SL1:
                leastSignificantBit = 1;
                break;
            case SLX:
                leastSignificantBit = arg0.getValue() & 0b00000001;
                break;
            default:
                leastSignificantBit = registers.C ? 1 : 0;
                break;
        }

        registers.setCarry((arg0.getValue() & 0b10000000) != 0);

        int newValue = (arg0.getValue() << 1) + leastSignificantBit;
        arg0.setValue(newValue & Register.MAX_VALUE);

        registers.setZero(arg0.getValue() == 0);
    }

    public void SR(InstructionSet instruction, InstructionArgument arg0) {
        int mostSignificantBit;
        switch (instruction) {
            case SR0:
                mostSignificantBit = 0;
                break;
            case SR1:
                mostSignificantBit = 0b10000000;
                break;
            case SRX:
                mostSignificantBit = arg0.getValue() & 0b10000000;
                break;
            default:
                mostSignificantBit = registers.C ? 0b10000000 : 0;
                break;
        }

        registers.setCarry((arg0.getValue() & 0b00000001) != 0);

        int newValue = (arg0.getValue() >> 1) + mostSignificantBit;
        arg0.setValue(newValue & Register.MAX_VALUE);

        registers.setZero(arg0.getValue() == 0);
    }

    public void RL(InstructionArgument arg0) {
        registers.setCarry((arg0.getValue() & 0b10000000) != 0);
        SL(InstructionSet.SLA, arg0);
    }

    public void RR(InstructionArgument arg0) {
        registers.setCarry((arg0.getValue() & 0b00000001) != 0);
        SR(InstructionSet.SRA, arg0);
    }
}

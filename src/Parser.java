public class Parser {
    Registers registers;

    // Register loading
    private void LOAD(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg1.getValue());
    }

    // Logical
    private void AND(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg0.getValue() & instruction.arg1.getValue());

        registers.setCarry(false);
        registers.setZero(instruction.arg0.getValue() == Register.MIN_VALUE);
    }

    private void OR(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg0.getValue() | instruction.arg1.getValue());

        registers.setCarry(false);
        registers.setZero(instruction.arg0.getValue() == Register.MIN_VALUE);
    }

    private void XOR(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg0.getValue() ^ instruction.arg1.getValue());

        registers.setCarry(false);
        registers.setZero(instruction.arg0.getValue() == Register.MIN_VALUE);
    }

    // Arithmetic
    private void ADD(Instruction instruction) {
        InstructionArgument arg0 = instruction.arg0;
        InstructionArgument arg1 = instruction.arg1;

        int result = arg0.getValue() + arg1.getValue();

        if (result >= Register.MAX_VALUE) {
            registers.setCarry(true);
            result = result % Register.MAX_VALUE;
        } else {
            registers.setCarry(false);
        }

        registers.setZero(result == Register.MIN_VALUE);

        arg0.setValue(result);
    }

    /*
      This was just copied from the Python implementation. It could likely be implemented better.
     */
    private void ADDCY(Instruction instruction) {
        boolean beforeZ = registers.Z;

        if (registers.C) {
            ADD(new Instruction(instruction.instruction, instruction.arg0, new Constant(1)));
        }

        ADD(instruction);

        if (!beforeZ) {
            registers.setZero(false);
        }
    }

    private void SUB(Instruction instruction) {
        InstructionArgument arg0 = instruction.arg0;
        InstructionArgument arg1 = instruction.arg1;

        int result = arg0.getValue() - arg1.getValue();

        if (result < Register.MIN_VALUE) {
            result += Register.MAX_VALUE;
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
    private void SUBCY(Instruction instruction) {
        boolean beforeZ = registers.Z;

        if (registers.C) {
            SUB(new Instruction(instruction.instruction, instruction.arg0, new Constant(1)));
        }

        SUB(instruction);

        if (!beforeZ) {
            registers.setZero(false);
        }
    }

    // Test and Compare
    private void TEST(Instruction instruction) {
        int result = instruction.arg0.getValue() & instruction.arg1.getValue();

        // Carry bit true if odd number of 1 bits
        boolean carry = false;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE);
    }

    private void TESTCY(Instruction instruction) {
        int result = instruction.arg0.getValue() & instruction.arg1.getValue();

        // Carry bit true if odd number of 1 bits including carry bit
        boolean carry = registers.C;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    private void COMPARE(Instruction instruction) {
        int result = instruction.arg0.getValue() - instruction.arg1.getValue();

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE);
    }

    private void COMPARECY(Instruction instruction) {
        int result = instruction.arg0.getValue() - instruction.arg1.getValue();

        if (registers.C) {
            result -= 1;
        }

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    public void parse(Instruction[] program) {
        for (Instruction instruction : program) {
            if (instruction == null) {
                continue;
            }

            switch (instruction.instruction) {
                // Register loading
                case LOAD:
                    LOAD(instruction);
                    break;

                // Logical
                case AND:
                    AND(instruction);
                    break;
                case OR:
                    OR(instruction);
                    break;
                case XOR:
                    XOR(instruction);
                    break;

                // Arithmetic
                case ADD:
                    ADD(instruction);
                    break;
                case ADDCY:
                    ADDCY(instruction);
                    break;
                case SUB:
                    SUB(instruction);
                    break;
                case SUBCY:
                    SUBCY(instruction);
                    break;

                // Test and Compare
                case TEST:
                    TEST(instruction);
                    break;
                case TESTCY:
                    TESTCY(instruction);
                    break;
                case COMPARE:
                    COMPARE(instruction);
                    break;
                case COMPARECY:
                    COMPARECY(instruction);
                    break;

                default:
                    throw new UnsupportedOperationException("Unrecognised instruction. Has the instruction been added to the switch statement in Parser?");
            }

            System.out.println(registers);
        }
    }

    public Parser(Registers registers) {
        this.registers = registers;
    }
}

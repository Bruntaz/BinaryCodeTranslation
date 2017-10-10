public class Parser {
    Registers registers;

    private void LOAD(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg1.getValue());
    }

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

    public void parse(Instruction[] program) {
        for (Instruction instruction : program) {
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
            }

            System.out.format("%s: %s, C=%b, Z=%b\n", Integer.toHexString(registers.getRegister(RegisterName.s0).getValue()), Integer.toBinaryString(registers.getRegister(RegisterName.s0).getValue()), registers.C, registers.Z);
            System.out.format("%s: %s, C=%b, Z=%b\n", Integer.toHexString(registers.getRegister(RegisterName.s1).getValue()), Integer.toBinaryString(registers.getRegister(RegisterName.s1).getValue()), registers.C, registers.Z);
            System.out.println();
        }
    }

    public Parser(Registers registers) {
        this.registers = registers;
    }
}

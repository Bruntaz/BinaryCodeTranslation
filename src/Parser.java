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

    public void parse(Instruction[] program) {
        for (Instruction instruction : program) {
            System.out.format("%d: %s, C=%b, Z=%b\n", registers.getRegister(RegisterName.s0).getValue(), Integer.toBinaryString(registers.getRegister(RegisterName.s0).getValue()), registers.C, registers.Z);

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
                case SUB:
                    SUB(instruction);
                    break;
            }
        }
    }

    public Parser(Registers registers) {
        this.registers = registers;
    }
}

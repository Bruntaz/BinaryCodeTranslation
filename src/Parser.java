public class Parser {
    Registers registers;

    private void LOAD(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg1.getValue());
    }

    private void AND(Instruction instruction) {
        System.out.println(Integer.toBinaryString(instruction.arg0.getValue()) + " AND");
        System.out.println(Integer.toBinaryString(instruction.arg1.getValue()) + " EQUALS");
        instruction.arg0.setValue(instruction.arg0.getValue() & instruction.arg1.getValue());
        System.out.println(Integer.toBinaryString(instruction.arg0.getValue()) + " (" + Integer.toHexString(instruction.arg0.getValue()) + ")");

        registers.setCarry(false);

        if (instruction.arg0.getValue() == 0) {
            registers.setZero(true);
        } else {
            registers.setZero(false);
        }
    }

    private void ADD(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg0.add(instruction.arg1));
    }

    private void SUB(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg0.subtract(instruction.arg1));
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

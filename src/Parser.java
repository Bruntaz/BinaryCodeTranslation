public class Parser {
    Registers registers;

    private void ADD(Instruction instruction) {
//        instruction.arg0.setValue((byte) (instruction.arg0.getValue() + instruction.arg1.getValue()));
//        registers.addToRegister(instruction.arg0, instruction.arg1.getValue());
        instruction.arg0.setValue(instruction.arg0.add(instruction.arg1));
    }

    private void SUB(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg0.subtract(instruction.arg1));
    }

    public void parse(Instruction[] program) {
        for (Instruction instruction : program) {
            System.out.format("%d: %s, C=%b, Z=%b\n", registers.getRegister(RegisterName.s0).getValue(), Integer.toBinaryString(registers.getRegister(RegisterName.s0).getValue()), registers.C, registers.Z);

            switch (instruction.instruction) {
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

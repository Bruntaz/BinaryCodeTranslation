public class Parser {
    Registers registers;

    private void ADD(Instruction instruction) {
//        instruction.arg0.setValue((byte) (instruction.arg0.getValue() + instruction.arg1.getValue()));
//        registers.addToRegister(instruction.arg0, instruction.arg1.getValue());
        instruction.arg0.setValue(instruction.arg0.add(instruction.arg1));
    }

    public void parse(Instruction[] program) {
        for (Instruction instruction : program) {
            switch (instruction.instruction) {
                case ADD:
                    ADD(instruction);
            }
        }
    }

    public Parser(Registers registers) {
        this.registers = registers;
    }
}

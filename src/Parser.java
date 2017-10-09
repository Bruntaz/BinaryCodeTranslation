public class Parser {
    Registers registers = new Registers();

    private void ADD(RegisterName register, int toAdd) {
        registers.addToRegister(register, toAdd);
    }

    private void ADD(RegisterName register, RegisterName toAdd) {
        this.ADD(register, registers.getRegister(toAdd));
    }

    public void parse(Instruction[] program) {

    }
}

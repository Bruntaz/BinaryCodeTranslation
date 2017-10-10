public class Register implements InstructionArgument {
    Registers registers;
    RegisterName registerName;
    int value;

    public int add(InstructionArgument other) {
        int result = this.getValue() + other.getValue();

        if (result > 255) {
            registers.setCarry(true);
            result = result % 256;
        } else {
            registers.setCarry(false);
        }

        if (result == 0) {
            registers.setZero(true);
        } else {
            registers.setZero(false);
        }

        return result;
    }

    public RegisterName getRegisterName() {
        return registerName;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        this.value = newValue;
    }

    public Register(Registers registers, RegisterName registerName) {
        this.registers = registers;
        this.registerName = registerName;
        this.value = 0;
    }
}

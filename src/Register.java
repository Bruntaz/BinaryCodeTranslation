public class Register implements InstructionArgument {
    static int MAX_VALUE = 256;
    static int MIN_VALUE = 0;

    Registers registers;
    RegisterName registerName;
    int value;

    public int add(InstructionArgument other) {
        int result = this.getValue() + other.getValue();

        if (result >= MAX_VALUE) {
            registers.setCarry(true);
            result = result % MAX_VALUE;
        } else {
            registers.setCarry(false);
        }

        if (result == MIN_VALUE) {
            registers.setZero(true);
        } else {
            registers.setZero(false);
        }

        return result;
    }

    public int subtract(InstructionArgument other) {
        int result = this.getValue() - other.getValue();

        if (result < MIN_VALUE) {
            result += MAX_VALUE;
            registers.setCarry(true);
        } else {
            registers.setCarry(false);
        }

        if (result == MIN_VALUE) {
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
        this.value = MIN_VALUE;
    }
}

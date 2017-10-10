public class Register implements InstructionArgument {
    static int MAX_VALUE = 256;
    static int MIN_VALUE = 0;

    Registers registers;
    RegisterName registerName;
    int value;

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

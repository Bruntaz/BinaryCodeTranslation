import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

public class Register implements InstructionArgument {
    static final int MAX_VALUE = 255;
    static final int MIN_VALUE = 0;

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
        if (newValue < MIN_VALUE || newValue > MAX_VALUE) {
            throw new ValueException("Register set to an illegal number (" + newValue + ")");
        }
        this.value = newValue;
    }

    public Register(Registers registers, RegisterName registerName) {
        this.registers = registers;
        this.registerName = registerName;
        this.value = MIN_VALUE;
    }
}

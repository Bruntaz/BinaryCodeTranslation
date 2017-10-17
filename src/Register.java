import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

public class Register implements InstructionArgument {
    static final int MAX_VALUE = 255;
    static final int MIN_VALUE = 0;

    private Registers registers;
    private RegisterName registerName;
    private int aValue;
    private int bValue;

    public RegisterName getRegisterName() {
        return registerName;
    }

    @Override
    public int getValue() {
        return registers.aRegisterBank ? aValue : bValue;
    }

    @Override
    public void setValue(int newValue) {
        if (newValue < MIN_VALUE || newValue > MAX_VALUE) {
            throw new ValueException("Register set to an illegal number (" + newValue + ")");
        }

        if (registers.aRegisterBank) {
            this.aValue = newValue;
        } else {
            this.bValue = newValue;
        }
    }

    public void reset() {
        aValue = 0;
        bValue = 0;
    }

    public Register(Registers registers, RegisterName registerName) {
        this.registers = registers;
        this.registerName = registerName;
        this.aValue = MIN_VALUE;
        this.bValue = MIN_VALUE;
    }
}

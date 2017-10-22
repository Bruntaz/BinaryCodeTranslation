import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

public class AbsoluteAddress implements InstructionArgument {
    static final int MAX_VALUE = 0xFFF;
    static final int MIN_VALUE = 0x000;

    private int value;

    @Override
    public boolean hasStringValue() {
        return false;
    }

    @Override
    public String getStringValue() {
        return null;
    }

    @Override
    public boolean hasIntValue() {
        return true;
    }

    @Override
    public int getIntValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        if (value > MAX_VALUE || value < MIN_VALUE) {
            throw new ValueException("Address set to an illegal number (" + newValue + ")");
        }

        value = newValue;
    }

    @Override
    public void setValue(String newValue) {
    }

    public AbsoluteAddress(int address) {
        value = address;
    }
}

package Jorvik5.InstructionArguments;

public class ShortRelativeAddress implements InstructionArgument {
    public static final int MAX_VALUE = 0xF;
    public static final int MIN_VALUE = 0x0;

    private int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        if (newValue > MAX_VALUE || newValue < MIN_VALUE) {
            throw new Error("Short relative address set to illegal value (" + newValue + ")");
        }

        value = newValue;
    }

    public ShortRelativeAddress(int address) {
        value = address;
    }
}
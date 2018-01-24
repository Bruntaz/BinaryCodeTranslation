package Jorvik5.InstructionArguments;

public class J5RelativeAddress implements J5InstructionArgument {
    public static final int MAX_VALUE = 0xFF;
    public static final int MIN_VALUE = 0x00;

    private int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        if (newValue > MAX_VALUE || newValue < MIN_VALUE) {
            throw new Error("Relative address set to illegal value (" + newValue + ")");
        }

        value = newValue;
    }

    public J5RelativeAddress(int address) {
        value = address;
    }
}
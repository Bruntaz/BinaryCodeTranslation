package Jorvik5.InstructionArguments;

public class J5AbsoluteAddress implements J5InstructionArgument {
    public static final int MAX_VALUE = 0xFFF;
    public static final int MIN_VALUE = 0x000;

    private int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        if (newValue > MAX_VALUE || newValue < MIN_VALUE) {
            throw new Error("Absolute address set to illegal value (" + newValue + ")");
        }

        value = newValue;
    }

    public J5AbsoluteAddress(int address) {
        value = address;
    }
}

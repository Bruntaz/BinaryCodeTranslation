package Jorvik5.InstructionArguments;

public class AbsoluteAddress implements InstructionArgument {
    public static final int MAX_VALUE = 0xFFFF;
    public static final int MIN_VALUE = 0x0000;

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

    public AbsoluteAddress(int address) {
        value = address;
    }
}

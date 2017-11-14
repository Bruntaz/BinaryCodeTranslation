package Jorvik5.InstructionArguments;

public class Literal implements InstructionArgument {
    public static final int MAX_VALUE = 0xFFFF;
    public static final int MIN_VALUE = 0;

    private int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        if (newValue > MAX_VALUE) {
            throw new Error("Literal above max value (" + newValue + " > " + MAX_VALUE + ")");
        } else if (newValue < MIN_VALUE) {
            throw new Error("Literal below min value (" + newValue + " < " + MIN_VALUE + ")");
        }

        value = newValue;
    }

    public Literal(int value) {
        setValue(value);
    }
}

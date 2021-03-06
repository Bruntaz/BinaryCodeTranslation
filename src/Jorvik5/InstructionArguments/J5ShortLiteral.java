package Jorvik5.InstructionArguments;

public class J5ShortLiteral implements J5InstructionArgument {
    public static final int MAX_VALUE = 0xFF;
    public static final int MIN_VALUE = 0;

    private int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        if (newValue > MAX_VALUE) {
            throw new Error("J5ShortLiteral above max value (" + newValue + " > " + MAX_VALUE + ")");
        } else if (newValue < MIN_VALUE) {
            throw new Error("J5ShortLiteral below min value (" + newValue + " < " + MIN_VALUE + ")");
        }

        value = newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof J5ShortLiteral)) {
            return false;
        }

        J5ShortLiteral i = (J5ShortLiteral) o;

        return value == i.value;
    }

    @Override
    public int hashCode() {
        return "J5RelativeAddress".hashCode() ^ value;
    }

    public J5ShortLiteral(int value) {
        setValue(value);
    }
}

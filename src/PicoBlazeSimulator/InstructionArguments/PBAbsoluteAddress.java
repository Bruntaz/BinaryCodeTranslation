package PicoBlazeSimulator.InstructionArguments;

public class PBAbsoluteAddress implements PBInstructionArgument {
    public static final int MAX_VALUE = 0xFFF;
    public static final int MIN_VALUE = 0x000;

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
            throw new Error("Address set to an illegal number (" + newValue + ")");
        }

        value = newValue;
    }

    @Override
    public void setValue(String newValue) {
    }

    public PBAbsoluteAddress(int address) {
        value = address;
    }
}

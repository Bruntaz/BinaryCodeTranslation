package PicoBlazeSimulator.InstructionArguments;

public class NamedArgument implements InstructionArgument {
    private String value;

    @Override
    public boolean hasStringValue() {
        return true;
    }

    @Override
    public String getStringValue() {
        return value;
    }

    @Override
    public boolean hasIntValue() {
        return false;
    }

    @Override
    public int getIntValue() {
        return -1;
    }

    @Override
    public void setValue(int newValue) {
    }

    @Override
    public void setValue(String newValue) {
        value = newValue;
    }

    public NamedArgument(String name) {
        value = name;
    }
}

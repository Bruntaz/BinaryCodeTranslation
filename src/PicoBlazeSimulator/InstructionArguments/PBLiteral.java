package PicoBlazeSimulator.InstructionArguments;

public class PBLiteral implements PBInstructionArgument {
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
        value = newValue;
    }

    @Override
    public void setValue(String newValue) {

    }

    public PBLiteral(int value) {
        this.value = value;
    }
}

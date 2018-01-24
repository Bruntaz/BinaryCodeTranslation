package PicoBlazeSimulator.InstructionArguments;

public class PBFlagArgument implements PBInstructionArgument {
    public static final String C = "C";
    public static final String NC = "NC";
    public static final String Z = "Z";
    public static final String NZ = "NZ";

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

    public PBFlagArgument(String flag) {
        value = flag;
    }
}

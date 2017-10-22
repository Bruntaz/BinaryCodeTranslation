public class FlagArgument implements InstructionArgument {
    static final String C = "C";
    static final String NC = "NC";
    static final String Z = "Z";
    static final String NZ = "NZ";

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

    public FlagArgument(String flag) {
        value = flag;
    }
}

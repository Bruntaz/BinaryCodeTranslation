public class NoArgument implements InstructionArgument {
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
    }
}

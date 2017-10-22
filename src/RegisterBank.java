public class RegisterBank implements InstructionArgument {
    static final String A = "A";
    static final String B = "B";

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

    public RegisterBank(String registerBank) {
        this.value = registerBank;
    }
}

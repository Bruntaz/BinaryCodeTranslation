package PicoBlazeSimulator.InstructionArguments;

public class PBRegisterBank implements PBInstructionArgument {
    public static final String A = "A";
    public static final String B = "B";

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

    public PBRegisterBank(String registerBank) {
        if (!(registerBank.equals(A) || registerBank.equals(B))) {
            throw new Error("PBRegisterBank argument neither A or B");
        }
        this.value = registerBank;
    }
}

public class FlagArgument implements InstructionArgument {
    static final int C = 0;
    static final int NC = 1;
    static final int Z = 2;
    static final int NZ = 3;

    int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        value = newValue;
    }

    public FlagArgument(int flag) {
        value = flag;
    }
}

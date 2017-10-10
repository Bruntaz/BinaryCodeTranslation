public class Constant implements InstructionArgument {
    int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        value = newValue;
    }

    public Constant(int value) {
        this.value = value;
    }
}

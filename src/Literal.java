public class Literal implements InstructionArgument {
    int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int newValue) {
        value = newValue;
    }

    public Literal(int value) {
        this.value = value;
    }
}

public class Register implements InstructionArgument {
    byte value;

    @Override
    public byte getValue() {
        return value;
    }

    @Override
    public void setValue(byte newValue) {
        this.value = newValue;
    }

    public Register() {
        this.value = Byte.MIN_VALUE;
    }
}

public class RegisterBank implements InstructionArgument {
    // 1 = A, 0 = B. This is because InstructionArguments need to be integers
    private int A = 1;

    @Override
    public int getValue() {
        return A;
    }

    @Override
    public void setValue(int newValue) {
        A = newValue;
    }

    public RegisterBank(int A) {
        this.A = A;
    }
}

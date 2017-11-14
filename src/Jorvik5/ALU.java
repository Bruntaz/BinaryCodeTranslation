package Jorvik5;

public class ALU {
    private static ALU ourInstance = new ALU();
    public static ALU getInstance() {
        return ourInstance;
    }

    private Stack stack = Stack.getInstance();
    private Registers registers = Registers.getInstance();

    void ADD() {
        int top = stack.pop();
        int next = stack.pop();
        int addition = top + next;

        if (addition > Stack.MAX_VALUE) {
            addition = addition % (Stack.MAX_VALUE + 1);
            registers.setCarry(true);

        } else {
            registers.setCarry(false);
        }

        stack.push(addition);
        registers.setZero(addition == 0);
    }

    void SUB() {
        int top = stack.pop();
        int next = stack.pop();
        int subtraction = next - top;

        if (subtraction < Stack.MIN_VALUE) {
            subtraction += Stack.MAX_VALUE + 1;
            registers.setCarry(true);

        } else {
            registers.setCarry(false);
        }

        stack.push(subtraction);
        registers.setZero(subtraction == 0);
    }

    void INC() {
        int top = stack.pop();
        int incremented = top + 1;

        if (incremented > Stack.MAX_VALUE) {
            incremented = incremented % (Stack.MAX_VALUE + 1);
            registers.setCarry(true);

        } else {
            registers.setCarry(false);
        }

        stack.push(incremented);
        registers.setZero(incremented == 0);
    }

    void DEC() {
        int top = stack.pop();
        int decremented = top - 1;

        if (decremented < Stack.MIN_VALUE) {
            decremented += Stack.MAX_VALUE + 1;
            registers.setCarry(true);

        } else {
            registers.setCarry(false);
        }

        stack.push(decremented);
        registers.setZero(decremented == 0);
    }
}

package Jorvik5;

public class ALU {
    private static ALU ourInstance = new ALU();
    public static ALU getInstance() {
        return ourInstance;
    }

    private Stack stack = Stack.getInstance();
    private Flags flags = Flags.getInstance();

    void ADD() {
        int top = stack.pop();
        int next = stack.pop();
        int addition = top + next;

        if (addition > Stack.MAX_VALUE) {
            addition = addition % (Stack.MAX_VALUE + 1);
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        stack.push(addition);
        flags.setZero(addition == 0);
    }

    void SUB() {
        int top = stack.pop();
        int next = stack.pop();
        int subtraction = next - top;

        if (subtraction < Stack.MIN_VALUE) {
            subtraction += Stack.MAX_VALUE + 1;
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        stack.push(subtraction);
        flags.setZero(subtraction == 0);
    }

    void INC() {
        int top = stack.pop();
        int incremented = top + 1;

        if (incremented > Stack.MAX_VALUE) {
            incremented = incremented % (Stack.MAX_VALUE + 1);
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        stack.push(incremented);
        flags.setZero(incremented == 0);
    }

    void DEC() {
        int top = stack.pop();
        int decremented = top - 1;

        if (decremented < Stack.MIN_VALUE) {
            decremented += Stack.MAX_VALUE + 1;
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        stack.push(decremented);
        flags.setZero(decremented == 0);
    }

    void TGT() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setZero(top > next);
    }

    void TLT() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setZero(top < next);
    }

    void TEQ() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setZero(top == next);
    }

    void TSZ() {
        flags.setZero(stack.getTop() == 0);
    }

    void AND() {
        int top = stack.pop();
        int next = stack.pop();

        stack.push(top & next);
        flags.setZero(stack.getTop() == 0);
    }

    void OR() {
        int top = stack.pop();
        int next = stack.pop();

        stack.push(top | next);
        flags.setZero(stack.getTop() == 0);
    }

    void XOR() {
        int top = stack.pop();
        int next = stack.pop();

        stack.push(top ^ next);
        flags.setZero(stack.getTop() == 0);
    }

    void NOT() {
        stack.setTop(stack.getTop() ^ Stack.MAX_VALUE);
        flags.setZero(stack.getTop() == 0);
    }
}

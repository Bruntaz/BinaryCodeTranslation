package Jorvik5;

public class J5ALU {
    private static J5ALU ourInstance = new J5ALU();
    public static J5ALU getInstance() {
        return ourInstance;
    }

    private J5Stack stack = J5Stack.getInstance();
    private J5Flags flags = J5Flags.getInstance();

    void ADD() {
        int top = stack.pop();
        int next = stack.pop();
        int addition = top + next;

        if (addition > J5Stack.MAX_VALUE) {
            addition = addition % (J5Stack.MAX_VALUE + 1);
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        stack.push(addition);
        flags.setZero(addition == J5Stack.MIN_VALUE);
    }

    void SUB() {
        int top = stack.pop();
        int next = stack.pop();
        int subtraction = next - top;

        if (subtraction < J5Stack.MIN_VALUE) {
            subtraction += J5Stack.MAX_VALUE + 1;
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        stack.push(subtraction);
        flags.setZero(subtraction == J5Stack.MIN_VALUE);
    }

    void INC() {
        int top = stack.pop();
        int incremented = top + 1;

        if (incremented > J5Stack.MAX_VALUE) {
            incremented = incremented % (J5Stack.MAX_VALUE + 1);
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        stack.push(incremented);
        flags.setZero(incremented == J5Stack.MIN_VALUE);
    }

    void DEC() {
        int top = stack.pop();
        int decremented = top - 1;

        if (decremented < J5Stack.MIN_VALUE) {
            decremented += J5Stack.MAX_VALUE + 1;
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        stack.push(decremented);
        flags.setZero(decremented == J5Stack.MIN_VALUE);
    }

    void TGT() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setCarry(top > next);
        flags.setZero(false);
    }

    void TLT() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setCarry(top < next);
        flags.setZero(false);
    }

    void TEQ() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setCarry(top == next);
        flags.setZero(false);
    }

    void TSZ() {
        flags.setCarry(false);
        flags.setZero(stack.getTop() == J5Stack.MIN_VALUE);
    }

    void AND() {
        int top = stack.pop();
        int next = stack.pop();

        stack.push(top & next);
        flags.setCarry(false);
        flags.setZero(stack.getTop() == J5Stack.MIN_VALUE);
    }

    void OR() {
        int top = stack.pop();
        int next = stack.pop();

        stack.push(top | next);
        flags.setCarry(false);
        flags.setZero(stack.getTop() == J5Stack.MIN_VALUE);
    }

    void XOR() {
        int top = stack.pop();
        int next = stack.pop();

        stack.push(top ^ next);
        flags.setCarry(false);
        flags.setZero(stack.getTop() == J5Stack.MIN_VALUE);
    }

    void NOT() {
        stack.setTop(stack.getTop() ^ J5Stack.MAX_VALUE);
        flags.setCarry(false);
        flags.setZero(stack.getTop() == J5Stack.MIN_VALUE);
    }
}

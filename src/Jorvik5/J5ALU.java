package Jorvik5;

public class J5ALU {
    private static J5ALU ourInstance = new J5ALU();
    public static J5ALU getInstance() {
        return ourInstance;
    }

    private J5Stack stack = J5Stack.getInstance();
    private J5Flags flags = J5Flags.getInstance();

    void ADD(boolean includeCarry) {
        boolean beforeZ = flags.getZero();

        int top = stack.pop();
        int next = stack.pop();
        int addition = top + next;

        if (includeCarry && flags.getCarry()) {
            addition += 1;
        }

        if (addition > J5Stack.MAX_VALUE) {
            addition = addition % (J5Stack.MAX_VALUE + 1);
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        if (includeCarry && !beforeZ) {
            flags.setZero(false);
        } else {
            flags.setZero(addition == J5Stack.MIN_VALUE);
        }

        stack.push(addition);
    }

    void SUB(boolean includeCarry) {
        boolean beforeZ = flags.getZero();

        int top = stack.pop();
        int next = stack.pop();
        int subtraction = next - top;

        if (includeCarry && flags.getCarry()) {
            subtraction -= 1;
        }

        if (subtraction < J5Stack.MIN_VALUE) {
            subtraction += J5Stack.MAX_VALUE + 1;
            flags.setCarry(true);

        } else {
            flags.setCarry(false);
        }

        if (includeCarry && !beforeZ) {
            flags.setZero(false);
        } else {
            flags.setZero(subtraction == J5Stack.MIN_VALUE);
        }

        stack.push(subtraction);
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

    void TEST() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        int result = (top & next);

        // Carry bit true if odd number of 1 bits
        boolean carry = false;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        flags.setCarry(carry);
        flags.setZero(result == J5Stack.MIN_VALUE);
    }

    void TESTCY() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        int result = (top & next);

        // Carry bit true if odd number of 1 bits
        boolean carry = flags.getCarry();
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        flags.setCarry(carry);
        flags.setZero(result == J5Stack.MIN_VALUE && flags.getZero());
    }

    void COMPARE() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setCarry(top < next);
        flags.setZero(top == next);
    }

    void COMPARECY() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        int carry = flags.getCarry() ? 1 : 0;
        int result = top - next - carry;

        flags.setCarry(result < J5Stack.MIN_VALUE);
        flags.setZero(result == J5Stack.MIN_VALUE && flags.getZero());
    }

    /*
    void TGT() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setCarry(top > next);
    }

    void TLT() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setCarry(top < next);
    }

    void TEQ() {
        int top = stack.pop();
        int next = stack.getTop();

        stack.push(top);

        flags.setZero(top == next);
    }

    void TSZ() {
        flags.setZero(stack.getTop() == J5Stack.MIN_VALUE);
    }
    */

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

    void SL() {
        int top = stack.pop();
        int result = (top << 1) & J5Stack.MAX_VALUE;

        stack.push(result);

        flags.setCarry((top & 0b10000000) != 0);
        flags.setZero(result == J5Stack.MIN_VALUE);
    }

    void SR() {
        int top = stack.pop();
        int result = (top >> 1) & J5Stack.MAX_VALUE;

        stack.push(result);

        flags.setCarry((top & 0b00000001) != 0);
        flags.setZero(result == J5Stack.MIN_VALUE);
    }

    void RL() {
        int currentTop = stack.pop();
        int newLeastSignificantBit = currentTop >> 7;

        int result = ((currentTop << 1) + newLeastSignificantBit) & J5Stack.MAX_VALUE;
        stack.push(result);

        flags.setCarry(newLeastSignificantBit != 0);
        flags.setZero(result == J5Stack.MIN_VALUE);
    }

    void RR() {
        int currentTop = stack.pop();
        int newMostSignificantBit = (currentTop & 0b00000001) << 7;

        int result = ((currentTop >> 1) + newMostSignificantBit) & J5Stack.MAX_VALUE;
        stack.push(result);

        flags.setCarry(newMostSignificantBit != 0);
        flags.setZero(result == J5Stack.MIN_VALUE);
    }
}

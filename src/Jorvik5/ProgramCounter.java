package Jorvik5;

import java.util.Stack;

public class ProgramCounter {
    private static ProgramCounter ourInstance = new ProgramCounter();

    public static ProgramCounter getInstance() {
        return ourInstance;
    }

    private Stack<Integer> pc;

    public int get() {
        return pc.peek();
    }

    // Copied from picoblaze
    public void set(int value) {
        set(value, false);
    }

    // Copied from picoblaze
    public void set(int value, boolean push) {
        if (push) {
            if (pc.size() <= 30) {
                pc.push(value);
            } else {
                throw new Error("Program counter stack size reached. Program should reset here.");
            }
        } else if (pc.size() == 0) {
            pc.push(value);
        } else {
            pc.pop();
            pc.push(value);
        }
    }

    // Copied from picoblaze
    public void increment() {
        int nextValue = pc.peek() + 1;
        set(nextValue > 0x3ff ? 0 : nextValue);
    }

    public void reset() {
        pc = new Stack<>();
        pc.push(0);
    }

    private ProgramCounter() {
        reset();
    }
}

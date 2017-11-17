package PicoBlazeSimulator;

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

    public void set(int value) {
        if (pc.size() != 0) {
            pc.pop();
        }

        pc.push(value);
    }

    public void push(int value) {
        if (pc.size() <= 30) {
            pc.push(value);
        } else {
            throw new Error("Program counter stack size reached. Program should reset here.");
        }
    }

    public void pop() {
        pc.pop();
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

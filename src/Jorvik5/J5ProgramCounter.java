package Jorvik5;

import java.util.Stack;

public class J5ProgramCounter {
    private static J5ProgramCounter ourInstance = new J5ProgramCounter();

    public static J5ProgramCounter getInstance() {
        return ourInstance;
    }

    private Stack<Integer> pc;
    private boolean justJumped;

    public void setJustJumped(boolean state) {
        justJumped = state;
    }

    public boolean hasJustJumped() {
        return justJumped;
    }

    public int get() {
        return pc.peek();
    }

    public void set(int value, boolean isJump) {
        int previousValue = 0;
        if (pc.size() != 0) {
            previousValue = pc.pop();
        }

        pc.push(value);

        if (isJump) {
            setJustJumped(true);
        } else {
            setJustJumped(false);
        }
    }

    public void push(int value) {
        if (pc.size() <= 30) {
            pc.push(value);
            setJustJumped(true);
        } else {
            throw new Error("Program counter stack size reached. Program should reset here.");
        }
    }

    public void pop() {
        pc.pop();
        setJustJumped(true);
    }

    // Copied from picoblaze
    public void increment() {
        int nextValue = pc.peek() + 1;
        set(nextValue > 0x3ff ? 0 : nextValue, false);
    }

    public void reset() {
        pc = new Stack<>();
        pc.push(0);
        setJustJumped(false);
    }

    private J5ProgramCounter() {
        reset();
    }
}

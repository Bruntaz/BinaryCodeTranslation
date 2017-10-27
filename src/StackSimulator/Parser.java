package StackSimulator;

import java.util.Stack;

public class Parser {
    // This will be the parser for the stack instructions (when it is implemented)
    private static Parser ourInstance = new Parser();
    public static Parser getInstance() {
        return ourInstance;
    }

    public Stack<Integer> programCounter = new Stack<>();

    // Copied from picoblaze
    public void setProgramCounter(int value) {
        setProgramCounter(value, false);
    }

    // Copied from picoblaze
    /*
     Requires error handling for max size of stack being reached
     */
    public void setProgramCounter(int value, boolean push) {
        if (push) {
            if (programCounter.size() <= 30) {
                programCounter.push(value);
            } else {
                System.out.println("Program counter stack size reached. Program should reset here.");
            }
        } else if (programCounter.size() == 0){
            programCounter.push(value);
        } else {
            programCounter.pop();
            programCounter.push(value);
        }
    }

    // Copied from picoblaze
    public void incrementProgramCounter() {
        int nextValue = programCounter.peek() + 1;
        setProgramCounter(nextValue > 0x3ff ? 0 : nextValue);
    }

    public void parse(Instruction[] program) {
    }

    private Parser() {
        programCounter = new Stack<>();
        setProgramCounter(0);
    }
}

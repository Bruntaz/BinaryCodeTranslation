package Jorvik5;

import Jorvik5.InstructionArguments.Literal;

public class Stack {
    private static Stack ourInstance = new Stack();
    public static Stack getInstance() {
        return ourInstance;
    }

    public static final int MAX_VALUE = 0xFF;
    public static final int MIN_VALUE = 0;

    private java.util.Stack<Integer> values = new java.util.Stack<>();

    public int getTop() {
        return values.peek();
    }

    public void setTop(int newValue) {
        values.pop();
        values.push(newValue);
    }

    public int pop() {
        return values.pop();
    }

    public void push(int newValue) {
        if (newValue > Literal.MAX_VALUE || newValue < Literal.MIN_VALUE) {
            throw new Error("Illegal value pushed onto stack (" + newValue + ")");
        }
        values.push(newValue);
    }
}

package Jorvik5;

public class Stack {
    private static Stack ourInstance = new Stack();
    public static Stack getInstance() {
        return ourInstance;
    }

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
        values.push(newValue);
    }
}

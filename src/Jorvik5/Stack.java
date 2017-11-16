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
    private Flags flags = Flags.getInstance();

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

    void DROP() {
        pop();
    }

    void SWAP() {
        int top = pop();
        int next = pop();
        push(top);
        push(next);
    }

    void ROT() {
        int top = pop();
        int next = pop();
        int third = pop();
        push(top);
        push(third);
        push(next);
    }

    void RROT() {
        int top = pop();
        int next = pop();
        int third = pop();
        push(next);
        push(top);
        push(third);
    }

    void DUP() {
        push(getTop());
    }

    @Override
    public String toString() {
        StringBuilder toPrint = new StringBuilder("{\n");
        Object[] stackArray = values.toArray();
        String[] names = new String[] {"TOP", "2ND", "3RD", "MEM"};

        for (int pointer = 0; pointer < stackArray.length; pointer++) {
            int i = (int) stackArray[stackArray.length - pointer - 1];
            toPrint.append(String.format("\t%3s:", names[pointer < 3 ? pointer : 3]));

            toPrint.append(
                    String.format(
                            "\t%2s\t(%8s)\n",
                            Integer.toHexString(i),
                            Integer.toBinaryString(i)
                    ).replace(' ', '0')
            );
        }

        toPrint.append(
                String.format("\n\tZ = %b\n" +
                                "\tC = %b\n",
                                flags.getZero(),
                                flags.getCarry()
                )
        );
        toPrint.append("}");

        return toPrint.toString();
    }
}

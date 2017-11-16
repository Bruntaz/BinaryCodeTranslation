package Jorvik5;

import Jorvik5.InstructionArguments.InstructionArgument;
import Jorvik5.InstructionArguments.ShortLiteral;

public class ScratchPad {
    private static ScratchPad ourInstance = new ScratchPad();
    public static ScratchPad getInstance() {
        return ourInstance;
    }

    private int memorySize;
    private int[] memory;
    private Stack stack = Stack.getInstance();

    public int getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
        memory = new int[memorySize];
    }

    public int getMemory(int location) {
        return memory[location];
    }

    public void setMemory(int location, int value) {
        if (value < ShortLiteral.MIN_VALUE || value > ShortLiteral.MAX_VALUE) {
            throw new Error("Scratch pad set to an illegal number (" + value + ")");
        }

        memory[location] = value;
    }

    void STORE(int location) {
        setMemory(location, stack.getTop());
    }

    void FETCH(int location) {
        stack.push(getMemory(location));
    }

    public void reset() {
        setMemorySize(64);
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();

        for (int i=0; i<memorySize; i++) {
            if (i % 16 == 0) {
                toReturn.append("\n");
            }

            toReturn.append(
                    String.format(
                            "%2s\t", Integer.toHexString(memory[i])
                    ).replace(' ', '0')
            );
        }

        return toReturn.toString();
    }

    private ScratchPad() {
        this.memorySize = 64;
        this.memory = new int[memorySize];
    }
}

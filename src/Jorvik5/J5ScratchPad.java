package Jorvik5;

import Jorvik5.InstructionArguments.J5ShortLiteral;

public class J5ScratchPad {
    private static J5ScratchPad ourInstance = new J5ScratchPad();
    public static J5ScratchPad getInstance() {
        return ourInstance;
    }

    private int memorySize;
    private int[] memory;
    private J5Stack stack = J5Stack.getInstance();
    private int memoryReads;
    private int memoryWrites;

    public void setMemoryReads(int newReads) { memoryReads = newReads; }
    public void setMemoryWrites(int newWrites) { memoryWrites = newWrites; }
    public int getMemoryReads() { return memoryReads; }
    public int getMemoryWrites() { return memoryWrites; }

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
        if (value < J5ShortLiteral.MIN_VALUE || value > J5ShortLiteral.MAX_VALUE) {
            throw new Error("Scratch pad set to an illegal number (" + value + ")");
        }

        memory[location] = value;
    }

    void ISTORE() {
        int location = stack.pop();
        STORE(location);
        stack.push(location);
    }

    void STORE(int location) {
        setMemory(location, stack.getTop());
        memoryWrites += 1;
    }

    void IFETCH() {
        int location = stack.pop();
        FETCH(location);
        stack.push(location);
        stack.SWAP();
    }

    void FETCH(int location) {
        stack.push(getMemory(location));
        memoryReads += 1;
    }

    public void reset() {
        setMemorySize(64);
        setMemoryReads(0);
        setMemoryWrites(0);
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

    private J5ScratchPad() {
        this.memorySize = 64;
        this.memory = new int[memorySize];
    }
}

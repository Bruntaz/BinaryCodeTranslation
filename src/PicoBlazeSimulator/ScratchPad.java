package PicoBlazeSimulator;

import PicoBlazeSimulator.InstructionArguments.InstructionArgument;
import PicoBlazeSimulator.InstructionArguments.Register;

public class ScratchPad {
    private static ScratchPad ourInstance = new ScratchPad();
    public static ScratchPad getInstance() {
        return ourInstance;
    }

    private int memorySize;
    private int[] memory;

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
        if (value < Register.MIN_VALUE || value > Register.MAX_VALUE) {
            throw new Error("Scratch pad set to an illegal number (" + value + ")");
        }

        memory[location] = value;
    }

    public void STORE(InstructionArgument arg0, InstructionArgument arg1) {
        setMemory(arg1.getIntValue(), arg0.getIntValue());
    }

    public void FETCH(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(getMemory(arg1.getIntValue()));
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

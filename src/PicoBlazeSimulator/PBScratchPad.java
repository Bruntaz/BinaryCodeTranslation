package PicoBlazeSimulator;

import PicoBlazeSimulator.InstructionArguments.PBInstructionArgument;
import PicoBlazeSimulator.InstructionArguments.PBRegister;

public class PBScratchPad {
    private static PBScratchPad ourInstance = new PBScratchPad();
    public static PBScratchPad getInstance() {
        return ourInstance;
    }

    private int memorySize;
    private int[] memory;
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
        if (value < PBRegister.MIN_VALUE || value > PBRegister.MAX_VALUE) {
            throw new Error("Scratch pad set to an illegal number (" + value + ")");
        }

        memory[location] = value;
    }

    void STORE(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        setMemory(arg1.getIntValue(), arg0.getIntValue());
        memoryWrites += 1;
    }

    void FETCH(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        arg0.setValue(getMemory(arg1.getIntValue()));
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

    private PBScratchPad() {
        this.memorySize = 64;
        this.memory = new int[memorySize];
    }
}

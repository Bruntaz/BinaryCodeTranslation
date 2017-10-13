import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

public class ScratchPad {
    private int memorySize;
    private int[] memory;

    public int getMemorySize() {
        return memorySize;
    }

    public int getMemory(int location) {
        return memory[location];
    }

    public void setMemory(int location, int value) {
        if (value < Register.MIN_VALUE || value > Register.MAX_VALUE) {
            throw new ValueException("Scratch pad set to an illegal number (" + value + ")");
        }

        memory[location] = value;
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

    public ScratchPad() {
        this(64);
    }

    public ScratchPad(int memorySize) {
        this.memorySize = memorySize;
        this.memory = new int[memorySize];
    }
}

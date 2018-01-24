package PicoBlazeSimulator.InstructionArguments;

import PicoBlazeSimulator.Groups.PBRegisterName;

public class PBRegister implements PBInstructionArgument {
    public static final int MAX_VALUE = 255;
    public static final int MIN_VALUE = 0;

    private boolean aRegisterBank = true;
    private PBRegisterName registerName;
    private int aValue;
    private int bValue;

    public PBRegisterName getRegisterName() {
        return registerName;
    }

    @Override
    public boolean hasStringValue() {
        return false;
    }

    @Override
    public String getStringValue() {
        return null;
    }

    @Override
    public boolean hasIntValue() {
        return true;
    }

    @Override
    public int getIntValue() {
        return aRegisterBank ? aValue : bValue;
    }

    @Override
    public void setValue(int newValue) {
        if (newValue < MIN_VALUE || newValue > MAX_VALUE) {
            throw new Error("PicoBlazeSimulator.InstructionArguments.PBRegister set to an illegal number (" + newValue + ")");
        }

        if (aRegisterBank) {
            this.aValue = newValue;
        } else {
            this.bValue = newValue;
        }
    }

    @Override
    public void setValue(String newValue) {}

    public boolean isARegisterBank() {
        return aRegisterBank;
    }

    public void useARegisterBank(boolean state) {
        aRegisterBank = state;
    }

    public void reset() {
        aValue = 0;
        bValue = 0;
    }

    public PBRegister(PBRegisterName registerName) {
        this.registerName = registerName;
        this.aValue = MIN_VALUE;
        this.bValue = MIN_VALUE;
    }
}

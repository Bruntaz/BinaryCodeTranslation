package PicoBlazeSimulator.InstructionArguments;

import PicoBlazeSimulator.Groups.RegisterName;

public class Register implements InstructionArgument {
    public static final int MAX_VALUE = 255;
    public static final int MIN_VALUE = 0;

    private boolean aRegisterBank = true;
    private RegisterName registerName;
    private int aValue;
    private int bValue;

    public RegisterName getRegisterName() {
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
            throw new Error("PicoBlazeSimulator.InstructionArguments.Register set to an illegal number (" + newValue + ")");
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

    public Register(RegisterName registerName) {
        this.registerName = registerName;
        this.aValue = MIN_VALUE;
        this.bValue = MIN_VALUE;
    }
}

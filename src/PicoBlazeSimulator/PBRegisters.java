package PicoBlazeSimulator;

import PicoBlazeSimulator.Groups.PBRegisterName;
import PicoBlazeSimulator.InstructionArguments.PBInstructionArgument;
import PicoBlazeSimulator.InstructionArguments.PBRegister;
import PicoBlazeSimulator.InstructionArguments.PBRegisterBank;

import java.util.HashMap;

/**
 * Created by jamesbrunton on 06/10/2017.
 */

public class PBRegisters {
    private static PBRegisters ourInstance = new PBRegisters();
    public static PBRegisters getInstance() {
        return ourInstance;
    }

    public boolean C = false;
    public boolean Z = false;
    public boolean aRegisterBank = true;

    private HashMap<PBRegisterName, PBRegister> registers = new HashMap<>();

    public PBRegister getRegister(PBRegisterName register) {
        return registers.get(register);
    }

    public void setCarry(boolean newState) {
        C = newState;
    }

    public void setZero(boolean newState) {
        Z = newState;
    }

    public void useARegisterBank(boolean newState) {
        aRegisterBank = newState;

        for (PBRegister register : registers.values()) {
            register.useARegisterBank(newState);
        }
    }

    public void toggleActiveRegisters() {
        useARegisterBank(!aRegisterBank);
    }

    public void resetRegisters() {
        for (PBRegister register : registers.values()) {
            register.reset();
        }
    }

    void LOAD(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        arg0.setValue(arg1.getIntValue());
    }

    void STAR(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        int copiedValue = arg1.getIntValue();
        toggleActiveRegisters();

        arg0.setValue(copiedValue);
        toggleActiveRegisters();
    }

    // PBRegister Bank Selection
    void REGBANK(PBInstructionArgument arg0) {
        useARegisterBank(arg0.getStringValue().equals(PBRegisterBank.A));
    }

    private PBRegisters() {
        for (PBRegisterName registerName : PBRegisterName.values()) {
            this.registers.put(registerName, new PBRegister(registerName));
        }
    }

    @Override
    public String toString() {
        StringBuilder toPrint = new StringBuilder("{\n");

        for (PBRegisterName registerName : PBRegisterName.values()) {
            int registerValue = registers.get(registerName).getIntValue();
            toPrint.append(
                    String.format(
                            "\t%2s:\t%2s\t(%8s)\n",
                            registerName,
                            Integer.toHexString(registerValue),
                            Integer.toBinaryString(registerValue)
                    ).replace(' ', '0')
            );
        }

        toPrint.append(
                String.format("\n\tZ = %b\n" +
                                "\tC = %b\n", Z, C)
        );

        return toPrint + "}";
    }
}

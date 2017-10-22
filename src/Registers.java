import java.util.HashMap;

/**
 * Created by jamesbrunton on 06/10/2017.
 */

public class Registers {
    private static Registers ourInstance = new Registers();
    public static Registers getInstance() {
        return ourInstance;
    }

    public boolean C = false;
    public boolean Z = false;
    public boolean aRegisterBank = true;

    private HashMap<RegisterName, Register> registers = new HashMap<RegisterName, Register>();

    public Register getRegister(RegisterName register) {
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
    }

    public void toggleActiveRegisters() {
        aRegisterBank = !aRegisterBank;
    }

    public void resetRegisters() {
        for (Register register : registers.values()) {
            register.reset();
        }
    }

    public void LOAD(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg1.getIntValue());
    }

    public void STAR(InstructionArgument arg0, InstructionArgument arg1) {
        int copiedValue = arg1.getIntValue();
        toggleActiveRegisters();

        arg0.setValue(copiedValue);
        toggleActiveRegisters();
    }

    // Register Bank Selection
    public void REGBANK(InstructionArgument arg0) {
        aRegisterBank = arg0.getStringValue().equals(RegisterBank.A);
    }

    private  Registers() {
        for (RegisterName registerName : RegisterName.values()) {
            this.registers.put(registerName, new Register(this, registerName));
        }
    }

    @Override
    public String toString() {
        StringBuilder toPrint = new StringBuilder("{\n");

        for (RegisterName registerName : RegisterName.values()) {
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
                String.format("\tZ = %b\n" +
                        "\tC = %b\n", Z, C)
        );

        return toPrint + "}";
    }
}

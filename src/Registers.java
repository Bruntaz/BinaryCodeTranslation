import java.util.HashMap;

/**
 * Created by jamesbrunton on 06/10/2017.
 */

public class Registers {
    public boolean C = false;
    public boolean Z = false;

    private HashMap<RegisterName, Register> registers = new HashMap<RegisterName, Register>();

    public Registers() {
        for (RegisterName registerName : RegisterName.values()) {
            this.registers.put(registerName, new Register(this, registerName));
        }
    }

    @Override
    public String toString() {
        StringBuilder toPrint = new StringBuilder("{\n");

        for (RegisterName registerName : RegisterName.values()) {
            int registerValue = registers.get(registerName).getValue();
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

    public Register getRegister(RegisterName register) {
        return registers.get(register);
    }

    public void setCarry(boolean newState) {
        C = newState;
    }

    public void setZero(boolean newState) {
        Z = newState;
    }
}

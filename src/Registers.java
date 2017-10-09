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
            this.registers.put(registerName, new Register());
        }
    }

    public int getRegister(RegisterName register) {
        return registers.get(register).value + Byte.MAX_VALUE + 1;
    }

    public void setRegister(RegisterName register, int value) {
        registers.get(register).value = (byte)(value + Byte.MIN_VALUE);
//        registers.put(register, (byte)(value + Byte.MIN_VALUE));
    }

    public void addToRegister(RegisterName register, int value) {
        int currentRegisterValue = getRegister(register);
        int newRegisterValue = currentRegisterValue + value;

        setRegister(register, newRegisterValue);

        C = newRegisterValue > 255;
        Z = newRegisterValue == 256;
    }

    public void subtractFromRegister(RegisterName register, int value) {
        int currentRegisterValue = getRegister(register);
        int newRegisterValue = currentRegisterValue - value;

        setRegister(register, newRegisterValue);

        C = newRegisterValue < 0;
        Z = newRegisterValue == 0;
    }
}

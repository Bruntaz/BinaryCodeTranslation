package Jorvik5;

public class Registers {
    private static Registers ourInstance = new Registers();
    public static Registers getInstance() {
        return ourInstance;
    }

    private boolean C = false;
    private boolean Z = false;

    public boolean getCarry() {
        return C;
    }

    public boolean getZero() {
        return Z;
    }

    public void setCarry(boolean newState) {
        C = newState;
    }

    public void setZero(boolean newState) {
        Z = newState;
    }
}

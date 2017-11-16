package Jorvik5;

public class Flags {
    private static Flags ourInstance = new Flags();
    public static Flags getInstance() {
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

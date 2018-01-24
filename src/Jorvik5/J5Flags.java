package Jorvik5;

public class J5Flags {
    private static J5Flags ourInstance = new J5Flags();
    public static J5Flags getInstance() {
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

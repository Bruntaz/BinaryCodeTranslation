public interface InstructionArgument {
    boolean hasStringValue();
    String getStringValue();
    boolean hasIntValue();
    int getIntValue();
    void setValue(int newValue);
    void setValue(String newValue);
}

package StackSimulator;

public class Lexer {
    // This will be the lexer for the stack assembly code (when it is implemented)

    private static Lexer ourInstance = new Lexer();
    public static Lexer getInstance() {
        return ourInstance;
    }
}

package Jorvik5;

public class Parser {
    // This will be the parser for the stack instructions (when it is implemented)
    private static Parser ourInstance = new Parser();
    public static Parser getInstance() {
        return ourInstance;
    }

    private ProgramCounter programCounter = ProgramCounter.getInstance();
    private Stack stack = Stack.getInstance();

    public void parse(Instruction instruction) {
        programCounter.increment();

        if (instruction.instruction == null) {
            return;
        }

        switch (instruction.instruction) {
        }
    }

    public void parse(Instruction[] program) {
        int clockCycles = 0;

        while (programCounter.get() < program.length) {
            parse(program[programCounter.get()]);

            clockCycles += 1;
        }

        System.out.println("Finished in " + clockCycles + " clock cycles");
    }
}

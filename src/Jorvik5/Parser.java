package Jorvik5;

import Jorvik5.InstructionArguments.InstructionArgument;

public class Parser {
    // This will be the parser for the stack instructions (when it is implemented)
    private static Parser ourInstance = new Parser();
    public static Parser getInstance() {
        return ourInstance;
    }

    private ProgramCounter programCounter = ProgramCounter.getInstance();
    private Stack stack = Stack.getInstance();
    private ALU alu = ALU.getInstance();

    private void SET(InstructionArgument arg) {
        stack.push(arg.getValue());
    }

    public void parse(Instruction instruction) {
        programCounter.increment();

        if (instruction.instruction == null) {
            return;
        }

        switch (instruction.instruction) {
            // Assignment
//            case SET:
            case SSET:
                SET(instruction.arg);
                break;

            // ALU
            case ADD:
                alu.ADD();
                break;
            case SUB:
                alu.SUB();
                break;
            case INC:
                alu.INC();
                break;
            case DEC:
                alu.DEC();
                break;
        }
        System.out.println(stack);
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

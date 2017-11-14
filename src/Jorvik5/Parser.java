package Jorvik5;

import Jorvik5.InstructionArguments.InstructionArgument;
import PicoBlazeSimulator.InstructionArguments.Register;

public class Parser {
    // This will be the parser for the stack instructions (when it is implemented)
    private static Parser ourInstance = new Parser();
    public static Parser getInstance() {
        return ourInstance;
    }

    private ProgramCounter programCounter = ProgramCounter.getInstance();
    private Stack stack = Stack.getInstance();
    private ALU alu = ALU.getInstance();
    private Registers registers = Registers.getInstance();

    private void SET(InstructionArgument value) {
        stack.push(value.getValue());
    }

    private void BRANCH(InstructionArgument address) {
        programCounter.set(address.getValue());
    }

    private void BRZERO(InstructionArgument address) {
        if (registers.getZero()) {
            BRANCH(address);
        }
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

            // Branching
            case BRANCH:
                BRANCH(instruction.arg);
                break;
            case BRZERO:
                BRZERO(instruction.arg); // TODO: Implement the rest of the branch instructions
                break;

            // Stack Management
            case DROP:
                stack.DROP();
                break;
            case SWAP:
                stack.SWAP();
                break;
            case ROT:
                stack.ROT();
                break;
            case RROT:
                stack.RROT();
                break;
            case DUP:
                stack.DUP();
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

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
    private Flags flags = Flags.getInstance();

    private void SET(InstructionArgument value) {
        stack.push(value.getValue());
    }

    private void BRANCH(InstructionArgument address) {
        programCounter.set(address.getValue());
    }

    private void BRZERO(InstructionArgument address) {
        if (flags.getZero()) {
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
            case TGT:
                alu.TGT();
                break;
            case TLT:
                alu.TLT();
                break;
            case TEQ:
                alu.TEQ();
                break;
            case TSZ:
                alu.TSZ();
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

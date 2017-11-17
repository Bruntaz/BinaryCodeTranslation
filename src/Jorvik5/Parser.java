package Jorvik5;

import Jorvik5.InstructionArguments.InstructionArgument;

public class Parser {
    private static Parser ourInstance = new Parser();
    public static Parser getInstance() {
        return ourInstance;
    }

    private ProgramCounter programCounter = ProgramCounter.getInstance();
    private Stack stack = Stack.getInstance();
    private ALU alu = ALU.getInstance();
    private ScratchPad scratchPad = ScratchPad.getInstance();
    private Flags flags = Flags.getInstance();

    private void SET(InstructionArgument value) {
        stack.push(value.getValue());
    }

    private void BRANCH(int address, boolean conditional) {
        int currentAddress = programCounter.get();

        int newAddress;
        if (conditional) {
            newAddress = currentAddress + address - 1;
        } else {
            newAddress = currentAddress - address - 1;
        }

        programCounter.set(newAddress);
    }

    private void BRZERO(int address) {
        if (flags.getZero()) {
            BRANCH(address, true);
        }
    }

    private void LBRANCH(int address) {
        programCounter.set(address);
    }

    private void IBRANCH() {
        BRANCH(stack.getTop(), false);
    }

    private void CALL(int address) {
        programCounter.push(address);
    }

    private void RETURN() {
        programCounter.pop();
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
            case AND:
                alu.AND();
                break;
            case OR:
                alu.OR();
                break;
            case XOR:
                alu.XOR();
                break;
            case NOT:
                alu.NOT();
                break;

            // Branching
            case BRANCH:
            case SBRANCH:
                BRANCH(instruction.arg.getValue(), false);
                break;
            case BRZERO:
            case SBRZERO:
                BRZERO(instruction.arg.getValue());
                break;
            case LBRANCH:
                LBRANCH(instruction.arg.getValue());
                break;
            case IBRANCH:
                IBRANCH();
                break;
            case CALL:
                CALL(instruction.arg.getValue());
                break;
            case RETURN:
                RETURN();
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

            // Scratch Pad
            case FETCH:
                scratchPad.FETCH(instruction.arg.getValue());
                break;
            case STORE:
                scratchPad.STORE(instruction.arg.getValue());
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

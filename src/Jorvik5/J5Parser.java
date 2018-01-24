package Jorvik5;

import Jorvik5.Groups.J5InstructionSet;
import Jorvik5.InstructionArguments.J5InstructionArgument;

public class J5Parser {
    private static J5Parser ourInstance = new J5Parser();
    public static J5Parser getInstance() {
        return ourInstance;
    }

    private J5ProgramCounter programCounter = J5ProgramCounter.getInstance();
    private J5Stack stack = J5Stack.getInstance();
    private J5ALU alu = J5ALU.getInstance();
    private J5ScratchPad scratchPad = J5ScratchPad.getInstance();
    private J5Flags flags = J5Flags.getInstance();
    private int clockCycles;

    public void setClockCycles(int number) {
        clockCycles = number;
    }

    public int getClockCycles() {
        return clockCycles;
    }

    public void RESET() {
        programCounter.reset();
        setClockCycles(0);

        flags.setCarry(false);
        flags.setZero(false);
    }

    private void SET(J5InstructionArgument value) {
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

    public void parse(J5Instruction instruction) {
        System.out.println(instruction);
        programCounter.increment();

        if (instruction.instruction == J5InstructionSet.NOP) {
            return;
        }

        switch (instruction.instruction) {
            // Assignment
//            case SET:
            case SSET:
                SET(instruction.arg);
                break;

            // J5ALU
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

            // J5Stack Management
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
            case IFETCH:
                scratchPad.IFETCH();
                break;
            case STORE:
                scratchPad.STORE(instruction.arg.getValue());
                break;
            case ISTORE:
                scratchPad.ISTORE();
                break;
        }

        System.out.println(stack);
        clockCycles += 1;
    }

    public void parse(J5Instruction[] program) {
        while (programCounter.get() < program.length) {
            parse(program[programCounter.get()]);
        }

        System.out.println(String.format("Finished in %d clock cycles", clockCycles));
    }

    private J5Parser() {
        RESET();
    }
}
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

        programCounter.set(newAddress, true);
    }

    private void BRZERO(int address) {
        if (flags.getZero()) {
            BRANCH(address, true);
        }
    }

    private void BRCARRY(int address) {
        if (flags.getCarry()) {
            BRANCH(address, true);
        }
    }

    private void LBRANCH(int address) {
        programCounter.set(address, true);
    }

    private void IBRANCH() {
        BRANCH(stack.getTop(), false);
    }

    private void CALL(int address) {
        programCounter.push(address);
    }

    private void CALLZERO(int address) {
        if (flags.getZero()) {
            CALL(address);
        }
    }

    private void CALLCARRY(int address) {
        if (flags.getCarry()) {
            CALL(address);
        }
    }

    private void RETURN() {
        programCounter.pop();
    }

    public void parse(J5Instruction instruction) {
        System.out.println(instruction);

        if (instruction.instruction == J5InstructionSet.NOP || instruction.instruction == J5InstructionSet.STOP ||
            instruction.instruction == J5InstructionSet.PASS) {
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
                alu.ADD(false);
                break;
            case ADDCY:
                alu.ADD(true);
                break;
            case SUB:
                alu.SUB(false);
                break;
            case SUBCY:
                alu.SUB(true);
                break;
            case INC:
                alu.INC();
                break;
            case DEC:
                alu.DEC();
                break;

            case TEST:
                alu.TEST();
                break;
            case TESTCY:
                alu.TESTCY();
                break;
            case COMPARE:
                alu.COMPARE();
                break;
            case COMPARECY:
                alu.COMPARECY();
                break;
//            case TGT:
//                alu.TGT();
//                break;
//            case TLT:
//                alu.TLT();
//                break;
//            case TEQ:
//                alu.TEQ();
//                break;
//            case TSZ:
//                alu.TSZ();
//                break;
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
            case SL0:
                alu.SL(J5InstructionSet.SL0);
                break;
            case SL1:
                alu.SL(J5InstructionSet.SL1);
                break;
            case SLX:
                alu.SL(J5InstructionSet.SLX);
                break;
            case SLA:
                alu.SL(J5InstructionSet.SLA);
                break;
            case SR0:
                alu.SR(J5InstructionSet.SR0);
                break;
            case SR1:
                alu.SR(J5InstructionSet.SR1);
                break;
            case SRX:
                alu.SR(J5InstructionSet.SRX);
                break;
            case SRA:
                alu.SR(J5InstructionSet.SRA);
                break;
            case RL:
                alu.RL();
                break;
            case RR:
                alu.RR();
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
            case BRCARRY:
            case SBRCARRY:
                BRCARRY(instruction.arg.getValue());
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
            case CALLZERO:
                CALLZERO(instruction.arg.getValue());
                break;
            case CALLCARRY:
                CALLCARRY(instruction.arg.getValue());
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
            case OVER:
                stack.OVER();
                break;
            case UNDER:
                stack.UNDER();
                break;
            case TUCK:
                stack.TUCK();
                break;
            case NIP:
                stack.NIP();
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

            default:
                throw new Error("Instruction " + instruction.instruction + " not supported yet.");
        }

        System.out.println(stack);
        clockCycles += 1;
    }

    public void parse(J5Instruction[] program) {
        int pc = programCounter.get();
        while (pc < program.length) {
            programCounter.increment();
            parse(program[pc]);
            pc = programCounter.get();
        }

        System.out.println(String.format("Finished in %d clock cycles", clockCycles));
    }

    private J5Parser() {
        RESET();
    }
}

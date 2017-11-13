package PicoBlazeSimulator;

import PicoBlazeSimulator.Groups.InstructionSet;
import PicoBlazeSimulator.InstructionArguments.AbsoluteAddress;
import PicoBlazeSimulator.InstructionArguments.FlagArgument;
import PicoBlazeSimulator.InstructionArguments.InstructionArgument;
import PicoBlazeSimulator.InstructionArguments.NoArgument;

import java.util.*;
import java.util.Stack;

public class Parser {
    private static Parser ourInstance = new Parser();
    public static Parser getInstance() {
        return ourInstance;
    }

    private Registers registers = Registers.getInstance();
    private ScratchPad scratchPad = ScratchPad.getInstance();
    private ALU alu = ALU.getInstance();

    public Stack<Integer> programCounter = new Stack<>();
    private int clockCycles;

    HashSet<InstructionSet> blockEntrances = new HashSet<>(Arrays.asList(InstructionSet.RETURN, InstructionSet.CALL,
            InstructionSet.CALLAT, InstructionSet.JUMP, InstructionSet.JUMPAT));

    public void RESET() {
        programCounter = new Stack<>();
        setProgramCounter(0);

        registers.setCarry(false);
        registers.setZero(false);
    }

    public void setProgramCounter(int value) {
        setProgramCounter(value, false);
    }

    /*
     Requires error handling for max size of stack being reached
     */
    public void setProgramCounter(int value, boolean push) {
        if (push) {
            if (programCounter.size() <= 30) {
                programCounter.push(value);
            } else {
                System.out.println("Program counter stack size reached. Program should reset here.");
            }
        } else if (programCounter.size() == 0){
            programCounter.push(value);
        } else {
            programCounter.pop();
            programCounter.push(value);
        }
    }

    public void incrementProgramCounter() {
        int nextValue = programCounter.peek() + 1;
        setProgramCounter(nextValue > 0x3ff ? 0 : nextValue);
    }

    // Jump
    private void JUMP(InstructionArgument arg0) {
        setProgramCounter(arg0.getIntValue());
    }

    private void JUMP(InstructionArgument arg0, InstructionArgument arg1) {
        switch (arg0.getStringValue()) {
            case FlagArgument.C:
                setProgramCounter(registers.C ? arg1.getIntValue() : programCounter.peek());
                break;
            case FlagArgument.NC:
                setProgramCounter(registers.C ? programCounter.peek() : arg1.getIntValue());
                break;
            case FlagArgument.Z:
                setProgramCounter(registers.Z ? arg1.getIntValue() : programCounter.peek());
                break;
            case FlagArgument.NZ:
                setProgramCounter(registers.Z ? programCounter.peek() : arg1.getIntValue());
                break;
        }
    }

    private void JUMPAT(InstructionArgument arg0, InstructionArgument arg1) {
        int top4Bits = (arg0.getIntValue() & 0b00001111) << 8;

        setProgramCounter(top4Bits + arg1.getIntValue());
    }

    // Subroutines
    private void CALL(InstructionArgument arg0, InstructionArgument arg1) {
        if (arg0 instanceof AbsoluteAddress) {
            setProgramCounter(arg0.getIntValue(), true);

        } else {
            switch (arg0.getStringValue()) {
                case FlagArgument.C:
                    setProgramCounter(registers.C ? arg1.getIntValue() : programCounter.peek(), true);
                    break;
                case FlagArgument.NC:
                    setProgramCounter(registers.C ? programCounter.peek() : arg1.getIntValue(), true);
                    break;
                case FlagArgument.Z:
                    setProgramCounter(registers.Z ? arg1.getIntValue() : programCounter.peek(), true);
                    break;
                case FlagArgument.NZ:
                    setProgramCounter(registers.Z ? programCounter.peek() : arg1.getIntValue(), true);
                    break;
            }
        }
    }

    private void CALLAT(InstructionArgument arg0, InstructionArgument arg1) {
        int top4Bits = (arg0.getIntValue() & 0b00001111) << 8;

        setProgramCounter(top4Bits + arg1.getIntValue(), true);
    }

    private void RETURN() {
        programCounter.pop();
    }

    private void RETURN(InstructionArgument arg0, InstructionArgument arg1) {
        switch (arg0.getStringValue()) {
            case FlagArgument.C:
                if (registers.C) programCounter.pop();
                break;
            case FlagArgument.NC:
                if (!registers.C) programCounter.pop();
                break;
            case FlagArgument.Z:
                if (registers.Z) programCounter.pop();
                break;
            case FlagArgument.NZ:
                if (!registers.Z) programCounter.pop();
                break;
        }
    }

    private void LOADANDRETURN(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg1.getIntValue());
        programCounter.pop();
        clockCycles += 1; // This instruction takes 2 clock cycles to run
    }

    // Version Control
    private void HWBUILD(InstructionArgument arg0) {
        arg0.setValue(0); // This should be definable, setting to 0 for now for simplicity
        registers.setCarry(true);
        registers.setZero(arg0.getIntValue() == 0);
    }

    public int getNextBlockStart(Instruction[] instructions, int startOfBlock) {
        int nextBlockStart = startOfBlock + 1;

        if (instructions.length <= nextBlockStart) {
            return instructions.length;
        }

        while (!instructions[nextBlockStart].isBlockStart) {
            nextBlockStart += 1;

            if (instructions.length <= nextBlockStart) {
                break;
            }
        }

        return nextBlockStart;
    }

    public void parse(Instruction instruction) {
        incrementProgramCounter();

        if (instruction.instruction == null) {
            return;
        }

        switch (instruction.instruction) {
            // Register loading
            case LOAD:
                registers.LOAD(instruction.arg0, instruction.arg1);
                break;
            case STAR:
                registers.STAR(instruction.arg0, instruction.arg1);
                break;

            // Logical
            case AND:
                alu.AND(instruction.arg0, instruction.arg1);
                break;
            case OR:
                alu.OR(instruction.arg0, instruction.arg1);
                break;
            case XOR:
                alu.XOR(instruction.arg0, instruction.arg1);
                break;

            // Arithmetic
            case ADD:
                alu.ADD(instruction.arg0, instruction.arg1, false);
                break;
            case ADDCY:
                alu.ADD(instruction.arg0, instruction.arg1, true);
                break;
            case SUB:
                alu.SUB(instruction.arg0, instruction.arg1, false);
                break;
            case SUBCY:
                alu.SUB(instruction.arg0, instruction.arg1, true);
                break;

            // Test and Compare
            case TEST:
                alu.TEST(instruction.arg0, instruction.arg1);
                break;
            case TESTCY:
                alu.TESTCY(instruction.arg0, instruction.arg1);
                break;
            case COMPARE:
                alu.COMPARE(instruction.arg0, instruction.arg1);
                break;
            case COMPARECY:
                alu.COMPARECY(instruction.arg0, instruction.arg1);
                break;

            // Shift and Rotate
            case SL0:
            case SL1:
            case SLX:
            case SLA:
                alu.SL(instruction.instruction, instruction.arg0);
                break;
            case SR0:
            case SR1:
            case SRX:
            case SRA:
                alu.SR(instruction.instruction, instruction.arg0);
                break;
            case RL:
                alu.RL(instruction.arg0);
                break;
            case RR:
                alu.RR(instruction.arg0);
                break;

            // Register Bank Selection
            case REGBANK:
                registers.REGBANK(instruction.arg0);
                break;

            // Scratch Pad Memory
            case STORE:
                scratchPad.STORE(instruction.arg0, instruction.arg1);
                break;
            case FETCH:
                scratchPad.FETCH(instruction.arg0, instruction.arg1);
                break;

            // Jump
            case JUMP:
                if (instruction.arg0 instanceof AbsoluteAddress) {
                    JUMP(instruction.arg0);
                } else {
                    JUMP(instruction.arg0, instruction.arg1);
                }
                break;
            case JUMPAT:
                JUMPAT(instruction.arg0, instruction.arg1);
                break;

            // Subroutines
            case CALL:
                CALL(instruction.arg0, instruction.arg1);
                break;
            case CALLAT:
                CALLAT(instruction.arg0, instruction.arg1);
                break;
            case RETURN:
                if (instruction.arg0 instanceof NoArgument) {
                    RETURN();
                } else {
                    RETURN(instruction.arg0, instruction.arg1);
                }
                break;
            case LOADANDRETURN:
                LOADANDRETURN(instruction.arg0, instruction.arg1);
                break;

            // Version Control
            case HWBUILD:
                HWBUILD(instruction.arg0);
                break;

            default:
                throw new UnsupportedOperationException("Unrecognised instruction. Has the instruction been added to the switch statement in Parser?");
        }
    }

    public void parse(Instruction[] program) {
        clockCycles = 0;

        while (programCounter.peek() < program.length) {
            Instruction instruction = program[programCounter.peek()];

            parse(instruction);

            clockCycles += 1;
            System.out.println(registers);
        }

        System.out.println(String.format("Finished in %d clock cycles", clockCycles));
    }

    private Parser() {
        RESET();
    }
}

package PicoBlazeSimulator;

import PicoBlazeSimulator.Groups.PBInstructionSet;
import PicoBlazeSimulator.InstructionArguments.PBAbsoluteAddress;
import PicoBlazeSimulator.InstructionArguments.PBFlagArgument;
import PicoBlazeSimulator.InstructionArguments.PBInstructionArgument;
import PicoBlazeSimulator.InstructionArguments.PBNoArgument;

import java.util.*;

public class PBParser {
    private static PBParser ourInstance = new PBParser();
    public static PBParser getInstance() {
        return ourInstance;
    }

    private PBRegisters registers = PBRegisters.getInstance();
    private PBScratchPad scratchPad = PBScratchPad.getInstance();
    private PBALU alu = PBALU.getInstance();

    private PBProgramCounter programCounter = PBProgramCounter.getInstance();
    private int clockCycles;

    HashSet<PBInstructionSet> blockEntrances = new HashSet<>(Arrays.asList(PBInstructionSet.RETURN, PBInstructionSet.CALL,
            PBInstructionSet.CALLAT, PBInstructionSet.JUMP, PBInstructionSet.JUMPAT));

    public void setClockCycles(int number) {
        clockCycles = number;
    }

    public int getClockCycles() {
        return clockCycles;
    }

    public void RESET() {
        programCounter.reset();
        setClockCycles(0);

        registers.setCarry(false);
        registers.setZero(false);
    }

    // Jump
    private void JUMP(PBInstructionArgument arg0) {
        programCounter.set(arg0.getIntValue());
    }

    private void JUMP(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        switch (arg0.getStringValue()) {
            case PBFlagArgument.C:
                programCounter.set(registers.C ? arg1.getIntValue() : programCounter.get());
                break;
            case PBFlagArgument.NC:
                programCounter.set(registers.C ? programCounter.get() : arg1.getIntValue());
                break;
            case PBFlagArgument.Z:
                programCounter.set(registers.Z ? arg1.getIntValue() : programCounter.get());
                break;
            case PBFlagArgument.NZ:
                programCounter.set(registers.Z ? programCounter.get() : arg1.getIntValue());
                break;
        }
    }

    private void JUMPAT(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        int top4Bits = (arg0.getIntValue() & 0b00001111) << 8;

        programCounter.set(top4Bits + arg1.getIntValue());
    }

    // Subroutines
    private void CALL(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        if (arg0 instanceof PBAbsoluteAddress) {
            programCounter.push(arg0.getIntValue());

        } else {
            switch (arg0.getStringValue()) {
                case PBFlagArgument.C:
                    programCounter.push(registers.C ? arg1.getIntValue() : programCounter.get());
                    break;
                case PBFlagArgument.NC:
                    programCounter.push(registers.C ? programCounter.get() : arg1.getIntValue());
                    break;
                case PBFlagArgument.Z:
                    programCounter.push(registers.Z ? arg1.getIntValue() : programCounter.get());
                    break;
                case PBFlagArgument.NZ:
                    programCounter.push(registers.Z ? programCounter.get() : arg1.getIntValue());
                    break;
            }
        }
    }

    private void CALLAT(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        int top4Bits = (arg0.getIntValue() & 0b00001111) << 8;

        programCounter.push(top4Bits + arg1.getIntValue());
    }

    private void RETURN() {
        programCounter.pop();
    }

    private void RETURN(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        switch (arg0.getStringValue()) {
            case PBFlagArgument.C:
                if (registers.C) programCounter.pop();
                break;
            case PBFlagArgument.NC:
                if (!registers.C) programCounter.pop();
                break;
            case PBFlagArgument.Z:
                if (registers.Z) programCounter.pop();
                break;
            case PBFlagArgument.NZ:
                if (!registers.Z) programCounter.pop();
                break;
        }
    }

    private void LOADANDRETURN(PBInstructionArgument arg0, PBInstructionArgument arg1) {
        arg0.setValue(arg1.getIntValue());
        programCounter.pop();
        clockCycles += 1; // This instruction takes 2 clock cycles to run
    }

    // Version Control
    private void HWBUILD(PBInstructionArgument arg0) {
        arg0.setValue(0); // This should be definable, setting to 0 for now for simplicity
        registers.setCarry(true);
        registers.setZero(arg0.getIntValue() == 0);
    }

    public int getNextBlockStart(PBInstruction[] instructions, int startOfBlock) {
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

    public void parse(PBInstruction instruction) {
        System.out.println(instruction);
        programCounter.increment();

        if (instruction.instruction == PBInstructionSet.NOP) {
            return;
        }

        switch (instruction.instruction) {
            // PBRegister loading
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

            // PBRegister Bank Selection
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
                if (instruction.arg0 instanceof PBAbsoluteAddress) {
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
                if (instruction.arg0 instanceof PBNoArgument) {
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
                throw new UnsupportedOperationException("Unrecognised instruction. Has the instruction been added to the switch statement in J5Parser?");
        }

        clockCycles += 1;
    }

    public void parse(PBInstruction[] program) {
        while (programCounter.get() < program.length) {
            PBInstruction instruction = program[programCounter.get()];

            parse(instruction);

            System.out.println(registers);
        }

        System.out.println(String.format("Finished in %d clock cycles", clockCycles));
    }

    private PBParser() {
        RESET();
    }
}

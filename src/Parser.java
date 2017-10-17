import java.util.Stack;

public class Parser {
    private Registers registers = Registers.getInstance();
    private ScratchPad scratchPad = ScratchPad.getInstance();
    private ALU alu = ALU.getInstance();

    Stack<Integer> programCounter = new Stack<>();

    public void RESET() {
        programCounter = new Stack<>();
        setProgramCounter(0);

        registers.setCarry(false);
        registers.setZero(false);
    }

    private void setProgramCounter(int value) {
        setProgramCounter(value, false);
    }

    /*
     Requires error handling for max size of stack being reached
     */
    private void setProgramCounter(int value, boolean push) {
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

    private void incrementProgramCounter() {
        int nextValue = programCounter.peek() + 1;
        setProgramCounter(nextValue > 0x3ff ? 0 : nextValue);
    }

    // Register loading
    public void LOAD(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg1.getValue());
    }

    /*
    NOTE: This currently allows a Literal as the second argument, which isn't in the spec.
    I think these functions should have the Instruction arguments replaced with explicit NAME(InstructionSet, arg0, arg1)
    to make it easier to disallow invalid instructions.
     */
    public void STAR(InstructionArgument arg0, InstructionArgument arg1) {
        int copiedValue = arg1.getValue();
        registers.toggleActiveRegisters();

        arg0.setValue(copiedValue);
        registers.toggleActiveRegisters();
    }

    // Register Bank Selection
    private void REGBANK(InstructionArgument arg0) {
        registers.aRegisterBank = arg0.getValue() == 1;
    }

    // Jump
    private void JUMP(InstructionArgument arg0) {
        setProgramCounter(arg0.getValue());
    }

    private void JUMP(InstructionArgument arg0, InstructionArgument arg1) {
        switch (arg0.getValue()) {
            case FlagArgument.C:
                setProgramCounter(registers.C ? arg1.getValue() : programCounter.peek());
                break;
            case FlagArgument.NC:
                setProgramCounter(registers.C ? programCounter.peek() : arg1.getValue());
                break;
            case FlagArgument.Z:
                setProgramCounter(registers.Z ? arg1.getValue() : programCounter.peek());
                break;
            case FlagArgument.NZ:
                setProgramCounter(registers.Z ? programCounter.peek() : arg1.getValue());
                break;
        }
    }

    private void JUMPAT(InstructionArgument arg0, InstructionArgument arg1) {
        int top4Bits = (arg0.getValue() & 0b00001111) << 8;

        setProgramCounter(top4Bits + arg1.getValue());
    }

    // Subroutines
    private void CALL(InstructionArgument arg0, InstructionArgument arg1) {
        if (arg0 instanceof AbsoluteAddress) {
            setProgramCounter(arg0.getValue(), true);

        } else {
            switch (arg0.getValue()) {
                case FlagArgument.C:
                    setProgramCounter(registers.C ? arg1.getValue() : programCounter.peek(), true);
                    break;
                case FlagArgument.NC:
                    setProgramCounter(registers.C ? programCounter.peek() : arg1.getValue(), true);
                    break;
                case FlagArgument.Z:
                    setProgramCounter(registers.Z ? arg1.getValue() : programCounter.peek(), true);
                    break;
                case FlagArgument.NZ:
                    setProgramCounter(registers.Z ? programCounter.peek() : arg1.getValue(), true);
                    break;
            }
        }
    }

    private void CALLAT(InstructionArgument arg0, InstructionArgument arg1) {
        int top4Bits = (arg0.getValue() & 0b00001111) << 8;

        setProgramCounter(top4Bits + arg1.getValue(), true);
    }

    private void RETURN() {
        programCounter.pop();
    }

    private void RETURN(InstructionArgument arg0, InstructionArgument arg1) {
        switch (arg0.getValue()) {
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
        arg0.setValue(arg1.getValue());
        programCounter.pop();
    }

    // Version Control
    private void HWBUILD(InstructionArgument arg0) {
        arg0.setValue(0); // This should be definable, setting to 0 for now for simplicity
        registers.setCarry(true);
        registers.setZero(arg0.getValue() == 0);
    }

    public void parse(Instruction[] program) {
        while (programCounter.peek() < program.length) {
            Instruction instruction = program[programCounter.peek()];
            incrementProgramCounter();

            if (instruction == null) {
                continue;
            }

            switch (instruction.instruction) {
                // Register loading
                case LOAD:
                    LOAD(instruction.arg0, instruction.arg1);
                    break;
                case STAR:
                    STAR(instruction.arg0, instruction.arg1);
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
                    alu.ADD(instruction.arg0, instruction.arg1);
                    break;
                case ADDCY:
                    alu.ADDCY(instruction.arg0, instruction.arg1);
                    break;
                case SUB:
                    alu.SUB(instruction.arg0, instruction.arg1);
                    break;
                case SUBCY:
                    alu.SUBCY(instruction.arg0, instruction.arg1);
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
                    REGBANK(instruction.arg0);
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

            System.out.println(registers);
        }
    }

    public Parser() {
        RESET();
    }
}

import java.util.Stack;

public class Parser {
    private Registers registers;
    private ScratchPad scratchPad;

    Stack<Integer> programCounter = new Stack<Integer>();

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

    // Register loading
    private void LOAD(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg1.getValue());
    }

    /*
    NOTE: This currently allows a Constant as the second argument, which isn't in the spec.
    I think these functions should have the Instruction arguments replaced with explicit NAME(InstructionSet, arg0, arg1)
    to make it easier to disallow invalid instructions.
     */
    private void STAR(InstructionArgument arg0, InstructionArgument arg1) {
        int copiedValue = arg1.getValue();
        registers.toggleActiveRegisters();

        arg0.setValue(copiedValue);
        registers.toggleActiveRegisters();
    }

    // Logical
    private void AND(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg0.getValue() & arg1.getValue());

        registers.setCarry(false);
        registers.setZero(arg0.getValue() == Register.MIN_VALUE);
    }

    private void OR(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg0.getValue() | arg1.getValue());

        registers.setCarry(false);
        registers.setZero(arg0.getValue() == Register.MIN_VALUE);
    }

    private void XOR(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(arg0.getValue() ^ arg1.getValue());

        registers.setCarry(false);
        registers.setZero(arg0.getValue() == Register.MIN_VALUE);
    }

    // Arithmetic
    private void ADD(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() + arg1.getValue();

        if (result > Register.MAX_VALUE) {
            registers.setCarry(true);
            result = result % (Register.MAX_VALUE + 1);
        } else {
            registers.setCarry(false);
        }

        registers.setZero(result == Register.MIN_VALUE);

        arg0.setValue(result);
    }

    /*
      This was just copied from the Python implementation. It could likely be implemented better.
     */
    private void ADDCY(InstructionArgument arg0, InstructionArgument arg1) {
        boolean beforeZ = registers.Z;

        if (registers.C) {
            ADD(arg0, new Constant(1));
        }

        ADD(arg0, arg1);

        if (!beforeZ) {
            registers.setZero(false);
        }
    }

    private void SUB(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() - arg1.getValue();

        if (result < Register.MIN_VALUE) {
            result += Register.MAX_VALUE + 1;
            registers.setCarry(true);
        } else {
            registers.setCarry(false);
        }

        registers.setZero(result == Register.MIN_VALUE);

        arg0.setValue(result);
    }

    /*
      This was just copied from the Python implementation. It could likely be implemented better.
     */
    private void SUBCY(InstructionArgument arg0, InstructionArgument arg1) {
        boolean beforeZ = registers.Z;

        if (registers.C) {
            SUB(arg0, new Constant(1));
        }

        SUB(arg0, arg1);

        if (!beforeZ) {
            registers.setZero(false);
        }
    }

    // Test and Compare
    private void TEST(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() & arg1.getValue();

        // Carry bit true if odd number of 1 bits
        boolean carry = false;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE);
    }

    private void TESTCY(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() & arg1.getValue();

        // Carry bit true if odd number of 1 bits including carry bit
        boolean carry = registers.C;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    private void COMPARE(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() - arg1.getValue();

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE);
    }

    private void COMPARECY(InstructionArgument arg0, InstructionArgument arg1) {
        int result = arg0.getValue() - arg1.getValue();

        if (registers.C) {
            result -= 1;
        }

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    // Shift and Rotate
    private void SL(InstructionSet instruction, InstructionArgument arg0) {
        int leastSignificantBit;
        switch (instruction) {
            case SL0:
                leastSignificantBit = 0;
                break;
            case SL1:
                leastSignificantBit = 1;
                break;
            case SLX:
                leastSignificantBit = arg0.getValue() & 0b00000001;
                break;
            default:
                leastSignificantBit = registers.C ? 1 : 0;
                break;
        }

        registers.setCarry((arg0.getValue() & 0b10000000) != 0);

        int newValue = (arg0.getValue() << 1) + leastSignificantBit;
        arg0.setValue(newValue & Register.MAX_VALUE);

        registers.setZero(arg0.getValue() == 0);
    }

    private void SR(InstructionSet instruction, InstructionArgument arg0) {
        int mostSignificantBit;
        switch (instruction) {
            case SR0:
                mostSignificantBit = 0;
                break;
            case SR1:
                mostSignificantBit = 0b10000000;
                break;
            case SRX:
                mostSignificantBit = arg0.getValue() & 0b10000000;
                break;
            default:
                mostSignificantBit = registers.C ? 0b10000000 : 0;
                break;
        }

        registers.setCarry((arg0.getValue() & 0b00000001) != 0);

        int newValue = (arg0.getValue() >> 1) + mostSignificantBit;
        arg0.setValue(newValue & Register.MAX_VALUE);

        registers.setZero(arg0.getValue() == 0);
    }

    private void RL(InstructionArgument arg0) {
        registers.setCarry((arg0.getValue() & 0b10000000) != 0);
        SL(InstructionSet.SLA, arg0);
    }

    private void RR(InstructionArgument arg0) {
        registers.setCarry((arg0.getValue() & 0b00000001) != 0);
        SR(InstructionSet.SRA, arg0);
    }

    // Register Bank Selection
    private void REGBANK(InstructionArgument arg0) {
        registers.aRegisterBank = arg0.getValue() == 1;
    }

    // Scratch Pad Memory
    private void STORE(InstructionArgument arg0, InstructionArgument arg1) {
        scratchPad.setMemory(arg1.getValue(), arg0.getValue());
    }

    private void FETCH(InstructionArgument arg0, InstructionArgument arg1) {
        arg0.setValue(scratchPad.getMemory(arg1.getValue()));
    }

    // Jump
    private void JUMP(InstructionArgument arg0, InstructionArgument arg1) {
        if (arg0 instanceof AbsoluteAddress) {
            setProgramCounter(arg0.getValue());

        } else {
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
            setProgramCounter(programCounter.peek() + 1);

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
                    AND(instruction.arg0, instruction.arg1);
                    break;
                case OR:
                    OR(instruction.arg0, instruction.arg1);
                    break;
                case XOR:
                    XOR(instruction.arg0, instruction.arg1);
                    break;

                // Arithmetic
                case ADD:
                    ADD(instruction.arg0, instruction.arg1);
                    break;
                case ADDCY:
                    ADDCY(instruction.arg0, instruction.arg1);
                    break;
                case SUB:
                    SUB(instruction.arg0, instruction.arg1);
                    break;
                case SUBCY:
                    SUBCY(instruction.arg0, instruction.arg1);
                    break;

                // Test and Compare
                case TEST:
                    TEST(instruction.arg0, instruction.arg1);
                    break;
                case TESTCY:
                    TESTCY(instruction.arg0, instruction.arg1);
                    break;
                case COMPARE:
                    COMPARE(instruction.arg0, instruction.arg1);
                    break;
                case COMPARECY:
                    COMPARECY(instruction.arg0, instruction.arg1);
                    break;

                // Shift and Rotate
                case SL0:
                case SL1:
                case SLX:
                case SLA:
                    SL(instruction.instruction, instruction.arg0);
                    break;
                case SR0:
                case SR1:
                case SRX:
                case SRA:
                    SR(instruction.instruction, instruction.arg0);
                    break;
                case RL:
                    RL(instruction.arg0);
                    break;
                case RR:
                    RR(instruction.arg0);
                    break;

                // Register Bank Selection
                case REGBANK:
                    REGBANK(instruction.arg0);
                    break;

                // Scratch Pad Memory
                case STORE:
                    STORE(instruction.arg0, instruction.arg1);
                    break;
                case FETCH:
                    FETCH(instruction.arg0, instruction.arg1);
                    break;

                // Jump
                case JUMP:
                    JUMP(instruction.arg0, instruction.arg1);
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

    public Parser(Registers registers, ScratchPad scratchPad) {
        this.registers = registers;
        this.scratchPad = scratchPad;

        setProgramCounter(0);
    }
}

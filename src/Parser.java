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
    private void LOAD(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg1.getValue());
    }

    /*
    NOTE: This currently allows a Constant as the second argument, which isn't in the spec.
    I think these functions should have the Instruction arguments replaced with explicit NAME(InstructionSet, arg0, arg1)
    to make it easier to disallow invalid instructions.
     */
    private void STAR(Instruction instruction) {
        int copiedValue = instruction.arg1.getValue();
        registers.toggleActiveRegisters();

        instruction.arg0.setValue(copiedValue);
        registers.toggleActiveRegisters();
    }

    // Logical
    private void AND(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg0.getValue() & instruction.arg1.getValue());

        registers.setCarry(false);
        registers.setZero(instruction.arg0.getValue() == Register.MIN_VALUE);
    }

    private void OR(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg0.getValue() | instruction.arg1.getValue());

        registers.setCarry(false);
        registers.setZero(instruction.arg0.getValue() == Register.MIN_VALUE);
    }

    private void XOR(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg0.getValue() ^ instruction.arg1.getValue());

        registers.setCarry(false);
        registers.setZero(instruction.arg0.getValue() == Register.MIN_VALUE);
    }

    // Arithmetic
    private void ADD(Instruction instruction) {
        InstructionArgument arg0 = instruction.arg0;
        InstructionArgument arg1 = instruction.arg1;

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
    private void ADDCY(Instruction instruction) {
        boolean beforeZ = registers.Z;

        if (registers.C) {
            ADD(new Instruction(instruction.instruction, instruction.arg0, new Constant(1)));
        }

        ADD(instruction);

        if (!beforeZ) {
            registers.setZero(false);
        }
    }

    private void SUB(Instruction instruction) {
        InstructionArgument arg0 = instruction.arg0;
        InstructionArgument arg1 = instruction.arg1;

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
    private void SUBCY(Instruction instruction) {
        boolean beforeZ = registers.Z;

        if (registers.C) {
            SUB(new Instruction(instruction.instruction, instruction.arg0, new Constant(1)));
        }

        SUB(instruction);

        if (!beforeZ) {
            registers.setZero(false);
        }
    }

    // Test and Compare
    private void TEST(Instruction instruction) {
        int result = instruction.arg0.getValue() & instruction.arg1.getValue();

        // Carry bit true if odd number of 1 bits
        boolean carry = false;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE);
    }

    private void TESTCY(Instruction instruction) {
        int result = instruction.arg0.getValue() & instruction.arg1.getValue();

        // Carry bit true if odd number of 1 bits including carry bit
        boolean carry = registers.C;
        for (int i=0; i<8; i++) {
            carry ^= ((result >> i) & 1) == 1;
        }

        registers.setCarry(carry);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    private void COMPARE(Instruction instruction) {
        int result = instruction.arg0.getValue() - instruction.arg1.getValue();

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE);
    }

    private void COMPARECY(Instruction instruction) {
        int result = instruction.arg0.getValue() - instruction.arg1.getValue();

        if (registers.C) {
            result -= 1;
        }

        registers.setCarry(result < 0);
        registers.setZero(result == Register.MIN_VALUE && registers.Z);
    }

    // Shift and Rotate
    private void SL(Instruction instruction) {
        int leastSignificantBit;
        switch (instruction.instruction) {
            case SL0:
                leastSignificantBit = 0;
                break;
            case SL1:
                leastSignificantBit = 1;
                break;
            case SLX:
                leastSignificantBit = instruction.arg0.getValue() & 0b00000001;
                break;
            default:
                leastSignificantBit = registers.C ? 1 : 0;
                break;
        }

        registers.setCarry((instruction.arg0.getValue() & 0b10000000) != 0);

        int newValue = (instruction.arg0.getValue() << 1) + leastSignificantBit;
        instruction.arg0.setValue(newValue & Register.MAX_VALUE);

        registers.setZero(instruction.arg0.getValue() == 0);
    }

    private void SR(Instruction instruction) {
        int mostSignificantBit;
        switch (instruction.instruction) {
            case SR0:
                mostSignificantBit = 0;
                break;
            case SR1:
                mostSignificantBit = 0b10000000;
                break;
            case SRX:
                mostSignificantBit = instruction.arg0.getValue() & 0b10000000;
                break;
            default:
                mostSignificantBit = registers.C ? 0b10000000 : 0;
                break;
        }

        registers.setCarry((instruction.arg0.getValue() & 0b00000001) != 0);

        int newValue = (instruction.arg0.getValue() >> 1) + mostSignificantBit;
        instruction.arg0.setValue(newValue & Register.MAX_VALUE);

        registers.setZero(instruction.arg0.getValue() == 0);
    }

    private void RL(Instruction instruction) {
        registers.setCarry((instruction.arg0.getValue() & 0b10000000) != 0);
        SL(new Instruction(InstructionSet.SLA, instruction.arg0, null));
    }

    private void RR(Instruction instruction) {
        registers.setCarry((instruction.arg0.getValue() & 0b00000001) != 0);
        SR(new Instruction(InstructionSet.SRA, instruction.arg0, null));
    }

    // Register Bank Selection
    private void REGBANK(Instruction instruction) {
        registers.aRegisterBank = instruction.arg0.getValue() == 1;
    }

    // Scratch Pad Memory
    private void STORE(Instruction instruction) {
        scratchPad.setMemory(instruction.arg1.getValue(), instruction.arg0.getValue());
    }

    private void FETCH(Instruction instruction) {
        instruction.arg0.setValue(scratchPad.getMemory(instruction.arg1.getValue()));
    }

    // Jump
    private void JUMP(Instruction instruction) {
        if (instruction.arg0 instanceof AbsoluteAddress) {
            setProgramCounter(instruction.arg0.getValue());

        } else {
            switch (instruction.arg0.getValue()) {
                case FlagArgument.C:
                    setProgramCounter(registers.C ? instruction.arg1.getValue() : programCounter.peek());
                    break;
                case FlagArgument.NC:
                    setProgramCounter(registers.C ? programCounter.peek() : instruction.arg1.getValue());
                    break;
                case FlagArgument.Z:
                    setProgramCounter(registers.Z ? instruction.arg1.getValue() : programCounter.peek());
                    break;
                case FlagArgument.NZ:
                    setProgramCounter(registers.Z ? programCounter.peek() : instruction.arg1.getValue());
                    break;
            }
        }
    }

    private void JUMPAT(Instruction instruction) {
        int top4Bits = (instruction.arg0.getValue() & 0b00001111) << 8;

        setProgramCounter(top4Bits + instruction.arg1.getValue());
    }

    // Subroutines
    private void CALL(Instruction instruction) {
        if (instruction.arg0 instanceof AbsoluteAddress) {
            setProgramCounter(instruction.arg0.getValue(), true);

        } else {
            switch (instruction.arg0.getValue()) {
                case FlagArgument.C:
                    setProgramCounter(registers.C ? instruction.arg1.getValue() : programCounter.peek(), true);
                    break;
                case FlagArgument.NC:
                    setProgramCounter(registers.C ? programCounter.peek() : instruction.arg1.getValue(), true);
                    break;
                case FlagArgument.Z:
                    setProgramCounter(registers.Z ? instruction.arg1.getValue() : programCounter.peek(), true);
                    break;
                case FlagArgument.NZ:
                    setProgramCounter(registers.Z ? programCounter.peek() : instruction.arg1.getValue(), true);
                    break;
            }
        }
    }

    private void CALLAT(Instruction instruction) {
        int top4Bits = (instruction.arg0.getValue() & 0b00001111) << 8;

        setProgramCounter(top4Bits + instruction.arg1.getValue(), true);
    }

    private void RETURN(Instruction instruction) {
        if (instruction.arg0 instanceof NoArgument) {
            programCounter.pop();

        } else {
            switch (instruction.arg0.getValue()) {
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
    }

    private void LOADANDRETURN(Instruction instruction) {
        instruction.arg0.setValue(instruction.arg1.getValue());
        programCounter.pop();
    }

    // Version Control
    private void HWBUILD(Instruction instruction) {
        instruction.arg0.setValue(0); // This should be definable, setting to 0 for now for simplicity
        registers.setCarry(true);
        registers.setZero(instruction.arg0.getValue() == 0);
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
                    LOAD(instruction);
                    break;
                case STAR:
                    STAR(instruction);
                    break;

                // Logical
                case AND:
                    AND(instruction);
                    break;
                case OR:
                    OR(instruction);
                    break;
                case XOR:
                    XOR(instruction);
                    break;

                // Arithmetic
                case ADD:
                    ADD(instruction);
                    break;
                case ADDCY:
                    ADDCY(instruction);
                    break;
                case SUB:
                    SUB(instruction);
                    break;
                case SUBCY:
                    SUBCY(instruction);
                    break;

                // Test and Compare
                case TEST:
                    TEST(instruction);
                    break;
                case TESTCY:
                    TESTCY(instruction);
                    break;
                case COMPARE:
                    COMPARE(instruction);
                    break;
                case COMPARECY:
                    COMPARECY(instruction);
                    break;

                // Shift and Rotate
                case SL0:
                case SL1:
                case SLX:
                case SLA:
                    SL(instruction);
                    break;
                case SR0:
                case SR1:
                case SRX:
                case SRA:
                    SR(instruction);
                    break;
                case RL:
                    RL(instruction);
                    break;
                case RR:
                    RR(instruction);
                    break;

                // Register Bank Selection
                case REGBANK:
                    REGBANK(instruction);
                    break;

                // Scratch Pad Memory
                case STORE:
                    STORE(instruction);
                    break;
                case FETCH:
                    FETCH(instruction);
                    break;

                // Jump
                case JUMP:
                    JUMP(instruction);
                    break;
                case JUMPAT:
                    JUMPAT(instruction);
                    break;

                // Subroutines
                case CALL:
                    CALL(instruction);
                    break;
                case CALLAT:
                    CALLAT(instruction);
                    break;
                case RETURN:
                    RETURN(instruction);
                    break;
                case LOADANDRETURN:
                    LOADANDRETURN(instruction);
                    break;

                // Version Control
                case HWBUILD:
                    HWBUILD(instruction);
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

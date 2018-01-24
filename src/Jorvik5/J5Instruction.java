package Jorvik5;

import Jorvik5.Groups.J5InstructionSet;
import Jorvik5.InstructionArguments.J5InstructionArgument;

public class J5Instruction {
    public J5InstructionSet instruction;
    public J5InstructionArgument arg;

    public J5Instruction() {
        this(null, null);
    }

    @Override
    public String toString() {
        if (instruction == null) {
            return null;
        }

        if (arg == null) {
            return instruction.toString();
        } else {
            return String.format("%s %s", instruction, Integer.toHexString(arg.getValue()));
        }
    }

    public J5Instruction(J5InstructionSet instruction, J5InstructionArgument arg) {
        this.instruction = instruction;
        this.arg = arg;
    }
}
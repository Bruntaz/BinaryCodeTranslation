package Jorvik5;

import Jorvik5.Groups.InstructionSet;
import Jorvik5.InstructionArguments.InstructionArgument;

public class Instruction {
    public InstructionSet instruction;
    public InstructionArgument arg;

    public Instruction() {
        this(null, null);
    }

    @Override
    public String toString() {
        if (arg == null) {
            return instruction.toString();
        } else {
            return String.format("%s %s", instruction, Integer.toHexString(arg.getValue()));
        }
    }

    public Instruction(InstructionSet instruction, InstructionArgument arg) {
        this.instruction = instruction;
        this.arg = arg;
    }
}

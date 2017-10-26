package PicoBlazeSimulator;

import PicoBlazeSimulator.Groups.InstructionSet;
import PicoBlazeSimulator.InstructionArguments.InstructionArgument;

public class Instruction {
    public InstructionSet instruction;
    public InstructionArgument arg0;
    public InstructionArgument arg1;
    public boolean isBlockStart;

    public Instruction() {
        this(null, null, null, false);
    }

    public Instruction(InstructionSet instruction, InstructionArgument arg0, InstructionArgument arg1, boolean isBlockStart) {
        this.instruction = instruction;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.isBlockStart = isBlockStart;
    }
}

public class Instruction {
    InstructionSet instruction;
    InstructionArgument arg0;
    InstructionArgument arg1;
    boolean hasLabel;

    public Instruction(InstructionSet instruction, InstructionArgument arg0, InstructionArgument arg1, boolean hasLabel) {
        this.instruction = instruction;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.hasLabel = hasLabel;
    }
}

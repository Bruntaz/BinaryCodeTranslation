public class Instruction {
    InstructionSet instruction;
    InstructionArgument arg0;
    InstructionArgument arg1;

    public Instruction(InstructionSet instruction, InstructionArgument arg0, InstructionArgument arg1) {
        this.instruction = instruction;
        this.arg0 = arg0;
        this.arg1 = arg1;
    }
}

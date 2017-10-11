import java.util.List;

public class Lexer {
    public static Instruction[] lex(Registers registers, List<String> program) {
        Instruction[] instructions = new Instruction[program.size()];
        for (int i=0; i<program.size(); i++) {
            String[] splitInstruction = program.get(i).split(" ");

            if (splitInstruction.length >= 3) {
                InstructionSet instructionSet = InstructionSet.valueOf(splitInstruction[0]);

                InstructionArgument arg0;
                try {
                    arg0 = registers.getRegister(RegisterName.valueOf(splitInstruction[1]));
                } catch (Exception e) {
                    arg0 = new RegisterBank(splitInstruction[1].equals("A") ? 1 : 0);
                }

                InstructionArgument arg1;
                try {
                    arg1 = registers.getRegister(RegisterName.valueOf(splitInstruction[2]));
                } catch (Exception e) {
                    arg1 = new Constant(Integer.parseInt(splitInstruction[2], 16));
                }

                instructions[i] = new Instruction(instructionSet, arg0, arg1);
            }
        }

        return instructions;
    }
}

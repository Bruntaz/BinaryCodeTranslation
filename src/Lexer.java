import java.util.List;

/*
This entire class is terrible and not fit for purpose. It needs to be completely rewritten to actually be able to read
KCPSM6 assembly.
 */
public class Lexer {
    public static Instruction[] lex(List<String> program) {
        Registers registers = Registers.getInstance();
        Instruction[] instructions = new Instruction[program.size()];

        for (int i=0; i<program.size(); i++) {
            String[] splitInstruction = program.get(i).split(" ");

            if (splitInstruction.length >= 3) {

                InstructionSet instructionSet;
                switch (splitInstruction[0]) {
                    case "JUMP@":
                        instructionSet = InstructionSet.JUMPAT;
                        break;
                    case "CALL@":
                        instructionSet = InstructionSet.CALLAT;
                        break;
                    case "LOAD&RETURN":
                        instructionSet = InstructionSet.LOADANDRETURN;
                        break;
                    default:
                        instructionSet = InstructionSet.valueOf(splitInstruction[0]);
                }

                InstructionArgument arg0;
                if (instructionSet == InstructionSet.JUMP ||
                    instructionSet == InstructionSet.CALL ||
                    instructionSet == InstructionSet.RETURN) {

                    try {
                        arg0 = new AbsoluteAddress(Integer.parseInt(splitInstruction[1], 16));
                    } catch (NumberFormatException e) {
                        switch (splitInstruction[1]) {
                            case "C":
                                arg0 = new FlagArgument(FlagArgument.C);
                                break;
                            case "NC":
                                arg0 = new FlagArgument(FlagArgument.NC);
                                break;
                            case "Z":
                                arg0 = new FlagArgument(FlagArgument.Z);
                                break;
                            default:
                                arg0 = new FlagArgument(FlagArgument.NZ);
                        }
                    }

                } else {
                    try {
                        arg0 = registers.getRegister(RegisterName.valueOf(splitInstruction[1]));
                    } catch (Exception e) {
                        arg0 = new RegisterBank(splitInstruction[1].equals("A") ? 1 : 0);
                    }
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

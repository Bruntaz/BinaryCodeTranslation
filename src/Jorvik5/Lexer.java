package Jorvik5;

import Jorvik5.Groups.InstructionSet;
import Jorvik5.InstructionArguments.InstructionArgument;
import Jorvik5.InstructionArguments.Literal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Lexer {
    // This will be the lexer for the stack assembly code (when it is implemented)

    private static Lexer ourInstance = new Lexer();
    public static Lexer getInstance() {
        return ourInstance;
    }

    private HashSet<InstructionSet> hasArg = new HashSet<>(Arrays.asList(
            InstructionSet.SSET, InstructionSet.BRANCH, InstructionSet.SBRANCH, InstructionSet.BRZERO,
            InstructionSet.SBRZERO, InstructionSet.BRZERO, InstructionSet.SBRANCH, InstructionSet.SBRZERO,
            InstructionSet.LBRANCH, InstructionSet.CALL)
    );


    private InstructionSet getInstruction(String[] sections) {
        String upperCaseInstruction = sections[0].toUpperCase();
        return InstructionSet.valueOf(upperCaseInstruction);
    }

    /*
    This needs to support more than just literals in the future (addresses etc)
     */
    private InstructionArgument getArgument(String[] sections) {
        int intArg = Integer.parseInt(sections[1], 16);
        return new Literal(intArg);
    }

    public Instruction[] lex(List<String> program) {
        Instruction[] instructions = new Instruction[program.size()];

        String[][] allSections = new String[program.size()][];

        for (int lineNumber = 0; lineNumber < program.size(); lineNumber++) {
            String line = program.get(lineNumber).split(";")[0]; // Remove comments
            String[] sections = line.trim().split("\\s+"); // Split on (and remove) whitespace

            allSections[lineNumber] = sections;

            // Initialise array to empty Instructions to fill later
            instructions[lineNumber] = new Instruction();
        }

        for (int lineNumber = 0; lineNumber < program.size(); lineNumber++) {
            String[] sections = allSections[lineNumber];

            if (sections[0].isEmpty()) {
                // Blank line
                continue;
            }

            InstructionSet instruction = getInstruction(sections);
            instructions[lineNumber].instruction = instruction;

            if (hasArg.contains(instruction)) {
                instructions[lineNumber].arg = getArgument(sections);
            }
        }

        return instructions;
    }
}

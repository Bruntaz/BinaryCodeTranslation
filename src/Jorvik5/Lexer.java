package Jorvik5;

import Jorvik5.Groups.InstructionSet;
import Jorvik5.InstructionArguments.*;

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
            InstructionSet.SBRZERO, InstructionSet.LBRANCH, InstructionSet.CALL, InstructionSet.FETCH,
            InstructionSet.STORE)
    );


    private InstructionSet getInstruction(String[] sections) {
        String upperCaseInstruction = sections[0].toUpperCase();
        return InstructionSet.valueOf(upperCaseInstruction);
    }

    private InstructionArgument getArgument(InstructionSet instruction, String[] sections) {
        int intArg = Integer.parseInt(sections[1], 16);

        switch (instruction) {
            case SSET:
                return new ShortLiteral(intArg);
            case BRANCH:
            case BRZERO:
                return new RelativeAddress(intArg);
            case SBRANCH:
            case SBRZERO:
                return new ShortRelativeAddress(intArg);
            case LBRANCH:
            case CALL:
                return new AbsoluteAddress(intArg - 1);
            case FETCH:
            case STORE:
                return new AbsoluteAddress(intArg);
        }

        throw new Error("Unsupported argument type in source");
    }

    private String[] splitInput(String input) {
        String line; // Remove comments

        String[] split = input.split(";");
        if (split.length == 0) {
            line = "";
        } else {
            line = split[0];
        }

        return line.trim().split("\\s+"); // Split on (and remove) whitespace
    }

    public Instruction lex(String instruction) {
        String[] sections = splitInput(instruction);

        if (sections[0].isEmpty()) {
            // Blank line
            return new Instruction(InstructionSet.NOP, null);
        }

        Instruction toReturn = new Instruction();
        toReturn.instruction = getInstruction(sections);

        if (hasArg.contains(toReturn.instruction)) {
            toReturn.arg = getArgument(toReturn.instruction, sections);
        }

        return toReturn;
    }

    public Instruction[] lex(List<String> program) {
        Instruction[] instructions = new Instruction[program.size()];

        String[][] allSections = new String[program.size()][];

        for (int lineNumber = 0; lineNumber < program.size(); lineNumber++) {
            allSections[lineNumber] = splitInput(program.get(lineNumber));

            // Initialise array to empty Instructions to fill later
            instructions[lineNumber] = new Instruction(InstructionSet.NOP, null);
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
                instructions[lineNumber].arg = getArgument(instruction, sections);
            }
        }

        return instructions;
    }
}

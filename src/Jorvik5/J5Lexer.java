package Jorvik5;

import Jorvik5.Groups.J5InstructionSet;
import Jorvik5.InstructionArguments.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class J5Lexer {
    // This will be the lexer for the stack assembly code (when it is implemented)

    private static J5Lexer ourInstance = new J5Lexer();
    public static J5Lexer getInstance() {
        return ourInstance;
    }

    private HashMap<String, Integer> labelMap = new HashMap<>();
    private HashSet<J5InstructionSet> hasArg = new HashSet<>(Arrays.asList(
            J5InstructionSet.SSET, J5InstructionSet.BRANCH, J5InstructionSet.SBRANCH, J5InstructionSet.BRZERO,
            J5InstructionSet.SBRZERO, J5InstructionSet.BRCARRY, J5InstructionSet.SBRCARRY, J5InstructionSet.LBRANCH,
            J5InstructionSet.CALL, J5InstructionSet.FETCH, J5InstructionSet.STORE)
    );

    /*
    Labels must be on a blank line for simplicity
     */
    private String getLabel(String[] sections) {
        if (sections[0].endsWith(":")) {
            return sections[0].split(":")[0];
        } else {
            return null;
        }
    }

    private J5InstructionSet getInstruction(String[] sections) {
        String upperCaseInstruction = sections[0].toUpperCase();
        return J5InstructionSet.valueOf(upperCaseInstruction);
    }

    private J5InstructionArgument getArgument(J5InstructionSet instruction, String[] sections) {
        int intArg;
        if (labelMap.containsKey(sections[1])) {
            intArg = labelMap.get(sections[1]) + 1;
        } else {
            intArg = Integer.parseInt(sections[1], 16);
        }

        switch (instruction) {
            case SSET:
                return new J5ShortLiteral(intArg);
            case BRANCH:
            case BRZERO:
            case BRCARRY:
                return new J5RelativeAddress(intArg);
            case SBRANCH:
            case SBRZERO:
            case SBRCARRY:
                return new J5ShortRelativeAddress(intArg);
            case LBRANCH:
            case CALL:
                return new J5AbsoluteAddress(intArg - 1);
            case FETCH:
            case STORE:
                return new J5AbsoluteAddress(intArg);
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

    public J5Instruction lex(String instruction) {
        String[] sections = splitInput(instruction);

        if (sections[0].isEmpty()) {
            // Blank line
            return new J5Instruction(J5InstructionSet.NOP, null);
        }

        J5Instruction toReturn = new J5Instruction();
        toReturn.instruction = getInstruction(sections);

        if (hasArg.contains(toReturn.instruction)) {
            toReturn.arg = getArgument(toReturn.instruction, sections);
        }

        return toReturn;
    }

    public J5Instruction[] lex(List<String> program) {
        J5Instruction[] instructions = new J5Instruction[program.size()];

        String[][] allSections = new String[program.size()][];

        for (int lineNumber = 0; lineNumber < program.size(); lineNumber++) {
            allSections[lineNumber] = splitInput(program.get(lineNumber));

            String label = getLabel(allSections[lineNumber]);
            if (label != null) {
                labelMap.put(label, lineNumber);
            }

            // Initialise array to empty Instructions to fill later
            instructions[lineNumber] = new J5Instruction(J5InstructionSet.NOP, null);
        }

        for (int lineNumber = 0; lineNumber < program.size(); lineNumber++) {
            String[] sections = allSections[lineNumber];

            if (sections[0].isEmpty() || getLabel(sections) != null) {
                // Blank line
                continue;
            }

            J5InstructionSet instruction = getInstruction(sections);
            instructions[lineNumber].instruction = instruction;

            if (hasArg.contains(instruction)) {
                instructions[lineNumber].arg = getArgument(instruction, sections);
            }
        }

        return instructions;
    }
}

package PicoBlazeSimulator;

import PicoBlazeSimulator.Groups.PBInstructionSet;
import PicoBlazeSimulator.Groups.PBRegisterName;
import PicoBlazeSimulator.InstructionArguments.*;

import java.util.*;

public class PBLexer {
    private static PBLexer ourInstance = new PBLexer();
    public static PBLexer getInstance() {
        return ourInstance;
    }

    private PBRegisters registers = PBRegisters.getInstance();
    private HashMap<String, Integer> labelMap = new HashMap<>();
    private HashMap<String, Integer> constantMap = new HashMap<>();

    private HashSet<PBInstructionSet> noArgs = new HashSet<>(Collections.singletonList(PBInstructionSet.RETURN));
    private HashSet<PBInstructionSet> oneArg = new HashSet<>(Arrays.asList(
            PBInstructionSet.SL0, PBInstructionSet.SL1, PBInstructionSet.SLX, PBInstructionSet.SLA, PBInstructionSet.RL,
            PBInstructionSet.SR0, PBInstructionSet.SR1, PBInstructionSet.SRX, PBInstructionSet.SRA, PBInstructionSet.RR,
            PBInstructionSet.REGBANK, PBInstructionSet.JUMP, PBInstructionSet.CALL, PBInstructionSet.RETURN)
    );
    private HashSet<PBInstructionSet> twoArgs = new HashSet<>(Arrays.asList(
            PBInstructionSet.LOAD, PBInstructionSet.STAR, PBInstructionSet.AND, PBInstructionSet.OR, PBInstructionSet.XOR,
            PBInstructionSet.ADD, PBInstructionSet.ADDCY, PBInstructionSet.SUB, PBInstructionSet.SUBCY,
            PBInstructionSet.TEST, PBInstructionSet.TESTCY, PBInstructionSet.COMPARE, PBInstructionSet.COMPARECY,
            PBInstructionSet.STORE, PBInstructionSet.FETCH, PBInstructionSet.JUMP, PBInstructionSet.JUMPAT,
            PBInstructionSet.CALL, PBInstructionSet.RETURN, PBInstructionSet.LOADANDRETURN, PBInstructionSet.CONSTANT)
    );

    private class LabelAndColon {
        String label;
        int colonSection;

        LabelAndColon(String label, int colonSection) {
            this.label = label;
            this.colonSection = colonSection;
        }
    }

    private class InstructionAndSection {
        PBInstructionSet instruction;
        int instructionSection;

        InstructionAndSection(PBInstructionSet instruction, int instructionSection) {
            this.instruction = instruction;
            this.instructionSection = instructionSection;
        }
    }

    private LabelAndColon getLabel(String[] sections) {
        if (sections.length > 1 && sections[1].startsWith(":")) {
            return new LabelAndColon(sections[0], 1);
        }

        if (sections[0].contains(":")) {
            return new LabelAndColon(sections[0].split(":")[0], 0);
        } else {
            return null;
        }
    }

    private InstructionAndSection getInstruction(String[] sections, LabelAndColon label) {
        if (sections[sections.length - 1].endsWith(":")) {
            // Blank line with label
            return null;
        }

        String upperCaseInstruction;
        int instructionSection;

        if (label == null) {
            upperCaseInstruction = sections[0].toUpperCase();
            instructionSection = 0;

        } else {
            String sectionWithInstruction;

            if (sections[label.colonSection].endsWith(":")) {
                instructionSection = label.colonSection + 1;
                sectionWithInstruction = sections[instructionSection];
            } else {
                instructionSection = label.colonSection;
                sectionWithInstruction = sections[label.colonSection];
            }

            String[] sectionSplit = sectionWithInstruction.split(":");
            upperCaseInstruction = sectionSplit[sectionSplit.length - 1].toUpperCase();
        }

        upperCaseInstruction = upperCaseInstruction.replaceAll("@", "AT");
        upperCaseInstruction = upperCaseInstruction.replaceAll("&", "AND");

        return new InstructionAndSection(PBInstructionSet.valueOf(upperCaseInstruction), instructionSection);
    }

    private boolean isValidNumberOfArguments(PBInstructionSet instructionName, int number) {
        switch (number) {
            case 0:
                return noArgs.contains(instructionName);
            case 1:
                return oneArg.contains(instructionName);
            case 2:
                return twoArgs.contains(instructionName);
            default:
                return false;
        }
    }

    private int convertToInteger(String toConvert) throws NumberFormatException {
        boolean invert = toConvert.startsWith("~");
        if (invert || constantMap.containsKey(toConvert)) {
            int convertedInt;

            if (invert) {
                convertedInt = constantMap.get(toConvert.substring(1)) ^ 0b11111111;
            } else {
                convertedInt = constantMap.get(toConvert);
            }

            return convertedInt;
        }

        int base = 16;
        if (toConvert.endsWith("'b")) {
            base = 2;
            toConvert = toConvert.substring(0, toConvert.length() - 2);
        } else if (toConvert.endsWith("'d")) {
            base = 10;
            toConvert = toConvert.substring(0, toConvert.length() - 2);
        }

        return Integer.parseInt(toConvert, base);
    }

    /*
    Not finished but it's a start
     */
    private PBInstructionArgument convertStringToArgument(PBInstructionSet instructionName, boolean firstArgument, String toConvert) {
        if (toConvert == null) {
            return new PBNoArgument();
        }

        if (instructionName == PBInstructionSet.CONSTANT && firstArgument) {
            return new PBNamedArgument(toConvert);
        }

        if (instructionName == PBInstructionSet.REGBANK) {
            return new PBRegisterBank(toConvert);
        }

        try {
            return registers.getRegister(PBRegisterName.valueOf(toConvert.toUpperCase()));

        } catch (IllegalArgumentException ignored) {}

        if (firstArgument && (instructionName == PBInstructionSet.JUMP
                           || instructionName == PBInstructionSet.CALL
                           || instructionName == PBInstructionSet.RETURN)) {
            switch (toConvert) {
                case "C":
                    return new PBFlagArgument(PBFlagArgument.C);
                case "NC":
                    return new PBFlagArgument(PBFlagArgument.NC);
                case "Z":
                    return new PBFlagArgument(PBFlagArgument.Z);
                case "NZ":
                    return new PBFlagArgument(PBFlagArgument.NZ);
            }
        }

        if (instructionName == PBInstructionSet.JUMP
                || instructionName == PBInstructionSet.CALL
                || instructionName == PBInstructionSet.RETURN) {

            try {
                return new PBAbsoluteAddress(labelMap.get(toConvert));

            } catch (NullPointerException ignore) {
                try {
                    return new PBAbsoluteAddress(convertToInteger(toConvert) - 1);
                } catch (NumberFormatException ignored) {}
            }
        }

        try {
            return new PBLiteral(convertToInteger(toConvert));
        } catch (NumberFormatException ignored) {}

        return new PBNoArgument();
    }

    private PBInstructionArgument[] getArguments(String[] sections, InstructionAndSection instruction) {
        int firstArgSection = instruction.instructionSection + 1;

        // No arguments
        if (sections.length == firstArgSection) {
            if (isValidNumberOfArguments(instruction.instruction, 0)) {
                return new PBInstructionArgument[]{
                        new PBNoArgument(),
                        new PBNoArgument(),
                };
            } else {
                return null;
            }
        } else {
            // Arguments
            String firstArgument = sections[firstArgSection];
            String secondArgument = null;
            int secondArgSection;

            if (firstArgument.contains(",")) {
                String[] splitArg = firstArgument.split(",");
                firstArgument = splitArg[0];

                if (splitArg.length > 1) {
                    secondArgSection = firstArgSection;
                    secondArgument = splitArg[1];
                } else {
                    secondArgSection = firstArgSection + 1;
                    secondArgument = sections[secondArgSection];
                }

            } else {
                if (sections.length > firstArgSection + 1) {
                    if (sections[firstArgSection + 1].equals(",")) {
                        secondArgSection = firstArgSection + 2;
                        secondArgument = sections[secondArgSection];
                    } else {
                        secondArgSection = firstArgSection + 1;
                        String[] splitSections = sections[secondArgSection].split(",");
                        if (splitSections.length > 1) {
                            secondArgument = splitSections[1];
                        } else {
                            System.err.println("Illegal number of arguments. Did you miss a comma?");
                            return null;
                        }
                    }
                } else {
                    // There is no second argument
                    secondArgSection = firstArgSection;
                }
            }

            if (sections.length > secondArgSection + 1) {
                // Invalid number of arguments (too many)
                return null;
            }

//            System.out.println(firstArgument);
//            System.out.println(secondArgument);

            if ((secondArgument == null && !isValidNumberOfArguments(instruction.instruction, 1)) ||
                (secondArgument != null && !isValidNumberOfArguments(instruction.instruction, 2))) {
                return null;

            } else {
                return new PBInstructionArgument[] {
                        convertStringToArgument(instruction.instruction, true, firstArgument),
                        convertStringToArgument(instruction.instruction, false, secondArgument),
                };
            }
        }
    }

    public PBInstruction[] lex(List<String> program) {
        PBInstruction[] instructions = new PBInstruction[program.size()];

        String[][] allSections = new String[program.size()][];
        for (int lineNumber=0; lineNumber<program.size(); lineNumber++) {
            String line; // Remove comments

            String[] split = program.get(lineNumber).split(";");
            if (split.length == 0) {
                line = "";
            } else {
                line = split[0];
            }

            String[] sections = line.trim().split("\\s+"); // Split on (and remove) whitespace
            allSections[lineNumber] = sections;

            /*
            TODO: Don't repeat this code in the next for loop. It is currently done twice to allow for knowledge of
            the location of all labels before execution.
             */
            LabelAndColon label = getLabel(sections);
            if (label != null) {
                labelMap.put(label.label, lineNumber);
            }

            // Initialise array to empty Instructions to fill later
            instructions[lineNumber] = new PBInstruction();
        }

        for (int lineNumber=0; lineNumber<program.size(); lineNumber++) {
            String[] sections = allSections[lineNumber];

            if (sections[0].isEmpty()) {
                // Blank line
                continue;
            }

            LabelAndColon label = getLabel(sections);
            if (label != null) {
                instructions[lineNumber].isBlockStart = true;
            }

            InstructionAndSection instructionAndSection = getInstruction(sections, label);
            if (instructionAndSection != null) {
                PBInstructionSet instructionName = instructionAndSection.instruction;
//                System.out.println(instructionName);

                PBInstructionArgument[] args = getArguments(sections, instructionAndSection);

                if (args == null) {
                    throw new IllegalArgumentException(String.format("Illegal number of arguments on line %d", lineNumber + 1));
                }

//                System.out.println(args[0]);
//                System.out.println(args[1]);

                if (instructionName == PBInstructionSet.CONSTANT) {
                    // Convert all constants to values in lexer so don't include constants in instructions array
                    constantMap.put(args[0].getStringValue(), args[1].getIntValue());

                } else {
                    instructions[lineNumber].instruction = instructionName;
                    instructions[lineNumber].arg0 = args[0];
                    instructions[lineNumber].arg1 = args[1];
                }

                // This does not cover all cases of block entrances. The JUMP@ and CALL@ instructions can still start
                // new blocks (by jumping). This needs to be covered in the PicoBlazeSimulator.J5Parser though because the values are
                // calculated at runtime.
                switch (instructionName) {
                    case JUMP:
                    case JUMPAT:
                    case CALL:
                    case CALLAT:
                    case RETURN:
                    case LOADANDRETURN:
                        if (instructions.length > lineNumber + 1) {
                            instructions[lineNumber + 1].isBlockStart = true;
                        }
                }

                if (instructionName == PBInstructionSet.CALL || instructionName == PBInstructionSet.JUMP) {
                    int arg = args[0] instanceof PBAbsoluteAddress ? 0 : 1;
                    int address = args[arg].getIntValue();

                    if (instructions.length > address) {
                        instructions[address].isBlockStart = true;
                    }
                }
            }

//            System.out.println(labelMap.keySet());
        }

        return instructions;
    }

    private PBLexer() {
        constantMap.put("NUL", 0x00);
        constantMap.put("BEL", 0x07);
        constantMap.put("BS",  0x08);
        constantMap.put("HT",  0x09);
        constantMap.put("LF",  0x0A);
        constantMap.put("VT",  0x0B);
        constantMap.put("CR",  0x0D);
        constantMap.put("ESC", 0x1B);
        constantMap.put("DEL", 0x7F);
        constantMap.put("DCS", 0x90);
        constantMap.put("ST",  0x9C);
    }
}

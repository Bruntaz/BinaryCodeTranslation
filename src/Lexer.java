import java.util.*;

public class Lexer {
    private static Lexer ourInstance = new Lexer();
    public static Lexer getInstance() {
        return ourInstance;
    }

    Registers registers = Registers.getInstance();
    HashMap<String, Integer> labelMap = new HashMap<>();
    HashMap<String, Integer> constantMap = new HashMap<>();

    HashSet<InstructionSet> noArgs = new HashSet<>(Collections.singletonList(InstructionSet.RETURN));
    HashSet<InstructionSet> oneArg = new HashSet<>(Arrays.asList(
            InstructionSet.SL0, InstructionSet.SL1, InstructionSet.SLX, InstructionSet.SLA, InstructionSet.RL,
            InstructionSet.SR0, InstructionSet.SR1, InstructionSet.SRX, InstructionSet.SRA, InstructionSet.RR,
            InstructionSet.REGBANK, InstructionSet.JUMP, InstructionSet.CALL, InstructionSet.RETURN)
    );
    HashSet<InstructionSet> twoArgs = new HashSet<>(Arrays.asList(
            InstructionSet.LOAD, InstructionSet.STAR, InstructionSet.AND, InstructionSet.OR, InstructionSet.XOR,
            InstructionSet.ADD, InstructionSet.ADDCY, InstructionSet.SUB, InstructionSet.SUBCY,
            InstructionSet.TEST, InstructionSet.TESTCY, InstructionSet.COMPARE, InstructionSet.COMPARECY,
            InstructionSet.STORE, InstructionSet.FETCH, InstructionSet.JUMP, InstructionSet.JUMPAT,
            InstructionSet.CALL, InstructionSet.RETURN, InstructionSet.LOADANDRETURN, InstructionSet.CONSTANT)
    );

    private class LabelAndColon {
        String label;
        int colonSection;

        public LabelAndColon(String label, int colonSection) {
            this.label = label;
            this.colonSection = colonSection;
        }
    }

    private class InstructionAndSection {
        InstructionSet instruction;
        int instructionSection;

        public InstructionAndSection(InstructionSet instruction, int instructionSection) {
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

        return new InstructionAndSection(InstructionSet.valueOf(upperCaseInstruction), instructionSection);
    }

    private boolean isValidNumberOfArguments(InstructionSet instructionName, int number) {
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
    private InstructionArgument convertStringToArgument(InstructionSet instructionName, boolean firstArgument, String toConvert) {
        if (toConvert == null) {
            return new NoArgument();
        }

        if (instructionName == InstructionSet.CONSTANT && firstArgument) {
            return new NamedArgument(toConvert);
        }

        try {
            return registers.getRegister(RegisterName.valueOf(toConvert.toUpperCase()));

        } catch (IllegalArgumentException ignored) {}

        if (firstArgument && (instructionName == InstructionSet.JUMP
                           || instructionName == InstructionSet.CALL
                           || instructionName == InstructionSet.RETURN)) {
            switch (toConvert) {
                case "C":
                    return new FlagArgument(FlagArgument.C);
                case "NC":
                    return new FlagArgument(FlagArgument.NC);
                case "Z":
                    return new FlagArgument(FlagArgument.Z);
                case "NZ":
                    return new FlagArgument(FlagArgument.NZ);
            }
        }

        if (instructionName == InstructionSet.JUMP
                || instructionName == InstructionSet.CALL
                || instructionName == InstructionSet.RETURN) {

            try {
                return new AbsoluteAddress(labelMap.get(toConvert));

            } catch (NullPointerException ignore) {
                try {
                    return new AbsoluteAddress(convertToInteger(toConvert) - 1);
                } catch (NumberFormatException ignored) {}
            }
        }

        try {
            return new Literal(convertToInteger(toConvert));
        } catch (NumberFormatException ignored) {}

        return new NoArgument();
    }

    private InstructionArgument[] getArguments(String[] sections, InstructionAndSection instruction) {
        int firstArgSection = instruction.instructionSection + 1;

        // No arguments
        if (sections.length == firstArgSection) {
            if (isValidNumberOfArguments(instruction.instruction, 0)) {
                return new InstructionArgument[]{
                        new NoArgument(),
                        new NoArgument(),
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

            System.out.println(firstArgument);
            System.out.println(secondArgument);

            if ((secondArgument == null && !isValidNumberOfArguments(instruction.instruction, 1)) ||
                (secondArgument != null && !isValidNumberOfArguments(instruction.instruction, 2))) {
                return null;

            } else {
                return new InstructionArgument[] {
                        convertStringToArgument(instruction.instruction, true, firstArgument),
                        convertStringToArgument(instruction.instruction, false, secondArgument),
                };
            }
        }
    }

    public Instruction[] lex(List<String> program) {
        Instruction[] instructions = new Instruction[program.size()];
        System.out.println(instructions.length);
        for (int lineNumber=0; lineNumber<program.size(); lineNumber++) {
            String line = program.get(lineNumber).split(";")[0]; // Remove comments
            String[] sections = line.trim().split("\\s+"); // Split on (and remove) whitespace

            if (sections[0].isEmpty()) {
                // Blank line
                instructions[lineNumber] = new Instruction(null, null, null, false);
                continue;
            }

            LabelAndColon label = getLabel(sections);
            if (label != null) {
                labelMap.put(label.label, lineNumber);
            }

            InstructionAndSection instructionName = getInstruction(sections, label);
            if (instructionName != null){
                System.out.println(instructionName.instruction);

                InstructionArgument[] args = getArguments(sections, instructionName);

                if (args == null) {
                    throw new IllegalArgumentException(String.format("Illegal number of arguments on line %d", lineNumber + 1));
                }

                System.out.println(args[0]);
                System.out.println(args[1]);

                if (instructionName.instruction == InstructionSet.CONSTANT) {
                    // Convert all constants to values in lexer so don't include constants in instructions array
                    constantMap.put(args[0].getStringValue(), args[1].getIntValue());
                    instructions[lineNumber] = new Instruction(null, null, null, label != null);
                } else {
                    instructions[lineNumber] = new Instruction(instructionName.instruction, args[0], args[1],
                            label != null);
                }
            } else {
                instructions[lineNumber] = new Instruction(null, null, null, label != null);
            }

            System.out.println(labelMap.keySet());
        }

        return instructions;
    }

    private Lexer() {
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

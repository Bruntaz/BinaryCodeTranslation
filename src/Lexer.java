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
        if (constantMap.containsKey(toConvert)) {
            return constantMap.get(toConvert);
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

            if (firstArgument.contains(",")) {
                String[] splitArg = firstArgument.split(",");
                firstArgument = splitArg[0];

                if (splitArg.length > 1) {
                    secondArgument = splitArg[1];
                } else {
                    secondArgument = sections[firstArgSection + 1];
                }

            } else {
                if (sections.length > firstArgSection + 1) {
                    if (sections[firstArgSection + 1].equals(",")) {
                        secondArgument = sections[firstArgSection + 2];
                    } else {
                        secondArgument = sections[firstArgSection + 1].split(",")[1];
                    }
                }
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
                    constantMap.put(args[0].getStringValue(), args[1].getIntValue());
                } else {
                    instructions[lineNumber] = new Instruction(instructionName.instruction, args[0], args[1]);
                }

            }

            System.out.println(labelMap.keySet());
        }

        return instructions;
    }
}

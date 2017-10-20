import java.util.HashMap;
import java.util.List;

public class Lexer {
    private static Lexer ourInstance = new Lexer();
    public static Lexer getInstance() {
        return ourInstance;
    }

    private String getLabel(String[] sections) {
        if (sections.length > 1 && sections[1].startsWith(":")) {
            return sections[0];
        }

        if (sections[0].contains(":")) {
            return sections[0].split(":")[0];
        } else {
            return null;
        }
    }

    private InstructionSet getInstruction(String[] sections, boolean containsLabel) {
        if (containsLabel && sections.length == 1) {
            // Blank line with label
            return null;
        }

        String upperCaseInstruction;

        if (containsLabel) {
            String sectionWithInstruction;

            if (sections[0].contains(":")) {
                if (sections[0].endsWith(":")) {
                    sectionWithInstruction = sections[1];
                } else {
                    sectionWithInstruction = sections[0];
                }
            } else {
                if (sections[1].equals(":")) {
                    sectionWithInstruction = sections[2];
                } else {
                    sectionWithInstruction = sections[1];
                }
            }
            String[] sectionSplit = sectionWithInstruction.split(":");
            upperCaseInstruction = sectionSplit[sectionSplit.length - 1].toUpperCase();

        } else {
            upperCaseInstruction = sections[0].toUpperCase();
        }

        upperCaseInstruction = upperCaseInstruction.replaceAll("@", "AT");
        upperCaseInstruction = upperCaseInstruction.replaceAll("&", "AND");

        return InstructionSet.valueOf(upperCaseInstruction);
    }

    public Instruction[] lex(List<String> program) {
        HashMap<String, Integer> labelMap = new HashMap<>();

        for (int lineNumber=0; lineNumber<program.size(); lineNumber++) {
            String line = program.get(lineNumber).split(";")[0]; // Remove comments
            String[] sections = line.trim().split("\\s+"); // Split on (and remove) whitespace

            if (sections[0].isEmpty()) {
                // Blank line
                continue;
            }

            String label = getLabel(sections);
            if (label != null) {
                labelMap.put(label, lineNumber);
            }

            InstructionSet instructionName = getInstruction(sections, label != null);
            System.out.println(instructionName);
            
            System.out.println(labelMap.keySet());
        }

        return null;
    }
}

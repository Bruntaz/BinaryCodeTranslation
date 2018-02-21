import Jorvik5.Groups.J5InstructionSet;

public class J5InstructionPair implements Comparable<J5InstructionPair> {
    public J5InstructionSet instruction1;
    public J5InstructionSet instruction2;

    public J5InstructionPair(J5InstructionSet ins1, J5InstructionSet ins2) {
        instruction1 = ins1;
        instruction2 = ins2;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof J5InstructionPair)) {
            return false;
        }

        J5InstructionPair i = (J5InstructionPair) obj;

        return instruction1.equals(i.instruction1) && instruction2.equals(i.instruction2);
    }

    @Override
    public int hashCode() {
        return instruction1.hashCode() ^ instruction2.hashCode();
    }

    @Override
    public String toString() {
        return "(" + repeat(" ",  9-instruction1.toString().length()) + instruction1 + ","
                   + repeat(" ", 10-instruction2.toString().length()) + instruction2 + ")";
    }

    public String repeat(String s, int count) {
        StringBuilder outString = new StringBuilder();
        for (int i = 0; i < count; i++) {
            outString.append(s);
        }
        return outString.toString();
    }

    @Override
    public int compareTo(J5InstructionPair o) {
        return 0;
    }
}

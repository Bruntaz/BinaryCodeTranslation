import Jorvik5.J5Instruction;

class ReAdd implements Comparable<ReAdd> {
    J5Instruction instruction;
    int originalLine;

    public ReAdd(J5Instruction ins, int line) {
        instruction = ins;
        originalLine = line;
    }

    @Override
    public int compareTo(ReAdd o) {
        return originalLine - o.originalLine;
    }
}
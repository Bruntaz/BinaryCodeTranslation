import Jorvik5.InstructionArguments.J5InstructionArgument;

class Pair implements Comparable<Pair> {
    int reuseLine;
    int useLine;
    int lineDistance;
    J5InstructionArgument registerLocation;

    public Pair(int useLine, int reuseLine, J5InstructionArgument rLocation) {
        this.useLine = useLine;
        this.reuseLine = reuseLine;
        lineDistance = reuseLine - useLine;
        registerLocation = rLocation;
    }

    public String toString() {
        return "Pair(reuseLine: " + reuseLine + ", useLine: " + useLine + ", registerLocation: " +
                registerLocation.getValue() + ")";
    }

    @Override
    public int compareTo(Pair o) {
        return lineDistance - o.lineDistance;
    }
}
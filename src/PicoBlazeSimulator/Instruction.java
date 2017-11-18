package PicoBlazeSimulator;

import PicoBlazeSimulator.Groups.InstructionSet;
import PicoBlazeSimulator.InstructionArguments.InstructionArgument;
import PicoBlazeSimulator.InstructionArguments.Register;

public class Instruction {
    public InstructionSet instruction;
    public InstructionArgument arg0;
    public InstructionArgument arg1;
    public boolean isBlockStart;

    public Instruction() {
        this(InstructionSet.NOP, null, null, false);
    }

    @Override
    public String toString() {
        if (instruction == null) {
            return null;
        }

        StringBuilder toReturn = new StringBuilder(instruction.toString());

        if (arg0 != null) {
            if (arg0.hasIntValue()) {
                toReturn.append(String.format(" %s, ",
                        arg0 instanceof Register ?
                                ((Register)arg0).getRegisterName() :
                                Integer.toHexString(arg0.getIntValue())
                ));
            } else {
                toReturn.append(String.format(" %s, ", arg0.getStringValue()));
            }
        } else {
            toReturn.append(" null, ");
        }

        if (arg1 != null) {
            if (arg1.hasIntValue()) {
                toReturn.append(arg1 instanceof Register ?
                                ((Register)arg1).getRegisterName() :
                                Integer.toHexString(arg1.getIntValue())
                );
            } else {
                toReturn.append(arg1.getStringValue());
            }
        } else {
            toReturn.append("null");
        }

        return toReturn.toString();
    }

    public Instruction(InstructionSet instruction, InstructionArgument arg0, InstructionArgument arg1, boolean isBlockStart) {
        this.instruction = instruction;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.isBlockStart = isBlockStart;
    }
}

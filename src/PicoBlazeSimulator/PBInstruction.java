package PicoBlazeSimulator;

import PicoBlazeSimulator.Groups.PBInstructionSet;
import PicoBlazeSimulator.InstructionArguments.PBInstructionArgument;
import PicoBlazeSimulator.InstructionArguments.PBRegister;

public class PBInstruction {
    public PBInstructionSet instruction;
    public PBInstructionArgument arg0;
    public PBInstructionArgument arg1;
    public boolean isBlockStart;

    public PBInstruction() {
        this(PBInstructionSet.NOP, null, null, false);
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
                        arg0 instanceof PBRegister ?
                                ((PBRegister)arg0).getRegisterName() :
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
                toReturn.append(arg1 instanceof PBRegister ?
                                ((PBRegister)arg1).getRegisterName() :
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

    public PBInstruction(PBInstructionSet instruction, PBInstructionArgument arg0, PBInstructionArgument arg1, boolean isBlockStart) {
        this.instruction = instruction;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.isBlockStart = isBlockStart;
    }
}

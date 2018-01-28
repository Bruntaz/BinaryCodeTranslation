import Jorvik5.*;
import Jorvik5.Groups.J5InstructionSet;
import PicoBlazeSimulator.*;
import PicoBlazeSimulator.Groups.*;
import PicoBlazeSimulator.InstructionArguments.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Translator {
    private PBProgramCounter pbPC = PBProgramCounter.getInstance();
    private PBLexer pbLexer = PBLexer.getInstance();
    private PBParser pbParser = PBParser.getInstance();

    private J5ProgramCounter j5PC = J5ProgramCounter.getInstance();
    private J5Lexer j5Lexer = J5Lexer.getInstance();
    private J5Parser j5Parser = J5Parser.getInstance();

    private HashMap<PBRegisterName, Integer> registerMemoryLocation = new HashMap<PBRegisterName, Integer>() {{
        put(PBRegisterName.S0, 0);
        put(PBRegisterName.S1, 1);
        put(PBRegisterName.S2, 2);
        put(PBRegisterName.S3, 3);
        put(PBRegisterName.S4, 4);
        put(PBRegisterName.S5, 5);
        put(PBRegisterName.S6, 6);
        put(PBRegisterName.S7, 7);
        put(PBRegisterName.S8, 8);
        put(PBRegisterName.S9, 9);
        put(PBRegisterName.SA, 10);
        put(PBRegisterName.SB, 11);
        put(PBRegisterName.SC, 12);
        put(PBRegisterName.SD, 13);
        put(PBRegisterName.SE, 14);
        put(PBRegisterName.SF, 15);
    }};

    private int translateRegisterIntoMemory(PBInstructionArgument register) {
        PBRegisterName registerName = ((PBRegister) register).getRegisterName();
        return registerMemoryLocation.get(registerName);
    }

    /*
    NOTE: This currently only supports register arguments for the logical operators. It will be necessary to add SSET
     <value> at some point instead of FETCH <register>
     */
    public J5Instruction[] translate(PBInstruction pbInstruction) {
        if (pbInstruction == null || pbInstruction.instruction == PBInstructionSet.NOP) {
            return new J5Instruction[] {new J5Instruction(J5InstructionSet.NOP, null)};
        }

        PBInstructionSet instruction = pbInstruction.instruction;
        PBInstructionArgument arg0 = pbInstruction.arg0;
        PBInstructionArgument arg1 = pbInstruction.arg1;

        switch (instruction) {
            // PBRegister loading
            case LOAD:
                return new J5Instruction[] {
                        j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };

            // Logical
            case AND:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                        j5Lexer.lex("AND"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };
            case OR:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                        j5Lexer.lex("OR"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };
            case XOR:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                        j5Lexer.lex("XOR"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };

            // Arithmetic
            case ADD:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                        j5Lexer.lex("ADD"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };
            case SUB:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                        j5Lexer.lex("SUB"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };


            // Jump
            case JUMP:
                if (arg0 instanceof PBAbsoluteAddress) {
                    return new J5Instruction[] {
                            j5Lexer.lex("LBRANCH " + arg0.getIntValue())
                    };
                } else {
                    PBFlagArgument a0 = (PBFlagArgument) arg0;
                    if (a0.getStringValue().equals(PBFlagArgument.Z)) {
                        return new J5Instruction[] {
                                j5Lexer.lex("BRZERO " + (arg1.getIntValue() + 1 - pbPC.get())), // This will
                                // crash if the jump instruction isn't forward.
                        };
                    } else if (a0.getStringValue().equals(PBFlagArgument.NZ)) { // TODO: Fix this. If a LOAD is before
                        // TODO: here, it may fail because the NOTs will affect the Z flag where the LOADs shouldn't
                        return new J5Instruction[] {
                                j5Lexer.lex("NOT"), // This tests zero for us
                                j5Lexer.lex("BRZERO " + (arg1.getIntValue() + 1 - pbPC.get())),// This will
                                // crash if the jump instruction isn't forward.
                                j5Lexer.lex("NOT"),
                        };
                    }
                }
        }

        throw new Error("Translation for this command (" + instruction + ") is not supported yet.");
    }

    private List<String> readFile(Path filePath) {
        List<String> file = null;
        try {
            file = Files.readAllLines(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public void runPicoBlazeFileNatively(Path filePath) {
        List<String> file = readFile(filePath);

        PBInstruction[] instructions = pbLexer.lex(file);

        pbParser.parse(instructions);
        System.out.println(PBScratchPad.getInstance());
    }

    public void runJ5FileNatively(Path filePath) {
        List<String> file = readFile(filePath);

        J5Instruction[] instructions = j5Lexer.lex(file);

        j5Parser.parse(instructions);
        System.out.println(J5ScratchPad.getInstance());
    }

    public void runPicoBlazeFileOnJ5(Path filePath) {
        List<String> file = readFile(filePath);

        PBInstruction[] picoBlazeInstructions = pbLexer.lex(file);

        J5Instruction[][] j5Instructions = new J5Instruction[picoBlazeInstructions.length][];

        pbParser.RESET();
        int pbPC = this.pbPC.get();
        while (pbPC < picoBlazeInstructions.length) {
            if (j5Instructions[pbPC] == null) { // Currently this just converts everything because the program
                                                      // counter isn't being changed on jumps in the stack machine.
                picoBlazeInstructions[pbPC].isBlockStart = true;
                int nextBlockStart = pbParser.getNextBlockStart(picoBlazeInstructions, pbPC);

                // Iterate through current PicoBlaze block and translate it
                for (int instructionNumber = pbPC; instructionNumber < nextBlockStart; instructionNumber++) {
                    j5Instructions[instructionNumber] = translate(picoBlazeInstructions[instructionNumber]);
                }

                System.out.println("-------------Currently translated---------------");
                for (int i=0; i<j5Instructions.length; i++) {
                    System.out.println(Arrays.toString(j5Instructions[i]) + ", " + picoBlazeInstructions[i].instruction);
                }
                System.out.println("-------------Currently translated---------------");
            }

            j5PC.set(pbPC);
            System.out.println("PicoBlaze J5Instruction: " + picoBlazeInstructions[pbPC]);
            for (J5Instruction instruction : j5Instructions[pbPC]) {
                // Loop here because parse(J5Instruction[]) will break on jumps
                // For example if you have a block whick loops to itself
                j5Parser.parse(instruction);
            }

            // Move the PBPC if the J5 machine has jumped
            if (j5PC.hasJustJumped()) {
                this.pbPC.set(j5PC.get());
            } else {
                this.pbPC.increment();
            }

            pbPC = this.pbPC.get();
        }

//        for (int i=0; i<j5Instructions.length; i++) {
//            System.out.println(Arrays.toString(j5Instructions[i]) + ", " + picoBlazeInstructions[i].instruction);
//        }
//        System.out.println(j5Instructions.length);
        System.out.println(String.format("\nFinished in %d clock cycles", j5Parser.getClockCycles()));
        System.out.println(J5ScratchPad.getInstance());
    }

    public static void main(String[] args) {
        Path filePath = FileSystems.getDefault().getPath("src", "TestCode", args[0]);

        Translator translator = new Translator();
        if (args[1].equals("PB")) {
            translator.runPicoBlazeFileNatively(filePath);

        } else if (args[1].equals("J5")) {
            translator.runJ5FileNatively(filePath);

        } else {
            translator.runPicoBlazeFileOnJ5(filePath);
        }
    }
}

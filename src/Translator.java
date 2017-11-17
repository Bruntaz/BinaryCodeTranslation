import Jorvik5.Instruction;
import PicoBlazeSimulator.Groups.RegisterName;
import PicoBlazeSimulator.InstructionArguments.AbsoluteAddress;
import PicoBlazeSimulator.InstructionArguments.FlagArgument;
import PicoBlazeSimulator.InstructionArguments.InstructionArgument;
import PicoBlazeSimulator.InstructionArguments.Register;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Translator {
    private PicoBlazeSimulator.ProgramCounter picoBlazePC = PicoBlazeSimulator.ProgramCounter.getInstance();
    private PicoBlazeSimulator.Lexer picoBlazeLexer = PicoBlazeSimulator.Lexer.getInstance();
    private PicoBlazeSimulator.Parser picoBlazeParser = PicoBlazeSimulator.Parser.getInstance();

    private Jorvik5.ProgramCounter j5PC = Jorvik5.ProgramCounter.getInstance();
    private Jorvik5.Lexer j5Lexer = Jorvik5.Lexer.getInstance();
    private Jorvik5.Parser j5Parser = Jorvik5.Parser.getInstance();

    private HashMap<RegisterName, Integer> registerMemoryLocation = new HashMap<RegisterName, Integer>() {{
        put(RegisterName.S0, 0);
        put(RegisterName.S1, 1);
        put(RegisterName.S2, 2);
        put(RegisterName.S3, 3);
        put(RegisterName.S4, 4);
        put(RegisterName.S5, 5);
        put(RegisterName.S6, 6);
        put(RegisterName.S7, 7);
        put(RegisterName.S8, 8);
        put(RegisterName.S9, 9);
        put(RegisterName.SA, 10);
        put(RegisterName.SB, 11);
        put(RegisterName.SC, 12);
        put(RegisterName.SD, 13);
        put(RegisterName.SE, 14);
        put(RegisterName.SF, 15);
    }};

    private int translateRegisterIntoMemory(InstructionArgument register) {
        RegisterName registerName = ((Register) register).getRegisterName();
        return registerMemoryLocation.get(registerName);
    }

    /*
    NOTE: This currently only supports register arguments for the logical operators. It will be necessary to add SSET
     <value> at some point instead of FETCH <register>
     */
    public Jorvik5.Instruction[] translate(PicoBlazeSimulator.Instruction picoBlazeInstruction) {
        PicoBlazeSimulator.Groups.InstructionSet instruction = picoBlazeInstruction.instruction;
        PicoBlazeSimulator.InstructionArguments.InstructionArgument arg0 = picoBlazeInstruction.arg0;
        PicoBlazeSimulator.InstructionArguments.InstructionArgument arg1 = picoBlazeInstruction.arg1;

        switch (instruction) {
            // Register loading
            case LOAD:
                return new Jorvik5.Instruction[] {
                        j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };

            // Logical
            case AND:
                return new Jorvik5.Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                        j5Lexer.lex("AND"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };
            case OR:
                return new Jorvik5.Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                        j5Lexer.lex("OR"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };
            case XOR:
                return new Jorvik5.Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                        j5Lexer.lex("XOR"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                };

            // Jump
            case JUMP:
                if (arg0 instanceof AbsoluteAddress) {
                    return new Jorvik5.Instruction[] {
                            j5Lexer.lex("LBRANCH " + arg0.getIntValue())
                    };
                } else {
                    FlagArgument a0 = (FlagArgument) arg0;
                    if (a0.getStringValue().equals(FlagArgument.Z)) {
                        return new Jorvik5.Instruction[] {
                                j5Lexer.lex("BRZERO " + (arg1.getIntValue() + 1 - picoBlazePC.get())), // This will
                                // crash if the jump instruction isn't forward.
                        };
                    } else if (a0.getStringValue().equals(FlagArgument.NZ)) { // TODO: Fix this. If a LOAD is before
                        // TODO: here, it may fail because the NOTs will affect the Z flag where the LOADs shouldn't
                        return new Jorvik5.Instruction[] {
                                j5Lexer.lex("NOT"), // This tests zero for us
                                j5Lexer.lex("BRZERO " + (arg1.getIntValue() + 1 - picoBlazePC.get())),// This will
                                // crash if the jump instruction isn't forward.
                                j5Lexer.lex("NOT"),
                        };
                    }
                }
        }
        return new Jorvik5.Instruction[] {};
    }

    private List<String> readFile(String filename) {
        Path filePath = FileSystems.getDefault().getPath(filename);

        List<String> file = null;
        try {
            file = Files.readAllLines(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public void runPicoBlazeFileNatively(String filename) {
        List<String> file = readFile(filename);

        PicoBlazeSimulator.Instruction[] instructions = picoBlazeLexer.lex(file);

        picoBlazeParser.parse(instructions);
        System.out.println(PicoBlazeSimulator.ScratchPad.getInstance());
    }

    public void runJ5FileNatively(String filename) {
        List<String> file = readFile(filename);

        Jorvik5.Instruction[] instructions = j5Lexer.lex(file);

        j5Parser.parse(instructions);
        System.out.println(Jorvik5.ScratchPad.getInstance());
    }

    public void runPicoBlazeFileOnJ5(String filename) {
        List<String> file = readFile(filename);

        PicoBlazeSimulator.Instruction[] picoBlazeInstructions = picoBlazeLexer.lex(file);

        Jorvik5.Instruction[][] j5Instructions = new Jorvik5.Instruction[picoBlazeInstructions.length][];

        int pbPC = this.picoBlazePC.get();
        while (pbPC < picoBlazeInstructions.length) {
            if (j5Instructions[pbPC] == null) { // Currently this just converts everything because the program
                                                      // counter isn't being changed on jumps in the stack machine.
                picoBlazeInstructions[pbPC].isBlockStart = true;
                int nextBlockStart = picoBlazeParser.getNextBlockStart(picoBlazeInstructions, pbPC);

                // Iterate through current PicoBlaze block and translate it
                for (int instructionNumber = pbPC; instructionNumber < nextBlockStart; instructionNumber++) {
                    if (picoBlazeInstructions[instructionNumber].instruction != null) {
                        j5Instructions[instructionNumber] = translate(picoBlazeInstructions[instructionNumber]);
                    } else {
                        j5Instructions[instructionNumber] = new Instruction[] {};
                    }
                }
            }

            j5PC.reset();
            System.out.println(picoBlazeInstructions[pbPC]);
            for (Instruction instruction : j5Instructions[pbPC]) {
                // Loop here because parse(Instruction[]) will break on jumps
                // For example if you have a block whick loops to itself
                j5Parser.parse(instruction);
            }

            // Move the PBPC if the J5 machine has jumped
            if (j5PC.hasJustJumped()) {
                picoBlazePC.set(j5PC.get());
            } else {
                this.picoBlazePC.increment();
            }

            pbPC = this.picoBlazePC.get();
        }

        for (int i=0; i<j5Instructions.length; i++) {
            System.out.println(Arrays.toString(j5Instructions[i]) + ", " + picoBlazeInstructions[i].instruction);
        }
//        System.out.println(j5Instructions.length);
        System.out.println(Jorvik5.ScratchPad.getInstance());
    }

    public static void main(String[] args) {
        Translator translator = new Translator();
        if (args[1].equals("PB")) {
            translator.runPicoBlazeFileNatively(args[0]);

        } else if (args[1].equals("J5")) {
            translator.runJ5FileNatively(args[0]);

        } else {
            translator.runPicoBlazeFileOnJ5(args[0]);
        }
    }
}

import Jorvik5.Groups.InstructionSet;
import Jorvik5.Instruction;
import Jorvik5.InstructionArguments.ShortLiteral;
import PicoBlazeSimulator.Groups.RegisterName;
import PicoBlazeSimulator.InstructionArguments.Register;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Translator {
    private PicoBlazeSimulator.ProgramCounter picoBlazePC = PicoBlazeSimulator.ProgramCounter.getInstance();
    private PicoBlazeSimulator.Lexer picoBlazeLexer = PicoBlazeSimulator.Lexer.getInstance();
    private PicoBlazeSimulator.Parser picoBlazeParser = PicoBlazeSimulator.Parser.getInstance();

    private Jorvik5.ProgramCounter j5PC = Jorvik5.ProgramCounter.getInstance();
    private Jorvik5.Lexer j5Lexer = Jorvik5.Lexer.getInstance();
    private Jorvik5.Parser j5Parser = Jorvik5.Parser.getInstance();

    private int translateRegisterIntoMemory(RegisterName registerName) {
        switch (registerName) {
            case S0:
                return 0;
            case S1:
                return 1;
            case S2:
                return 2;
            case S3:
                return 3;
            case S4:
                return 4;
            case S5:
                return 5;
            case S6:
                return 6;
            case S7:
                return 7;
            case S8:
                return 8;
            case S9:
                return 9;
            case SA:
                return 10;
            case SB:
                return 11;
            case SC:
                return 12;
            case SD:
                return 13;
            case SE:
                return 14;
            default:
                return 15;
        }
    }

    public Jorvik5.Instruction[] translate(PicoBlazeSimulator.Instruction picoBlazeInstruction) {
        switch (picoBlazeInstruction.instruction) {
            case LOAD:
                return new Jorvik5.Instruction[] {
                        new Instruction(InstructionSet.SSET, new ShortLiteral(picoBlazeInstruction.arg1.getIntValue())),
                        new Instruction(Jorvik5.Groups.InstructionSet.STORE, new ShortLiteral(
                                translateRegisterIntoMemory(((Register)picoBlazeInstruction.arg0).getRegisterName()))),
                };

        }
        return new Jorvik5.Instruction[] {new Instruction()};
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
            for (Instruction instruction : j5Instructions[pbPC]) {
                // Loop here because parse(Instruction[]) will break on jumps
                // For example if you have a block whick loops to itself
                j5Parser.parse(instruction);
            }

            // Move the PBPC if the J5 machine has jumped
            if (j5PC.hasJustJumped()) {
                picoBlazePC.set(j5PC.get());
            }

            this.picoBlazePC.increment();
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

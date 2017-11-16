import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Translator {
    private PicoBlazeSimulator.Lexer picoBlazeLexer = PicoBlazeSimulator.Lexer.getInstance();
    private PicoBlazeSimulator.Parser picoBlazeParser = PicoBlazeSimulator.Parser.getInstance();

    private Jorvik5.ProgramCounter j5PC = Jorvik5.ProgramCounter.getInstance();
    private Jorvik5.Lexer j5Lexer = Jorvik5.Lexer.getInstance();
    private Jorvik5.Parser j5Parser = Jorvik5.Parser.getInstance();

    public Jorvik5.Instruction[] translate(PicoBlazeSimulator.Instruction picoBlazeInstruction) {
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
    }

    public void runPicoBlazeFileOnJ5(String filename) {
        List<String> file = readFile(filename);

        PicoBlazeSimulator.Instruction[] picoBlazeInstructions = picoBlazeLexer.lex(file);

        Jorvik5.Instruction[][] j5Instructions = new Jorvik5.Instruction[picoBlazeInstructions.length][];

        int j5PC = this.j5PC.get();
        while (j5PC < j5Instructions.length) {
            if (j5Instructions[j5PC] == null) { // Currently this just converts everything because the program
                                                      // counter isn't being changed on jumps in the stack machine.
                // Translate next block
                picoBlazeInstructions[j5PC].isBlockStart = true;
                int nextBlockStart = picoBlazeParser.getNextBlockStart(picoBlazeInstructions, j5PC);

                for (int instructionNumber = j5PC; instructionNumber < nextBlockStart; instructionNumber++) {
                    j5Instructions[instructionNumber] = translate(picoBlazeInstructions[instructionNumber]);
                }
            }

            j5Parser.parse(j5Instructions[j5PC]);

            this.j5PC.increment();
            j5PC = this.j5PC.get();
        }

        for (int i=0; i<j5Instructions.length; i++) {
            System.out.println(Arrays.toString(j5Instructions[i]) + ", " + picoBlazeInstructions[i].instruction);
        }
        System.out.println(j5Instructions.length);
    }

    public static void main(String[] args) {
        Translator translator = new Translator();
        if (args[1].equals("PB")) {
            translator.runPicoBlazeFileNatively(args[0]);

        } else if (args[1].equals("J5")) {
            translator.runJ5FileNatively(args[0]);

        } else {
            System.out.println(args[1]);
            translator.runPicoBlazeFileOnJ5(args[0]);
        }
    }
}
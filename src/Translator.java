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
        translator.runPicoBlazeFileNatively(args[0]);
//        translator.runPicoBlazeFileOnJ5(args[0]);

        System.out.println(PicoBlazeSimulator.ScratchPad.getInstance());
    }
}

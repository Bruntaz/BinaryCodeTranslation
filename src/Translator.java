import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Translator {
    private PicoBlazeSimulator.Lexer picoBlazeLexer = PicoBlazeSimulator.Lexer.getInstance();
    private PicoBlazeSimulator.Parser picoBlazeParser = PicoBlazeSimulator.Parser.getInstance();

    private StackSimulator.Lexer stackLexer = StackSimulator.Lexer.getInstance();
    private StackSimulator.Parser stackParser = StackSimulator.Parser.getInstance();

    public StackSimulator.Instruction[] translate(PicoBlazeSimulator.Instruction picoBlazeInstruction) {
        return new StackSimulator.Instruction[] {};
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

    public void runPicoBlazeFileOnStackMachine(String filename) {
        List<String> file = readFile(filename);

        PicoBlazeSimulator.Instruction[] picoBlazeInstructions = picoBlazeLexer.lex(file);

        StackSimulator.Instruction[][] stackInstructions = new StackSimulator.Instruction[picoBlazeInstructions
                .length][];

        int stackPC = stackParser.programCounter.peek();
        while (stackPC < stackInstructions.length) {
            if (stackInstructions[stackPC] == null) { // Currently this just converts everything because the program
                                                      // counter isn't being changed on jumps in the stack machine.
                // Translate next block
                picoBlazeInstructions[stackPC].isBlockStart = true;
                int nextBlockStart = picoBlazeParser.getNextBlockStart(picoBlazeInstructions, stackPC);

                for (int instructionNumber = stackPC; instructionNumber < nextBlockStart; instructionNumber++) {
                    stackInstructions[instructionNumber] = translate(picoBlazeInstructions[instructionNumber]);
                }
            }

            stackParser.parse(stackInstructions[stackPC]);

            stackParser.incrementProgramCounter();
            stackPC = stackParser.programCounter.peek();
        }

        for (int i=0; i<stackInstructions.length; i++) {
            System.out.println(Arrays.toString(stackInstructions[i]) + ", " + picoBlazeInstructions[i].instruction);
        }
        System.out.println(stackInstructions.length);
    }

    public static void main(String[] args) {
        Translator translator = new Translator();
        translator.runPicoBlazeFileNatively(args[0]);
//        translator.runPicoBlazeFileOnStackMachine(args[0]);

        System.out.println(PicoBlazeSimulator.ScratchPad.getInstance());
    }
}

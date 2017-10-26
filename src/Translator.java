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

        /*
        This comment may be old now, read it over later and see how much of it is still relevant.

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        This would presumably translate the whole file. You'd actually want to have a run loop in here which would
        use the PicoBlaze block detection to translate little chunks and then send them repeatedly to the stack
        machine.

        I think it might be a good idea to remove the while loop from the parsers and just have them parse individual
        instructions. This would allow you to within here (where you have access to both machines) do the translation
        and ensure that if something hasn't been translated yet then it can be before it is passed through.
        Alternatively, you might want to pass little arrays through. This could be a good idea if the translated
        stack code is longer, so you might translate to an array of arrays of instructions instead of an array of
        instructions.

        I'd imagine that it'll look something along the lines of this (this is more pseudocode than Java):

        PicoBlazeInstructions[] picoBlazeInstructions = picoBlazeLexer.lex(file);
        StackInstructions[] translated = new StackInstructions[];

        int startOfBlock = 0;
        int pointer = 0;
        while pointer < translated.length {
            if translated[pointer] == null {
                int blockEnd = picoBlazeParser.getNextBlockEnd(picoBlazeInstructions, startOfBlock);
                translate(picoBlazeInstructions, startOfBlock, endOfBlock);
            }
            stackParser.parse(translated[pointer])
            pointer += 1; // This obviously doesn't take into account jumps etc. Figure that out at implementation time
        }
         */
        StackSimulator.Instruction[][] stackInstructions = new StackSimulator.Instruction[picoBlazeInstructions
                .length][];

        int stackPC = stackParser.programCounter.peek();
        while (stackPC < stackInstructions.length) {
            if (stackInstructions[stackPC] == null) { // Currently this just converts everything because the program
                                                      // counter isn't being changed on jumps in the stack machine.
                // Translate next block
                int nextBlockEnd = picoBlazeParser.getNextBlockEnd(picoBlazeInstructions, stackPC);

                for (int instructionNumber = stackPC; instructionNumber <= nextBlockEnd; instructionNumber++) {
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
//        translator.runPicoBlazeFileNatively(args[0]);
        translator.runPicoBlazeFileOnStackMachine(args[0]);

//        System.out.println(PicoBlazeSimulator.ScratchPad.getInstance());
    }
}

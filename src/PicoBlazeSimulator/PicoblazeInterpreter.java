package PicoBlazeSimulator;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by jamesbrunton on 06/10/2017.
 */
public class PicoblazeInterpreter {
    public void runFile(String filename) {
        Path filePath = FileSystems.getDefault().getPath(filename);

        List<String> file = null;
        try {
            file = Files.readAllLines(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Instruction[] instructions = Lexer.getInstance().lex(file);

        Parser parser = Parser.getInstance();
        parser.parse(instructions);
    }

    public static void main(String[] args) {
        PicoblazeInterpreter picoblazeInterpreter = new PicoblazeInterpreter();
        picoblazeInterpreter.runFile(args[0]);

        System.out.println(ScratchPad.getInstance());
    }
}

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by jamesbrunton on 06/10/2017.
 */
public class PicoblazeInterpreter {


    public static void main(String[] args) {
        ScratchPad scratchPad = new ScratchPad();

        Path filePath = FileSystems.getDefault().getPath("Test Instructions.psm");

        List<String> file = null;
        try {
            file = Files.readAllLines(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Instruction[] instructions = Lexer.lex(file);

        Parser parser = new Parser(scratchPad);
        parser.parse(instructions);
        System.out.println(scratchPad);
    }
}

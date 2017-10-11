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
        Registers registers = new Registers();

//        System.out.println(Integer.toBinaryString(registers.getRegister(RegisterName.s0)));
//        registers.setRegister(RegisterName.s0,250);
//
//        for (int i=251; i < 260; i++) {
////            registers.setRegister("s0", i);
//            registers.addToRegister(RegisterName.s0, 1);
////            registers.subtractFromRegister("s0", 1);
//            System.out.format("%d: %s, C=%b, Z=%b\n", registers.getRegister(RegisterName.s0), Integer.toBinaryString(registers.getRegister(RegisterName.s0)), registers.C, registers.Z);
//        }

//        String program = "";
//
//        Parser parser = new Parser();
//        parser.parse(program);

//        Instruction i1 = new Instruction(InstructionSet.LOAD, registers.getRegister(RegisterName.s0), new Constant(0x7B));
//        Instruction i2 = new Instruction(InstructionSet.LOAD, registers.getRegister(RegisterName.s1), new Constant(0xA2));
//        Instruction i4 = new Instruction(InstructionSet.COMPARE, registers.getRegister(RegisterName.s0), new Constant(0x7B));
//        Instruction i5 = new Instruction(InstructionSet.COMPARECY, registers.getRegister(RegisterName.s1), new Constant(0xB9));
//        Parser parser = new Parser(registers);
//        parser.parse(new Instruction[] {i1, i2, i4, i5});

        Path filePath = FileSystems.getDefault().getPath("Test Instructions.psm");

        List<String> file = null;
        try {
            file = Files.readAllLines(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Instruction[] instructions = Lexer.lex(registers, file);

        Parser parser = new Parser(registers);
        parser.parse(instructions);
    }
}

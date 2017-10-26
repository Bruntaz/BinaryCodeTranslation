import PicoBlazeSimulator.Groups.RegisterName;
import PicoBlazeSimulator.Parser;
import PicoBlazeSimulator.Registers;
import PicoBlazeSimulator.*;

public class PicoblazeInterpreterTests {
    static Translator picoblazeInterpreter = new Translator();
    static Registers registers = Registers.getInstance();
    static ScratchPad scratchPad = ScratchPad.getInstance();
    static Parser parser = Parser.getInstance();

    private static void resetAll() {
        PicoblazeInterpreterTests.picoblazeInterpreter = new Translator();
        PicoblazeInterpreterTests.registers.resetRegisters();
        PicoblazeInterpreterTests.scratchPad.reset();
        PicoblazeInterpreterTests.parser.RESET();
    }

    private static boolean testS0(int shouldEqual) {
        Registers registers = Registers.getInstance();
        return registers.getRegister(RegisterName.S0).getIntValue() == shouldEqual;
    }

    private static boolean invertRegister() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Invert Register.psm");
        return testS0(0b01010101);
    }

    private static boolean toggleBit() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Toggle Bit.psm");
        return testS0(0b00000001);

    }

    private static boolean clearRegister() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Clear Register.psm");
        return testS0(0b00000000);
    }

    private static boolean setBit() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Set Bit.psm");
        return testS0(0b00000001);
    }

    private static boolean clearBit() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Clear Bit.psm");
        return testS0(0b11111110);
    }

    public static void main(String args[]) throws Exception {
        boolean[] results = new boolean[] {
                invertRegister(),
                toggleBit(),
                clearRegister(),
                setBit(),
                clearBit(),
        };

        for (int i=0; i<results.length; i++) {
            System.out.println(String.format("Test %d: %b", i, results[i]));
        }
    }
}

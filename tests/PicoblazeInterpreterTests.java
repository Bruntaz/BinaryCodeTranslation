import PicoBlazeSimulator.Groups.PBRegisterName;
import PicoBlazeSimulator.PBParser;
import PicoBlazeSimulator.PBRegisters;
import PicoBlazeSimulator.*;

public class PicoblazeInterpreterTests {
    static Translator picoblazeInterpreter = new Translator();
    static PBRegisters registers = PBRegisters.getInstance();
    static PBScratchPad scratchPad = PBScratchPad.getInstance();
    static PBParser parser = PBParser.getInstance();

    private static void resetAll() {
        PicoblazeInterpreterTests.picoblazeInterpreter = new Translator();
        PicoblazeInterpreterTests.registers.resetRegisters();
        PicoblazeInterpreterTests.scratchPad.reset();
        PicoblazeInterpreterTests.parser.RESET();
    }

    private static boolean testRegister(PBRegisterName registerName, int shouldEqual) {
        PBRegisters registers = PBRegisters.getInstance();
        return registers.getRegister(registerName).getIntValue() == shouldEqual;
    }

    private static boolean invertRegister() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Invert PBRegister.psm");
        return testRegister(PBRegisterName.S0, 0b01010101);
    }

    private static boolean toggleBit() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Toggle Bit.psm");
        return testRegister(PBRegisterName.S0, 0b00000001);

    }

    private static boolean clearRegister() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Clear PBRegister.psm");
        return testRegister(PBRegisterName.S0, 0b00000000);
    }

    private static boolean setBit() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Set Bit.psm");
        return testRegister(PBRegisterName.S0, 0b00000001);
    }

    private static boolean clearBit() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Clear Bit.psm");
        return testRegister(PBRegisterName.S0, 0b11111110);
    }

    private static boolean addCarry() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Add Carry.psm");
        return     testRegister(PBRegisterName.S0, 0x95)
                && testRegister(PBRegisterName.S1, 0x00)
                && testRegister(PBRegisterName.SA, 0x00)
                && testRegister(PBRegisterName.SB, 0x00);
    }

    private static boolean subCarry() {
        resetAll();
        picoblazeInterpreter.runPicoBlazeFileNatively("tests/Sub Carry.psm");
        return     testRegister(PBRegisterName.S0, 0xC2)
                && testRegister(PBRegisterName.S1, 0x00)
                && testRegister(PBRegisterName.SA, 0x00)
                && testRegister(PBRegisterName.SB, 0x00);
    }

    public static void main(String args[]) throws Exception {
        boolean[] results = new boolean[] {
                invertRegister(),
                toggleBit(),
                clearRegister(),
                setBit(),
                clearBit(),
                addCarry(),
                subCarry(),
        };

        for (int i=0; i<results.length; i++) {
            System.out.println(String.format("Test %d: %b", i, results[i]));
        }
    }
}

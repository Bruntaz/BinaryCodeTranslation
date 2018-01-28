import PicoBlazeSimulator.Groups.PBRegisterName;
import PicoBlazeSimulator.PBParser;
import PicoBlazeSimulator.PBRegisters;
import PicoBlazeSimulator.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PicoblazeInterpreterTests {
    static Translator picoblazeInterpreter = new Translator();
    static PBRegisters registers = PBRegisters.getInstance();
    static PBScratchPad scratchPad = PBScratchPad.getInstance();
    static PBParser parser = PBParser.getInstance();
    static Path currentPath = Paths.get(System.getProperty("user.dir"), "tests");

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
        Path filePath = Paths.get(currentPath.toString(), "Invert Register.psm");

        picoblazeInterpreter.runPicoBlazeFileNatively(filePath);
        return testRegister(PBRegisterName.S0, 0b01010101);
    }

    private static boolean toggleBit() {
        resetAll();
        Path filePath = Paths.get(currentPath.toString(), "Toggle Bit.psm");

        picoblazeInterpreter.runPicoBlazeFileNatively(filePath);
        return testRegister(PBRegisterName.S0, 0b00000001);

    }

    private static boolean clearRegister() {
        resetAll();
        Path filePath = Paths.get(currentPath.toString(), "Clear Register.psm");

        picoblazeInterpreter.runPicoBlazeFileNatively(filePath);
        return testRegister(PBRegisterName.S0, 0b00000000);
    }

    private static boolean setBit() {
        resetAll();
        Path filePath = Paths.get(currentPath.toString(), "Set Bit.psm");

        picoblazeInterpreter.runPicoBlazeFileNatively(filePath);
        return testRegister(PBRegisterName.S0, 0b00000001);
    }

    private static boolean clearBit() {
        resetAll();
        Path filePath = Paths.get(currentPath.toString(), "Clear Bit.psm");

        picoblazeInterpreter.runPicoBlazeFileNatively(filePath);
        return testRegister(PBRegisterName.S0, 0b11111110);
    }

    private static boolean addCarry() {
        resetAll();
        Path filePath = Paths.get(currentPath.toString(), "Add Carry.psm");

        picoblazeInterpreter.runPicoBlazeFileNatively(filePath);
        return     testRegister(PBRegisterName.S0, 0x95)
                && testRegister(PBRegisterName.S1, 0x00)
                && testRegister(PBRegisterName.SA, 0x00)
                && testRegister(PBRegisterName.SB, 0x00);
    }

    private static boolean subCarry() {
        resetAll();
        Path filePath = Paths.get(currentPath.toString(), "Sub Carry.psm");

        picoblazeInterpreter.runPicoBlazeFileNatively(filePath);
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

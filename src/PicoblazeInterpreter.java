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

        Instruction instruction = new Instruction(InstructionSet.ADD, registers.getRegister(RegisterName.s0), new Constant(254));
        Instruction instruction2 = new Instruction(InstructionSet.ADD, registers.getRegister(RegisterName.s0), new Constant(1));
        Parser parser = new Parser(registers);
        parser.parse(new Instruction[] {instruction, instruction2, instruction2, instruction, instruction2});

        System.out.format("%d: %s, C=%b, Z=%b\n", registers.getRegister(RegisterName.s0).getValue(), Integer.toBinaryString(registers.getRegister(RegisterName.s0).getValue()), registers.C, registers.Z);
    }
}

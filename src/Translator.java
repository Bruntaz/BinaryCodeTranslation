import Jorvik5.*;
import Jorvik5.Groups.J5InstructionSet;
import PicoBlazeSimulator.*;
import PicoBlazeSimulator.Groups.*;
import PicoBlazeSimulator.InstructionArguments.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Translator {
    private PBProgramCounter pbPC = PBProgramCounter.getInstance();
    private PBLexer pbLexer = PBLexer.getInstance();
    private PBParser pbParser = PBParser.getInstance();

    private J5ProgramCounter j5PC = J5ProgramCounter.getInstance();
    private J5Lexer j5Lexer = J5Lexer.getInstance();
    private J5Parser j5Parser = J5Parser.getInstance();

    private HashMap<PBRegisterName, Integer> registerMemoryLocation = new HashMap<PBRegisterName, Integer>() {{
        put(PBRegisterName.S0, 0);
        put(PBRegisterName.S1, 1);
        put(PBRegisterName.S2, 2);
        put(PBRegisterName.S3, 3);
        put(PBRegisterName.S4, 4);
        put(PBRegisterName.S5, 5);
        put(PBRegisterName.S6, 6);
        put(PBRegisterName.S7, 7);
        put(PBRegisterName.S8, 8);
        put(PBRegisterName.S9, 9);
        put(PBRegisterName.SA, 10);
        put(PBRegisterName.SB, 11);
        put(PBRegisterName.SC, 12);
        put(PBRegisterName.SD, 13);
        put(PBRegisterName.SE, 14);
        put(PBRegisterName.SF, 15);
    }};

    private String translateRegisterIntoMemory(PBInstructionArgument register) {
        PBRegisterName registerName = ((PBRegister) register).getRegisterName();
        return Integer.toHexString(registerMemoryLocation.get(registerName));
    }

    /*
    NOTE: This currently only supports register arguments for the logical operators. It will be necessary to add SSET
     <value> at some point instead of FETCH <register>
     */
    public J5Instruction[] translate(PBInstruction pbInstruction, int pbLineNumber) {
        if (pbInstruction == null || pbInstruction.instruction == PBInstructionSet.NOP) {
            return new J5Instruction[] {new J5Instruction(J5InstructionSet.NOP, null)};
        }

        PBInstructionSet instruction = pbInstruction.instruction;
        PBInstructionArgument arg0 = pbInstruction.arg0;
        PBInstructionArgument arg1 = pbInstruction.arg1;

        switch (instruction) {
            // PBRegister loading
            case LOAD:
                return new J5Instruction[] {
                        j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),
                };

            // Logical
            case AND:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("AND"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("AND"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };

                }
            case OR:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("OR"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("OR"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                }
            case XOR:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("XOR"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("XOR"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                }

            // Arithmetic
            case ADD:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("ADD"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("ADD"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                }
            case ADDCY:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("ADDCY"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("ADDCY"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                }
            case SUB:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("SUB"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("SUB"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                }
            case SUBCY:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("SUBCY"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("SUBCY"),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                }
            case SL0:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("SL"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),
                };
            case SR0:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("SR"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),
                };
            case RL:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("RL"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),
                };
            case RR:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("RR"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),
                };

            // Test and Compare
            case TEST:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("TEST"),
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("TEST"),
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                }
            case TESTCY:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("TESTCY"),
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("TESTCY"),
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                }

            case COMPARE:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("COMPARE"),
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("COMPARE"),
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                }
            case COMPARECY:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("COMPARECY"),
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("COMPARECY"),
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                }

            // Jump
            case JUMP:
                if (arg0 instanceof PBAbsoluteAddress) {
                    return new J5Instruction[] {
                            j5Lexer.lex("LBRANCH " + Integer.toHexString(arg0.getIntValue() + 1)),
                            j5Lexer.lex("STOP"),
                    };
                } else {
                    PBFlagArgument a0 = (PBFlagArgument) arg0;
                    switch (a0.getStringValue()) {
                        case PBFlagArgument.Z:
                            if (arg1.getIntValue() > pbLineNumber) {
                                return new J5Instruction[] {
                                        j5Lexer.lex("BRZERO " + Integer.toHexString((arg1.getIntValue() - pbLineNumber))),
                                        j5Lexer.lex("STOP"),
                                };
                            } else {
                                return new J5Instruction[] {
                                        // Conditional jump backwards
                                        j5Lexer.lex("BRZERO 2"),
                                        j5Lexer.lex("NOP"),
                                        j5Lexer.lex("LBRANCH " + Integer.toHexString((arg1.getIntValue() + 1))),
                                        j5Lexer.lex("STOP"),
                                };
                            }

                        case PBFlagArgument.NZ:
                            return new J5Instruction[] {
                                    j5Lexer.lex("BRZERO 2"), // Jump to location of next PB line
                                    j5Lexer.lex("LBRANCH " + Integer.toHexString((arg1.getIntValue() + 1))),
                                    j5Lexer.lex("STOP"),
                            };
                        case PBFlagArgument.C:
                            if (arg1.getIntValue() > pbLineNumber) {
                                return new J5Instruction[]{
                                        j5Lexer.lex("BRCARRY " + Integer.toHexString((arg1.getIntValue() - pbLineNumber))),
                                        j5Lexer.lex("STOP"),
                                };
                            } else {
                                return new J5Instruction[] {
                                        // Conditional jump backwards
                                        j5Lexer.lex("BRCARRY 2"),
                                        j5Lexer.lex("NOP"),
                                        j5Lexer.lex("LBRANCH " + Integer.toHexString((arg1.getIntValue() + 1))),
                                        j5Lexer.lex("STOP"),
                                };
                            }
                        case PBFlagArgument.NC:
                            return new J5Instruction[] {
                                    j5Lexer.lex("BRCARRY 2"),
                                    j5Lexer.lex("LBRANCH " + Integer.toHexString((arg1.getIntValue() + 1))),
                                    j5Lexer.lex("STOP"),
                            };
                    }
                }

            // Subroutines
            case CALL:
                if (arg0 instanceof PBAbsoluteAddress) {
                    return new J5Instruction[] {
                            j5Lexer.lex("CALL " + Integer.toHexString((arg0.getIntValue() + 1))),
                            j5Lexer.lex("STOP"),
                    };
                } else {
                    PBFlagArgument a0 = (PBFlagArgument) arg0;
                    switch (a0.getStringValue()) {
                        case PBFlagArgument.Z:
                            return new J5Instruction[] {
                                    j5Lexer.lex("CALLZERO " + Integer.toHexString((arg1.getIntValue() + 1))),
                                    j5Lexer.lex("STOP"),
                            };
                        case PBFlagArgument.NZ:
                            return new J5Instruction[] {
                                    j5Lexer.lex("BRZERO 2"),
                                    j5Lexer.lex("CALL " + Integer.toHexString((arg1.getIntValue() + 1))),
                                    j5Lexer.lex("STOP"),
                            };
                        case PBFlagArgument.C:
                            return new J5Instruction[] {
                                    j5Lexer.lex("CALLCARRY " + Integer.toHexString((arg1.getIntValue() + 1))),
                                    j5Lexer.lex("STOP"),
                            };
                        case PBFlagArgument.NC:
                            return new J5Instruction[] {
                                    j5Lexer.lex("BRCARRY 2"),
                                    j5Lexer.lex("CALL " + Integer.toHexString((arg1.getIntValue() + 1))),
                                    j5Lexer.lex("STOP"),
                            };
                    }
                }

            case RETURN:
                if (arg0 instanceof PBNoArgument) {
                    return new J5Instruction[] {
                            j5Lexer.lex("RETURN"),
                            j5Lexer.lex("STOP"),
                    };
                } else {
                    PBFlagArgument a0 = (PBFlagArgument) arg0;
                    switch (a0.getStringValue()) {
                        case PBFlagArgument.Z:
                            return new J5Instruction[] {
                                    j5Lexer.lex("BRZERO 2"),
                                    j5Lexer.lex("NOP"),
                                    j5Lexer.lex("RETURN"),
                                    j5Lexer.lex("STOP"),
                            };
                        case PBFlagArgument.NZ:
                            return new J5Instruction[] {
                                    j5Lexer.lex("BRZERO 2"),
                                    j5Lexer.lex("RETURN"),
                                    j5Lexer.lex("STOP"),
                            };
                        case PBFlagArgument.C:
                            return new J5Instruction[] {
                                    j5Lexer.lex("BRCARRY 2"),
                                    j5Lexer.lex("NOP"),
                                    j5Lexer.lex("RETURN"),
                                    j5Lexer.lex("STOP"),
                            };
                        case PBFlagArgument.NC:
                            return new J5Instruction[] {
                                    j5Lexer.lex("BRCARRY 2"),
                                    j5Lexer.lex("RETURN"),
                                    j5Lexer.lex("STOP"),
                            };
                    }
                }

            // Scratch Pad Memory
            case STORE:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)), // Register to store
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)), // Register with location
                            j5Lexer.lex("SSET 10"), // Change location to next line in memory
                            j5Lexer.lex("ADD"),
                            j5Lexer.lex("ISTORE"), // Location at TOS
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("STORE " + Integer.toHexString(arg1.getIntValue() + 16)),
                            j5Lexer.lex("DROP"),
                    };
                }
            case FETCH:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)), // Register with location
                            j5Lexer.lex("SSET 10"), // Change location to next line in memory
                            j5Lexer.lex("ADD"),
                            j5Lexer.lex("IFETCH"), // Location at TOS
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)), // Store in register
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + Integer.toHexString(arg1.getIntValue() + 16)),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                }

        }

        throw new Error("Translation for this command (" + instruction + ") is not supported yet.");
    }

    private List<String> readFile(Path filePath) {
        List<String> file = null;
        try {
            file = Files.readAllLines(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private void outputCodeToFile(J5Instruction[][] j5Instructions) {
        StringBuilder outputString = new StringBuilder();

        for (J5Instruction[] instructionGroup : j5Instructions) {
            if (instructionGroup == null) {
                continue;
            }

            for (J5Instruction instruction : instructionGroup) {
                if (instruction == null) {
                    continue;
                }
                outputString.append(instruction.toString());
                outputString.append("\n");
            }
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/TestCode/Output.j5a"));
            writer.write(outputString.toString());
            writer.close();

        } catch (IOException e) {
            System.out.println("Failed to write output file.");
            System.out.println(e);
        }
    }

    public void runPicoBlazeFileNatively(Path filePath) {
        List<String> file = readFile(filePath);

        PBInstruction[] instructions = pbLexer.lex(file);

        pbParser.parse(instructions);
        System.out.println(PBScratchPad.getInstance());
    }

    public void runJ5FileNatively(Path filePath) {
        List<String> file = readFile(filePath);

        J5Instruction[] instructions = j5Lexer.lex(file);

        j5Parser.parse(instructions);
        System.out.println(J5ScratchPad.getInstance());
    }

    public void runPicoBlazeFileOnJ5(Path filePath) {
        List<String> file = readFile(filePath);

        PBInstruction[] picoBlazeInstructions = pbLexer.lex(file);

        J5Instruction[][] j5Instructions = new J5Instruction[picoBlazeInstructions.length][];

        pbParser.RESET();
        int pbPC = this.pbPC.get();
        while (pbPC < picoBlazeInstructions.length) {
            if (j5Instructions[pbPC] == null) { // Currently this just converts everything because the program
                                                      // counter isn't being changed on jumps in the stack machine.
                picoBlazeInstructions[pbPC].isBlockStart = true;
                int nextBlockStart = pbParser.getNextBlockStart(picoBlazeInstructions, pbPC);

                // Iterate through current PicoBlaze block and translate it
                for (int instructionNumber = pbPC; instructionNumber < nextBlockStart; instructionNumber++) {
                    j5Instructions[instructionNumber] = translate(picoBlazeInstructions[instructionNumber], instructionNumber);
                }

                System.out.println("-------------Currently translated---------------");
                for (int i=0; i<j5Instructions.length; i++) {
                    System.out.println(Arrays.toString(j5Instructions[i]) + ", " + picoBlazeInstructions[i].instruction);
                }
                System.out.println("-------------Currently translated---------------");
            }

            j5PC.set(pbPC, false);
            j5PC.increment();
            System.out.println("################################################ PBPC = " + pbPC);
            System.out.println("PicoBlaze Instruction: " + picoBlazeInstructions[pbPC]);

            int j5InstructionPointer = 0;
            while (j5InstructionPointer < j5Instructions[pbPC].length) {
                J5Instruction instruction = j5Instructions[pbPC][j5InstructionPointer];

                if (j5PC.hasJustJumped()) {
                    if (instruction.instruction == J5InstructionSet.STOP) {
                        // NOP implies that the jump is the end of the block
                        break;
                    } else {
                        j5InstructionPointer = j5PC.get() - pbPC;
                        j5PC.setJustJumped(false);
                        continue;
                    }
                } else if (instruction.instruction == J5InstructionSet.NOP) {
                    // NOP implies that there has been an intentionally missed jump
                    break;
                }

                j5Parser.parse(instruction);

                j5InstructionPointer++;
            }

            // Move the PBPC if the J5 machine has jumped
            if (j5PC.hasJustJumped()) {
                this.pbPC.set(j5PC.get());
            } else {
                this.pbPC.increment();
            }

            pbPC = this.pbPC.get();
        }

//        for (int i=0; i<j5Instructions.length; i++) {
//            System.out.println(Arrays.toString(j5Instructions[i]) + ", " + picoBlazeInstructions[i].instruction);
//        }
//        System.out.println(j5Instructions.length);

        outputCodeToFile(j5Instructions);

        System.out.println(String.format("\nFinished in %d clock cycles", j5Parser.getClockCycles()));
        System.out.println(J5ScratchPad.getInstance());
    }

    public static void main(String[] args) {
        Path filePath = FileSystems.getDefault().getPath("src", "TestCode", args[0]);

        Translator translator = new Translator();
        if (args[1].equals("PB")) {
            translator.runPicoBlazeFileNatively(filePath);

        } else if (args[1].equals("J5")) {
            translator.runJ5FileNatively(filePath);

        } else {
            translator.runPicoBlazeFileOnJ5(filePath);
        }
    }
}

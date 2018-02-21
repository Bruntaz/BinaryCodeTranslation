import Jorvik5.*;
import Jorvik5.Groups.J5InstructionSet;
import Jorvik5.InstructionArguments.J5InstructionArgument;
import PicoBlazeSimulator.*;
import PicoBlazeSimulator.Groups.*;
import PicoBlazeSimulator.InstructionArguments.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

    private HashMap<J5InstructionPair, Integer> staticPairFrequency = new HashMap<>();
    private HashMap<J5InstructionPair, Integer> dynamicPairFrequency = new HashMap<>();

    private HashSet<J5InstructionSet> increaseStackSize = new HashSet<>(Arrays.asList(
            J5InstructionSet.SSET, J5InstructionSet.FETCH, J5InstructionSet.IFETCH, J5InstructionSet.DUP,
            J5InstructionSet.OVER, J5InstructionSet.UNDER, J5InstructionSet.TUCK, J5InstructionSet.TUCK2)
    );

    private HashSet<J5InstructionSet> decreaseStackSize = new HashSet<>(Arrays.asList(
            J5InstructionSet.AND, J5InstructionSet.OR, J5InstructionSet.XOR, J5InstructionSet.ADD,
            J5InstructionSet.ADDCY, J5InstructionSet.SUB, J5InstructionSet.SUBCY, J5InstructionSet.DROP,
            J5InstructionSet.NIP)
    );

    private HashSet<J5InstructionSet> constantStackSize = new HashSet<>(Arrays.asList(
            J5InstructionSet.NOT, J5InstructionSet.INC, J5InstructionSet.DEC, J5InstructionSet.TEST,
            J5InstructionSet.TESTCY, J5InstructionSet.COMPARE, J5InstructionSet.COMPARECY, J5InstructionSet.BRANCH,
            J5InstructionSet.BRZERO, J5InstructionSet.BRCARRY, J5InstructionSet.SBRANCH, J5InstructionSet.SBRZERO,
            J5InstructionSet.SBRCARRY, J5InstructionSet.LBRANCH, J5InstructionSet.IBRANCH, J5InstructionSet.CALL,
            J5InstructionSet.CALLZERO, J5InstructionSet.CALLCARRY, J5InstructionSet.RETURN, J5InstructionSet.SWAP,
            J5InstructionSet.ROT, J5InstructionSet.RROT, J5InstructionSet.STORE, J5InstructionSet.ISTORE,
            J5InstructionSet.SL0, J5InstructionSet.SL1, J5InstructionSet.SLX, J5InstructionSet.SLA,
            J5InstructionSet.SR0, J5InstructionSet.SR1, J5InstructionSet.SRX, J5InstructionSet.SRA, J5InstructionSet.RL,
            J5InstructionSet.RR, J5InstructionSet.NOP, J5InstructionSet.STOP, J5InstructionSet.PASS)
    );

    private int registerOffset = 32;
    private int alternateLocationOffset = 16;

    private String translateRegisterIntoMemory(PBInstructionArgument register, int offset) {
        PBRegisterName registerName = ((PBRegister) register).getRegisterName();
        return Integer.toHexString(registerMemoryLocation.get(registerName) + offset);
    }

    private String translateRegisterIntoMemory(PBInstructionArgument register) {
        return translateRegisterIntoMemory(register, 0);
    }

    private List<J5Instruction> flattenJ5Instructions(J5Instruction[][] j5Instructions) {
        List<J5Instruction> flattened = new ArrayList<>();
        for (J5Instruction[] instructions : j5Instructions) {
            flattened.addAll(Arrays.asList(instructions));
        }
        return flattened;
    }

    private List<J5Instruction> getFlattenedInstructionBlock(PBInstruction[] pbInstructions, int blockStart) {
        int nextBlockStart = pbParser.getNextBlockStart(pbInstructions, blockStart);

        J5Instruction[][] naivelyTranslated = new J5Instruction[nextBlockStart - blockStart][];
        // Iterate through current PicoBlaze block and translate it
        for (int instructionNumber = blockStart; instructionNumber < nextBlockStart; instructionNumber++) {
            naivelyTranslated[instructionNumber - blockStart] = translate(pbInstructions[instructionNumber],
                    instructionNumber);
        }

        return flattenJ5Instructions(naivelyTranslated);
    }

    private boolean singlePassPeepholeOptimiseJ5(List<J5Instruction> j5Instructions) {
        boolean optimisationsPerformed = false;
        for (int i = 0; i < j5Instructions.size() - 1; i++) {
            if (j5Instructions.get(i).instruction == J5InstructionSet.DUP &&
                    j5Instructions.get(i+1).instruction == J5InstructionSet.DROP) {
                j5Instructions.set(i, j5Lexer.lex("NOP"));
                j5Instructions.set(i+1, j5Lexer.lex("NOP"));
                optimisationsPerformed = true;

            } else if (j5Instructions.get(i).instruction == J5InstructionSet.SWAP &&
                    j5Instructions.get(i+1).instruction == J5InstructionSet.ADD) {
                j5Instructions.set(i, j5Lexer.lex("NOP"));
                optimisationsPerformed = true;

            } else if (j5Instructions.get(i).instruction == J5InstructionSet.TUCK &&
                    j5Instructions.get(i+1).instruction == J5InstructionSet.DROP) {
                j5Instructions.set(i, j5Lexer.lex("SWAP"));
                j5Instructions.set(i+1, j5Lexer.lex("NOP"));
                optimisationsPerformed = true;

            } else if (j5Instructions.get(i).instruction == J5InstructionSet.TUCK2 &&
                    j5Instructions.get(i+1).instruction == J5InstructionSet.DROP) {
                j5Instructions.set(i, j5Lexer.lex("ROT"));
                j5Instructions.set(i+1, j5Lexer.lex("NOP"));
                optimisationsPerformed = true;

            } else if (j5Instructions.get(i).instruction == J5InstructionSet.SWAP &&
                    j5Instructions.get(i+1).instruction == J5InstructionSet.SWAP) {
                j5Instructions.set(i, j5Lexer.lex("NOP"));
                j5Instructions.set(i+1, j5Lexer.lex("NOP"));
                optimisationsPerformed = true;

            } else if ((j5Instructions.get(i).instruction == J5InstructionSet.ROT &&
                    j5Instructions.get(i+1).instruction == J5InstructionSet.RROT) ||
                    (j5Instructions.get(i).instruction == J5InstructionSet.RROT &&
                    j5Instructions.get(i+1).instruction == J5InstructionSet.ROT)) {
                j5Instructions.set(i, j5Lexer.lex("NOP"));
                j5Instructions.set(i+1, j5Lexer.lex("NOP"));
                optimisationsPerformed = true;

            } else if (j5Instructions.get(i).instruction == J5InstructionSet.SSET &&
                    j5Instructions.get(i+1).instruction == J5InstructionSet.DROP) {
                j5Instructions.set(i, j5Lexer.lex("NOP"));
                j5Instructions.set(i+1, j5Lexer.lex("NOP"));
                optimisationsPerformed = true;

            } // else if TUCK then STORE then ADD becomes SWAP then STORE then ADD
        }

        return optimisationsPerformed;
    }

    private List<J5Instruction> peepholeOptimiseJ5(List<J5Instruction> j5Instructions) {
        List<J5Instruction> instructions = j5Instructions;

        boolean optimisationsPerformed = true;

        while (optimisationsPerformed) {
            optimisationsPerformed = singlePassPeepholeOptimiseJ5(instructions);
            optimisationsPerformed |= removeRedundantStores(instructions);

            List<J5Instruction> nopsRemoved = new ArrayList<>();
            for (J5Instruction instruction : instructions) {
                if (instruction.instruction != J5InstructionSet.NOP) {
                    nopsRemoved.add(instruction);
                } else {
                    optimisationsPerformed |= true;
                }
            }

            instructions = nopsRemoved;
        }

        return instructions;
    }

    private boolean removeRedundantStores(List<J5Instruction> j5Instructions) {
        boolean optimisationsPerformed = false;
        for (int i = j5Instructions.size() - 1; i > 0; i--) {
            J5Instruction storeInstruction = j5Instructions.get(i);

            if (storeInstruction.instruction == J5InstructionSet.STORE) {
                J5InstructionArgument location = storeInstruction.arg;

                for (int j = i - 1; j >= 0; j--) {
                    J5Instruction redundantStore = j5Instructions.get(j);

                    if (redundantStore.equals(storeInstruction)) {
                        // Remove STORE instruction since value hasn't been modified since last use
                        j5Instructions.set(j, j5Lexer.lex("NOP"));
                        optimisationsPerformed = true;

                    } else if (redundantStore.instruction == J5InstructionSet.FETCH &&
                            redundantStore.arg.equals(location)) {
                        // Location has been fetched since previous STORE so previous STOREs aren't redundant
                        break;
                    }
                }
            }
        }

        return optimisationsPerformed;
    }

    private boolean optimiseKoopmanPair(List<J5Instruction> j5Instructions, Pair pair) {
        boolean optimisationsPerformed = false;
        int useStackSize = 0;
        int reuseStackSize = 0;

        // Find stack depth at use and reuse line for this pair
        for (int i = 0; i <= pair.reuseLine; i++) {
            J5Instruction instruction = j5Instructions.get(i);

            if (increaseStackSize.contains(instruction.instruction)) {
                if (i <= pair.useLine) {
                    useStackSize++;
                }
                reuseStackSize++;
            } else if (decreaseStackSize.contains(instruction.instruction)) {
                if (i <= pair.useLine) {
                    useStackSize--;
                }
                reuseStackSize--;
            } else if (!constantStackSize.contains(instruction.instruction)) {
                throw new Error("Instruction " + instruction.instruction + " leaves stack at unknown size. " +
                        "Please add it to the relevant set.");
            }
        }

        // If stack sizes are small enough insert optimisations
        if (reuseStackSize <= 3 && useStackSize <= 3) {
            J5Instruction reuseInstruction;
            switch (reuseStackSize) {
                case 1:
                    reuseInstruction = new J5Instruction(J5InstructionSet.NOP, null);
                    break;
                case 2:
                    reuseInstruction = new J5Instruction(J5InstructionSet.SWAP, null);
                    break;
                case 3:
                    reuseInstruction = new J5Instruction(J5InstructionSet.RROT, null);
                    break;
                default:
                    throw new Error("This means the reuse stack size is < 1, which should be impossible.");
            }

            J5Instruction useInstruction = j5Instructions.get(pair.useLine);
            switch (useStackSize) {
                case 0:
                    throw new Error("Stack size 0 in use statement. This should never happen because the reuse " +
                            "FETCH increases stack size.");
                case 1:
                    // Insert DUP to copy value to bottom
                    j5Instructions.set(pair.reuseLine, reuseInstruction);
                    j5Instructions.set(pair.useLine, j5Lexer.lex("DUP"));
                    j5Instructions.add(pair.useLine, useInstruction);
                    optimisationsPerformed = true;
                    break;
                case 2:
                    // Insert TUCK to copy value to bottom
                    j5Instructions.set(pair.reuseLine, reuseInstruction);
                    j5Instructions.set(pair.useLine, j5Lexer.lex("TUCK"));
                    j5Instructions.add(pair.useLine, useInstruction);
                    optimisationsPerformed = true;
                    break;
                case 3:
                    // Insert TUCK2 to copy value to bottom
                    j5Instructions.set(pair.reuseLine, reuseInstruction);
                    j5Instructions.set(pair.useLine, j5Lexer.lex("TUCK2"));
                    j5Instructions.add(pair.useLine, useInstruction);
                    optimisationsPerformed = true;
                    break;
            }
        }

        return optimisationsPerformed;
    }

    private void koopmanOptimise(List<J5Instruction> j5Instructions, OptimisationLevel optimisationLevel) {
        // Algorithm step 1: Get pairs where variables can be reused
        // This goes through the program to find FETCH commands and then finds the last use of the variable (STORE or
        // FETCH). It doesn't support searching for the last time the variable was used on the stack (currently)
        // because the naive translation will always load the variable in directly before anyway.
        boolean optimisationsPerformed = true;
//        int iterations = 0;
        System.out.println("  Initial: " + Arrays.toString(j5Instructions.toArray()));
        while (optimisationsPerformed) {
            optimisationsPerformed = false;

            List<Pair> pairs = new ArrayList<>();
            for (int i = 0; i < j5Instructions.size(); i++) {
                J5Instruction fetchInstruction = j5Instructions.get(i);

                if (fetchInstruction.instruction == J5InstructionSet.FETCH) {
                    J5InstructionArgument location = fetchInstruction.arg;

                    for (int j = i - 1; j >= 0; j--) {
                        J5Instruction storeInstruction = j5Instructions.get(j);

                        if (storeInstruction.instruction == J5InstructionSet.ISTORE) {
                            if (optimisationLevel == OptimisationLevel.level4) {
                                if (!(j > 1 && j5Instructions.get(j-1).instruction == J5InstructionSet.ADD &&
                                        j5Instructions.get(j-2).equals(
                                                j5Lexer.lex("SSET 20")
                                        ))) {
                                    break; // If an ISTORE is between the pair, do not optimise
                                }
                            } else {
                                break;
                            }
                        }

                        if (location.equals(storeInstruction.arg)) {
                            pairs.add(new Pair(j, i, location));
                            break;
                        }
                    }
                }
            }

            // Algorithm step 2: Candidates for stack scheduling (use/reuse pairs) are ranked in order of ascending distance
//            System.out.println("Pair array: " + Arrays.toString(pairs.toArray()));
            Collections.sort(pairs);
//            System.out.println("Pair array sorted: " + Arrays.toString(pairs.toArray()));

            for (Pair pair : pairs) {
                optimisationsPerformed = optimiseKoopmanPair(j5Instructions, pair);
                if (optimisationsPerformed) {
                    // If any optimisations have been performed, recalculate all pairs to update line numbers
                    break;
                }
            }

//            System.out.println("Optimisations performed for " + iterations++ + " loops.");
//            System.out.println("Current state: " + Arrays.toString(flattened.toArray()));
        }

    }

    private J5Instruction[] translateBlock(PBInstruction[] pbInstructions, int blockStart, OptimisationLevel optimisationLevel) {
        List<J5Instruction> flattened = getFlattenedInstructionBlock(pbInstructions, blockStart);

        if (optimisationLevel == OptimisationLevel.level2 || optimisationLevel == OptimisationLevel.level3 ||
                optimisationLevel == OptimisationLevel.level4) {
            // Algorithm steps 1 & 2: Get pairs and schedule
            koopmanOptimise(flattened, optimisationLevel);
        }

        if (optimisationLevel == OptimisationLevel.level1 || optimisationLevel == OptimisationLevel.level3 ||
                optimisationLevel == OptimisationLevel.level4) {
            // Algorithm step 3: Peephole optimisation
            flattened = peepholeOptimiseJ5(flattened);
            System.out.println("Optimised: " + Arrays.toString(flattened.toArray()));
        }

        J5Instruction[] flattenedArray = new J5Instruction[flattened.size()];
        flattened.toArray(flattenedArray);
        return flattenedArray;
    }

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
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("SSET " + Integer.toHexString(arg1.getIntValue())),
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DROP"),
                    };
                }
            case STAR:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0, alternateLocationOffset)),
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
                } else if (arg1.getIntValue() == 1) {
                    return new J5Instruction[]{
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("INC"),
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
                } else if (arg1.getIntValue() == 1) {
                    return new J5Instruction[]{
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("DEC"),
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

            // Shift and rotate
            case SL0:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("SL0"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),
                };
            case SL1:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("SL1"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),
                };
            case SLX:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("SLX"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),
                };
            case SLA:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("SLA"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),
                };
            case SR0:
                return new J5Instruction[] {
                    j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                    j5Lexer.lex("SR0"),
                    j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                    j5Lexer.lex("DROP"),
                };
            case SR1:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("SR1"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),

                };
            case SRX:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("SRX"),
                        j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("DROP"),

                };
            case SRA:
                return new J5Instruction[] {
                        j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                        j5Lexer.lex("SRA"),
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
                                        j5Lexer.lex("PASS"),
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
                                        j5Lexer.lex("PASS"),
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
                                    j5Lexer.lex("PASS"),
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
                                    j5Lexer.lex("PASS"),
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
                            j5Lexer.lex("SSET " + Integer.toHexString(registerOffset)), // Change location to next line in memory
                            j5Lexer.lex("ADD"),
                            j5Lexer.lex("ISTORE"), // Location at TOS
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg0)),
                            j5Lexer.lex("STORE " + Integer.toHexString(arg1.getIntValue() + registerOffset)),
                            j5Lexer.lex("DROP"),
                    };
                }
            case FETCH:
                if (arg1 instanceof PBRegister) {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + translateRegisterIntoMemory(arg1)), // Register with location
                            j5Lexer.lex("SSET " + Integer.toHexString(registerOffset)), // Change location to next
                            // line in memory
                            j5Lexer.lex("ADD"),
                            j5Lexer.lex("IFETCH"), // Location at TOS
                            j5Lexer.lex("STORE " + translateRegisterIntoMemory(arg0)), // Store in register
                            j5Lexer.lex("DROP"),
                            j5Lexer.lex("DROP"),
                    };
                } else {
                    return new J5Instruction[] {
                            j5Lexer.lex("FETCH " + Integer.toHexString(arg1.getIntValue() + registerOffset)),
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
        System.out.println(String.format("With %d memory reads and %d writes", PBScratchPad.getInstance().getMemoryReads(),
                PBScratchPad.getInstance().getMemoryWrites()));
        System.out.println(PBScratchPad.getInstance());
    }

    public void runJ5FileNatively(Path filePath) {
        List<String> file = readFile(filePath);

        J5Instruction[] instructions = j5Lexer.lex(file);

        j5Parser.parse(instructions);
        System.out.println(String.format("With %d memory reads and %d writes", J5ScratchPad.getInstance().getMemoryReads(),
                J5ScratchPad.getInstance().getMemoryWrites()));
        System.out.println(J5ScratchPad.getInstance());
    }

    public void runPicoBlazeFileOnJ5(Path filePath, OptimisationLevel optimisationLevel) {
        List<String> file = readFile(filePath);

        PBInstruction[] picoBlazeInstructions = pbLexer.lex(file);

        J5Instruction[][] j5Instructions = new J5Instruction[picoBlazeInstructions.length][];
        J5Instruction[][] j5BlockInstructions = new J5Instruction[picoBlazeInstructions.length][];

        J5ScratchPad j5ScratchPad = J5ScratchPad.getInstance();
        j5ScratchPad.setMemorySize(PBScratchPad.getInstance().getMemorySize() + registerOffset); // Increase scratch pad size

        pbParser.RESET();
        int pbPC = this.pbPC.get();
        int currentBlock = 0;
        int nextBlock = pbParser.getNextBlockStart(picoBlazeInstructions, pbPC);
        while (pbPC < picoBlazeInstructions.length) {
            currentBlock = pbPC;
            nextBlock = pbParser.getNextBlockStart(picoBlazeInstructions, pbPC);

            if (j5BlockInstructions[currentBlock] == null) {
                picoBlazeInstructions[currentBlock].isBlockStart = true;

                // Iterate through current PicoBlaze block and translate it
                for (int instructionNumber = currentBlock; instructionNumber < nextBlock; instructionNumber++) {
                    j5Instructions[instructionNumber] = translate(picoBlazeInstructions[instructionNumber], instructionNumber);
                }

                System.out.println("-------------Currently translated---------------");
                for (int i=0; i<j5Instructions.length; i++) {
                    System.out.println(Arrays.toString(j5Instructions[i]) + ", " + picoBlazeInstructions[i].instruction);
                }
                System.out.println("-------------Currently translated---------------");

                j5BlockInstructions[currentBlock] = translateBlock(picoBlazeInstructions, currentBlock,
                        optimisationLevel);

                System.out.println("-------------Block translated---------------");
                for (int i=0; i<j5BlockInstructions.length; i++) {
                    System.out.println(Arrays.toString(j5BlockInstructions[i]));
                }
                System.out.println("-------------Block translated---------------");
            }

            j5PC.set(currentBlock, false);
            System.out.println("################################################ PBPC = " + pbPC);
            System.out.println("PicoBlaze Instruction: " + picoBlazeInstructions[pbPC]);

            countInstructionPairs(j5BlockInstructions[currentBlock], dynamicPairFrequency);

            int j5InstructionPointer = 0;
            while (j5InstructionPointer < j5BlockInstructions[currentBlock].length) {
                J5Instruction instruction = j5BlockInstructions[currentBlock][j5InstructionPointer];

                if (j5PC.hasJustJumped()) {
                    J5Instruction jumpInstruction = j5BlockInstructions[currentBlock][j5InstructionPointer-1];
                    switch (jumpInstruction.instruction) {
                        case LBRANCH:
                        case IBRANCH:
                        case CALL:
                        case CALLZERO:
                        case CALLCARRY:
                            break;
                        case RETURN:
                            j5PC.set(pbParser.getNextBlockStart(picoBlazeInstructions, j5PC.get()), true);
                            break; // Ending unconditional jump

                        default:
                            int amount;
                            if (jumpInstruction.instruction == J5InstructionSet.BRCARRY ||
                                    jumpInstruction.instruction == J5InstructionSet.BRZERO) {
                                amount = jumpInstruction.arg.getValue() - 1;
                            } else {
                                amount = -(jumpInstruction.arg.getValue() + 1);
                            }

                            if (instruction.instruction == J5InstructionSet.STOP) {
                                j5PC.set(nextBlock + amount, true); // Conditional ending jump
                                break;

                            } else {
                                j5InstructionPointer += amount; // Conditional continuing jump
                                j5PC.setJustJumped(false);
                                continue;
                            }
                    }
                    break;

                } else if (instruction.instruction == J5InstructionSet.PASS) {
                    // PASS implies that there has been an intentionally missed jump
                    break;
                }

                j5Parser.parse(instruction);

                j5InstructionPointer++;
            }

            // Move the PBPC if the J5 machine has jumped
            if (j5PC.hasJustJumped()) {
                this.pbPC.set(j5PC.get());
            } else {
                this.pbPC.set(nextBlock);
            }

            pbPC = this.pbPC.get();
            System.out.println(J5ScratchPad.getInstance());
        }

        outputCodeToFile(j5BlockInstructions);

        for (J5Instruction[] instructions : j5BlockInstructions) {
            if (instructions == null) {
                continue;
            }

            countInstructionPairs(instructions, staticPairFrequency);
        }

        System.out.println("\n\nFrequency of static pairs:");
        for (J5InstructionPair pair : sortHashMapByValues(staticPairFrequency).keySet()) {
            System.out.println(pair.toString() + " = " + staticPairFrequency.get(pair));
        }

        System.out.println("\n\nFrequency of dynamic pairs:");
        for (J5InstructionPair pair : sortHashMapByValues(dynamicPairFrequency).keySet()) {
            System.out.println(pair.toString() + " = " + dynamicPairFrequency.get(pair));
        }

        System.out.println(String.format("\nFinished in %d clock cycles", j5Parser.getClockCycles()));
        System.out.println(String.format("With %d memory reads and %d writes", j5ScratchPad.getMemoryReads(),
                j5ScratchPad.getMemoryWrites()));
        System.out.println(J5ScratchPad.getInstance());
    }

    private void countInstructionPairs(J5Instruction[] j5Instructions, HashMap<J5InstructionPair, Integer> map) {
        for (int i = 0; i < j5Instructions.length - 1; i++) {
            J5InstructionPair pair = new J5InstructionPair(j5Instructions[i].instruction, j5Instructions[i+1].instruction);
            if (map.containsKey(pair)) {
                map.put(pair, map.get(pair) + 1);
            } else {
                map.put(pair, 1);
            }
        }
    }

    private LinkedHashMap<J5InstructionPair, Integer> sortHashMapByValues(
            HashMap<J5InstructionPair, Integer> passedMap) {

        List<J5InstructionPair> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<>(passedMap.values());

        mapValues.sort(Collections.reverseOrder());
        mapKeys.sort(Collections.reverseOrder());

        LinkedHashMap<J5InstructionPair, Integer> sortedMap = new LinkedHashMap<>();

        for (Integer value : mapValues) {
            Iterator<J5InstructionPair> keyIterator = mapKeys.iterator();

            while (keyIterator.hasNext()) {
                J5InstructionPair key = keyIterator.next();
                Integer compareValue = passedMap.get(key);

                if (compareValue.equals(value)) {
                    keyIterator.remove();
                    sortedMap.put(key, value);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public static void main(String[] args) {
        Path filePath = FileSystems.getDefault().getPath("src", "TestCode", args[0]);

        Translator translator = new Translator();
        switch (args[1]) {
            case "PB":
                translator.runPicoBlazeFileNatively(filePath);
                break;

            case "J5":
                translator.runJ5FileNatively(filePath);
                break;

            default:
                switch (args[2]) {
                    case "0":
                        translator.runPicoBlazeFileOnJ5(filePath, OptimisationLevel.level0);
                        break;
                    case "1":
                        translator.runPicoBlazeFileOnJ5(filePath, OptimisationLevel.level1);
                        break;
                    case "2":
                        translator.runPicoBlazeFileOnJ5(filePath, OptimisationLevel.level2);
                        break;
                    case "3":
                        translator.runPicoBlazeFileOnJ5(filePath, OptimisationLevel.level3);
                        break;
                    case "4":
                        translator.runPicoBlazeFileOnJ5(filePath, OptimisationLevel.level4);
                        break;
                    default:
                        throw new Error("Invalid optimisation level");
                }
                break;
        }
    }
}

# Binary Code Translation From Register to Stack Based Code
This project has been created for my final year project in Computer Science at the University of York. My project is 
to write a binary translator from a register to a stack based processor architecture.

The intention for my project is to live translate the code as it is being executed, meaning that only the 
necessary code will be translated and therefore processing time will not be wasted by translating unused code. For 
example:

```
loadRegisters:
    LOAD s0, FF          ; Set register s0 to FF
    LOAD s1, 1           ; Set register s1 to 1
    JUMP callSubroutines ; Jump to callSubroutines label 

setS2:
    LOAD s2, EE          ; Set register s2 to EE
    RETURN               ; Return to next value on program counter stack

setS3:
    LOAD s3, DD          ; Set register s3 to DD
    RETURN               ; Return to next value on program counter stack

callSubroutines:
    ADD s0, s1           ; Store in s0 the value of s0 + s1 (FF + 01 = 00 and a carry bit)
    CALL C, setS2        ; Call setS2 subroutine if the carry bit is set (it is here)
    
    ADD s0, s1           ; Store in s0 the value of s0 + s1 (00 + 01 = 01 with no carry bit)
    CALL C, setS3        ; Call setS3 subroutine if the carry bit is set (it isn't here)
    
    LOAD s4, CC          ; Set register s4 to CC
```

When this code is executed, the registers will become:

| s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7 | s8 | s9 | sA | sB | sC | sD | sE | sF |
|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|
| 01 | 01 | EE | 00 | CC | 00 | 00 | 00 | 00 | 00 | 00 | 00 | 00 | 00 | 00 | 00 |

The `loadRegisters`, `setS2` and `callSubroutines` sections will all be executed in their entirety, but the `setS3` 
section will never be executed. Because of this, the most efficient translation of this code would translate 
everything apart from the `setS3` subroutine.

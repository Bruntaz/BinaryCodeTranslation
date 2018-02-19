# Binary Code Translation From Register to Stack Based Code
This project has been created for my final year project in Computer Science at the University of York. My project is 
to write a binary translator from a register to a stack based processor architecture.

The intention for my project is to live translate the code as it is being executed, meaning that only the 
necessary code will be translated and therefore processing time will not be wasted by translating unused code. For 
example:

```
loadRegisters:
    LOAD s0, FF              ; Set register s0 to FF
    LOAD s1, 1               ; Set register s1 to 1
    JUMP callSubroutines     ; Jump to callSubroutines label 

setS2:
    LOAD s2, EE              ; Set register s2 to EE
    RETURN                   ; Return to next value on program counter stack

setS3:
    LOAD s3, DD              ; Set register s3 to DD
    RETURN                   ; Return to next value on program counter stack

callSubroutines:
    ADD s0, s1               ; Store in s0 the value of s0 + s1 (FF + 01 = 00 and a carry bit)
    CALL C, setS2            ; Call setS2 subroutine if the carry bit is set (it is here)
    
    ADD s0, s1               ; Store in s0 the value of s0 + s1 (00 + 01 = 01 with no carry bit)
    CALL C, setS3            ; Call setS3 subroutine if the carry bit is set (it isn't here)
    
    LOAD s4, CC              ; Set register s4 to CC
```

When this code is executed, the registers will become:

| s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7 | s8 | s9 | sA | sB | sC | sD | sE | sF |
|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|
| 01 | 01 | EE | 00 | CC | 00 | 00 | 00 | 00 | 00 | 00 | 00 | 00 | 00 | 00 | 00 |

The `loadRegisters`, `setS2` and `callSubroutines` sections will all be executed in their entirety, but the `setS3` 
section will never be executed. Because of this, the most efficient translation of this code would translate 
everything apart from the `setS3` subroutine. An example of a translation of the above code to Jorvik-5 assembly is
as follows:

```
loadRegisters:
    SSET FF                 ; (FF)      Set top of stack to FF
    STORE 0                 ; (FF)      Store FF in memory location 0
    DROP                    ; ()        Drop value off stack
    SSET 1                  ; (1)       Set top of stack to 1
    STORE 1                 ; (1)       Store 1 in memory location 1
    DROP                    ; ()        Drop value off stack
    LBRANCH callSubroutines ; ()        Jump to line D
    
setS2:
    SSET EE                 ; (EE)      Set top of stack to EE
    STORE 2                 ; (EE)      Store EE in memory location 2
    DROP                    ; ()        Drop value off stack
    RETURN                  ; ()        Return to next value on program counter stack

setS3:                      ; Not executed so not translated

callSubroutines:
    FETCH 0                 ; (FF)      Set top of stack to value in memory location 0
    FETCH 1                 ; (FF, 1)   Set top of stack to value in memory location 1
    ADD                     ; (0)       Add top two on stack
    STORE 0                 ; (0)       Store result in memory location 0
    DROP                    ; ()        Drop result from stack
    CALLCARRY setS2         ; ()        Call subroutine setS2 if carry flag is set (it is)
    
    FETCH 0                 ; (0)       Set top of stack to value in memory location 0
    FETCH 1                 ; (0, 1)    Set top of stack to value in memory location 1
    ADD                     ; (1)       Add top two on stack
    STORE 0                 ; (0)       Store result in memory location 0
    DROP                    ; ()        Drop result from stack
    CALLCARRY setS3         ; ()        Call subroutine setS3 if carry flag is set (it isn't)
    
    SSET CC                 ; (CC)      Set top of stack to CC
    STORE 4                 ; (CC)      Store CC in memory location 0
    DROP                    ; ()        Drop value from stack
```
This code uses the first 5 values in memory to simulate the registers s0 through s4.

# Jorvik-5 Simulator
This will be a simulator for the Jorvik-5 (J5) architecture, created for my final year project for my degree in 
Computer Science at the University of York.

The J5 processor is defined in the lectures for one of the modules on UoY Computer Science. The instruction set is
not fully defined and my simulator will deviate from the instructions to make it more complete and more suitable for 
translation with the PicoBlaze machine.

The circuit diagram for the J5 processor that my design is based on is here:

![J5 design](../../Images/J5Architecture.png)

## Modifications to the design
A list of the modifications to the standard architecture I have made is as follows:
* Stack width is 8 bit instead of 16 bit
    * The SET statement has been removed because of this (the SSET statement remains)
* Add FETCH, IFETCH, STORE and ISTORE instructions for Scratch Pad Memory
* Add AND, OR, XOR and NOT instructions
* Add carry flag and make its state consistent with that in PicoBlaze
* Add ADDCY, SUBCY, BRCARRY, SBRCARRY, CALLCARRY, CALLZERO
* Use the same semantics for testing & comparison as in PicoBlaze
* Add SL0, SL1, SLX, SLA, SR0, SR1, SRX, SRA, RL, RR commands (consistent with PB)
* Add PASS. This is a specific instruction to aid with translation and does nothing
* Add OVER, UNDER, TUCK, TUCK2, NIP 
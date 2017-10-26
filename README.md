# PicoBlaze Simulator
This is a simulator for PicoBlaze, using the KCPSM6 instruction set created for my final year project for my degree in 
computer science at the University of York. 

The simulator supports the whole instruction set apart from anything IO related (the `INPUT`, `OUTPUT`, `OUTPUTK`, 
`DISABLE`, `ENABLE` and `RETURNI` instructions are not supported). It only supports the `CONSTANT` directive so far
but more directives may be added in the future.

To run a file, call the main function (in `PicoBlazeSimulator.PicoblazeInterpreter`) with the first argument as the name of the file to be 
executed.

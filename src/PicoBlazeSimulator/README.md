# PicoBlaze Simulator
This is a simulator for PicoBlaze, using the KCPSM6 instruction set created for my final year project for my degree in 
Computer Science at the University of York. 

The simulator supports the whole instruction set apart from anything IO related (the `INPUT`, `OUTPUT`, `OUTPUTK`, 
`DISABLE`, `ENABLE` and `RETURNI` instructions are not supported). It only supports the `CONSTANT` directive so far
but more directives may be added in the future.

## Running Information
There is currently no `main` file in this simulator, however it can be run from the `Translator` class in the parent
directory. The function `runPicoBlazeFileNatively` takes a filename argument and will run the PicoBlaze assembly
contained in the file on the PicoBlaze simulator.

If you want to add your own `main` file to this package, it will need to send a `List` of `Strings` to the 
`Lexer.lex` function (where each `String` is a line of assembly code). This will return an array of `Instruction`s
which can then be passed to the `Parser.parse` function to be executed.

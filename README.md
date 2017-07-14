# ep
Equation Parser

This is to practice building your own simple data
structures. Q1. Building any data structure that you decide you need,
write a program which takes in a String representing an algebraic
computation. That String will be in one of two forms: either infix or
reverse Polish. If the input is in infix format, then your program
should test to see if it's well-formed (eg, do the parentheses
match?). Your program should be able to convert the input format into
the other format (ie infix to reverse Polish, and vice versa). You
should also provide the ability to perform the actual calculation from
the reverse Polish format via a stack, showing the state of the stack
at each stage (ie don't just output the answer, and actually use the
stack to perform the calculation by actively pushing characters onto
the stack, on seeing an operator character call a method to add,
subtract, etc, and then push the result back onto the stack, etc).

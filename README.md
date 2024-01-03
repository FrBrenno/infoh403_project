# PASCALMaisPresque

## Overview

PASCALMaisPresque is a programming language designed to showcase the principles of compiler construction. This project aims to provide a simplified Pascal-like language, exploring concepts such as lexical analysis, parsing, abstract syntax trees (AST), and code generation.

This project was part of the course INFO-F403 of Computer Science Master's Degree of ULB at 2023-2024. Project statements for part one, two and three are in the doc directory.

## Features

- **Lexical Analysis:** Tokenization of source code.
- **Parsing:** Construction of a ParseTree from the token stream.
- **Abstract Syntax Tree (AST) Generation:** Creation of an AST representing the source code structure.
- **LLVM IR Code Generation:** Transformation of the AST into LLVM Intermediate Representation (IR) code.
- **Primitive Operations:** Support for basic arithmetic operations: `+`, `-`, `*`, `/`.
- **Control Flow:** Implementation of conditional statements (IF) and loops (WHILE).
- **Input/Output:** Reading and writing data to/from the console.

## Examples

```pmp

'' This prompt is a piece of code that should work ''
'' As there is no string definition in our language, we will use comments instead. ''

begin
    numTerms := 15... '' Number of terms to display ''

    ** Initialize variables
    term1 := 0...
    term2 := 1...
    i := 1...

    ** Display the first two terms
    print(term1)...
    print(term2)...

    ** Calculate and display the remaining terms
    while i < numTerms - 2 do
        begin
            nextTerm := term1 + term2...
            print(nextTerm)...
            term1 := term2...
            term2 := nextTerm...
            i := i + 1
        end
end
```

## Authors

FERREIRA Brenno ([FrBrenno](https://github.com/FrBrenno))
MUTKOWSKI Phillipe ([mtkski](https://github.com/mtkski))

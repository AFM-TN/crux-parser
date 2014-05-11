Crux Parser
====

###Introduction

A compiler is comprised of seven stages:
-	Lexical analysis: Identify the logical pieces of the input.
-	Syntax analysis: Identify how those pieces relate to each other.
-	Semantic analysis: Identify the meaning of the overall structure.
-	IR Generation: Design one possible structure.
-	IR Optimization: Simplify the intended structure.
-	Generation: Fabricate the structure.
-	Optimization: Improve the resulting structure.

###Project

This project implements the syntax analysis stage, or parser, of the compiler for the language Crux. The parser is a LL(0) parser( left-to-right scanning, leftmost derivation, 0 lookaheads) that utilizes a First Set to identify which production rule it will use. The parser uses the previously implemented scanner to generate the tokens needed to parse the source input. If the source input cannot be parsed successfully, an error message will be returned to notify the user.

###Classes

Compiler: Takes a source input file and tries to parse the source input. If it parses succesfully, a parse tree will be generated. If not, an error message will be generated instead.

Scanner: Uses a greedy approach to generate the tokens from a source input.

Parser: Uses the scanner to generate the tokens from the source input and creates a parse tree from them.

Token: Represents the logical pieces of Crux.

NonTerminal: Contains the First Set of each production rule.

####Sample

#####Input

```
func main() : void {
    let a = 4;
    let b = 5.2;
    let c = true;
    let d = false;
}
```

#####Output
```
PROGRAM
  DECLARATION_LIST
    DECLARATION
      FUNCTION_DEFINITION
        PARAMETER_LIST
        TYPE
        STATEMENT_BLOCK
          STATEMENT_LIST
            STATEMENT
              ASSIGNMENT_STATEMENT
                DESIGNATOR
                EXPRESSION0
                  EXPRESSION1
                    EXPRESSION2
                      EXPRESSION3
                        LITERAL
            STATEMENT
              ASSIGNMENT_STATEMENT
                DESIGNATOR
                EXPRESSION0
                  EXPRESSION1
                    EXPRESSION2
                      EXPRESSION3
                        LITERAL
            STATEMENT
              ASSIGNMENT_STATEMENT
                DESIGNATOR
                EXPRESSION0
                  EXPRESSION1
                    EXPRESSION2
                      EXPRESSION3
                        LITERAL
            STATEMENT
              ASSIGNMENT_STATEMENT
                DESIGNATOR
                EXPRESSION0
                  EXPRESSION1
                    EXPRESSION2
                      EXPRESSION3
                        LITERAL
```

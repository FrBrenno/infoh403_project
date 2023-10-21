// INFOF403
// FERREIRA Brenno
// MUTKOWSKI Philippe
// M-IRIFS

%% // Options

%class LexicalAnalyzer
%unicode
%standalone
%line
%column
%type Symbol

// ERE: Extended Regular Expression
AlphaNum = [a-zA-Z0-9]
VarName = [a-z]{AlphaNum}*
Number = "-"?[0-9]*
EndOfLine = "\r"?"\n"

// STATES
%xstate YYINITIAL, SHORT_COMMENT, LONG_COMMENT

// USER CODE
%eofval{
    return new Symbol(LexicalUnit.EOS, yyline, yycolumn);
%eofval}

%% // Token identification

// Reserved keywords

"begin" {System.out.print("BEGIN :___________________________"); return new Symbol(LexicalUnit.BEG, yyline, yycolumn);}
"end" {return new Symbol(LexicalUnit.END, yyline, yycolumn);}
"if" {return new Symbol(LexicalUnit.IF, yyline, yycolumn);}
"then" {return new Symbol(LexicalUnit.THEN, yyline, yycolumn);}
"else" {return new Symbol(LexicalUnit.ELSE, yyline, yycolumn);}
"and" {return new Symbol(LexicalUnit.AND, yyline, yycolumn);}
"or" {return new Symbol(LexicalUnit.OR, yyline, yycolumn);}
"while" {return new Symbol(LexicalUnit.WHILE, yyline, yycolumn);}
"do" {return new Symbol(LexicalUnit.DO, yyline, yycolumn);}
"print" {return new Symbol(LexicalUnit.PRINT, yyline, yycolumn);}
"read" {return new Symbol(LexicalUnit.READ, yyline, yycolumn);}

// Structure Symbols

"(" {return new Symbol(LexicalUnit.LPAREN, yyline, yycolumn);}
")" {return new Symbol(LexicalUnit.RPAREN, yyline, yycolumn);}
"{" {return new Symbol(LexicalUnit.LBRACK, yyline, yycolumn);}
"}" {return new Symbol(LexicalUnit.RBRACK, yyline, yycolumn);}

// Operation Symbols

":=" {return new Symbol(LexicalUnit.ASSIGN, yyline, yycolumn);}
"-" {return new Symbol(LexicalUnit.MINUS, yyline, yycolumn);}
"+" {return new Symbol(LexicalUnit.PLUS, yyline, yycolumn);}
"*" {return new Symbol(LexicalUnit.TIMES, yyline, yycolumn);}
"/" {return new Symbol(LexicalUnit.DIVIDE, yyline, yycolumn);}

// Relational Symbols

"=" {return new Symbol(LexicalUnit.EQUAL, yyline, yycolumn);}
"<" {return new Symbol(LexicalUnit.SMALLER, yyline, yycolumn);}

// ERE

^{VarName} {return new Symbol(LexicalUnit.VARNAME, yyline, yycolumn, yytext());}
{Number}  {return new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, yytext());} // "... Number is a string of digits"

// Comments

<YYINITIAL> {
    ^"**" {yybegin(SHORT_COMMENT);}
    ^"''" {yybegin(LONG_COMMENT);}
    [^"**""''"] {} // To check
}

<SHORT_COMMENT> {
    {EndOfLine}$ {yybegin(YYINITIAL);}
    . {}
}

<LONG_COMMENT> {
    "''"$ {yybegin(YYINITIAL);}
    . {}
}
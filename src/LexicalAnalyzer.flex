// INFOF403
// FERREIRA Brenno
// MUTKOWSKI Philippe
// M-IRIFS

%% //### Options ###

%class LexicalAnalyzer
%unicode
%line
%column
%type Symbol

//ERE: Extended Regular Expression
AlphaNum = [a-zA-Z0-9]
VarName = [a-z]{AlphaNum}*
Number = "-"?[0-9]+
EndOfLine = ["\r""\n""\r\n"]

//STATES
// YYINITIAL (default), SHORT_COMMENT starting by **, LONG_COMMENT starting by '' and ending by ''
%xstate YYINITIAL, SHORT_COMMENT, LONG_COMMENT

//USER CODE
%eofval{
    // End of stream
    return new Symbol(LexicalUnit.EOS, yyline, yycolumn);
%eofval}

%% //### Token identification ###

// Reserved keywords
"begin" {return new Symbol(LexicalUnit.BEG, yyline, yycolumn, yytext());}
"end" {return new Symbol(LexicalUnit.END, yyline, yycolumn, yytext());}
"if" {return new Symbol(LexicalUnit.IF, yyline, yycolumn, yytext());}
"then" {return new Symbol(LexicalUnit.THEN, yyline, yycolumn, yytext());}
"else" {return new Symbol(LexicalUnit.ELSE, yyline, yycolumn, yytext());}
"and" {return new Symbol(LexicalUnit.AND, yyline, yycolumn, yytext());}
"or" {return new Symbol(LexicalUnit.OR, yyline, yycolumn, yytext());}
"while" {return new Symbol(LexicalUnit.WHILE, yyline, yycolumn, yytext());}
"do" {return new Symbol(LexicalUnit.DO, yyline, yycolumn, yytext());}
"print" {return new Symbol(LexicalUnit.PRINT, yyline, yycolumn, yytext());}
"read" {return new Symbol(LexicalUnit.READ, yyline, yycolumn, yytext());}

// Structure Symbols
"(" {return new Symbol(LexicalUnit.LPAREN, yyline, yycolumn, yytext());}
")" {return new Symbol(LexicalUnit.RPAREN, yyline, yycolumn, yytext());}
"{" {return new Symbol(LexicalUnit.LBRACK, yyline, yycolumn, yytext());}
"}" {return new Symbol(LexicalUnit.RBRACK, yyline, yycolumn, yytext());}

// Operation Symbols
":=" {return new Symbol(LexicalUnit.ASSIGN, yyline, yycolumn, yytext());}
"-" {return new Symbol(LexicalUnit.MINUS, yyline, yycolumn, yytext());}
"+" {return new Symbol(LexicalUnit.PLUS, yyline, yycolumn, yytext());}
"*" {return new Symbol(LexicalUnit.TIMES, yyline, yycolumn, yytext());}
"/" {return new Symbol(LexicalUnit.DIVIDE, yyline, yycolumn, yytext());}

// Relational Symbols
"=" {return new Symbol(LexicalUnit.EQUAL, yyline, yycolumn, yytext());}
"<" {return new Symbol(LexicalUnit.SMALLER, yyline, yycolumn, yytext());}

// ERE
{VarName} {return new Symbol(LexicalUnit.VARNAME, yyline, yycolumn, yytext());} // "... VarName is a string of letters and digits starting with a lowercase letter"
{Number}  {return new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, yytext());} // "... Number is a string of digits"

// States
<YYINITIAL> {
    "**" {yybegin(SHORT_COMMENT);}
    "''" { yybegin(LONG_COMMENT);}
    [^"**""''"] {} // Ignore all other characters that are not comments or the ERE defined here
}

<SHORT_COMMENT> {
    {EndOfLine} {yybegin(YYINITIAL);}
    . {} // Ignore comments characters
}

<LONG_COMMENT> {
    "''" {yybegin(YYINITIAL);}
    {EndOfLine} {}
    . {} // Ignore comments characters
}
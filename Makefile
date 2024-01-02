# JAR file name and main class
PROJECT_NAME = part2
AUTHORS = "BrennoFerreira_PhilippeMutkowski"
JAR_NAME = $(PROJECT_NAME).jar
MAIN_CLASS = Main

# Test file and arguments
TEST_SOURCE = ./test/sourceFile.pmp

# Determine the operating system
ifeq ($(OS),Windows_NT)
    RM := del /Q .\dist\* .\src\LexicalAnalyzer.java~ .\src\LexicalAnalyzer.java .\src\*.class .\test\*.out .\test\out\*
else
    RM := rm -f dist/* & rm src/LexicalAnalyzer.java~ src/LexicalAnalyzer.java src/*.class test/*.out test/out/*
endif


# Targets
all: $(JAR_NAME)

$(JAR_NAME): compile
	@echo ---Compiling .jar file---
	jar cfe ./dist/$(JAR_NAME) $(MAIN_CLASS) -C ./src .

compile: generate_lexer ./src/*.java
	@echo ---Compiling java classes file---
	javac ./src/*.java

generate_lexer: ./src/*.flex
	@echo ---Generating Lexer---
	jflex -d ./src/ $^

javadoc:
	javadoc -d ./doc/javadoc/ -sourcepath ./src/ ./src/*.java

rebuild:
	@echo ---Rebuilding the project---
	make clean all

test:
	@echo ---Running tests---
# java -jar ./dist/$(JAR_NAME) ./test/all_lexical_units.pmp
# java -jar ./dist/$(JAR_NAME) -wt "./test/out/all_lexical_units.tex" ./test/all_lexical_units.pmp
# java -jar ./dist/$(JAR_NAME)  -wt "./test/out/comments.tex" ./test/comments.pmp
# java -jar ./dist/$(JAR_NAME)  -wt "./test/out/euclid.tex" ./test/euclid.pmp
# java -jar ./dist/$(JAR_NAME)  -wt "./test/out/exprArith.tex" ./test/exprArith.pmp
# java -jar ./dist/$(JAR_NAME)  -wt "./test/out/fibonacci.tex" ./test/fibonacci.pmp
# java -jar ./dist/$(JAR_NAME)  -wt "./test/out/ast2.tex" ./test/AST2.pmp

gen :
	java -jar ./dist/$(JAR_NAME)  -wt "./test/out/gen.tex" ./test/gen.pmp
	llvm-as ./test/out/gen.ll 
	lli ./test/out/gen.bc

testRead :
	java -jar ./dist/$(JAR_NAME)  -wt "./test/out/testRead.tex" ./test/testRead.pmp
	llvm-as ./test/out/testRead.ll 
	lli ./test/out/testRead.bc

testAssign :
	java -jar ./dist/$(JAR_NAME)  -wt "./test/out/testAssign.tex" ./test/testAssign.pmp
	llvm-as ./test/out/testAssign.ll
	lli ./test/out/testAssign.bc

testIf :
	java -jar ./dist/$(JAR_NAME)  -wt "./test/out/testIf.tex" ./test/testIf.pmp
	llvm-as ./test/out/testIf.ll 
	lli ./test/out/testIf.bc

testExprArith :
	java -jar ./dist/$(JAR_NAME)  -wt "./test/out/testExprArith_simpler.tex" ./test/testExprArith_simpler.pmp
	llvm-as ./test/out/testExprArith_simpler.ll 
	lli ./test/out/testExprArith_simpler.bc
	
testWhile :
	java -jar ./dist/$(JAR_NAME)  -wt "./test/out/testWhile.tex" ./test/testWhile.pmp
	llvm-as ./test/out/testWhile.ll 
	lli ./test/out/testWhile.bc

deliverables:
	make rebuild test javadoc

clean:
	@echo ---Cleaning the project---
	$(RM)

.PHONY: all generate_lexer compile test clean

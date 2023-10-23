# JAR file name and main class
JAR_NAME = part1.jar
MAIN_CLASS = Main.main

# Test file and arguments
TEST_SOURCE = ./test/sourceFile.pmp

# Determine the operating system
ifeq ($(OS),Windows_NT)
    RM := del /Q .\dist\* .\src\LexicalAnalyzer.java .\src\LexicalAnalyzer.java~
else
    RM := rm -f dist/* & rm src/LexicalAnalyzer.java src/LexicalAnalyzer.java~
endif


# Targets
all: $(JAR_NAME)

$(JAR_NAME): compile
	@echo ---Compiling .jar file---
	jar cfe ./dist/$(JAR_NAME) $(MAIN_CLASS) -C ./dist/ .

compile: generate_lexer
	@echo ---Compiling java classes file---
	javac -d ./dist/ ./src/*.java

generate_lexer: ./src/*.flex
	@echo ---Generating Lexer---
	jflex -d ./src/ $^

# test: $(JAR_NAME)
# 	java -cp $(JAR_NAME) $(MAIN_CLASS) $(TEST_SOURCE)

test:
	@echo ---Running tests---
	jflex src/LexicalAnalyzer.flex
	javac src/LexicalAnalyzer.java

clean:
	@echo ---Cleaning the project---
	$(RM)



.PHONY: all generate_lexer compile test clean

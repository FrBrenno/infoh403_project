# JAR file name and main class
JAR_NAME = part1.jar
MAIN_CLASS = Main

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
	jar cfe ./dist/$(JAR_NAME) $(MAIN_CLASS) -C ./src .

compile: generate_lexer ./src/*.java
	@echo ---Compiling java classes file---
	javac ./src/*.java

generate_lexer: ./src/*.flex
	@echo ---Generating Lexer---
	jflex -d ./src/ $^

# test: $(JAR_NAME)
# 	java -cp $(JAR_NAME) $(MAIN_CLASS) $(TEST_SOURCE)

test:
	@echo ---Running tests---
	java -jar ./dist/$(JAR_NAME) ./test/sourceFile.pmp

clean:
	@echo ---Cleaning the project---
	$(RM)

rebuild:
	@echo ---Rebuilding the project---
	make clean $(JAR_NAME)

.PHONY: all generate_lexer compile test clean

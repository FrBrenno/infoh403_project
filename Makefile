# JAR file name and main class
PROJECT_NAME = part1
AUTHORS = "BrennoFerreira_PhilippeMutkowski"
JAR_NAME = $(PROJECT_NAME).jar
MAIN_CLASS = Main

# Test file and arguments
TEST_SOURCE = ./test/sourceFile.pmp

# Determine the operating system
ifeq ($(OS),Windows_NT)
    RM := del /Q .\dist\* .\src\LexicalAnalyzer.java .\src\LexicalAnalyzer.java~ .\test\*.out
else
    RM := rm -f dist/* & rm src/LexicalAnalyzer.java~ src/*.class
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
	javadoc -d ./doc/ -sourcepath ./src/ ./src/*.java

rebuild:
	@echo ---Rebuilding the project---
	make clean all

test:
	@echo ---Running tests---
	java -jar ./dist/$(JAR_NAME) ./test/*.pmp

deliverables:
	make rebuild test javadoc

clean:
	@echo ---Cleaning the project---
	$(RM)

.PHONY: all generate_lexer compile test clean

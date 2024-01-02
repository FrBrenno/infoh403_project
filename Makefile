# JAR file name and main class
PROJECT_NAME = part2
AUTHORS = "BrennoFerreira_PhilippeMutkowski"
JAR_NAME = $(PROJECT_NAME).jar
MAIN_CLASS = Main

# Test file and arguments
TEST_FILES := $(wildcard test/*.pmp)
TEST_NAMES := $(patsubst test/%.pmp,%,$(TEST_FILES))

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

test: $(TEST_NAMES)
# rm -f test/out/*.ll  

$(TEST_NAMES): 
	@echo "Running test $@"
	-java -jar ./dist/$(JAR_NAME) ./test/$@.pmp
	-llvm-as ./test/out/$@.ll
# -lli ./test/out/$@.bc > ./test/out/$@.out
	@echo \

deliverables:
	make rebuild test javadoc

clean:
	@echo ---Cleaning the project---
	$(RM)

.PHONY: all generate_lexer compile test clean

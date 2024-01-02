# JAR file name and main class
PROJECT_NAME = part2
AUTHORS = "BrennoFerreira_PhilippeMutkowski"
JAR_NAME = $(PROJECT_NAME).jar
MAIN_CLASS = Main

# Test file and arguments
OUT_DIR = ./test/out
TEST_DIR = ./test
TEST_FILES := $(wildcard $(TEST_DIR)/*.pmp)
TEX_FILES := $(patsubst $(TEST_DIR)/%.pmp, $(OUT_DIR)/%.tex, $(TEST_FILES))


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

test: $(TEX_FILES)

$(OUT_DIR)/%.tex: $(TEST_DIR)/%.pmp
	@echo ---Testing $<---
	-java -jar ./dist/$(JAR_NAME) -wt $@ $<
	-llvm-as $(OUT_DIR)/$*.ll -o $(OUT_DIR)/$*.bc
	-lli $(OUT_DIR)/$*.bc > $(OUT_DIR)/$*.out
	@echo \
	
deliverables:
	make rebuild test javadoc

clean:
	@echo ---Cleaning the project---
	$(RM)

.PHONY: all generate_lexer compile test clean

# JAR file name and main class
JAR_NAME = part1.jar
MAIN_CLASS = PascalMaisPresqueLexer.main

# Test file and arguments
TEST_SOURCE = ./test/sourceFile.pmp

# Targets
all: $(JAR_NAME)

$(JAR_NAME): compile
	jar cfe $@ $(MAIN_CLASS) -C ./dist/ .

compile: ./src/*.java generate_lexer
	javac -d ./dist/ $^

generate_lexer: ./src/*.flex
	jflex -d ./src/ $^

# test: $(JAR_NAME)
# 	java -cp $(JAR_NAME) $(MAIN_CLASS) $(TEST_SOURCE)

test: 
	jflex src/LexicalAnalyzer.flex
	javac src/LexicalAnalyzer.java

clean:
	rm -rf ./src/PascalMaisPresqueLexer.java 
	rm -rf ./src/PMPLexer.java 
	rm -rf ./src/PMPLexer.java~
# lignes au dessus à retirer une fois que c'est bon
	rm -rf dist/*.class $(JAR_NAME)
	rm -rf src/LexicalAnalyzer.java 
	rm -rf src/LexicalAnalyzer.java~
	rm -rf src/*.class

.PHONY: all generate_lexer compile test clean

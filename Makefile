# JAR file name and main class
JAR_NAME = part1.jar
MAIN_CLASS = PascalMaisPresqueLexer.Main

# Test file and arguments
TEST_SOURCE = ./test/sourceFile.pmp

# Targets
all: $(JAR_NAME)

$(JAR_NAME): compile
	jar cfe $@ $(MAIN_CLASS) -C ./dist/ .

compile: ./src/*.java generate_lexer
	javac -d ./dist/ $^

generate_lexer: ./src/*.jflex
	jflex -d ./src/ $^

test: $(JAR_NAME)
	java -cp $(JAR_NAME) $(MAIN_CLASS) $(TEST_SOURCE)

clean:
	rm -rf ./dist/*.class $(JAR_NAME)

.PHONY: all generate_lexer compile test clean

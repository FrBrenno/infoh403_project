public class LLVMGenerator {
    AST ast;
    StringBuilder code ;

    public LLVMGenerator(AST ast) {
        this.ast = ast;
        this.code = new StringBuilder();
    }

    public StringBuilder getCode() {
        return code;
    }

    public void generate() {
        addBasicFunctions();
        //readInt
        //print
        //readVar
        code.append("define i32 @main() {\n");
        processProgram(ast.getChildren().get(0));
        
    }

    private void processProgram(ParseTree ast) {
        // écrire des trucs ? dans la fonction direct ?
        switch(ast.getLabel().getType()){
            case ASSIGN:
                processAssign(ast.getChildren().get(0));
                break;
            case IF:
                processIf(ast.getChildren().get(0));
                break;
            case WHILE:
                processWhile(ast.getChildren().get(0));
                break;
            case PRINT:
                processPrint(ast.getChildren().get(0));
                break;
            case READ:
                processRead(ast.getChildren().get(0));
                break;
            case INSTLIST:
                processInstList(ast.getChildren().get(0));
                break;
            
            default:
                break;
        }
    }

    private void processInstList(ParseTree parseTree) {
    }

    private void processRead(ParseTree parseTree) {
    }

    private void processPrint(ParseTree parseTree) {
    }

    private void processWhile(ParseTree parseTree) {
    }

    private void processIf(ParseTree parseTree) {
    }

    private void processAssign(ParseTree parseTree) {
    }

    private void addBasicFunctions() {
        code.append("declare i32 @getchar()\n");
        // ............................;
        // ............................;
        // ............................;
        // #todo add basic functions
        // readint 
        // print
    }
}

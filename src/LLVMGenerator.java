public class LLVMGenerator {
    AST ast;
    StringBuilder code ;
    Integer lastVarCount = 0;
    Integer varCount = 1;

    public LLVMGenerator() {
        this.code = new StringBuilder();
    }

    private void incrVarCount() {
        varCount++;
        lastVarCount++ ;
    }
    private void resetVarCount() {
        varCount = 0;
    }

    private void DEBUGshowAST(String message, ParseTree myAst) {
        System.out.println("________________" + message + "________________");
 
        for (ParseTree child : myAst.getChildren()) {
            System.out.println("| "+child.getLabel().getType());
        }
        System.out.println("_______________________________________________\n");
    }

    public StringBuilder getCode() {
        return code;
    }

    public void generate(ParseTree ast) {
        addBasicFunctions();
        code.append("define i32 @main() {\n");
        processProgram(ast);
        code.append("   ret i32 0\n}");
        
    }

    private void processProgram(ParseTree ast) {
        // écrire des trucs ? dans la fonction direct ?$
        for (ParseTree child : ast.getChildren()) {
            processInstList(child);
        }
    }

    private void processInstList(ParseTree ast) {
        // DEBUGshowAST("processInstList", ast);
        for (ParseTree child : ast.getChildren()) {
            switch(child.getLabel().getType()){
                case ASSIGN:
                    System.out.println("trouvé le assign");
                    processAssign(child);
                    break;
                case IF:
                    processIf(ast);
                    break;
                case WHILE:
                    processWhile(ast);
                    break;
                case PRINT:
                    processPrint(child);
                    break;
                case READ:
                    processRead(child);
                    break;
                case INSTLIST:
                    processInstList(child);
                    break;
                default:
                    System.out.println("inside InstList default");
        }
        }
        
        
    }
    
    private void processAssign(ParseTree ast) {
        String varname = ast.getChildren().get(0).getLabel().getValue().toString();
        processExprArit(ast.getChildren().get(1));
        //    %a = alloca i32, align 4
        code.append("   %"+varname+ " = alloca i32 \n");
        code.append("   store i32 %" + varCount.toString() + ", i32* %" + varname + "\n");
        code.append("\n");
        incrVarCount();
    }   

    private void processExprArit(ParseTree ast) {
        for (ParseTree child : ast.getChildren()) {
            switch(child.getLabel().getType()){
            case PRODPRIME:
                processProdPrime(child);
            case EXPRARITPRIME:
                processExprAritPrime(child);    
            default:
                System.out.println("inside exprArith default");
            }
        }
    }

    private void processExprAritPrime(ParseTree ast) {
        String operation = "";
        for (ParseTree child : ast.getChildren()) {
            switch(child.getLabel().getType()){
            case PLUS:
                operation = "add";
                break;
            case MINUS:
                operation = "sub";
                break;
            case PRODPRIME:
                processProdPrime(child);
                break;
            case EXPRARITPRIME:
                processExprAritPrime(child);
                break;
            default:
                System.out.println("inside exprArithPrime default");
            }
        }   

        Integer lastVarCount = varCount - 1;
        Integer lastVarCount2 = varCount - 2;
        code.append("   %"+varCount.toString()+" = "+operation+" i32 %"+ lastVarCount.toString() +", %"+ lastVarCount2.toString()+"\n");
        incrVarCount();
    }

    private void processProdPrime(ParseTree ast) {
        String operation = "";
        for (ParseTree child : ast.getChildren()) {
            switch(child.getLabel().getType()){
            case TIMES:
                operation = "mul";
                break;
            case DIVIDE:
                operation = "sdiv";
                break;
            case NUMBER:
                code.append("   %"+varCount.toString() +" = add i32 0,"+ child.getLabel().getValue().toString()+ "\n");
                break;
            case PRODPRIME:
                processProdPrime(child);
                break;
            default:
                System.out.println("inside prodPrime default");
            }
        }   

        Integer lastVarCount = varCount - 1;
        Integer lastVarCount2 = varCount - 2;
        code.append("   %"+varCount.toString()+" = "+operation+" i32 %"+ lastVarCount.toString() +", %"+ lastVarCount2.toString()+"\n");
        incrVarCount();
    }

    private void processRead(ParseTree ast) {
        String varname = ast.getChildren().get(0).getLabel().getValue().toString();
        code.append("   %"+varCount.toString()+" = call i32 @readInt()\n");

        // Partie assign
        code.append("   %"+varname+ " = alloca i32 \n");
        code.append("   store i32 %" + varCount.toString() + ", i32* %" + varname + "\n");
        code.append("\n");
        incrVarCount();
    }

    private void processPrint(ParseTree ast) {
        String varname = ast.getChildren().get(0).getLabel().getValue().toString();
        code.append("   %"+varCount.toString() +" = load i32, i32* %"+varname+"\n");
        code.append("   call void @println(i32 %"+varCount.toString()+")\n");
        code.append("\n");
        incrVarCount();
    }

    private void processWhile(ParseTree ast) {
    }

    private void processIf(ParseTree ast) {
    }

    private void addBasicFunctions() {
        code.append("@.strR = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1 \n");
        code.append("define i32 @readInt() #0 {\n" + //
            "   %x = alloca i32, align 4\n" + //
            "   %1 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %x)\n" + //
            "   %2 = load i32, i32* %x, align 4\n" + //
            "   ret i32 %2\n" + //
            "}\n"+ //
            "\n");

        code.append("declare i32 @scanf(i8*, ...) #1\n") ; 

        code.append("@.strP = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\", align 1\n");
        
        code.append("define void @println(i32 %x) #0 {\n" + //
            "   %1 = alloca i32, align 4\n" + //
            "   store i32 %x, i32* %1, align 4\n"+ //
            "   %2 = load i32, i32* %1, align 4\n"+ //
            "   %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)\n"+ //
            "   ret void\n"+ //
            "}\n" +//
            "\n");

        code.append("declare i32 @printf(i8*, ...) #1\n" );

    }
}

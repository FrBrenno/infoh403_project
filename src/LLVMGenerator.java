import java.util.ArrayList;

public class LLVMGenerator {
    AST ast;
    StringBuilder code ;

    Integer varCount = 1;
    Integer ifCount = 0;
    ArrayList<String> varNames = new ArrayList<String>();

    public LLVMGenerator() {
        this.code = new StringBuilder();
    }

    private void incrVarCount() {
        varCount++;
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


    private Boolean hasExprAritPrime(ParseTree ast) {
        for (ParseTree child : ast.getChildren()) {
            if (child.getLabel().getType() == LexicalUnit.EXPRARITPRIME && child.getLabel().getType() == LexicalUnit.PRODPRIME) {
                return true;
            }
            else if (child.getLabel().getType() == LexicalUnit.EXPRARITPRIME) {
                return true;
            }
            else if (child.getLabel().getType() == LexicalUnit.PRODPRIME) {
                return true;
            }
        }
        return false;
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
                    processAssign(child);
                    break;
                case IF:
                    processIf(child);
                    break;
                case WHILE:
                    processWhile(child);
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
        if (!varNames.contains(varname)) {
            varNames.add(varname);
            code.append("   %"+varname+ " = alloca i32 \n");
        }
        Integer lastvar = varCount - 1;
        code.append("   store i32 %" + lastvar.toString() + ", i32* %" + varname + "\n");
        code.append("\n");
    }   

    private void processExprArit(ParseTree ast) {
        String operation = "";
        Integer first_var = 0;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case PROD:
                    processProd(child);
                    if (operation == "") {
                        first_var = varCount - 1;
                    }
                    else {
                        Integer second_var = varCount - 1;
                        code.append("   %"+varCount.toString()+" = "+operation+" i32 %"+first_var.toString()+", %"+second_var.toString()+"\n");
                        first_var = varCount;
                        incrVarCount();
                    }
                    break;
                case PLUS:
                    operation = "add";
                    break;
                case MINUS:
                    operation = "sub";
                    break;
                default:
                    break;
            }
        }
    }
    
    private void processProd(ParseTree ast) {
        String operation = "";
        Integer first_var = 0;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case ATOM:
                    processAtom(child);
                    if (operation == "") {
                        first_var = varCount - 1;
                    }
                    else {
                        Integer second_var = varCount - 1;
                        code.append("   %"+varCount.toString()+" = "+operation+" i32 %"+first_var.toString()+", %"+second_var.toString()+"\n");
                        first_var = varCount;
                        incrVarCount();
                    }
                    break;
                case TIMES:
                    operation = "mul";
                    break;
                case DIVIDE:
                    operation = "sdiv";
                    break;
                default:
                    break;
            }
        }
    }

    private void processAtom(ParseTree ast) {
        boolean is_unary_minus = false;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case ATOM:
                    processAtom(child);
                    break;
                case EXPRARIT:
                    processExprArit(child);
                    break;
                case MINUS:
                    is_unary_minus = true;
                    break;
                case NUMBER:
                    code.append("   %"+varCount.toString()+" = add i32 0, "+child.getLabel().getValue().toString()+"\n");
                    incrVarCount();
                    break;
                case VARNAME:
                    code.append("   %"+varCount.toString()+" = load i32, i32* %"+child.getLabel().getValue().toString()+"\n");
                    incrVarCount();
                    break;
                default:
                    break;
            }
        }
        if (is_unary_minus) {
            Integer lastvar = varCount - 1;
            code.append("   %"+varCount.toString()+" = mul i32 -1, %"+lastvar.toString()+"\n");
            incrVarCount();
        }
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

    
    private void processIf(ParseTree ast) {
        ifCount++;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case COND:  // Condition
                    processCond(child);
                    Integer lastvar = varCount - 1;
                    code.append("   br i1 %"+lastvar.toString()+", label %if_true_"+ifCount.toString()+", label %if_false_"+ifCount.toString()+"\n\n");
                    break;
                case INSTLIST: // IF True
                    code.append("   if_true_"+ifCount.toString()+":\n");
                    processInstList(child);
                    code.append("   br label %if_end_"+ifCount.toString()+"\n\n");
                    break;
                case ELSETAIL: // IF False
                    code.append("   if_false_"+ifCount.toString()+":\n");
                    processElseTail(child);
                    code.append("   br label %if_end_"+ifCount.toString()+"\n\n");
                    break;
                default:
                    break;
            }
        }
        code.append("   if_end_"+ifCount.toString()+":\n");
    }

    private void processCond(ParseTree ast) {
        String operation = "";
        Integer left = null;
        Integer right = null;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case EXPRARIT:
                    processExprArit(child);
                    if (operation == "") {
                        left = varCount - 1;
                    }
                    else {
                        right = varCount - 1;
                    }

                    break;
                case SMALLER:
                    operation = "slt";
                    break;
                case EQUAL:
                    operation = "eq";
                    break;
                default:
                    break;
            }
        }
        code.append("   %"+varCount.toString()+" = icmp "+operation+" i32 %"+left.toString()+", %"+right.toString()+"\n");
        incrVarCount();
    }

    private void processElseTail(ParseTree ast) {
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case INSTLIST:
                    processInstList(child);
                    break;
                default:
                    break;
            }
        }
    }

    private void processWhile(ParseTree ast) {
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

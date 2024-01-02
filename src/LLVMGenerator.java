import java.util.ArrayList;

public class LLVMGenerator {
    AST ast;
    StringBuilder code ;

    Integer varCount = 1;
    Integer lastvar = 0;
    Integer ifCount = 0;
    Integer whileCount = 0;
    ArrayList<String> varNames = new ArrayList<String>();
    Integer OFFSET = 705;

    public LLVMGenerator(ParseTree ast) {
        this.ast = ast;
        this.code = new StringBuilder();
    }

    private void incrVarCount() {
        varCount++;
        lastvar = varCount - 1;
    }

    public StringBuilder getCode() {
        return code;
    }
    
    public void generate() {
        addBasicFunctions();
        code.append("define i32 @main() {\n");
        OFFSET = code.length();
        processProgram(ast);
        code.append("   ret i32 0\n}");
    }

    private void processProgram(ParseTree ast) {
        for (ParseTree child : ast.getChildren()) {
            processInstList(child);
        }
    }

    private void processInstList(ParseTree ast) {
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
                    break;
            }
        }
    }
    
    private boolean treeHasVariable(ParseTree ast, String varname) {
        if (ast.getLabel().getType() == LexicalUnit.VARNAME && ast.getLabel().getValue().toString().equals(varname)) {
            return true;
        }
        for (ParseTree child : ast.getChildren()) {
            if (treeHasVariable(child, varname)) {
                return true;
            }
        }
        return false;
    }

    private void processAssign(ParseTree ast) {
        String varname = ast.getChildren().get(0).getLabel().getValue().toString();
        processExprArit(ast.getChildren().get(1));
        if (!varNames.contains(varname)) {
            if (treeHasVariable(ast.getChildren().get(1), varname)) {
                System.out.println("Compilation error: variable "+varname+" is not initialized.");
                System.exit(1);
            }
            varNames.add(varname);
            String alloca = "   %"+varname+ " = alloca i32 \n";
            code.insert(OFFSET, alloca);
            OFFSET += alloca.length();
        }
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
                    if (operation == "") {          // Check si c'est le premier elem (operation pas encore traitée => d'office elem de gauche)
                        first_var = lastvar;
                    }
                    else {
                        Integer second_var = lastvar;
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
                    if (operation == "") {    // Check si c'est le premier elem (operation pas encore traitée => d'office elem de gauche)
                        first_var = lastvar;
                    }
                    else {
                        Integer second_var = lastvar;
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
            code.append("   %"+varCount.toString()+" = mul i32 -1, %"+lastvar.toString()+"\n");
            incrVarCount();
        }
    }
    

    private void processRead(ParseTree ast) {
        String varname = ast.getChildren().get(0).getLabel().getValue().toString();
        code.append("   %"+varCount.toString()+" = call i32 @readInt()\n");
        // Partie assign
        if (!varNames.contains(varname)) {
            varNames.add(varname);
            String alloca = "   %"+varname+ " = alloca i32 \n";
            code.insert(OFFSET, alloca);
            OFFSET += alloca.length();
        }
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

    private void processCond(ParseTree ast) {
        String operation = "";
        Integer first_var = 0;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case AND: 
                    processAnd(child);
                    if (operation == "") {          // Check si c'est le premier elem (operation pas encore traitée => d'office elem de gauche)
                        first_var = lastvar;
                    }
                    else {
                        Integer second_var = lastvar;
                        code.append("   %"+varCount.toString()+" = "+operation+" i1 %"+first_var.toString()+", %"+second_var.toString()+"\n");
                        first_var = varCount;
                        incrVarCount();
                    }
                    break;
                case OR:
                    operation = "or";
                    break;
                default:
                    break;
                
            }
        }
    }

    private void processAnd(ParseTree ast) {
        String operation = "";
        Integer first_var = 0;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case CONDATOM:
                    processCondAtom(child);
                    if (operation == "") {          // Check si c'est le premier elem (operation pas encore traitée => d'office elem de gauche)
                        first_var = lastvar;
                    }
                    else {
                        Integer second_var = lastvar;
                        code.append("   %"+varCount.toString()+" = "+operation+" i1 %"+first_var.toString()+", %"+second_var.toString()+"\n");
                        first_var = varCount;
                        incrVarCount();
                    }
                    break ;
                case AND:
                    operation = "and";
                    break;
                default:
                    break;
                }
            }
        }

    private void processCondAtom(ParseTree ast) {
        if (ast.getChildren().size() == 1){
            processCond(ast.getChildren().get(0));
            return;
        }
        
        else{
            String operation = "";
            ParseTree left = ast.getChildren().get(0);
            ParseTree right = ast.getChildren().get(2);

            if (ast.getChildren().get(1).getLabel().getType() == LexicalUnit.EQUAL) {
                operation = "eq";
            }
            else if (ast.getChildren().get(1).getLabel().getType() == LexicalUnit.SMALLER){
                operation = "slt";
            }
            processExprArit(left);
            Integer leftVar = lastvar;
            processExprArit(right);
            Integer rightVar = lastvar;

            code.append("   %"+varCount.toString()+" = icmp "+operation+" i32 %"+leftVar.toString()+", %"+rightVar.toString()+"\n");
            incrVarCount();
        }
    }

    private void processIf(ParseTree ast) {
        ifCount++;
        Integer memory_if_count = ifCount;
        String if_true_name = "if_true_"+memory_if_count.toString();
        String if_false_name = "if_false_"+memory_if_count.toString();
        String if_end_name = "if_end_"+memory_if_count.toString();

        // CONDITION
        ParseTree cond = ast.getChildren().get(0);
        processCond(cond);
        code.append("   br i1 %"+lastvar.toString()+", label %"+if_true_name+", label %"+if_false_name+"\n\n");

        // IF TRUE
        ParseTree ifTrueNode = ast.getChildren().get(1);
        code.append("   "+if_true_name+":\n");
        if (ifTrueNode.getLabel().getType() == LexicalUnit.INSTLIST)
        {
            processInstList(ifTrueNode);
        }else
        {
            processInstList(ast);
        }
        code.append("   br label %"+ if_end_name +"\n\n");

        // ELSE
        code.append("   "+if_false_name+":\n");
        if (ast.getChildren().size() == 3)
        {
            ParseTree elseNode = ast.getChildren().get(2);
            processElseTail(elseNode);
        }
        code.append("   br label %"+ if_end_name +"\n\n");
        code.append("   "+if_end_name+":\n");
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
        whileCount++;
        Integer memory_while_count = whileCount;
        String whileLoopName = "while_loop_"+memory_while_count.toString();
        String whileEndName = "while_end_"+memory_while_count.toString();
        // CONDITION
        ParseTree cond = ast.getChildren().get(0);
        processCond(cond);
        code.append("   br i1 %"+lastvar.toString()+", label %"+whileLoopName+", label %"+whileEndName+"\n\n");

        // WHILE LOOP
        ParseTree WhileLoopNode = ast.getChildren().get(1);
        code.append("   "+whileLoopName+":\n");
        if (WhileLoopNode.getLabel().getType() == LexicalUnit.INSTLIST)
        {
            processInstList(WhileLoopNode);
        }else
        {
            processInstList(ast);
        }
        // RE-EVALUATE CONDITION 
        processCond(cond);
        code.append("   br i1 %"+lastvar.toString()+", label %"+whileLoopName+", label %"+whileEndName+"\n\n");
        code.append("   "+whileEndName+":\n");
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

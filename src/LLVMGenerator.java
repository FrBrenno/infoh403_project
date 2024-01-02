import java.util.ArrayList;

public class LLVMGenerator {
    ParseTree ast;
    StringBuilder code ;

    Integer varCount = 1;
    Integer lastVar = 0;
    Integer ifCount = 0;
    Integer whileCount = 0;
    ArrayList<String> varNames = new ArrayList<String>(); // List of all the variables names (used to check if a variable is initialized)
    Integer OFFSET ;                                      // Offset used to know where to alloca variables


    public LLVMGenerator(ParseTree ast) {
        this.ast = ast;
        this.code = new StringBuilder();
    }

    private void incrVarCount() {
        varCount++;
        lastVar = varCount - 1;
    }

    /**
     * Returns the generated code 
     * @return Code
     */
    public StringBuilder getCode() {
        return code;
    }
    
    /**
     * Generates the code
     * processProgram is called first
     */
    public void generate() {
        addBasicFunctions();
        code.append("define i32 @main() {\n");
        OFFSET = code.length();
        processProgram(ast);
        code.append("   ret i32 0\n}");
    }

    /**
     * Processes the program node
     */
    private void processProgram(ParseTree ast) {
        for (ParseTree child : ast.getChildren()) {
            processInstList(child);
        }
    }

    /**
     * Processes the instruction list node
     * Checks the type of the instruction and calls the corresponding function
     */
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
    
    /**
     * Checks if a tree contains a specific variable name
     * Used to check if a variable is initialized
     * @param ast Tree to check
     * @param varname Variable name to check
     * @return True if the tree contains the variable, false otherwise
     */
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

    /**
     * Processes the assign node
     * Throws an error when initializing a variable with itself (x:=x+1)
     */
    private void processAssign(ParseTree ast) {
        String varname = ast.getChildren().get(0).getLabel().getValue().toString();
        processExprArit(ast.getChildren().get(1));

        if (!varNames.contains(varname)) {
            if (treeHasVariable(ast.getChildren().get(1), varname)) {
                System.out.println("Illegal action: variable "+varname+" is not initialized.");
                System.exit(1);
            }
            varNames.add(varname);
            String alloca = "   %"+varname+ " = alloca i32 \n";
            code.insert(OFFSET, alloca);
            OFFSET += alloca.length();
        }
        code.append("   store i32 %" + lastVar.toString() + ", i32* %" + varname + "\n");
        code.append("\n");
    }   

    /**
     * Processes the arithmetic expression node
     * Checks the type of the expression and calls the corresponding function
     * The childs are processed from left to right, so
     * when the operator is not defined yet, it's the left element,
     * then when operator is defined, it's the right element and
     * the two elements are added or substracted.
     */
    private void processExprArit(ParseTree ast) {
        String operation = "";
        Integer leftVar = 0;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case PROD:
                    processProd(child);
                    if (operation == "") {          // Check if first elem (operator not processed => d'office left elem)
                        leftVar = lastVar;
                    }
                    else {
                        Integer rightVar = lastVar;
                        code.append("   %"+varCount.toString()+" = "+operation+" i32 %"+leftVar.toString()+", %"+rightVar.toString()+"\n");
                        leftVar = varCount;
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
    
    /**
     * Processes the product node
     * Checks the type of the expression and calls the corresponding function
     * The childs are processed from left to right, so
     * when the operator is not defined yet, it's the left element,
     * then when operator is defined, it's the right element and 
     * the two elements are multiplied or divided.
     */
    private void processProd(ParseTree ast) {
        String operation = "";
        Integer leftVar = 0;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case ATOM:
                    processAtom(child);
                    if (operation == "") {          // Check if first elem (operator not processed => d'office left elem)
                        leftVar = lastVar;
                    }
                    else {
                        Integer rightVar = lastVar;
                        code.append("   %"+varCount.toString()+" = "+operation+" i32 %"+leftVar.toString()+", %"+rightVar.toString()+"\n");
                        leftVar = varCount;
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

    /**
     * Processes the atom node
     * Checks the type of the expression and calls the corresponding function
     */
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
            code.append("   %"+varCount.toString()+" = mul i32 -1, %"+lastVar.toString()+"\n");
            incrVarCount();
        }
    }
    

    /**
     * Processes the read node
     * Checks if the variable is already initialized first
     * Similar to assign
     */
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

    /**
     * Processes the print node
     */
    private void processPrint(ParseTree ast) {
        String varname = ast.getChildren().get(0).getLabel().getValue().toString();
        code.append("   %"+varCount.toString() +" = load i32, i32* %"+varname+"\n");
        code.append("   call void @println(i32 %"+varCount.toString()+")\n");
        code.append("\n");
        incrVarCount();
    }

    /**
     * Processes the condition node
     * Checks the type of the expression and calls the corresponding function
     * The childs are processed from left to right, so
     * when the operator is not defined yet, it's the left element,
     * then when operator is defined, it's the right element and 
     * the two elements are compared with "or".
     */
    private void processCond(ParseTree ast) {
        String operation = "";
        Integer leftVar = 0;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case AND: 
                    processAnd(child);
                    if (operation == "") {          // Check si c'est le premier elem (operation pas encore traitée => d'office elem de gauche)
                        leftVar = lastVar;
                    }
                    else {
                        Integer rightVar = lastVar;
                        code.append("   %"+varCount.toString()+" = "+operation+" i1 %"+leftVar.toString()+", %"+rightVar.toString()+"\n");
                        leftVar = varCount;
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

    /**
     * Processes the and node
     * Checks the type of the expression and calls the corresponding function
     * The childs are processed from left to right, so
     * when the operator is not defined yet, it's the left element,
     * then when operator is defined, it's the right element and 
     * the two elements are compared with "and".
     */
    private void processAnd(ParseTree ast) {
        String operation = "";
        Integer first_var = 0;
        for (ParseTree child : ast.getChildren()) {
            switch (child.getLabel().getType()) {
                case CONDATOM:
                    processCondAtom(child);
                    if (operation == "") {          // Check si c'est le premier elem (operation pas encore traitée => d'office elem de gauche)
                        first_var = lastVar;
                    }
                    else {
                        Integer second_var = lastVar;
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

    /**
     * Processes the cond atom node
     * CondAtom has either 3 children (left, operator, right)
     * or 1 child (Cond)
     */
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
            Integer leftVar = lastVar;
            processExprArit(right);
            Integer rightVar = lastVar;

            code.append("   %"+varCount.toString()+" = icmp "+operation+" i32 %"+leftVar.toString()+", %"+rightVar.toString()+"\n");
            incrVarCount();
        }
    }

    /**
     * Processes the if node
     * The if node has either 2 children (cond, if_true_code)
     * or 3 children (cond, if_true_code, else_code)
     * 
     * The else label is always created, but if there is no else_code,
     * it just does nothing and goes to the end label
     */
    private void processIf(ParseTree ast) {
        ifCount++;
        Integer memory_if_count = ifCount;
        String if_true_name = "if_true_"+memory_if_count.toString();
        String if_false_name = "if_false_"+memory_if_count.toString();
        String if_end_name = "if_end_"+memory_if_count.toString();

        // CONDITION
        ParseTree cond = ast.getChildren().get(0);
        processCond(cond);
        code.append("   br i1 %"+lastVar.toString()+", label %"+if_true_name+", label %"+if_false_name+"\n\n");

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

    /**
     * Processes the else node
     */
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

    /**
     * Processes the while node
     * The while node has 2 children (cond, while_code)
     */
    private void processWhile(ParseTree ast) {
        whileCount++;
        Integer memory_while_count = whileCount;
        String whileLoopName = "while_loop_"+memory_while_count.toString();
        String whileEndName = "while_end_"+memory_while_count.toString();
        // CONDITION
        ParseTree cond = ast.getChildren().get(0);
        processCond(cond);
        code.append("   br i1 %"+lastVar.toString()+", label %"+whileLoopName+", label %"+whileEndName+"\n\n");

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
        code.append("   br i1 %"+lastVar.toString()+", label %"+whileLoopName+", label %"+whileEndName+"\n\n");
        code.append("   "+whileEndName+":\n");
    }
    
    /**
     * Adds the code for readInt and println functions
     */
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

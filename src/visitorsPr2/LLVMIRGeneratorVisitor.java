package visitorsPr2;
import visitor.GJDepthFirst;
import symbolTable.*;
import syntaxtree.*;

import java.util.List;
import java.util.Map;
import static codeGenerator.CodeGenerator.*;


public class LLVMIRGeneratorVisitor extends GJDepthFirst <String, String[]> {

    public LLVMIRGeneratorVisitor() {
        this.defineVTables();
        this.defineHelperMethods();
    }


    private void defineHelperMethods(){
        emit("declare i8* @calloc(i32, i32)\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "declare void @exit(i32)\n" +
                "\n" +
                "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
                "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
                "define void @print_int(i32 %i) {\n" +
                "    %_str = bitcast [4 x i8]* @_cint to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
                "    ret void\n" +
                "}\n" +
                "\n" +
                "define void @throw_oob() {\n" +
                "    %_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str)\n" +
                "    call void @exit(i32 1)\n" +
                "    ret void\n" +
                "}\n");
    }

    private void defineVTables(){
        String outputStr = null;
        for(Map.Entry<String, AbstractType> entry : SymbolTable.symbolTable.entrySet()){
            Klass curKlass = (Klass)entry.getValue();
            outputStr = "@."+curKlass.getName()+"_vtable = global [" + curKlass.getNumberOfMethods() + " x i8*] [";
            if(curKlass.getNumberOfMethods() > 0 && !curKlass.classHasMain()){
                for(int i=0;i<curKlass.getMethods().size();i++){
                    if(i >= 1){
                        outputStr = outputStr + ", ";
                    }
                    outputStr = outputStr + "i8* bitcast (i32 (i8*, i32)* @"+ curKlass.getName() +"."
                            + curKlass.getMethods().get(i).getName() + " to i8*)";
                }
            }
            outputStr = outputStr + "]\n";
            emit(outputStr);
        }
        emit("\n\n");
    }


    //
    // User-generated visitor methods below
    //

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n, String[] argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n, String[] argu) {
        String _ret=null;
        String[] idNames = new String[2];idNames[0] = n.f1.accept(this, argu);
        idNames[1] = n.f6.toString();

        emit("define i32 @"+idNames[1]+"() {\n");
        if(n.f14.present())
            n.f14.accept(this, argu);
        if(n.f15.present())
            n.f15.accept(this, idNames);
        emit("\n\tret i32 0\n}\n\n");

        //reinitializeTempRegisters();

        return _ret;
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public String visit(TypeDeclaration n, String[] argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, String[] argu) {
        String _ret=null;
        String[] idNames = new String[2];
        idNames[0] = n.f1.accept(this, argu);
        if(n.f3.present())
            //n.f3.accept(this, argu);
        if(n.f4.present())
            n.f4.accept(this, idNames);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n, String[] argu) {
        String _ret=null;
        String[] idNames = new String[2];
        idNames[0] = n.f1.accept(this, argu);
        if (n.f5.present())
            //n.f5.accept(this, argu);
        if(n.f6.present())
            n.f6.accept(this, idNames);
        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String[] argu) {
        String _ret=null;
        String type, identifier;
        type = n.f0.accept(this, argu);
        identifier = n.f1.accept(this, argu);
        emit("\t%"+identifier+" = alloca " + get_LLVM_type(type)+"\n\n");
        return _ret;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, String[] argu) {
        String _ret=null;
        String type = null;
        String returnType = null;

        type = n.f1.accept(this, argu);

        /*argu is coming either from classDeclaration
         *or classExtendsDeclaration
         * Storing in argu the method name for later use
         * */

        argu[1] = n.f2.accept(this, argu);
        emit("define "+ get_LLVM_type(type) +" @"+argu[0]+"."+argu[1]+"( i8* %this");

        List <AbstractType> formalParameters = OffsetSymbolTable.getEntryClass(argu[0]).getMethod(argu[1]).getArguments();
        for (int i = 0; i < formalParameters.size(); i++){
            emit(", "+ get_LLVM_type(formalParameters.get(i).getFieldTypeName())
                    + " %." + formalParameters.get(i).getName());
        }
        emit(") {\n");

        if(n.f4.present())
            n.f4.accept(this, argu);

        if(n.f7.present())
            n.f7.accept(this, argu);
        if(n.f8.present())
            n.f8.accept(this, argu);

        returnType = n.f10.accept(this, argu);

        emit("\tret "+returnType+"\n}\n\n");

        reinitializeTempRegisters();
        reinitializeLabels();

        return _ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList n, String[] argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, String[] argu) {
        String _ret=null;
        String type, identifier;

        type = n.f0.accept(this, argu);
        identifier = n.f1.accept(this, argu);

        emit("\t%"+identifier+" = alloca " + get_LLVM_type(type) +"\n");
        emit("\tstore " + get_LLVM_type(type) + " %." + identifier + ", "
                + get_LLVM_type(type) + "* %" + identifier + "\n");

        return _ret;
    }

    /**
     * f0 -> ( FormalParameterTerm() )*
     */
    public String visit(FormalParameterTail n, String[] argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public String visit(FormalParameterTerm n, String[] argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type n, String[] argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> BooleanArrayType()
     *       | IntegerArrayType()
     */
    public String visit(ArrayType n, String[] argu) {
        return n.f0.accept(this, argu);
    }

    /**-
     * f0 -> "boolean"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(BooleanArrayType n, String[] argu) { return n.f0.toString()+n.f1.toString()+n.f2.toString(); }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(IntegerArrayType n, String[] argu) { return n.f0.toString()+n.f1.toString()+n.f2.toString(); }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, String[] argu) { return n.f0.toString(); }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, String[] argu) { return n.f0.toString(); }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public String visit(Statement n, String[] argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block n, String[] argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String[] argu) {
        String _ret=null;
        String identifier;
        String expression;
        String reg1, reg2;

        identifier = n.f0.accept(this, argu);
        expression = n.f2.accept(this, argu);

        String type = OffsetSymbolTable.getEntryClass(argu[0]).getMethod(argu[1]).getAbstractTypes(identifier);
        if(type != null){
            emit("\tstore " + expression + ", " + get_LLVM_type(type) + "* %" +identifier+ "\n\n");
        }else {
            reg1 = new_temp();
            String className = SymbolTable.getFieldClassName(identifier);
            type = OffsetSymbolTable.getEntryClass(argu[0]).getField(identifier).getFieldTypeName();
            int identifierOffset = OffsetSymbolTable.getFieldOffset(className, identifier) + 8;
            emit("\t"+ reg1 + " = getelementptr " + get_LLVM_type(identifier) + ", " + get_LLVM_type(identifier)
                    + "* %this, i32 " + identifierOffset + "\n");
            reg2 = new_temp();
            emit("\t" + reg2 + " = bitcast " + get_LLVM_type(identifier) + "* " + reg1 + " to " + get_LLVM_type(type) +"*\n");
            emit("\tstore " + expression + ", i32** " + reg2 + "\n\n");
        }
        return identifier;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, String[] argu) {
        String _ret=null;
        String identifier, expr1, expr2, oobLabel_OK, oobLabel_ERR;
        String reg1, reg2, reg3, reg4, reg5;

        identifier = n.f0.accept(this, argu);
        expr1 = n.f2.accept(this, argu);
        expr2 = n.f5.accept(this, argu);

        /*get the array from either the method variables/arguments or from the class fields*/
        String type = OffsetSymbolTable.getEntryClass(argu[0]).getMethod(argu[1]).getAbstractTypes(identifier);
        reg1 = new_temp();
        if(type != null){
            emit("\t" + reg1 + "= load i32*, i32** %" +identifier+ "\n\n");
        }else {
            String className;
            className = SymbolTable.getFieldClassName(identifier);
            type = OffsetSymbolTable.getEntryClass(argu[0]).getField(identifier).getFieldTypeName();
            int identifierOffset = OffsetSymbolTable.getFieldOffset(className, identifier) + 8;
            emit("\t"+ reg1 + " = getelementptr " + get_LLVM_type(identifier) + ", " + get_LLVM_type(identifier)
                    + "* %this, i32 " + identifierOffset + "\n");
            reg2 = new_temp();
            emit("\t" + reg2 + " = bitcast " + get_LLVM_type(identifier) + "* " + reg1 + " to " + get_LLVM_type(type) +"*\n");
            /*the array is in reg1*/
            reg1 = new_temp();
            emit("\t" + reg1 + "= load i32*, i32** " +reg2+ "----\n");
        }

        reg2 = new_temp();
        reg3 = new_temp();
        reg4 = new_temp();
        reg5 = new_temp();
        oobLabel_OK = newOOBLabel();
        oobLabel_ERR = newOOBLabel();

        emit("\t" + reg2 + " = load i32, i32* " + reg1 + "\n");
        emit("\t" + reg3 + " = icmp sge " + expr1 + ", 0\n");  /*check if the array index is greter than zero*/
        emit("\t" + reg4 + " = icmp slt " + expr1 + ", " + reg2 + "\n"); /*check that the index is less than the array size*/
        emit("\t" + reg5 + " and i1 " + reg3 + ", " + reg4 + "\n\tbr i1 " + reg5 + ", label %" + oobLabel_OK + ", label %" + oobLabel_ERR + "\n\n");
        emit(oobLabel_OK + ":\n");
        reg4 = new_temp();
        emit("\t" + reg4 + " = add " + expr1 + ", 1\n");
        reg3 = new_temp();
        emit("\t" + reg3+ " = getelementptr i32, i32* " + reg1 + ", i32 " + reg4 + "\n");
        reg5 = new_temp();
        oobLabel_OK = newOOBLabel();
        emit("\tstore " + expr2 + ", i32* " + reg3 + "\n\tbr label %" + oobLabel_OK + "\n\n");
        emit(oobLabel_ERR + ":\n\tcall void @throw_oob()\n\tbr label %" + oobLabel_OK + "\n\n");
        emit(oobLabel_OK + ":\n");

        return _ret;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, String[] argu) {
        String _ret=null;
        String expr=null;
        String ifElse, ifThen, ifEnd;

        ifElse = newIfElseLabel();
        ifThen = newIfThenLabel();
        ifEnd = newIfEndLabel();

        expr = n.f2.accept(this, argu);

        emit("\tbr i1 " + expr + ", label %" + ifThen + ", label %" + ifElse + "\n\n");
        emit(ifElse + ":\n\n");

        n.f4.accept(this, argu);

        emit("\n\tbr label %" + ifEnd + "\n\n");
        emit(ifThen + ":\n\n");

        n.f6.accept(this, argu);

        emit("\n\tbr label %" + ifEnd + "\n\n");
        emit(ifEnd + ":\n\n");

        return _ret;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, String[] argu) {
        String _ret=null;
        String expr;
        String whileLabel1 = newLoopLabel();
        String whileLabel2 = newLoopLabel();
        String whileLabelEnd = newLoopLabel();

        emit("\tbr label %" + whileLabel1 + "\n\n");
        emit(whileLabel1 + ":\n");

        expr = n.f2.accept(this, argu);

        emit("\tbr " + expr + ", label %" + whileLabel2 + ", label %" + whileLabelEnd + "\n\n");
        emit(whileLabel2 + ":\n");

        n.f4.accept(this, argu);

        emit("\tbr label %" + whileLabel1 + "\n\n");

        emit(whileLabelEnd + ":\n\n");

        return _ret;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, String[] argu) {
        String _ret=null;
        n.f0.accept(this, argu);
        String expression = n.f2.accept(this, argu);
        emit("\tcall void (i32) @print_int(" + expression + ")\n\n");
        return _ret;
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */
    public String visit(Expression n, String[] argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String[] argu) {
        String _ret=null;
        String clause1, clause2;
        String reg1;
        String label1, label2, label3;

        clause1 = n.f0.accept(this, argu);

        label1 = newLoopLabel();
        label2 = newLoopLabel();
        label3 = newLoopLabel();

        emit("\tbr " + clause1 + ", label %" + label2 + ", label %" + label1 + "\n\n");
        emit(label1 + ":\n\tbr label %" + label3 + "\n\n");

        emit(label2 + ":\n");
        clause2 = n.f2.accept(this, argu);
        emit("\tbr label %" + label3 + "\n\n");

        label2 = newLoopLabel();
        emit(label3 + ":\n\tbr label %" + label2 + "\n\n");

        reg1 = new_temp();

        String[] clauseArray = clause2.split(" ");

        emit(label2 + ":\n\t" + reg1 + " = phi i1 [ 0, %" + label1 + "], [ " + clauseArray[1] + ", %" + label3 + "]\n\n");

        _ret = "i1" + reg1;
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String[] argu) {
        String _ret=null;
        String expr1, expr2;
        String reg = null;

        expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        expr2 = n.f2.accept(this, argu);

        reg = new_temp();
        emit("\t" + reg + " = icmp slt "+ expr1 + ", " + expr2+"\n");

        _ret = "i1" + reg;

        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String[] argu) {
        String _ret=null;
        String expr1, expr2;
        String reg = null;

        expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        expr2 = n.f2.accept(this, argu);

        reg = new_temp();
        emit("\t" + reg + " = add "+ expr1 + ", " + expr2+"\n");

        _ret = "i32 " + reg;

        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String[] argu) {
        String _ret=null;
        String expr1, expr2;
        String reg = null;

        expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        expr2 = n.f2.accept(this, argu);

        reg = new_temp();
        emit("\t" + reg + " = sub "+ expr1 + ", " + expr2+"\n");

        _ret = "i32 " + reg;

        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String[] argu) {
        String _ret=null;
        String expr1, expr2;
        String reg = null;

        expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        expr2 = n.f2.accept(this, argu);

        reg = new_temp();
        emit("\t" + reg + " = mul "+ expr1 + ", " + expr2+"\n");

        _ret = "i32 " + reg;

        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String[] argu) {
        String _ret=null;
        String primaryExpr1;
        String primaryExpr2 = null;
        String reg1, reg2, reg3, reg4 ,oobLabel_OK, oobLabel_ERR;
        primaryExpr1 = n.f0.accept(this, argu);
        primaryExpr2 = n.f2.accept(this, argu);

        reg1 = new_temp();
        reg2 = new_temp();
        reg3 = new_temp();
        reg4 = new_temp();
        oobLabel_OK = newOOBLabel();
        oobLabel_ERR = newOOBLabel();

        emit("\t" + reg1 + " = load i32, " + primaryExpr1 + "\n");
        emit("\t" + reg2 + " = icmp sge " + primaryExpr2 + ", 0\n");  /*check if the array index is greter than zero*/
        emit("\t" + reg3 + " = icmp slt " + primaryExpr2 + ", " + reg1 + "\n"); /*check that the index is less than the array size*/
        emit("\t" + reg4 + " and i1 " + reg2 + ", " + reg3 + "\n\tbr i1 " + reg4 + ", label %" + oobLabel_OK + ", label %" + oobLabel_ERR + "\n\n");
        emit(oobLabel_OK + ":\n");
        reg4 = new_temp();
        emit("\t" + reg4 + " = add " + primaryExpr2 + ", 1\n");
        reg3 = new_temp();
        emit("\t" + reg3 + " = getelementptr i32, i32* " + reg1 + ", i32 " + reg4 + "\n");
        reg4 = new_temp();
        oobLabel_OK = newOOBLabel();
        emit("\t" + reg4 + " = load i32, i32* " + reg3 +"\n\tbr label %" + oobLabel_OK + "\n\n");
        emit(oobLabel_ERR + ":\n\tcall void @throw_oob()\n\tbr label %" + oobLabel_OK + "\n\n");
        emit(oobLabel_OK + ":\n");

        _ret = "i32 " + reg4;

        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String[] argu) {
        String _ret=null;
        String primaryExpr;
        String reg;
        primaryExpr = n.f0.accept(this, argu);
        reg = new_temp();
        emit("\t" + reg + " = load i32, " + primaryExpr);
        _ret = "i32 " + reg;
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, String[] argu) {
        String _ret=null;
        String exprList="";
        String reg1, reg2, type, methodClassName, identifier, primaryExpr;

        String[] primaryExprArgu = new String[3];
        primaryExprArgu[0] = argu[0];
        primaryExprArgu[1] = argu[1];
        primaryExprArgu[2] = null;

        primaryExpr = n.f0.accept(this, primaryExprArgu);
        identifier = n.f2.accept(this, argu);

        methodClassName = SymbolTable.getMethodClassName(identifier);
        int methodOffset = OffsetSymbolTable.getMethodOffset(methodClassName, identifier);
        type = OffsetSymbolTable.getEntryClass(methodClassName).getMethod(identifier).getReturnType().getFieldTypeName();
        List <AbstractType> formalParameters = OffsetSymbolTable.getEntryClass(methodClassName).getMethod(identifier).getArguments();

        /*
         *Produce IR code for MessageSend
         */
        reg1 = new_temp();
        reg2 = new_temp();
        emit("\t; " + methodClassName +"." + identifier + " : "+ methodOffset +"\n");
        emit("\t" + reg1 + " = bitcast " + primaryExpr + " to i8***\n");
        emit("\t" + reg2 + " = load i8**, i8*** " + reg1 + "\n");
        reg1 = new_temp();
        emit("\t" + reg1 + " = getelementptr i8*, i8** " + reg2 + ", i32 " + methodOffset + "\n");
        reg2 = new_temp();
        emit("\t" + reg2 + " = load i8*, i8** " + reg1 + "\n");
        reg1 = new_temp();
        emit("\t" + reg1 + " = bitcast i8* " + reg2 + " to " + get_LLVM_type(type) + " (i8*");
        for (int i = 0; i < formalParameters.size(); i++){
            emit(", "+ get_LLVM_type(formalParameters.get(i).getFieldTypeName()));
        }
        emit(")*\n");

        if(n.f4.present())
            exprList = ", " + n.f4.accept(this, argu);

        reg2 = new_temp();
        emit("\t" + reg2 + " = call " + get_LLVM_type(type) + " " + reg1 + "(" + primaryExpr + exprList + ")\n");

        if (argu.length == 3) argu[2] = type;

        return get_LLVM_type(type) + " " + reg2;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String[] argu) {
        String _ret=null;
        String[] exprList = new String[4];
        _ret = n.f0.accept(this, argu);
        _ret += n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, String[] argu) {
        String _ret=null;
        if(n.f0.present()){
            _ret = n.f0.accept(this, argu);
        }else
            _ret = "";
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String[] argu) {
        String _ret=null;
        _ret = ", " + n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, String[] argu) { return n.f0.accept(this, argu); }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, String[] argu) {
        String _ret=null;
        String primaryExpr;
        primaryExpr = n.f0.accept(this, argu);

        if(!primaryExpr.contains(" ")){
            String type = OffsetSymbolTable.getEntryClass(argu[0]).getMethod(argu[1]).getAbstractTypes(primaryExpr);

            if(type != null){
                String reg = new_temp();
                emit("\t" + reg + " = load " + get_LLVM_type(type) + ", " + get_LLVM_type(type) + "* %" + primaryExpr + "\n");

                if (argu.length == 3) argu[2] = type;

                _ret = get_LLVM_type(type) + " " + reg;
            }else{
                type = OffsetSymbolTable.getEntryClass(argu[0]).getField(primaryExpr).getFieldTypeName();
                if(type != null){
                    String reg1 = new_temp();
                    String className = SymbolTable.getFieldClassName(primaryExpr);
                    type = OffsetSymbolTable.getEntryClass(argu[0]).getField(primaryExpr).getFieldTypeName();
                    int identifierOffset = OffsetSymbolTable.getFieldOffset(className, primaryExpr) + 8;
                    emit("\t"+ reg1 + " = getelementptr " + get_LLVM_type(primaryExpr) + ", " + get_LLVM_type(primaryExpr)
                            + "* %this, i32 " + identifierOffset + "\n");
                    String reg2 = new_temp();
                    emit("\t" + reg2 + " = bitcast " + get_LLVM_type(primaryExpr) + "* " + reg1 + " to " + get_LLVM_type(type) + "*\n");
                    reg1 = new_temp();
                    emit("\t" + reg1 + " = load " + get_LLVM_type(type) +"," + get_LLVM_type(type) + "* "+ reg2 + "\n");

                    if (argu.length == 3) argu[2] = type;

                    _ret = get_LLVM_type(type) + " " + reg1;
                }
            }

        }else if(primaryExpr.contains(" "))
            _ret = primaryExpr;
        return _ret;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String[] argu) {
        return "i32 " + n.f0.toString();
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String[] argu) {
        return "i1 1";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String[] argu) {
        return "i1 0";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String[] argu) {
        return n.f0.toString();
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String[] argu) {
        return "i8* %" + n.f0.toString();
    }

    /**
     * f0 -> BooleanArrayAllocationExpression()
     *       | IntegerArrayAllocationExpression()
     */
    public String visit(ArrayAllocationExpression n, String[] argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(BooleanArrayAllocationExpression n, String[] argu) {
        String _ret=null;
        String arraySize=null;
        String reg1, reg2, reg3;
        String arrayLabel=null;
        String arrayLabelError=null;
        //todo: fix the boolean array allocation
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        emit("\n\t;check if the array zise is negative\n\n");
        arraySize = n.f3.accept(this, argu);

        reg1 = new_temp();
        arrayLabelError = newArrayLabel();
        arrayLabel = newArrayLabel();
        emit("\t" + reg1 + " = icmp slt " + arraySize + ", 0\n" );
        emit("\tbr i1 " + reg1 + ", label %" + arrayLabelError + ", label %" + arrayLabel + "\n");
        emit("\n" + arrayLabelError + ":\n\tcall void @throw_oob()\n\tbr label %" + arrayLabel + "\n\n");
        emit(arrayLabel + ":\n");

        reg2 = new_temp();
        emit("\t" + reg2 + " = add " + arraySize + ", 1\n");
        reg3 = new_temp();
        emit("\t" + reg3 + " = call i8* @calloc(i32 1, i32 " + reg2 + ")\n");
        reg2 = new_temp();
        emit("\t" + reg2 + " = bitcast i8* " + reg3 + " to i32*\n");
        emit("\tstore " + arraySize + ", i32* " + reg2 + "\n");

        _ret = "i32* " + reg2;
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(IntegerArrayAllocationExpression n, String[] argu) {
        String _ret=null;
        String arraySize=null;
        String reg1, reg2, reg3;
        String arrayLabel=null;
        String arrayLabelError=null;

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        emit("\n\t;check if the array zise is negative\n\n");
        arraySize = n.f3.accept(this, argu);

        reg1 = new_temp();
        arrayLabelError = newArrayLabel();
        arrayLabel = newArrayLabel();
        emit("\t" + reg1 + " = icmp slt " + arraySize + ", 0\n" );
        emit("\tbr i1 " + reg1 + ", label %" + arrayLabelError + ", label %" + arrayLabel + "\n");
        emit("\n" + arrayLabelError + ":\n\tcall void @throw_oob()\n\tbr label %" + arrayLabel + "\n\n");
        emit(arrayLabel + ":\n");

        reg2 = new_temp();
        emit("\t" + reg2 + " = add " + arraySize + ", 1\n");
        reg3 = new_temp();
        emit("\t" + reg3 + " = call i8* @calloc(i32 4, i32 " + reg2 + ")\n");
        reg2 = new_temp();
        emit("\t" + reg2 + " = bitcast i8* " + reg3 + " to i32*\n");
        emit("\tstore " + arraySize + ", i32* " + reg2 + "\n");

        _ret = "i32* " + reg2;
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String[] argu) {
        String _ret=null;
        String identifier;
        n.f0.accept(this, argu);
        identifier = n.f1.accept(this, argu);

        String reg = new_temp();
        String bitCastReg = new_temp();
        String ptrReg = new_temp();
        emit("\t" + reg + " = call i8* @calloc( i32 1, i32 " + OffsetSymbolTable.getClassFieldSizeSum(argu[0])+")\n");
        emit("\t" + bitCastReg + " = bitcast i8* " + reg + " to i8***\n");
        Klass klass = SymbolTable.getEntryClass(identifier);
        int numOfMethods = klass.getNumberOfMethods();
        emit("\t" + ptrReg + " = getelementptr [" + numOfMethods + " x i8*], ["
                + numOfMethods + " x i8*]* @." + klass.getName() + "_vtable, i32 0, i32 0\n");
        emit("\tstore i8** "+ ptrReg + ", i8*** " + bitCastReg +"\n");
        if(argu.length==3) argu[2] = identifier;
        return "i8* " +reg;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String[] argu) {
        String _ret=null;
        _ret = n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String[] argu) { return n.f1.accept(this, argu); }

}

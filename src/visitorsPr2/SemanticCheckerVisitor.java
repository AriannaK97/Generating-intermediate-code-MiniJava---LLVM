package visitorsPr2;

import symbolTable.*;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class SemanticCheckerVisitor extends GJDepthFirst <String, String[]> {
    //
    // User-generated visitor methods below
    //

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

        String[] idNames = new String[2];
        idNames[0] = n.f1.accept(this, argu);
        idNames[1] = n.f6.toString();
        if(n.f14.present())
            n.f14.accept(this, argu);
        if(n.f15.present())
            n.f15.accept(this, idNames);
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
            n.f3.accept(this, argu);
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
            n.f5.accept(this, argu);
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
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
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
        if(n.f4.present())
            n.f4.accept(this, argu);
        if(n.f7.present())
            n.f7.accept(this, argu);
        if(n.f8.present())
            n.f8.accept(this, argu);

        returnType = n.f10.accept(this, argu);

        /*
        * Check the method signature with the return value
        */

        if(SymbolTable.isPrimitiveType(returnType)){
            if(!returnType.equals(type)) {
                throw new SemanticCheckerException("Error! Incompatible return type of method " + argu[1] + ". Method returns type " +
                        type + " not " + returnType + ".");
            }
        }else if(SymbolTable.classNameExists(returnType)){
            if(!returnType.equals(type) && !SymbolTable.isSubType(returnType, type)){
                throw new SemanticCheckerException("Error! Incompatible return type of method "+ argu[1] +". Method returns type "+
                        type +" not " + returnType + ".");
            }
        }else{
            throw new SemanticCheckerException("Error! Incompatible return type of method "+ argu[1] +". Method returns type "+
                    type +" not " + returnType + ". Type " + returnType + "does not exist.");
        }

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
        if(!SymbolTable.isPrimitiveType(type) && !SymbolTable.classNameExists(type)){
            throw new SemanticCheckerException("Error! The class type "+type+" does not exist. Invalid parameter of type" +
                    type + " " + identifier + ".");
        }

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

    /**
     * f0 -> "boolean"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(BooleanArrayType n, String[] argu) {
        return n.f0.toString()+n.f1.toString()+n.f2.toString();
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(IntegerArrayType n, String[] argu) {
        return n.f0.toString()+n.f1.toString()+n.f2.toString();
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, String[] argu) {
        return n.f0.toString();
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, String[] argu) {
        return n.f0.toString();
    }

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

        String identifier = n.f0.accept(this, argu);

        String identType = SymbolTable.LookUp(argu[0], argu[1], identifier);

        String exprType = n.f2.accept(this, argu);

        if(SymbolTable.isPrimitiveType(exprType)){
            if(!exprType.equals(identType)) {
                throw new SemanticCheckerException("Error! In class " + argu[0] + " and method "+ argu[1] +", trying to assign " +
                        "expression of type " + exprType + " to incompatible variable named "+ identifier +" of type " + identType);
            }
        }else if(SymbolTable.classNameExists(exprType)){
            if(!exprType.equals(identType) && !SymbolTable.isSubType(exprType, identType)){
                throw new SemanticCheckerException("Error! In class " + argu[0] + " and method "+ argu[1] +", trying to assign " +
                        "expression of type " + exprType + " to incompatible variable named "+ identifier +" of type " + identType);
            }
        }else{
            throw new SemanticCheckerException("Error! In class " + argu[0] + " and method "+ argu[1] +", trying to assign " +
            "expression of not existing type " + exprType + " to variable named "+ identifier +" of type " + identType);
        }

        return _ret;
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
        String identifier;
        String identType, expr1Type , expr2Type;

        identifier = n.f0.accept(this, argu);

        identType = SymbolTable.LookUp(argu[0], argu[1], identifier);

        expr1Type = n.f2.accept(this, argu);

        if(!expr1Type.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Array Assignement Statement, " +
                    "expression in brackets should be type int not " + expr1Type);
        }

        expr2Type = n.f5.accept(this, argu);

        if(identType.equals("int[]")){
            if(!expr2Type.equals("int")){
                throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Array Assignement Statement, " +
                        "assigned expression should be of type int not " + identType);
            }
        }else if(identType.equals("boolean[]")){
            if(!expr2Type.equals("boolean")){
                throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Array Assignement Statement, " +
                        "assigned expression should be of type boolean not " + identType);
            }
        }else{
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Array Assignement Statement, " +
                    "array should be of type int[] or boolean[] not " + identType);
        }


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
        String _ret="boolean";
        String expr;
        expr = n.f2.accept(this, argu);
        if(!expr.equals("boolean")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid IfStatement, " +
                    "expression in parentheses should be type boolean not " + expr);
        }
        n.f4.accept(this, argu);
        n.f6.accept(this, argu);
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
        expr = n.f2.accept(this, argu);
        if(!expr.equals("boolean")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid IfStatement, " +
                    "expression in parentheses should be type boolean not " + expr);
        }
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
        String expr;
        expr = n.f2.accept(this, argu);
        if(expr.equals("int")){
            _ret = "int";
        }else if(expr.equals("boolean")){
            _ret = "boolean";
        }else{
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Expression in " +
                    "Print Statement, expression should be type int or boolean not " + expr);
        }
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
        String _ret="boolean";
        String clause1, clause2;
        clause1 = n.f0.accept(this, argu);
        if(!clause1.equals("boolean")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Clause, " +
                    "should be type boolean not " + clause1);
        }
        clause2 = n.f2.accept(this, argu);
        if(!clause2.equals("boolean")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Clause, " +
                    "should be type boolean not " + clause2);
        }
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String[] argu) {
        String _ret="boolean";
        String primaryExpr1, primaryExpr2;
        primaryExpr1 = n.f0.accept(this, argu);
        if(!primaryExpr1.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be type int not " + primaryExpr1);
        }
        primaryExpr2 = n.f2.accept(this, argu);
        if(!primaryExpr2.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be type int not " + primaryExpr2);
        }
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String[] argu) {
        String _ret="int";
        String primaryExpr1, primaryExpr2;
        primaryExpr1 = n.f0.accept(this, argu);
        if(!primaryExpr1.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be type int not " + primaryExpr1);
        }
        primaryExpr2 = n.f2.accept(this, argu);
        if(!primaryExpr2.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be type int not " + primaryExpr2);
        }
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String[] argu) {
        String _ret="int";
        String primaryExpr1, primaryExpr2;
        primaryExpr1 = n.f0.accept(this, argu);
        if(!primaryExpr1.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be type int not " + primaryExpr1);
        }
        primaryExpr2 = n.f2.accept(this, argu);
        if(!primaryExpr2.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be type int not " + primaryExpr2);
        }
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String[] argu) {
        String _ret="int";
        String primaryExpr1, primaryExpr2;
        primaryExpr1 = n.f0.accept(this, argu);
        if(!primaryExpr1.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be type int not " + primaryExpr1);
        }
        primaryExpr2 = n.f2.accept(this, argu);
        if(!primaryExpr2.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be type int not " + primaryExpr2);
        }
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
        String primaryExpr1, primaryExpr2;
        primaryExpr1 = n.f0.accept(this, argu);
        if(!primaryExpr1.equals("int[]") && !primaryExpr1.equals("boolean[]")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be array type (int[] or boolean[]) not " + primaryExpr1);
        }
        primaryExpr2 = n.f2.accept(this, argu);
        if(!primaryExpr2.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be type int not " + primaryExpr2);
        }

        /*the following 5 lines of code are missing in the version
         *of HW2 I have turned in - congrats to me -_- nailed it
         **/
        if(primaryExpr1.equals("boolean[]")){
            _ret = "boolean";
        }else
            _ret = "int";
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String[] argu) {
        String _ret="int";
        String primaryExpr = n.f0.accept(this, argu);
        if(!primaryExpr.equals("int[]") && !primaryExpr.equals("boolean[]")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Primary Expression, " +
                    "should be array type (int[] or boolean[]) not " + primaryExpr);
        }
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
        String primaryExpr, identifier, exprList;
        primaryExpr = n.f0.accept(this, argu);
        identifier = n.f2.accept(this, argu);

        if(SymbolTable.isPrimitiveType(primaryExpr)){
            throw new SemanticCheckerException("Error! In method "+ argu[1] +" trying to call a function on a primitive type "+ primaryExpr+" and " +
                    " not a class name or this");
        }else if(!SymbolTable.classNameExists(primaryExpr)){
            throw new SemanticCheckerException("Error! In method "+ argu[1] +" trying to call method "+identifier+" on a not existing type "+ primaryExpr+".");
        }
        if (n.f4.present()){
            exprList = n.f4.accept(this, argu);
        }else
            exprList = null;

        if(exprList!=null){
            String[] args;
            if(exprList.length()>1){
                args = exprList.split(",");
                if(SymbolTable.getEntryClass(primaryExpr).hasMethod(identifier, args)){
                    _ret = SymbolTable.getEntryClass(primaryExpr).getMethod(identifier).getFieldTypeName();
                }else{
                    throw new SemanticCheckerException("Error! In method "+ argu[1] +" trying to call undeclared method "+identifier+".");
                }
            }else{
                args = new String[1];
                args[0] = exprList;
                if(SymbolTable.getEntryClass(primaryExpr).hasMethod(identifier, args)){
                    _ret = SymbolTable.getEntryClass(primaryExpr).getMethod(identifier).getReturnType().getFieldTypeName();
                }else{
                    throw new SemanticCheckerException("Error! In method "+ argu[1] +" trying to call undeclared method "+identifier+".");
                }
            }
        }else{
            if(SymbolTable.getEntryClass(primaryExpr).hasMethod(identifier, null)){
                _ret = SymbolTable.getEntryClass(primaryExpr).getMethod(identifier).getReturnType().getFieldTypeName();
            }else{
                throw new SemanticCheckerException("Error! In method "+ argu[1] +" trying to call undeclared method "+identifier+".");
            }
        }

        return _ret;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String[] argu) {
        String _ret=null;
        String[] exprTypeList = new String[3];
        exprTypeList[0] = argu[0];
        exprTypeList[1] = argu[1];
        exprTypeList[2] = n.f0.accept(this, argu);
        n.f1.accept(this, exprTypeList);
        return exprTypeList[2];
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, String[] argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String[] argu) {
        String _ret=null;
        String exprType;
        exprType = n.f1.accept(this, argu);
        argu[2] = argu[2] + n.f0.toString() + exprType;
        return _ret;
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, String[] argu) {
        return n.f0.accept(this, argu);
    }

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
        int which;
        String primaryExpr, type;
        which = n.f0.which;
        primaryExpr = n.f0.accept(this, argu);
        if(which == 3)  /*identifier*/
            type = SymbolTable.LookUp(argu[0], argu[1], primaryExpr);
        else if(which == 4) /*this*/
            type = SymbolTable.getEntryClass(argu[0]).getName();
        else
            type = primaryExpr;
        return type;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String[] argu) {
        return "int";
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String[] argu) {
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String[] argu) {
        return "boolean";
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
        return n.f0.toString();
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
        String _ret="boolean[]";
        String expr = n.f3.accept(this, argu);
        if(!expr.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Expression, " +
                    "should be int not " + expr);
        }
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
        String _ret="int[]";
        String expr = n.f3.accept(this, argu);
        if(!expr.equals("int")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Expression, " +
                    "should be int not " + expr);
        }
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
        String identifier = n.f1.accept(this, argu);
        if(!SymbolTable.classNameExists(identifier)){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid allocation expression of " +
                    "undeclared class type" + identifier);
        }
        return identifier;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String[] argu) {
        String _ret="boolean";
        String notExpr = n.f1.accept(this, argu);
        if(!notExpr.equals("boolean")){
            throw new SemanticCheckerException("Error! In method "+ argu[1] + " of class " + argu[0] + ". Invalid Not Expression, " +
                    "should be boolean not " + notExpr);
        }
        return _ret;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String[] argu) {
        return n.f1.accept(this, argu);
    }
}

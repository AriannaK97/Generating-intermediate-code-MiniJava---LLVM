package symbolTable;

import visitorsPr2.SemanticCheckerException;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {

    public static LinkedHashMap<String, AbstractType> symbolTable = new LinkedHashMap<>();

    public static LinkedHashMap<String, AbstractType> getSymbolTable() {
        return symbolTable;
    }

    public static void addElement(AbstractType element) {
        if(symbolTable.isEmpty()){
            getSymbolTable().put(element.getFieldTypeName(),element);
        }else{
            if(!classNameExists(element.getFieldTypeName())){
                getSymbolTable().put(element.getFieldTypeName(),element);
            }else{
                throw new SemanticCheckerException("Class " + element.getFieldTypeName() + " already exists");
            }
        }
    }

    public static boolean classNameExists(String className){
        for (Map.Entry<String, AbstractType> entry : symbolTable.entrySet()){
            if(entry.getKey().equals(className)){
                return true;
            }
        }return false;
    }

    public static Klass getEntryClass(String className) {
        for (Map.Entry<String, AbstractType> entry : symbolTable.entrySet()){
            if(entry.getKey().equals(className)){
                return (Klass) entry.getValue();
            }
        }
        return null;
    }

    public static String getMainClassName(){
        Klass tempKlass;
        for (Map.Entry<String, AbstractType> entry : symbolTable.entrySet()){
            tempKlass = (Klass)entry.getValue();
            if(tempKlass.getMethod("main")!=null){
                return tempKlass.getName();
            }
        }
        return null;
    }

    public static String getMethodClassName(String className, String methodName){
        Klass tempKlass;
        tempKlass = getEntryClass(className);

        for (int i = 0; i < tempKlass.getMethods().size(); i++){
            if(tempKlass.getMethod(methodName)!=null){
                return tempKlass.getName();
            }
        }
        if(tempKlass.hasSuperClass()){
            return getMethodClassName(tempKlass.getSuperClassName(), methodName);
        }

        return null;
    }

    public static String getFieldClassName(String className, String fieldName){
        Klass tempKlass;
        tempKlass = getEntryClass(className);

        for (int i = 0; i < tempKlass.getFields().size(); i++){
            if(tempKlass.getField(fieldName)!=null){
                return tempKlass.getName();
            }
        }
        if(tempKlass.hasSuperClass()){
            return getFieldClassName(tempKlass.getSuperClassName(), fieldName);
        }


/*        Klass tempKlass;
        for (Map.Entry<String, AbstractType> entry : symbolTable.entrySet()){
            tempKlass = (Klass)entry.getValue();
            if(tempKlass.getField(fieldName)!=null){
                return tempKlass.getName();
            }
        }*/
        return null;
    }

    public static String LookUp(String currentClassName, String currentMethodName, String currentVarName){
        Klass klass = getEntryClass(currentClassName);
        if(klass == null){
            throw new SemanticCheckerException("Error! Class " + currentClassName + " is not defined.");
        }
        Method method = klass.getMethod(currentMethodName);
        if(method == null){
            throw new SemanticCheckerException("Error! Class " + currentClassName + " does not have method"+currentMethodName+".");
        }

        AbstractType var = method.getArgument(currentVarName);
        /*
         * Checking if the variable has been declared either as a method argument or as a method variable
         **/
        if(var == null){
            var = method.getVariable(currentVarName);
            if (var == null){
                var = klass.getField(currentVarName);
                if(var == null){
                    if(klass.hasSuperClass()) {
                        var = lookUpSubClasses(currentVarName, klass);
                        if (var == null) {
                            throw new SemanticCheckerException("Error! Trying to assign expression in undeclared variable " + currentVarName +
                                    " in method " + currentMethodName + " of class " + currentClassName
                            );
                        }
                    }else{
                        throw new SemanticCheckerException("Error! Trying to assign expression in undeclared variable " + currentVarName +
                                " in method " + currentMethodName + " of class " + currentClassName
                        );
                    }
                }
            }
        }
        return var.getFieldTypeName();
    }


    public static AbstractType lookUpSubClasses(String currentVarName, Klass currentClass){
        AbstractType var;
        Klass superClass = currentClass.getSuperClass();
        while (superClass != null){
            var = superClass.getField(currentVarName);
            if (var != null) return var;
            superClass = superClass.getSuperClass();
        }
        return null;
    }

    public static boolean isPrimitiveType(String typeName){
        if (typeName.equals("int")) return true;
        if (typeName.equals("boolean")) return true;
        if (typeName.equals("int[]")) return true;
        if (typeName.equals("boolean[]")) return true;
        return false;
    }

    public static boolean isSubType(String typeNameSub, String typeName) {
        Klass subClass = getEntryClass(typeNameSub);
        while (subClass != null){
            if (subClass.hasSuperClass() && subClass.isSuperClass(typeName)) return true;
            subClass = subClass.getSuperClass();
        }
        return false;
    }

    public static void clearSymbolTable(){
        symbolTable.clear();
    }

    public void printTable(){
        symbolTable.forEach((entryType,entry)->((AbstractType)entry).printType());
    }

    public static void printSymbolTable(){
        for (Map.Entry<String, AbstractType> entry : symbolTable.entrySet()){
            Klass tempKlass = (Klass)entry.getValue();
            System.out.println("---------- Class "+tempKlass.getFieldTypeName()+"----------");
            System.out.println("------Variables-------");
            for(int i=0;i<tempKlass.getFields().size();i++){
                System.out.println(tempKlass.getName() + "." + tempKlass.getFields().get(i).getName() + ": " + tempKlass.getFields().get(i).getOffset());
            }
            System.out.println("------Methods-------");
            for(int i=0;i<tempKlass.getMethods().size();i++){
                System.out.println(tempKlass.getName() + "." + tempKlass.getMethods().get(i).getName() + ": " + tempKlass.getMethods().get(i).getOffset());
            }
        }
    }

}

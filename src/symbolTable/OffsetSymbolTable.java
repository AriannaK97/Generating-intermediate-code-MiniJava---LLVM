package symbolTable;

import java.util.LinkedHashMap;
import java.util.Map;

public class OffsetSymbolTable extends SymbolTable{
    public static LinkedHashMap<AbstractType, Integer> offsetSymbolTable = new LinkedHashMap<>();

    public OffsetSymbolTable() {
        for (Map.Entry<String, AbstractType> entry : SymbolTable.symbolTable.entrySet()){
            Klass klass = (Klass)entry.getValue();
            if(!OffsetSymbolTable.hasClassNameIn(klass.getName())){
                createClassOffset(klass);
            }
        }
    }

    public static int getMethodOffset(String className, String methodName){
        if(OffsetSymbolTable.hasClassNameIn(className)) {
            Klass klass = getEntryClass(className);
            for (int i = 0; i < klass.getMethods().size(); i++) {
                if (klass.getMethods().get(i).getName().equals(methodName)) {
                    return i;
                }
            }
        }
        return 0;
    }

    public static int getFieldOffset(String className, String varName){
        int offset = 0;
        if(OffsetSymbolTable.hasClassNameIn(className)) {
            Klass klass = getEntryClass(className);
            for (int i = 0; i < klass.getFields().size(); i++) {
                if (klass.getFields().get(i).getName().equals(varName)) {
                    return offset;
                }
                offset += klass.getFields().get(i).getSize();
            }

        }
        return 0;
    }

    private static void createClassOffset(Klass klass){
        int klassOffset = 0;
        if(!klass.classHasMain()){
            if (klass.hasSuperClass()){
                Klass superClass = klass;
                while(superClass.hasSuperClass()) {
                    if(!OffsetSymbolTable.hasClassNameIn(superClass.getSuperClassName())) {
                        OffsetSymbolTable.createClassOffset(klass.getSuperClass());
                    }
                    superClass = superClass.getSuperClass();
                }
            }
            klassOffset = klassOffset + OffsetSymbolTable.createClassFieldsOffset(klass);
            klassOffset = klassOffset + OffsetSymbolTable.createClassMethodsOffset(klass);
            klass.setOffset(klassOffset);
            offsetSymbolTable.put(klass, klassOffset);
        }
    }

    private static int fieldsStartOffset(Klass klass){
        int fieldOffsetSum = 0;
        if(klass.classHasMain())
            return fieldOffsetSum;
        if(klass.hasSuperClass()){
            Klass superClass = klass.getSuperClass();
            if(OffsetSymbolTable.hasClassNameIn(superClass.getName())){
                fieldOffsetSum = fieldOffsetSum + fieldsStartOffset(superClass);
            }
        }
        if(OffsetSymbolTable.hasClassNameIn(klass.getName())){
            for(int i=0; i < klass.getFields().size(); i++){
                fieldOffsetSum = fieldOffsetSum + klass.getFields().get(i).getSize();
            }
        }

        return fieldOffsetSum;
    }

    private static int methodsStartOffset(Klass klass){
        int methodOffsetSum = 0;
        if(klass.classHasMain())
            return methodOffsetSum;
        if(klass.hasSuperClass()){
            Klass superClass = klass.getSuperClass();
            if(OffsetSymbolTable.hasClassNameIn(superClass.getName())){
                methodOffsetSum = methodOffsetSum+ methodsStartOffset(superClass);
            }
        }
        if(OffsetSymbolTable.hasClassNameIn(klass.getName())){
            for(int i=0; i < klass.getMethods().size(); i++){
                methodOffsetSum = methodOffsetSum + klass.getMethods().get(i).getSize();
            }
        }

        return methodOffsetSum;
    }

    private static int createClassFieldsOffset(Klass klass){
        int currentOffset = OffsetSymbolTable.fieldsStartOffset(klass);
        for(int i=0; i < klass.getFields().size(); i++){
            klass.getFields().get(i).setOffset(currentOffset);
            currentOffset += klass.getFields().get(i).getSize();
        }
        return currentOffset;
    }

    private static int createClassMethodsOffset(Klass klass){
        int currentOffset = OffsetSymbolTable.methodsStartOffset(klass);
        for(int i=0; i < klass.getMethods().size(); i++){
            if(!klass.getMethods().get(i).isOverridingMethod(klass)) {
                klass.getMethods().get(i).setOffset(currentOffset);
                currentOffset += klass.getMethods().get(i).getSize();
            }
        }
        return currentOffset;
    }


    private static boolean hasClassNameIn(String className){
        Klass currentClass = null;
        for (Map.Entry<AbstractType, Integer> entry : offsetSymbolTable.entrySet()){
            currentClass = (Klass)entry.getKey();
            if(currentClass.getName().equals(className)){
                return true;
            }
        }return false;
    }

    public static int getClassFieldSizeSum(String className){
        Klass currentClass = null;
        int fieldSizeSum = 8;   /*class has 8bytes by itself*/
        currentClass = OffsetSymbolTable.getEntryClass(className);

        for(int i = 0; i < currentClass.getFields().size(); i++){
            fieldSizeSum += currentClass.getFields().get(i).getSize();
        }

        return fieldSizeSum;
    }

    public static int getMethodVarSizeSum(String className, String methodName){
        Klass currentClass = null;
        Method currentMethod = null;
        int fieldSizeSum = 8;   /*class has 8bytes by itself*/
        currentClass = OffsetSymbolTable.getEntryClass(className);
        currentMethod = currentClass.getMethod(methodName);

        for(int i = 0; i < currentClass.getFields().size(); i++){
            fieldSizeSum += currentClass.getFields().get(i).getSize();
        }

        for (int i = 0; i < currentMethod.getVariables().size(); i++){
            fieldSizeSum += currentMethod.getVariables().get(i).getSize();
        }

        return fieldSizeSum;
    }


    public static void printOffsetSymbolTable(){
        for (Map.Entry<AbstractType, Integer> entry : offsetSymbolTable.entrySet()){
            Klass tempKlass = (Klass)entry.getKey();

            System.out.println("\n---------- Class " + tempKlass.getFieldTypeName() + "----------");
            System.out.println("------Variables-------");
            for (int i = 0; i < tempKlass.getFields().size(); i++) {
                System.out.println(tempKlass.getName() + "." + tempKlass.getFields().get(i).getName() + ": " + tempKlass.getFields().get(i).getOffset());
            }
            System.out.println("------Methods-------");
            for (int i = 0; i < tempKlass.getMethods().size(); i++) {
                if (!tempKlass.getMethods().get(i).isOverridingMethod(tempKlass))
                    System.out.println(tempKlass.getName() + "." + tempKlass.getMethods().get(i).getName() + ": " + tempKlass.getMethods().get(i).getOffset());
            }

        }
    }

    public static void clearOffsetSymbolTable(){
        offsetSymbolTable.clear();
    }

}

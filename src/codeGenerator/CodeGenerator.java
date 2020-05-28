package codeGenerator;

import symbolTable.AbstractType;
import symbolTable.Klass;
import symbolTable.SymbolTable;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class CodeGenerator {

    private static PrintWriter writer;
    private static int temporaryRegisterNum = 0;
    private static int ifElseLabelNum = 0;
    private static int ifThenLabelNum = 0;
    private static int ifEndLabelNum = 0;
    private static int whileLabelNum = 0;
    private static int oobNum = 0;
    private static int nszOkNum = 0;
    private static int nszErrNum = 0;

    public CodeGenerator(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        writer = new PrintWriter(/*"out/LLVM-IR-Ouputs/myOutput/" + */fileName + ".ll", "UTF-8");
        System.out.println("File " + fileName + ".ll has been successfully created");
    }

    public static void emit(String code){
        writer.write(code);
    }

    public static String new_temp(){
        String tempRegister = null;
        tempRegister = "%_" + String.valueOf(temporaryRegisterNum);
        temporaryRegisterNum+=1;
        return tempRegister;
    }

    public static void defineVTables(){
        String outputStr = null;
        String methodArguments;
        for(Map.Entry<String, AbstractType> entry : SymbolTable.symbolTable.entrySet()){
            Klass curKlass = (Klass)entry.getValue();
            outputStr = "@."+curKlass.getName()+"_vtable = global [" + curKlass.getNumberOfMethods() + " x i8*] [";
            if(curKlass.getNumberOfMethods() > 0 && !curKlass.classHasMain()){
                for(int i=0;i<curKlass.getMethods().size();i++){
                    if(i >= 1){
                        outputStr = outputStr + ", ";
                    }
                    methodArguments = "i8*";
                    if(curKlass.getMethods().get(i).getArguments().size() > 0){
                        for (int j=0; j < curKlass.getMethods().get(i).getArguments().size(); j++){
                            methodArguments = methodArguments + ", " +get_LLVM_type(curKlass.getMethods().get(i).getArguments().get(j).getFieldTypeName());
                        }
                    }
                    outputStr = outputStr + "i8* bitcast (" + get_LLVM_type(curKlass.getMethods().get(i).getFieldTypeName())
                            + " (" + methodArguments + ")* @"+ curKlass.getName() +"."
                            + curKlass.getMethods().get(i).getName() + " to i8*)";
                }
            }
            outputStr = outputStr + "]\n";
            emit(outputStr);
        }
        emit("\n");
    }

    public static void defineHelperMethods(){
        emit("declare i8* @calloc(i32, i32)\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "declare void @exit(i32)\n" +
                "\n" +
                "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
                "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
                "@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n"+
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
                "}\n" +
                "\n" +
                "define void @throw_nsz() {\n" +
                "%_str = bitcast [15 x i8]* @_cNSZ to i8*\n"+
                "call i32 (i8*, ...) @printf(i8* %_str)\n"+
                "call void @exit(i32 1)\n"+"ret void\n}" +
                "\n");
    }


    public static String get_LLVM_type(String type){
        switch (type) {
            case "int":
                return "i32";
            case "boolean":
                return "i1";
            case "int[]":
                return "i32*";
            case "boolean[]":
                return "i8*";
            default:
                return "i8*";
        }
    }

    public static String newIfElseLabel(){
        String tempLabel;
        tempLabel = "if_else_" + String.valueOf(ifElseLabelNum);
        ifElseLabelNum+=1;
        return tempLabel;
    }

    public static String newIfThenLabel(){
        String tempLabel;
        tempLabel = "if_then_" + String.valueOf(ifThenLabelNum);
        ifThenLabelNum+=1;
        return tempLabel;
    }

    public static String newIfEndLabel(){
        String tempLabel;
        tempLabel = "if_end_" + String.valueOf(ifEndLabelNum);
        ifEndLabelNum+=1;
        return tempLabel;
    }

    public static String newLoopLabel(){
        String tempLabel;
        tempLabel = "loop" + String.valueOf(whileLabelNum);
        whileLabelNum+=1;
        return tempLabel;
    }

    public static String newOOBLabel(){
        String tempLabel;
        tempLabel = "oob" + String.valueOf(oobNum);
        oobNum+=1;
        return tempLabel;
    }

    public static String newNszOkLabel(){
        String tempLabel;
        tempLabel = "nsz_ok_" + String.valueOf(nszOkNum);
        nszOkNum+=1;
        return tempLabel;
    }

    public static String newNszErrLabel(){
        String tempLabel;
        tempLabel = "nsz_err_" + String.valueOf(nszErrNum);
        nszErrNum+=1;
        return tempLabel;
    }

    public static void reinitializeTempRegisters(){
        temporaryRegisterNum = 0;
    }

    public static void reinitializeLabels(){
        ifElseLabelNum = 0;
        ifThenLabelNum = 0;
        ifEndLabelNum = 0;
        whileLabelNum = 0;
        oobNum = 0;
        nszErrNum = 0;
        nszOkNum = 0;
    }

    public static void closeWriter(){
        writer.close();
    }

}

package codeGenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class CodeGenerator {

    private static PrintWriter writer;
    private static int temporaryRegisterNum = 0;
    private static int ifElseLabelNum = 0;
    private static int ifThenLabelNum = 0;
    private static int ifEndLabelNum = 0;
    private static int whileLabelNum = 0;
    private static int forLabelNum = 0;
    private static int arrayAllocNum=0;

    public CodeGenerator(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        writer = new PrintWriter("out/LLVM-IR-Ouputs/myOutput/" + fileName + ".ll", "UTF-8");
        System.out.println("File out/LLVM-IR-Ouputs/myOutput/" + fileName + ".ll has been successfully created");
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

    public static String get_LLVM_type(String type){
        switch (type) {
            case "int":
                return "i32";
            case "boolean":
                return "i1";
            case "int[]":
            case "boolean[]":
                return "i32*";
            default:
                return "i8";
        }
    }

    public static String newIfElseLabel(){
        String tempLabel = null;
        tempLabel = "if_else_" + String.valueOf(ifElseLabelNum);
        ifElseLabelNum+=1;
        return tempLabel;
    }

    public static String newIfThenLabel(){
        String tempLabel = null;
        tempLabel = "if_then_" + String.valueOf(ifThenLabelNum);
        ifThenLabelNum+=1;
        return tempLabel;
    }

    public static String newIfEndLabel(){
        String tempLabel = null;
        tempLabel = "if_end_" + String.valueOf(ifEndLabelNum);
        ifEndLabelNum+=1;
        return tempLabel;
    }

    public static String new_While_label(){
        String tempLabel = null;
        tempLabel = "while_" + String.valueOf(whileLabelNum);
        whileLabelNum+=1;
        return tempLabel;
    }

    public static String new_For_label(){
        String tempLabel = null;
        tempLabel = "for_" + String.valueOf(forLabelNum);
        forLabelNum+=1;
        return tempLabel;
    }

    public static String newArrayLabel(){
        String tempLabel = null;
        tempLabel = "array_alloc" + String.valueOf(arrayAllocNum);
        arrayAllocNum+=1;
        return tempLabel;
    }

    public static void reinitializeTempRegisters(){
        temporaryRegisterNum = 0;
    }

    public static void closeWriter(){
        writer.close();
    }

}

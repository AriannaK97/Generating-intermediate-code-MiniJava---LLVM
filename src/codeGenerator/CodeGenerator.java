package codeGenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class CodeGenerator {

    private static PrintWriter writer;
    private static int temporaryRegisterNum = 0;

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

    public static void closeWriter(){
        writer.close();
    }

}

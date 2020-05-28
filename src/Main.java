import codeGenerator.CodeGenerator;
import symbolTable.SymbolTable;
import symbolTable.OffsetSymbolTable;
import syntaxtree.*;
import visitorsPr2.*;
import static codeGenerator.CodeGenerator.*;
import java.io.*;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;

class Main {
    public static void main (String [] args){
        if(args.length < 1){
            System.err.println("Usage: java Driver <inputFile>");
            System.exit(1);
        }
        FileInputStream fis = null;
        Path file = null;
        int i;
        for (i = 0; i < args.length; i++){
            try{
                System.out.println("_________________________________________\n");

                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                String[] inputFileName = args[i].split("/");
                System.out.println("Program file "+inputFileName[1]+" parsed successfully.\n");

                SymbolTableFillerVisitor symbolTableFillerVisitor = new SymbolTableFillerVisitor();
                Goal root = parser.Goal();
                root.accept(symbolTableFillerVisitor, null);
                SemanticCheckerVisitor semanticCheckerVisitor = new SemanticCheckerVisitor();
                root.accept(semanticCheckerVisitor, null);
                new OffsetSymbolTable();

                /*Uncomment line below to print the OffsetSymbolTable*/
                //OffsetSymbolTable.printOffsetSymbolTable();

                /*Get the name of the class that holds the main method in order to name
                 *the .ll file where the llvm code will be written*/
                String fileName = SymbolTable.getMainClassName();
                new CodeGenerator(fileName);

                /*visitor for llvm production*/
                LLVMIRGeneratorVisitor llvmirGeneratorVisitor = new LLVMIRGeneratorVisitor();
                root.accept(llvmirGeneratorVisitor, null);
                closeWriter();

                System.out.println("_________________________________________");
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch (SemanticCheckerException ex){
                System.err.println(ex.getMessage());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
            SymbolTable.clearSymbolTable();
            OffsetSymbolTable.clearOffsetSymbolTable();
        }
    }
}
package symbolTable;

import visitorsPr2.SemanticCheckerException;

import java.util.ArrayList;
import java.util.List;

public class Klass extends AbstractType {

    private List<AbstractType> fields = new ArrayList<AbstractType>();
    private List<Method> methods = new ArrayList<Method>();
    private Klass superClass;
    private String superClassName;
    private String className;

    public Klass(String name) {
        setClassName(name);
        setFieldTypeName(name);
    }

    public void addMethod(Method method) {
        if (isValidNewMethod(method)){
            getMethods().add(method);
        }
    }

    public void addField(AbstractType field) {
        if (isValidNewField(field)){
            getFields().add(field);
        }/*else{
            throw new TypeCheckerException("Type check error in field type:" + field);
        }*/
    }

    /**
     *  all methods are inherently polymorphic (i.e., “virtual” in C++ terminology). This means that foo can be defined
     *  in a subclass if it has the same return type and argument types (ordered) as in the parent, but it is an error
     *  if it exists with other argument types or return type in the parent.
     * @param method
     * @return
     */
    private boolean isValidNewMethod(Method method){
        /*check for existing method with the same name in the class*/
        for(int i=0;i<getMethods().size();i++){
            if (getMethods().get(i).getName().equals(method.getName()))
                throw new SemanticCheckerException("Error! Method " + method.getName() + " already exists in class "+ this.getName());
        }
        /*check for override*/
        method.isOverridingMethod(this);

        return true;
    }

    private boolean isValidNewField(AbstractType field){
        for(int i=0;i<getFields().size();i++){
            if (getFields().get(i).getName().equals(field.getName()))
                return false;
        }
        return true;
    }

    public AbstractType getField(String fieldName){
        for(int i=0; i < this.getFields().size(); i++){
            if(getFields().get(i).getName().equals(fieldName)){
                return getFields().get(i);
            }
        }

        if(this.hasSuperClass()){
            return this.superClass.getField(fieldName);
        }

        return null;
    }

    @Override
    public String getName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the fields
     */
    public List<AbstractType> getFields() {
        return fields;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(List<AbstractType> fields) {
        this.fields = fields;
    }

    /**
     * @return the methods
     */
    public List<Method> getMethods() {
        return methods;
    }

    public int getNumberOfMethods() {
        if(this.classHasMain())
            return this.methods.size() - 1;
        else
            return this.methods.size();
    }

    public boolean hasMethod(String methodName, String[] args){
        Method method = getMethod(methodName);
        if (method == null){
            return false;
        }

        if(args != null){
            /*check number of args between methods*/
            if(args.length != method.getArguments().size()){
                return false;
            }
            /*methods have the same number of args but the arg types should agree too*/
            for(int i = 0; i < args.length; i++){
                if(SymbolTable.isPrimitiveType(args[i])){
                    if(!args[i].equals(method.getArguments().get(i).getFieldTypeName())) {
                        return false;
                    }
                }else if(SymbolTable.classNameExists(args[i])){
                    if(!args[i].equals(method.getArguments().get(i).getFieldTypeName()) && !SymbolTable.isSubType(args[i],
                            method.getArguments().get(i).getFieldTypeName())){
                        return false;
                    }
                }else{
                    return false;
                }
            }
        }
        return true;
    }

    public boolean classHasMain(){
        Method main = null;
        for(int i=0; i < this.getMethods().size(); i++){
            if(getMethods().get(i).getName().equals("main")){
                main = getMethods().get(i);
            }
        }
        if(main == null){
            return false;
        }
        if(main.getArguments().size() == 1){
            if(main.getArguments().get(0).getFieldTypeName().equals("String[]"))
                return true;
        }
        return false;
    }

    public Method getMethod(String methodName){
        Method currentMethod = null;
        for(int i=0; i < this.getMethods().size(); i++){
            if(getMethods().get(i).getName().equals(methodName)){
                currentMethod = getMethods().get(i);
                break;
            }
        }
        /*if not found in the class, check if the method is in a parent class*/
        if(this.hasSuperClass() && currentMethod == null){
            Method superMethod = this.getSuperClass().getMethod(methodName);
            if(superMethod != null){
                currentMethod = superMethod;
            }
        }
        return currentMethod;
    }

    /**
     * @param methods the methods to set
     */
    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    /**
     * @return the superClass
     */
    public Klass getSuperClass() {
        return superClass;
    }

    /**
     * @param superClass the superClass to set
     */
    public void setSuperClass(Klass superClass) {
        this.superClass = superClass;
    }
    /**
     * @param superClass the superClassName to set
     */
    public void setSuperClassName(String superClass) {
        this.superClassName = superClass;
    }
    /**
     * @return the superClassName
     */
    public String getSuperClassName() {
        return superClassName;
    }

    /**
     * @return the superClassOffset
     */
    public int getSuperClassOffset() {
        return this.superClass.getOffset();
    }

    public boolean hasSuperClass() {
        if(getSuperClass() != null)return true;
        else return false;
    }

    public boolean isSuperClass(String superClassName){
        return this.getSuperClassName().equals(superClassName);
    }

    public void printFieldTypeList(){
        for(int i = 0; i < this.getFields().size(); i++){
            getFields().get(i).printType();
        }
    }

    public void printMethodList(){
        for(int i=0; i < this.getMethods().size(); i++){
            getMethods().get(i).printType();
        }
    }

    @Override
    public void printType() {
        System.out.println("---------- Class "+this.getFieldTypeName()+"----------");
        System.out.println("------Variables-------");
        for(int i=0; i < this.getFields().size(); i++){
            System.out.println(getFieldTypeName() + "." + getFields().get(i).getFieldTypeName() + ":");
        }
        System.out.println("------Methods-------");
        for(int i=0; i < this.getMethods().size(); i++){
            System.out.println(getFieldTypeName() + "." + getMethods().get(i).getFieldTypeName() + ":");
        }
    }
}
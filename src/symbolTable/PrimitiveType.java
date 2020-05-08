package symbolTable;

public class PrimitiveType extends AbstractType {

    private String name;
    private Primitives primitive;

    public PrimitiveType(String typeName, String name) {
        setName(name);
        setPrimitive(typeName);
        setFieldTypeName(typeName);
    }

    /**
     * this constructor creates an instance of this class only in the case of a method
     * a method is a pointer thus having size 8 - a l w a y s
    **/
    public PrimitiveType(String typeName){
        setFieldTypeName(typeName);
        setPrimitive(typeName);
        setSize(8);
    }

    @Override
    public void printType(){

        System.out.println(getPrimitive() + " " + getFieldTypeName() + "::" + getSize());
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Primitives getPrimitive() {
        return primitive;
    }

    public Primitives checkPrimitiveType(String typeName){
        if(typeName.equals("int")){
            this.setSize(4);
            return Primitives.INT;
        }else if(typeName.equals("boolean")){
            this.setSize(1);
            return Primitives.BOOLEAN;
        }else if(typeName.equals("String[]")){
            this.setSize(0);
            return Primitives.STRING;
        }else{
            this.setSize(8);
            return Primitives.POINTER;
        }
    }

    public void setPrimitive(String typeName) {

        this.primitive = checkPrimitiveType(typeName);
    }

}

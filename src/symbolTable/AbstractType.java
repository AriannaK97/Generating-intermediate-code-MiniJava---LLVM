package symbolTable;

abstract public class AbstractType {

    private String name;
    private int size;
    private int offset;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getFieldTypeName() {
        return name;
    }

    public void setFieldTypeName(String name) {
        this.name = name;
    }

    public abstract String getName();

    public abstract void printType();

}
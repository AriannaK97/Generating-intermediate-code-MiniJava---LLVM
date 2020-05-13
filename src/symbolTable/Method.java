package symbolTable;

import visitorsPr2.SemanticCheckerException;

import java.util.ArrayList;
import java.util.List;

public class Method extends AbstractType {

    private String name;
    private AbstractType returnType;
    private List<AbstractType> arguments = new ArrayList<AbstractType>();
    private List<AbstractType> variables = new ArrayList<AbstractType>();

    public Method(AbstractType symbolType, String name) {
        setName(name);
        setReturnType(symbolType);
        setFieldTypeName(symbolType.getFieldTypeName());
    }

    @Override
    public void printType() {
        for(int i=0;i<getArguments().size();i++){
            System.out.println(getReturnType() + " " + getFieldTypeName() + " ." + getArguments().get(i).getFieldTypeName());
        }
    }

    private boolean isValidArgument(AbstractType arg){
        for(int i=0;i<getArguments().size();i++){
            if (getArguments().get(i).getName().equals(arg.getName()))
                return false;
        }
        return true;
    }

    public void addArguments(AbstractType arg) {
        if(getArguments().isEmpty()){
            getArguments().add(arg);
        }else {
            if (isValidArgument(arg)) {
                getArguments().add(arg);
            } else {
                throw new SemanticCheckerException("Invalid argument variable " + arg.getName() + " in method " + this.getName());
            }
        }
    }

    public AbstractType getArgument(String argName) {
        for(int i = 0; i < getArguments().size(); i++){
            if (getArguments().get(i).getName().equals(argName))
                return getArguments().get(i);
        }
        return null;
    }

    private boolean isValidVariable(AbstractType var){
        for(int i=0;i<getVariables().size();i++){
            if (getVariables().get(i).getName().equals(var.getName()))
                return false;
        }
        /*check if the variable is already declared in arguments*/
        if(!isValidArgument(var)) return false;
        return true;
    }

    public void addVariable(AbstractType var) {
        if(getVariables().isEmpty()){
            if (isValidVariable(var)) {
                getVariables().add(var);
            }else{
                throw new SemanticCheckerException("Invalid method variable " + var.getFieldTypeName() + " in method " + getFieldTypeName()+
                        ". Previous declaration of variable " + var.getFieldTypeName()+" "+var.getName()+".");
            }
        }else {
            if (isValidVariable(var)) {
                getVariables().add(var);
            } else {
                throw new SemanticCheckerException("Invalid method variable " + var.getFieldTypeName() + " in method " + getFieldTypeName()+
                        ". Previous declaration of variable " + var.getFieldTypeName()+" "+var.getName()+".");
            }
        }
    }

    public String getAbstractTypes(String type) {
        AbstractType _ret=null;
        _ret = getVariable(type);
        if(_ret != null) {
            return _ret.getFieldTypeName();
        }else{
            _ret = getArgument(type);
            if(_ret != null){
                return _ret.getFieldTypeName();
            }
        }
        return null;
    }

    public AbstractType getVariable(String varName) {
        for(int i = 0; i < getVariables().size(); i++){
            if (getVariables().get(i).getName().equals(varName))
                return getVariables().get(i);
        }
        return null;
    }

    public boolean isOverridingMethod(Klass currentClass){
        /*"this" refers to the method we are overriding*/
        boolean initialMethod = false;
        if(currentClass.hasSuperClass()) {
            Klass superClass = currentClass.getSuperClass();
            Method overridenMethod = currentClass.getSuperClass().getMethod(this.getName());
            if(overridenMethod != null) {
                /*check number of args between methods*/
                if (this.getArguments().size() != overridenMethod.getArguments().size()) {
                    throw new SemanticCheckerException("Error! Wrong number of arguments in overriding method " + this.getName() +
                            "of class " + this.getName());
                }
                /*methods have the same number of args but the arg types should agree too*/
                for (int i = 0; i < this.getArguments().size(); i++) {
                    if (!this.getArguments().get(i).getFieldTypeName().equals(overridenMethod.getArguments().get(i).getFieldTypeName()))
                        throw new SemanticCheckerException("Error! Wrong argument types in overriding method " + this.getName() +
                                "of class " + this.getName());
                }
                if (superClass.hasSuperClass()) {
                    currentClass = superClass.getSuperClass();
                    initialMethod = this.isOverridingMethod(currentClass);
                    initialMethod = true;
                } else {
                    initialMethod = true;
                }
            }
        }else{
            return false;
        }
        return initialMethod;

    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public AbstractType getReturnType() {
        return returnType;
    }

    public void setReturnType(AbstractType returnSymbolType) {
        this.returnType = returnSymbolType;
        this.setSize(8);
    }

    public List<AbstractType> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<AbstractType> arguments) {
        this.arguments = arguments;
    }

    public List<AbstractType> getVariables() {
        return variables;
    }

    public void setVariables(List<AbstractType> variables) {
        this.variables = variables;
    }

}

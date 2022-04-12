package temp.lang;

import temp.primitives.Primitive;
import temp.primitives.PrimitiveType;

import java.util.HashMap;

public class GlobalVariable {
    HashMap<String, Global> variables;

    public GlobalVariable() {
        this.variables = new HashMap<>();
    }

    public void addVariable(String name, Primitive value, PrimitiveType type, boolean isConstant) {
        this.variables.put(name, new Global(value, type, isConstant));
    }

    public void reassignVariable(String name, Primitive value) {
        Global variable = this.variables.get(name);

        this.variables.put(name, new Global(value, variable.getType(), variable.isConstant()));
    }

    public Primitive getValue(String name) {
        return this.variables.get(name).getValue();
    }

    public boolean containsVariable(String name) {
        Global global = this.variables.get(name);
        return global != null;
    }

    public int getGlobalCount() {
        return this.variables.size();
    }

    static class Global {
        private Primitive value;
        private PrimitiveType type;

        private final boolean isConstant;

        public Global(Primitive value, PrimitiveType type, boolean isConstant) {
            this.value = value;
            this.type = type;
            this.isConstant = isConstant;
        }

        public Primitive getValue() {
            return this.value;
        }

        public PrimitiveType getType() {
            return this.type;
        }

        public boolean isConstant() {
            return this.isConstant;
        }
    }
}

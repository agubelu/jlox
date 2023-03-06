package lox;

import lox.tokens.Token;

import java.util.HashMap;

public class Environment {
    private final HashMap<String, Object> variables;

    public Environment() {
        this.variables = new HashMap<>();
    }

    /**
     * Declares a new variable, with a (maybe null) initial value.
     * It is allowed to re-declare an existing variable, in that case,
     * it is simply overwritten.
     */
    public void declare(String name, Object value) {
        variables.put(name, value);
    }

    /**
     * Assigns a new value to an existing variable denoted by a token.
     * If the variable does not exist, this will throw a RuntimeError.
     */
    public void assign(Token varToken, Object value) {
        assertVariableExists(varToken);
        variables.put(varToken.getLexeme(), value);
    }

    /**
     * Retrieves the value of a given variable token.
     * If the variable does not exist, this will throw a RuntimeError.
     */
    public Object get(Token varToken) {
        assertVariableExists(varToken);
        return variables.get(varToken.getLexeme());
    }

    private void assertVariableExists(Token varToken) {
        var name = varToken.getLexeme();

        if(!variables.containsKey(name)) {
            throw new RuntimeError("Undefined variable '" + name + "'", varToken);
        }
    }
}

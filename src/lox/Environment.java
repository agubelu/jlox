package lox;

import lox.tokens.Token;

import java.util.HashMap;

public class Environment {
    private final HashMap<String, Object> variables;
    // The parent/outer environment, which is queried when a variable identifier
    // doesn't exist in this one
    private final Environment outer;

    public Environment() {
        this.variables = new HashMap<>();
        this.outer = null;
    }

    public Environment(Environment outer) {
        this.variables = new HashMap<>();
        this.outer = outer;
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
     * If the variable does not exist in the current environment,
     * this method recursively tries to carry out the operation in
     * outer environments. If it is not found in any environment,
     * this will throw a RuntimeError.
     */
    public void assign(Token varToken, Object value) {
        var name = varToken.getLexeme();

        if(variables.containsKey(name)) {
            // If it's found in this environment, reassign here
            variables.put(name, value);
        } else if(outer != null) {
            // If not found but there is an outer environment, try there
            outer.assign(varToken, value);
        } else {
            // No luck and no more enclosing environments, throw an error
            throw new RuntimeError("Undefined variable '" + name + "'", varToken);
        }
    }

    /**
     * Retrieves the value of a given variable token, trying outer environments recursively.
     * If the variable does not exist, this will throw a RuntimeError.
     */
    public Object get(Token varToken) {
        var name = varToken.getLexeme();

        if(variables.containsKey(name)) {
            // If it's found in this environment, return it here
            return variables.get(name);
        } else if(outer != null) {
            // If not found but there is an outer environment, try there
            return outer.get(varToken);
        } else {
            // No luck and no more enclosing environments, throw an error
            throw new RuntimeError("Undefined variable '" + name + "'", varToken);
        }
    }
}

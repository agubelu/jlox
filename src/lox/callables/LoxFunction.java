package lox.callables;

import lox.Environment;
import lox.Interpreter;
import lox.decl.FunctionDecl;
import lox.exceptions.ReturnExc;

import java.util.List;

public class LoxFunction implements LoxCallable {
    public final FunctionDecl fn;
    public final Environment closure;

    public LoxFunction(FunctionDecl fn, Environment closure) {
        this.fn = fn;
        this.closure = closure;
    }


    @Override
    public int getArity() {
        return this.fn.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        // Create a new environment based on this function's closure,
        // and bind the arguments to the parameters there
        var callEnv = new Environment(this.closure);
        for(int i = 0; i < args.size(); i++) {
            var paramName = this.fn.parameters.get(i).getLexeme();
            var paramValue = args.get(i);
            callEnv.declare(paramName, paramValue);
        }

        try {
            interpreter.runBlock(this.fn.body, callEnv);
        } catch(ReturnExc ret) {
            return ret.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn '" + this.fn.identifier.getLexeme() + "'>";
    }
}

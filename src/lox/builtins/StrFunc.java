package lox.builtins;

import lox.Interpreter;
import lox.callables.LoxCallable;

import java.util.List;

public class StrFunc implements LoxCallable {
    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return args.get(0).toString();
    }

    @Override
    public String toString() {
        return "<native fn 'str'>";
    }
}

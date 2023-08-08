package lox.builtins;

import lox.Interpreter;
import lox.callables.LoxCallable;

import java.util.List;

public class PrintFunc implements LoxCallable {
    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        System.out.println(args.get(0));
        return null;
    }

    @Override
    public String toString() {
        return "<native fn 'print'>";
    }
}

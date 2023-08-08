package lox.builtins;

import lox.Interpreter;
import lox.callables.LoxCallable;

import java.util.List;

public class TimeFunc implements LoxCallable {
    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return (double) System.currentTimeMillis() / 1000.0;
    }

    @Override
    public String toString() {
        return "<native fn 'time'>";
    }
}

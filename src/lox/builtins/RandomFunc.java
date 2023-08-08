package lox.builtins;

import lox.Interpreter;
import lox.callables.LoxCallable;

import java.util.List;

public class RandomFunc implements LoxCallable {
    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return Math.random();
    }

    @Override
    public String toString() {
        return "<native fn 'random'>";
    }
}

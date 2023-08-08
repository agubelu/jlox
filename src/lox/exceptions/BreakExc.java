package lox.exceptions;

public class BreakExc extends RuntimeException {
    public BreakExc() {
        super(null, null, false, false);
    }
}

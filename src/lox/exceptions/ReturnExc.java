package lox.exceptions;

public class ReturnExc extends RuntimeException {

    public final Object value;

    public ReturnExc(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}

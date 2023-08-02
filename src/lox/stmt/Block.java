package lox.stmt;

import lox.visitors.StatementVisitor;

import java.util.ArrayList;

public class Block extends Statement {

    public final ArrayList<Statement> stmts;

    public Block(ArrayList<Statement> stmts) {
        this.stmts = stmts;
    }

    @Override
    public<T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitBlock(this);
    }
}

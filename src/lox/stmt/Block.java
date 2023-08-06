package lox.stmt;

import lox.decl.Declaration;
import lox.visitors.StatementVisitor;

import java.util.ArrayList;

public class Block extends Statement {

    public final ArrayList<Declaration> decls;

    public Block(ArrayList<Declaration> decls) {
        this.decls = decls;
    }

    @Override
    public<T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitBlock(this);
    }
}

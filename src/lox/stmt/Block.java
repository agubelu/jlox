package lox.stmt;

import lox.decl.Declaration;
import lox.visitors.StatementVisitor;

import java.util.List;

public class Block extends Statement {

    public final List<Declaration> decls;

    public Block(List<Declaration> decls) {
        this.decls = decls;
    }

    @Override
    public<T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitBlock(this);
    }
}

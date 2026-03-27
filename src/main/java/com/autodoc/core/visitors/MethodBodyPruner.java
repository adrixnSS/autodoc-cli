package com.autodoc.core.visitors;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class MethodBodyPruner extends ModifierVisitor<Void> {

    @Override
    public Visitable visit(MethodDeclaration md, Void arg) {
        super.visit(md, arg);
        md.removeBody();
        return md;
    }

    @Override
    public Visitable visit(ConstructorDeclaration cd, Void arg) {
        super.visit(cd, arg);
        cd.setBody(new BlockStmt());
        return cd;
    }
}

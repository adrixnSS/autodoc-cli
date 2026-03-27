package com.autodoc.core.languages;

import com.autodoc.core.visitors.MethodBodyPruner;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class JavaPruner implements LanguagePruner {
    @Override
    public String prune(String content) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(content);
            cu.accept(new MethodBodyPruner(), null);
            return cu.toString();
        } catch (Exception e) {
            return "// Error podando Java: " + e.getMessage() + "\n" + content;
        }
    }
}

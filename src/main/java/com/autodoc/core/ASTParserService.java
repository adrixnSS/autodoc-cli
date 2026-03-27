package com.autodoc.core;

import com.autodoc.core.visitors.MethodBodyPruner;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileNotFoundException;

public class ASTParserService {

    public String parseAndPrune(File javaFile) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            cu.accept(new MethodBodyPruner(), null);
            return cu.toString();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + javaFile.getAbsolutePath());
            return "";
        } catch (Exception e) {
            System.err.println("Error parsing " + javaFile.getName() + ": " + e.getMessage());
            return "";
        }
    }
}

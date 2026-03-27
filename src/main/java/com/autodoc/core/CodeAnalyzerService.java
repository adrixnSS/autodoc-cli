package com.autodoc.core;

import com.autodoc.core.languages.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class CodeAnalyzerService {

    private final Map<String, LanguagePruner> pruners;

    public CodeAnalyzerService() {
        this.pruners = new HashMap<>();
        this.pruners.put("java", new JavaPruner());
        this.pruners.put("js", new JavaScriptPruner());
        this.pruners.put("ts", new JavaScriptPruner());
        this.pruners.put("py", new PythonPruner());
    }

    public String parseAndPrune(File file) {
        String extension = getExtension(file.getName());
        String content;
        try {
            content = Files.readString(file.toPath());
        } catch (IOException e) {
            return "// Error leyendo archivo: " + e.getMessage();
        }

        LanguagePruner pruner = pruners.get(extension);
        if (pruner != null) {
            return pruner.prune(content);
        }

        // Fallback: Si no hay pruner, enviamos solo firmas de nivel superior si es posible,
        // o un sample del archivo para evitar saturación de tokens.
        return "// [AUTODOC: No pruner for ." + extension + " - Enviando extracto]\n" + 
               getExcerpt(content);
    }

    private String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex == -1) return "";
        return fileName.substring(lastIndex + 1).toLowerCase();
    }

    private String getExcerpt(String content) {
        if (content.length() <= 2000) return content;
        return content.substring(0, 1000) + "\n... [Corte por ahorro de tokens] ...\n" + 
               content.substring(content.length() - 1000);
    }
}

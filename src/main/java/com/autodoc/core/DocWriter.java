package com.autodoc.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DocWriter {

    public void writeDoc(String workspace, String content, String fileName) {
        File docsDir = new File(workspace, "docs");
        if (!docsDir.exists() && !docsDir.mkdirs()) {
            System.err.println("No se pudo crear el directorio /docs en " + workspace);
            return;
        }

        File docFile = new File(docsDir, fileName);
        try (FileWriter writer = new FileWriter(docFile)) {
            writer.write(content);
            System.out.println("✅ Documentación salvada en: " + docFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error escribiendo documento: " + e.getMessage());
        }
    }
}

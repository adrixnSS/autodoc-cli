package com.autodoc.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GitService {

    public List<String> getModifiedJavaFiles(String workspace, String diffTarget) {
        List<String> modifiedFiles = new ArrayList<>();
        try {
            // Buscamos cambios en archivos de código comunes
            ProcessBuilder pb = new ProcessBuilder(
                    "git", "diff", "--name-only", diffTarget, "--", 
                    "*.java", "*.js", "*.ts", "*.py", "*.go", "*.rb", "*.php"
            );
            pb.directory(new java.io.File(workspace));
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                modifiedFiles.add(line.trim());
            }
            process.waitFor();
            
        } catch (Exception e) {
            System.err.println("Error ejecutando git diff: " + e.getMessage());
        }
        return modifiedFiles;
    }
}

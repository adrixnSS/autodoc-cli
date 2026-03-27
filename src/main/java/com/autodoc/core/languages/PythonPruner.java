package com.autodoc.core.languages;

import java.util.Scanner;

public class PythonPruner implements LanguagePruner {
    @Override
    public String prune(String content) {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(content);
        boolean inFunction = false;
        int baseIndent = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String trimmed = line.trim();

            if (trimmed.startsWith("def ") || trimmed.startsWith("class ")) {
                sb.append(line).append("\n");
                inFunction = true;
                baseIndent = getIndentLevel(line);
                sb.append(" ".repeat(baseIndent + 4)).append("pass # [AUTODOC: Podado]\n");
            } else if (inFunction) {
                if (trimmed.isEmpty() || getIndentLevel(line) > baseIndent) {
                    // Saltar líneas indentadas (cuerpo de la función)
                    continue;
                } else {
                    inFunction = false;
                    sb.append(line).append("\n");
                }
            } else {
                sb.append(line).append("\n");
            }
        }
        scanner.close();
        return sb.toString();
    }

    private int getIndentLevel(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') count++;
            else if (c == '\t') count += 4;
            else break;
        }
        return count;
    }
}

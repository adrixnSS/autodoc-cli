package com.autodoc.gui;

import com.autodoc.cli.AutoDocCLI;
import picocli.CommandLine;

public class Launcher {
    public static void main(String[] args) {
        // Si se pasan argumentos CLI (--workspace, --diff, etc.) → modo CLI
        if (args.length > 0) {
            int exitCode = new CommandLine(new AutoDocCLI()).execute(args);
            System.exit(exitCode);
        } else {
            // Sin argumentos → arrancar GUI JavaFX
            AutoDocGUI.main(args);
        }
    }
}

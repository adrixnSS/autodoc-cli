package com.autodoc.cli;

import com.autodoc.core.CodeAnalyzerService;
import com.autodoc.core.DocWriter;
import com.autodoc.core.GitService;
import com.autodoc.core.LLMClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "autodoc generate", mixinStandardHelpOptions = true, version = "1.0",
        description = "AutoDoc CLI: Generador Autónomo de Documentación.")
public class AutoDocCLI implements Callable<Integer> {

    @Option(names = {"-w", "--workspace"}, description = "Directorio del proyecto a analizar.", defaultValue = ".", required = false)
    private String workspace;

    @Option(names = {"-d", "--diff"}, description = "Rama git contra la que comparar (ej. origin/main).", required = true)
    private String diffTarget;

    @Option(names = {"-l", "--local"}, description = "Enrutar hacia instancia local de OpenClaw para Zero-Cost mode.", defaultValue = "false")
    private boolean isLocal;

    @Override
    public Integer call() throws Exception {
        System.out.println("🚀 Iniciando AutoDoc CLI...");
        
        GitService gitService = new GitService();
        List<String> modifiedFiles = gitService.getModifiedJavaFiles(workspace, diffTarget);
        
        if (modifiedFiles.isEmpty()) {
            System.out.println("🤷 No se detectaron archivos Java modificados.");
            return 0;
        }
        
        System.out.println("🔍 Archivos modificados detectados: " + modifiedFiles.size());
        
        CodeAnalyzerService analyzer = new CodeAnalyzerService();
        StringBuilder prunedCodeBuilder = new StringBuilder();
        
        for (String filePath : modifiedFiles) {
            File codeFile = new File(workspace, filePath);
            if (codeFile.exists()) {
                System.out.println("✂️ Podando lógica de: " + filePath);
                prunedCodeBuilder.append("=========== ").append(filePath).append(" ===========\n");
                prunedCodeBuilder.append(analyzer.parseAndPrune(codeFile)).append("\n");
            }
        }
        
        System.out.println("🧠 Enviando Código Universal Podado al LLM (Local=" + isLocal + ")...");
        System.out.println("\n--- [INICIO CÓDIGO PODADO] ---");
        System.out.println(prunedCodeBuilder.toString());
        System.out.println("--- [FIN CÓDIGO PODADO] ---\n");

        LLMClient llmClient = new LLMClient(isLocal);
        
        try {
            String documentation = llmClient.generateDocumentation(prunedCodeBuilder.toString());
            
            DocWriter writer = new DocWriter();
            writer.writeDoc(workspace, documentation, "Architecture_Update_" + System.currentTimeMillis() + ".md");
            
            System.out.println("🎉 Documentación autogenerada con éxito.");
        } catch(Exception e) {
            System.err.println("❌ Fallo durante la generación o conexión al LLM: " + e.getMessage());
            return 1;
        }

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AutoDocCLI()).execute(args);
        System.exit(exitCode);
    }
}

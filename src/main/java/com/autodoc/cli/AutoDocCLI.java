package com.autodoc.cli;

import com.autodoc.core.ASTParserService;
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
        
        ASTParserService parser = new ASTParserService();
        StringBuilder prunedAstBuilder = new StringBuilder();
        
        for (String filePath : modifiedFiles) {
            File javaFile = new File(workspace, filePath);
            if (javaFile.exists()) {
                System.out.println("✂️ Podando lógica de: " + filePath);
                prunedAstBuilder.append("=========== ").append(filePath).append(" ===========\n");
                prunedAstBuilder.append(parser.parseAndPrune(javaFile)).append("\n");
            }
        }
        
        System.out.println("🧠 Enviando AST puro al LLM (Local=" + isLocal + ")...");
        LLMClient llmClient = new LLMClient(isLocal);
        
        try {
            String documentation = llmClient.generateDocumentation(prunedAstBuilder.toString());
            
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

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

    @Option(names = {"-s", "--stdout"}, description = "Imprimir documentación directamente en la consola (útil para CI/CD).", defaultValue = "false")
    private boolean isStdout;

    @Option(names = {"--pr-comment"}, description = "Formatear salida específicamente para comentarios de Pull Request.", defaultValue = "false")
    private boolean isPRComment;

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
        System.out.println("📂 Escaneando dependencias del proyecto (Inteligencia de Contexto)...");
        analyzer.scanWorkspace(new File(workspace));

        StringBuilder prunedCodeBuilder = new StringBuilder();
        StringBuilder relatedContextBuilder = new StringBuilder();
        
        for (String filePath : modifiedFiles) {
            File codeFile = new File(workspace, filePath);
            if (codeFile.exists()) {
                System.out.println("✂️ Podando lógica de: " + filePath);
                String pruned = analyzer.parseAndPrune(codeFile);
                prunedCodeBuilder.append("=========== ").append(filePath).append(" ===========\n");
                prunedCodeBuilder.append(pruned).append("\n");
                
                // Buscar contexto relacionado para este archivo específico
                relatedContextBuilder.append(analyzer.findContextFor(pruned));
            }
        }
        
        System.out.println("🧠 Enviando Código con Referencia Cruzada al LLM (Local=" + isLocal + ")...");
        System.out.println("\n--- [INICIO CÓDIGO PODADO] ---");
        System.out.println(prunedCodeBuilder.toString());
        System.out.println("--- [FIN CÓDIGO PODADO] ---\n");

        LLMClient llmClient = new LLMClient(isLocal);
        
        try {
            // Unimos el código podado con el contexto de las clases relacionadas encontradas
            String fullPrompt = "CONTRATO Y REFERENCIAS:\n" + relatedContextBuilder.toString() + 
                               "\nCAMBIOS ACTUALES:\n" + prunedCodeBuilder.toString();
            String documentation = llmClient.generateDocumentation(fullPrompt);
            
            if (isPRComment) {
                documentation = "### 🤖 AutoDoc Architect Report\n\n" + 
                                "He analizado los cambios de este Pull Request y he generado la siguiente documentación técnica:\n\n" + 
                                documentation + 
                                "\n\n---\n*Generado automáticamente por AutoDoc CLI v2.0*";
            }

            if (isStdout) {
                System.out.println("\n--- [INICIO DOCUMENTACIÓN GENERADA] ---");
                System.out.println(documentation);
                System.out.println("--- [FIN DOCUMENTACIÓN GENERADA] ---\n");
            } else {
                DocWriter writer = new DocWriter();
                writer.writeDoc(workspace, documentation, "Architecture_Update_" + System.currentTimeMillis() + ".md");
            }
            
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

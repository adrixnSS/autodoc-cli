package com.autodoc.gui;

import com.autodoc.core.CodeAnalyzerService;
import com.autodoc.core.DocWriter;
import com.autodoc.core.GitService;
import com.autodoc.core.LLMClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.List;

public class MainController {

    @FXML private TextField workspacePathField;
    @FXML private ComboBox<String> diffModeCombo;
    @FXML private CheckBox localModeCheck;
    @FXML private TextArea outputArea;

    @FXML
    public void initialize() {
        diffModeCombo.setItems(FXCollections.observableArrayList(
            "Último Commit (HEAD~1)", "Rama Main", "Cambios sin commit"
        ));
        diffModeCombo.getSelectionModel().selectFirst();
        outputArea.setText("👋 ¡Bienvenido a AutoDoc Architect!\nSelecciona un proyecto para comenzar.");
    }

    @FXML
    private void handleBrowse() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta del Proyecto");
        File selectedDirectory = directoryChooser.showDialog(workspacePathField.getScene().getWindow());
        if (selectedDirectory != null) {
            workspacePathField.setText(selectedDirectory.getAbsolutePath());
            outputArea.setText("📂 Proyecto seleccionado: " + selectedDirectory.getName() + "\nPulsa 'Analizar Ahora' para generar la documentación.");
        }
    }

    @FXML
    private void handleAnalyze() {
        String workspacePath = workspacePathField.getText();
        if (workspacePath == null || workspacePath.isEmpty()) {
            outputArea.setText("⚠️ ERROR: Selecciona un workspace válido.");
            return;
        }

        outputArea.setText("🚀 Iniciando análisis...\n📂 Escaneando dependencias...");
        
        // Ejecución asíncrona para no congelar la UI
        new Thread(() -> {
            try {
                String diffTarget = "HEAD~1"; 
                boolean isLocal = localModeCheck.isSelected();

                // Instanciación Lazy para evitar fallos de carga en el arranque
                GitService gitService = new GitService();
                List<String> modifiedFiles = gitService.getModifiedJavaFiles(workspacePath, diffTarget);

                if (modifiedFiles.isEmpty()) {
                    updateUI("✅ No se detectaron cambios que documentar en: " + diffTarget);
                    return;
                }

                CodeAnalyzerService analyzer = new CodeAnalyzerService();
                analyzer.scanWorkspace(new File(workspacePath));

                StringBuilder prunedCodeBuilder = new StringBuilder();
                StringBuilder relatedContextBuilder = new StringBuilder();

                for (String filePath : modifiedFiles) {
                    File codeFile = new File(workspacePath, filePath);
                    if (codeFile.exists()) {
                        String pruned = analyzer.parseAndPrune(codeFile);
                        prunedCodeBuilder.append("=========== ").append(filePath).append(" ===========\n");
                        prunedCodeBuilder.append(pruned).append("\n");
                        relatedContextBuilder.append(analyzer.findContextFor(pruned));
                    }
                }

                updateUI("🧠 Generando documentación con IA...");
                
                LLMClient llmClient = new LLMClient(isLocal, false, "gemini-1.5-flash");
                String fullPrompt = "CONTRATO Y REFERENCIAS:\n" + relatedContextBuilder.toString() + 
                                   "\nCAMBIOS ACTUALES:\n" + prunedCodeBuilder.toString();
                
                String documentation = llmClient.generateDocumentation(fullPrompt);
                
                updateUI(documentation);

            } catch (Exception e) {
                updateUI("❌ ERROR DE MOTOR: " + e.getMessage() + "\n\nTip: Asegúrate de que Git está instalado y la ruta es un repositorio.");
            }
        }).start();
    }

    @FXML
    private void handleClear() {
        outputArea.clear();
    }

    @FXML
    private void handleSave() {
        String doc = outputArea.getText();
        if (doc.isEmpty()) return;

        try {
            DocWriter writer = new DocWriter();
            writer.writeDoc(workspacePathField.getText(), doc, "GUI_Export_" + System.currentTimeMillis() + ".md");
            outputArea.appendText("\n\n✅ [SISTEMA]: Documentación guardada con éxito en /docs");
        } catch (Exception e) {
            outputArea.appendText("\n\n❌ [SISTEMA]: Error guardando: " + e.getMessage());
        }
    }

    private void updateUI(String text) {
        Platform.runLater(() -> outputArea.setText(text));
    }
}

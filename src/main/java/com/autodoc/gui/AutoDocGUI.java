package com.autodoc.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class AutoDocGUI extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/autodoc/gui/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        
        // Estilo moderno: Ventana sin bordes gruesos
        stage.setTitle("AutoDoc Architect v3.0");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

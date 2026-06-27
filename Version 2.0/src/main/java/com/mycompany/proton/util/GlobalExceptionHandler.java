package com.mycompany.proton.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace(); // ainda grava no log
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro Inesperado");
            alert.setHeaderText("Ocorreu um erro no sistema.");
            alert.setContentText("Detalhes: " + e.getMessage() + "\n\nPor favor, entre em contato com o suporte.");
            alert.showAndWait();
        });
    }
}
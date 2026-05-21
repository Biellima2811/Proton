package com.mycompany.proton;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private Label lblErro;

    @FXML
    private void entrar(ActionEvent event) {
        String email = txtEmail.getText().trim();

        // Limpa mensagens de erro anteriores
        lblErro.setVisible(false);

        if (email.isEmpty()) {
            mostrarErro("Digite seu e-mail corporativo.");
            return;
        }

        // Validação rigorosa do domínio
        if (email.toLowerCase().endsWith("@fortestecnologia.com.br")) {
            System.out.println("Login autorizado para: " + email);
            
            // Salvamos o usuário na sessão global do App
            App.setUsuarioLogado(email);
            
            try {
                // Navega para a tela principal (Dashboard)
                App.iniciarPainelPrincipal();
            } catch (IOException ex) {
                mostrarErro("Erro ao carregar o sistema.");
                ex.printStackTrace();
            }
        } else {
            mostrarErro("Acesso restrito a técnicos autorizados.");
        }
    }

    private void mostrarErro(String mensagem) {
        lblErro.setText(mensagem);
        lblErro.setVisible(true);
    }
}
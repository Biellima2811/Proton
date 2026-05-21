package com.mycompany.proton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ConfigBancoController{
    
    @FXML
    private void salvarConfiguracao(ActionEvent event){
        String ipServidor = "localhost";
        String nomeBanco = "proton";
        String usuario = "postgres";
        String senha = "tec@123";
        
        String urlConexao = "jdbc:postgresql://" + ipServidor + ":5432/" + nomeBanco;
        
        try(Connection conexao = DriverManager.getConnection(urlConexao, usuario, senha)){
            System.out.println("Sucesso!, Conectado ao SGBD PostgreSQL");
            try {
                App.iniciarPainelPrincipal();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.printf("ERRO de PostgreSQL: %s", e.getMessage());
        }
    }
}
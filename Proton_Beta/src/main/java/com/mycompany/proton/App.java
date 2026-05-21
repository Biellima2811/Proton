package com.mycompany.proton;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private static Stage palcoGlobal;
    private static String usuarioLogado;

    @Override
    public void start(Stage stage) throws IOException {
        palcoGlobal = stage;
        
        // 1. Inicia com a tela de Login (pequena e fixa)
        scene = new Scene(loadFXML("primary"));
        palcoGlobal.setScene(scene);
        palcoGlobal.setTitle("Proton - Acesso Restrito");
        palcoGlobal.setMaximized(true);
        palcoGlobal.show();
    }

    // Método que o LoginController chama quando a senha está correta
    public static void iniciarPainelPrincipal() throws IOException {
        scene.setRoot(loadFXML("primary"));
        
        // 2. Transforma a janela em Tela Cheia para o Dashboard
        palcoGlobal.setTitle("Proton - Gestão de Infraestrutura");
        palcoGlobal.setResizable(true);
        palcoGlobal.setMaximized(true); 
    }

    // Facilita carregar arquivos FXML
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    
    // Controle de Sessão (Quem está usando o sistema)
    public static String getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void setUsuarioLogado(String email) {
        usuarioLogado = email;
    }

    public static void main(String[] args) {
        launch();
    }
}
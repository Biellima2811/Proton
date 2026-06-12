package com.mycompany.proton;

import javafx.application.Application;  // Classe base para aplicações JavaFX
import javafx.fxml.FXMLLoader;           // Carrega arquivos FXML que definem a interface
import javafx.scene.Parent;              // Nó raiz de uma cena (container principal)
import javafx.scene.Scene;               // Representa a cena (conteúdo da janela)
import javafx.stage.Stage;               // Representa a janela principal (palco)
import java.io.IOException;              // Exceção para operações de entrada/saída
import javafx.scene.image.Image;         // Para carregar o ícone da aplicação

/**
 * Classe principal da aplicação Proton. Gerencia a janela (Stage) e a troca de
 * telas entre Login e Painel Principal. Mantém o controle de sessão do usuário
 * logado.
 */
public class App extends Application {

    // Cena atualmente exibida – será reutilizada para trocar a raiz (FXML)
    private static Scene scene;

    // Referência estática à janela principal, permitindo alterá-la de qualquer controlador
    private static Stage palcoGlobal;

    // Armazena o e‑mail do usuário autenticado (controle de sessão)
    private static String usuarioLogado;

    /**
     * Método de entrada da aplicação JavaFX. Configura e exibe a tela de login.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Guarda a referência da janela principal para uso posterior
        palcoGlobal = stage;

        // 1. Carrega o arquivo FXML da tela de login e cria a cena inicial.
        scene = new Scene(loadFXML("/com/mycompany/proton/views/login"));   // "login.fxml" é o layout da tela de login

        // Define a cena carregada no palco (janela)
        palcoGlobal.setScene(scene);

        // Título da janela – indica que o acesso é restrito
        palcoGlobal.setTitle("Proton - Acesso Restrito");

        // Janela inicia no modo normal (não maximizada) – tamanho fixo do login
        palcoGlobal.setMaximized(false);

        // Adiciona um ícone personalizado à janela (arquivo icone.png dentro do projeto)
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/com/mycompany/proton/images/icone.png")));

        // Exibe a janela na tela
        palcoGlobal.show();
    }

    /**
     * Chamado pelo LoginController quando o login é realizado com sucesso.
     * Substitui a raiz da cena pela tela principal e maximiza a janela.
     */
    public static void iniciarPainelPrincipal() throws IOException {
        // Carrega o FXML da tela principal ("primary.fxml") e o define como nova raiz da cena
        scene.setRoot(loadFXML("/com/mycompany/proton/views/primary"));

        // Atualiza o título para refletir a área de gestão
        palcoGlobal.setTitle("Proton - Gestão de Infraestrutura");

        // Permite redimensionamento da janela (útil se o usuário sair do modo maximizado)
        palcoGlobal.setResizable(true);

        // Maximiza a janela para ocupar toda a tela (dashboard)
        palcoGlobal.setMaximized(true);
    }

    /**
     * Método auxiliar para carregar um arquivo FXML.
     *
     * @param fxml Nome do arquivo FXML sem a extensão ".fxml".
     * @return O nó raiz (Parent) carregado a partir do arquivo.
     * @throws IOException Se o arquivo não for encontrado ou houver erro na
     * leitura.
     */
    private static Parent loadFXML(String fxml) throws IOException {
        // Constrói um carregador para o recurso FXML localizado junto da classe App
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        // Carrega o arquivo e retorna o nó raiz da interface
        return fxmlLoader.load();
    }

    // ----- Controle de Sessão -----
    /**
     * Retorna o e‑mail do usuário atualmente logado.
     *
     * @return String com o e‑mail do usuário.
     */
    public static String getUsuarioLogado() {
        return usuarioLogado;
    }

    /**
     * Define o usuário logado (armazenado na sessão estática).
     *
     * @param email E‑mail do usuário que acabou de autenticar.
     */
    public static void setUsuarioLogado(String email) {
        usuarioLogado = email;
    }

    /**
     * Ponto de entrada da JVM. Lança a aplicação JavaFX.
     */
    public static void main(String[] args) {
        // Inicializa o ambiente JavaFX e chama o método start(Stage)
        launch();
    }
}

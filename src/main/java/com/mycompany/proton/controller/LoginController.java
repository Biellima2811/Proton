package com.mycompany.proton.controller;

import com.mycompany.proton.App;
import com.mycompany.proton.util.GerenciadorDeLog;
import com.mycompany.proton.util.Seguranca;
import java.io.IOException;                     // Exceção para falhas ao carregar FXML
import java.net.URL;                           // Representa a localização do recurso FXML
import java.sql.Connection;                    // Conexão JDBC com o banco de dados
import java.sql.DriverManager;                 // Gerencia conexões JDBC
import java.sql.PreparedStatement;             // Executa consultas SQL parametrizadas
import java.sql.ResultSet;                     // Armazena o resultado de uma consulta
import java.sql.SQLException;                  // Exceção de SQL
import java.util.ResourceBundle;               // Pacote de recursos (idioma, etc.)
import java.util.prefs.Preferences;            // BIBLIOTECA PARA SALVAR AS PREFERÊNCIAS LOCAIS
import javafx.application.Platform;            // Executa código no thread da UI
import javafx.event.ActionEvent;               // Evento disparado por botões
import javafx.fxml.FXML;                       // Anotação para vincular elementos da interface
import javafx.fxml.FXMLLoader;                 // Carrega arquivos FXML
import javafx.fxml.Initializable;              // Interface para inicialização do controlador
import javafx.scene.Parent;                    // Nó raiz de uma cena
import javafx.scene.Scene;                     // Representa a cena (conteúdo da janela)
import javafx.scene.control.Alert;
import javafx.scene.control.Button;            // Componente de botão
import javafx.scene.control.CheckBox;          // NOVO ELEMENTO
import javafx.scene.control.Hyperlink;         // NOVO ELEMENTO
import javafx.scene.control.Label;             // Rótulo de texto
import javafx.scene.control.PasswordField;     // Campo de texto para senha (mascarado)
import javafx.scene.control.TextField;         // Campo de texto comum
import javafx.scene.input.KeyCode;             // Código da tecla pressionada
import javafx.scene.input.KeyEvent;            // Evento de teclado
import javafx.scene.layout.VBox;               // Container vertical
import javafx.stage.Modality;                  // Define modalidade de uma janela
import javafx.stage.Stage;                     // Janela do JavaFX

/**
 * Controlador da tela de Login do sistema Proton.
 *
 * Responsável por: - Autenticar o usuário contra a tabela "usuarios_sistema" no
 * PostgreSQL. - Detectar credenciais de banco inválidas e abrir a tela de
 * configuração. - Forçar a troca da senha padrão no primeiro acesso. - Navegar
 * para o painel principal após autenticação bem-sucedida.
 */
public class LoginController implements Initializable {

    // ==================== COMPONENTES VISUAIS ====================
    @FXML
    private VBox boxLogin;                   // Painel que contém os campos de login (e-mail e senha)
    @FXML
    private VBox boxNovaSenha;               // Painel exibido apenas na troca da senha padrão

    @FXML
    private TextField txtEmail;              // Campo de e-mail do usuário
    @FXML
    private PasswordField txtSenha;          // Campo de senha (mascarado)

    @FXML
    private PasswordField txtNovaSenha;      // Campo para a nova senha (primeiro acesso)
    @FXML
    private PasswordField txtConfirmaSenha;  // Campo de confirmação da nova senha

    @FXML
    private Button btnEntrar;                // Botão "Entrar" (ou "Salvar e Acessar" no primeiro acesso)
    @FXML
    private Label lblErro;                   // Rótulo que exibe mensagens de erro/aviso

    @FXML
    private CheckBox chkLembrar;
    @FXML
    private Hyperlink linkEsqueciSenha;

    // ==========================================
    // CONTROLE DE VERSÃO DA APLICAÇÃO
    // ==========================================
    public static final String VERSAO_APP = "1.0"; // Atualize isso a cada nova versão do .exe

    // ==================== CONTROLE DE ESTADO ====================
    private boolean modoPrimeiroAcesso = false;  // true quando o usuário precisa trocar a senha padrão
    private int usuarioIdLogando = -1;           // ID do usuário no banco durante o primeiro acesso

    // Instância de preferências do usuário (salva no Registro do Windows ou prefs do Linux/Mac)
    private Preferences preferenciasIniciais;

    /**
     * Inicializa o controlador após o FXML ser carregado. Configura o botão
     * "Entrar" como botão padrão (responde ao ENTER) e adiciona um listener
     * para fechar o sistema ao pressionar ESC.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Define o botão "Entrar" como default: pressionar ENTER em qualquer campo o aciona
        btnEntrar.setDefaultButton(true);

        // Inicializa o objeto de preferências para essa classe
        preferenciasIniciais = Preferences.userNodeForPackage(LoginController.class);

        // --- LÓGICA DO "LEMBRAR DE MIM" AO ABRIR O SISTEMA ---
        String emailSalvo = preferenciasIniciais.get("proton_email_lembrado", "");
        if (!emailSalvo.isEmpty()) {
            txtEmail.setText(emailSalvo);
            chkLembrar.setSelected(true);
            Platform.runLater(() -> txtSenha.requestFocus());// Move o cursor direto pra senha!
        }

        // O Platform.runLater garante que a cena já esteja disponível antes de adicionar o listener
        Platform.runLater(() -> {
            // Obtém a cena a partir do botão e escuta eventos de teclado
            btnEntrar.getScene().setOnKeyPressed((KeyEvent event) -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    System.exit(0); // Fecha a aplicação completamente
                }
            });
            if (ConfigBancoController.getIpServidor() == null || ConfigBancoController.getIpServidor().trim().isEmpty()
                    || ConfigBancoController.getNomeBanco() == null || ConfigBancoController.getNomeBanco().trim().isEmpty()) {

                mostrarErro("Configuração de conexão não encontrada. Por favor, defina os parâmetros do banco.");
                abrirConfiguracaoBanco(); // Abre a tela automaticamente no início
            }
        });
    }

    /**
     * Cria e retorna uma conexão JDBC com o banco de dados PostgreSQL. Utiliza
     * as configurações lidas do arquivo "config_banco.properties" através do
     * ConfigBancoController.
     */
    private Connection conectar() throws SQLException {
        // Monta a URL de conexão no formato jdbc:postgresql://<IP>:5432/<banco>
        String urlConexao = String.format("jdbc:postgresql://%s:5432/%s",
                ConfigBancoController.getIpServidor(),
                ConfigBancoController.getNomeBanco());
        // Retorna a conexão
        return DriverManager.getConnection(urlConexao,
                ConfigBancoController.getUsuarioBD(),
                ConfigBancoController.getSenhaBD());
    }

    /**
     * Método disparado ao clicar no botão "Entrar" (ou ao pressionar ENTER).
     *
     * Dois fluxos possíveis: 1. Login normal – verifica e-mail, senha e status
     * no banco. 2. Primeiro acesso – se a senha padrão foi detectada, direciona
     * para troca.
     */
    @FXML
    private void entrar(ActionEvent event) {
        if (!validarVersaoSistema()) {
            return; // Bloqueia se a versão estiver errada ou sem conexão
        }
        // Esconde qualquer erro anterior e define a cor padrão (vermelho)
        lblErro.setVisible(false);
        lblErro.setStyle("-fx-text-fill: #dc3545;");

        // Se já está no modo de primeiro acesso, processa a criação da nova senha
        if (modoPrimeiroAcesso) {
            processarNovaSenha();
            return;
        }

        // Remove espaços e converte para minúsculas para padronizar
        String email = txtEmail.getText().trim().toLowerCase();
        String senha = txtSenha.getText();

        // Validação básica: ambos os campos devem estar preenchidos
        if (email.isEmpty() || senha.isEmpty()) {
            mostrarErro("Preencha seu E-mail e Senha de acesso.");
            return;
        }

        // Restrição de domínio: apenas e-mails corporativos Fortes
        if (!email.endsWith("@fortestecnologia.com.br")) {
            mostrarErro("Acesso restrito a colaboradores da Fortes Tecnologia.");
            return;
        }

        // Consulta o banco para verificar as credenciais
        String sql = "SELECT id, senha, status FROM usuarios_sistema WHERE email = ?";
        try (Connection conn = conectar(); PreparedStatement cmd = conn.prepareStatement(sql)) {

            cmd.setString(1, email);             // Substitui o ? pelo e-mail digitado
            try (ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {                 // Se encontrou o usuário
                    String senhaBanco = rs.getString("senha");
                    String status = rs.getString("status");

                    // Verifica se o usuário está ativo
                    if (!status.equalsIgnoreCase("ATIVO")) {
                        mostrarErro("Acesso negado: Sua conta foi desativada.");
                        return;
                    }

                    // ===============================================================
                    // TÉCNICA DE FALLBACK: Aceita hashes novos E senhas antigas
                    // ===============================================================
                    boolean senhaValida = false;
                    if (senhaBanco != null && senhaBanco.startsWith("$2a$")) {
                        // É um hash gerado pelo jBCrypt, verifica usando a biblioteca
                        senhaValida = Seguranca.verificarSenha(senha, senhaBanco);
                    } else {
                        // É a senha antiga em texto puro (legado), verifica direto
                        senhaValida = senha.equals(senhaBanco);
                    }

                    // Se a senha (criptografada ou não) estiver correta:
                    if (senhaValida) {
                        if (chkLembrar.isSelected()) {
                            preferenciasIniciais.put("proton_email_lembrado", email);
                        } else {
                            preferenciasIniciais.remove("proton_email_lembrado");     // Esquece o e-mail
                        }
                        // Se a senha digitada for a padrão ("fortes123"), força a troca
                        if (senha.equals("fortes123")) {
                            usuarioIdLogando = rs.getInt("id"); // Guarda o ID
                            ativarModoPrimeiroAcesso();         // Muda para tela de nova senha
                        } else {
                            GerenciadorDeLog.info("Usuário logado no sistema com sucesso: " + email);
                            // Senha personalizada e correta: faz login e abre o sistema
                            App.setUsuarioLogado(email);
                            App.iniciarPainelPrincipal();
                        }
                    } else {
                        // Senha incorreta
                        mostrarErro("Credenciais inválidas. Verifique sua senha.");
                    }
                } else {
                    // E-mail não encontrado na tabela
                    mostrarErro("Usuário não encontrado.");
                }
            }
        } catch (SQLException e) {
            GerenciadorDeLog.erro("Falha de conexão com o BD PostgreSQL: " + e.getMessage());
            // Se der erro de conexão (banco offline, IP errado, credenciais de BD inválidas),
            // automaticamente abre a tela de configuração do banco para correção.
            System.out.println("Falha na conexão. Abrindo tela de configuração do banco: " + e.getMessage());
            abrirConfiguracaoBanco();
        } catch (IOException e) {
            mostrarErro("Erro interno ao carregar a interface principal.");
            e.printStackTrace();
        }
    }

    // --- NOVO MÉTODO: ESQUECI MINHA SENHA ---
    @FXML
    private void esqueciMinhaSenha(ActionEvent event) {
        exibirAlerta("Recuperação de Senha",
                "Para garantir a segurança do ambiente corporativo (LGPD), a recuperação de senha é feita de forma manual.\n\n"
                + "Por favor, solicite a redefinição da sua senha diretamente ao administrador do sistema (Gabriel Levi ou Gestão da Equipe N2).",
                Alert.AlertType.INFORMATION);
    }

    //  MÉTODO: ABRIR TELA DE CONFIGURAÇÃO VIA LINK ---
    @FXML
    private void abrirTelaConfigBancoManualmente(ActionEvent event) {
        abrirConfiguracaoBanco();
    }

    /**
     * Abre a janela modal de configuração do banco de dados (ConfigBanco.fxml).
     * Utilizada quando o sistema não consegue conectar ao PostgreSQL.
     */
    private void abrirConfiguracaoBanco() {
        try {
            // Carrega o FXML da tela de configuração
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/mycompany/proton/views/ConfigBanco.fxml"));
            Parent root = loader.load();

            // Cria uma nova janela (Stage)
            Stage stage = new Stage();
            stage.setTitle("Proton - Configuração de Banco de Dados");
            stage.setScene(new Scene(root));
            // Define como modal: bloqueia a tela de login até que seja fechada
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);   // Tamanho fixo
            stage.showAndWait();         // Exibe e aguarda o fechamento
        } catch (IOException ex) {
            mostrarErro("Não foi possível carregar a tela de configuração.");
        }
    }

    /**
     * Ativa o modo de primeiro acesso: - Esconde o painel de login normal. -
     * Exibe o painel de criação de nova senha. - Altera o rótulo e o texto do
     * botão.
     */
    private void ativarModoPrimeiroAcesso() {
        modoPrimeiroAcesso = true;

        // Esconde o painel de login e exibe o painel de nova senha
        boxLogin.setVisible(false);
        boxLogin.setManaged(false);     // managed=false remove o espaço ocupado pelo VBox

        boxNovaSenha.setVisible(true);
        boxNovaSenha.setManaged(true);

        // Altera o texto do botão para indicar a nova ação
        btnEntrar.setText("Salvar e Acessar o Sistema");

        // Aviso em laranja
        lblErro.setStyle("-fx-text-fill: #fd7e14;");
        mostrarErro("Ação Necessária: Substitua sua senha padrão para garantir a segurança de sua conta.");
    }

    /**
     * Processa a criação/atualização da senha no primeiro acesso. Validações: -
     * Campos preenchidos - Mínimo de 6 caracteres - Não pode ser igual à senha
     * padrão - Confirmação deve coincidir Em caso de sucesso, atualiza o banco
     * e abre o painel principal.
     */
    private void processarNovaSenha() {
        String novaSenha = txtNovaSenha.getText();
        String confirmaSenha = txtConfirmaSenha.getText();

        // Verifica preenchimento
        if (novaSenha.isEmpty() || confirmaSenha.isEmpty()) {
            mostrarErro("Preencha ambos os campos para registrar a nova senha.");
            return;
        }

        // Tamanho mínimo
        if (novaSenha.length() < 6) {
            mostrarErro("Sua nova senha deve conter no mínimo 6 caracteres.");
            return;
        }

        // Proíbe reutilizar a senha padrão
        if (novaSenha.equals("fortes123")) {
            mostrarErro("Sua nova senha não pode ser idêntica à senha padrão.");
            return;
        }

        // Confere se os dois campos são iguais
        if (!novaSenha.equals(confirmaSenha)) {
            mostrarErro("As senhas não coincidem. Digite novamente.");
            return;
        }

        // Atualiza a senha no banco de dados
        String sql = "UPDATE usuarios_sistema SET senha = ? WHERE id = ?";
        try (Connection conn = conectar(); PreparedStatement cmd = conn.prepareStatement(sql)) {

            String senhaHash = Seguranca.hashSenha(novaSenha);           // Nova senha (idealmente deveria ser hash)
            cmd.setString(1, senhaHash);
            cmd.setInt(2, usuarioIdLogando);       // ID do usuário capturado no login
            cmd.executeUpdate();                   // Executa o UPDATE

            // Senha salva com sucesso – realiza o login e abre o sistema
            App.setUsuarioLogado(txtEmail.getText().trim().toLowerCase());
            App.iniciarPainelPrincipal();

        } catch (SQLException e) {
            mostrarErro("Falha ao salvar a nova senha no banco: " + e.getMessage());
        } catch (IOException e) {
            mostrarErro("Erro interno ao carregar a interface principal.");
        }
    }

    /**
     * Exibe uma mensagem de erro/aviso no label de feedback. Torna o label
     * visível e define o texto.
     */
    private void mostrarErro(String mensagem) {
        lblErro.setText(mensagem);
        lblErro.setVisible(true);
    }

    private boolean validarVersaoSistema() {
        String sql = "SELECT versao_db FROM controle_versao ORDER BY id DESC LIMIT 1";
        try (Connection conn = conectar(); PreparedStatement cmd = conn.prepareStatement(sql); ResultSet rs = cmd.executeQuery()) {

            if (rs.next()) {
                String versaoBanco = rs.getString("versao_db");
                if (!VERSAO_APP.equals(versaoBanco)) {
                    exibirAlerta("Atualização Obrigatória",
                            "A versão do seu sistema (" + VERSAO_APP + ") é incompatível com a versão atual do Banco de Dados (" + versaoBanco + ").\n\nPor favor, acesso o servidor pelo caminho \\\\132.226.255.120\\Versões Proton.",
                            Alert.AlertType.ERROR);
                    return false;
                }
                return true; // Versões batem!
            }
        } catch (SQLException e) {
            System.out.println("Erro de conexão detectado na validação de versão: " + e.getMessage());

            // --- CORREÇÃO APLICADA AQUI ---
            // Em vez de dar apenas um alerta, forçamos a tela de configuração a abrir!
            mostrarErro("Falha de conexão com o banco. Verifique as configurações.");
            abrirConfiguracaoBanco();

            return false;
        }
        return false;
    }

    /**
     * Ação do botão "Sair" (caso exista no FXML). Encerra completamente a
     * aplicação.
     */
    @FXML
    private void sair() {
        System.exit(0);
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}

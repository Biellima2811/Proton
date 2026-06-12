package com.mycompany.proton.controller;

import com.mycompany.proton.App;
import com.mycompany.proton.util.Seguranca;
import com.mycompany.proton.controller.ConfigBancoController;
import java.io.File;                    // Representa o arquivo de backup
import java.io.BufferedReader;         // Lê a saída do processo
import java.io.InputStreamReader;      // Converte bytes da saída do processo em caracteres
import java.net.URL;                   // Usado no initialize
import java.sql.Connection;            // Conexão com banco
import java.sql.DriverManager;         // Gerencia conexões JDBC
import java.sql.PreparedStatement;     // Consultas SQL parametrizadas
import java.sql.ResultSet;             // Resultado de consultas
import java.sql.SQLException;          // Exceção de SQL
import java.time.LocalDate;            // Data atual para nomear o backup
import java.util.ResourceBundle;       // Pacote de recursos (initialize)
import javafx.beans.property.SimpleIntegerProperty;  // Propriedade observável para inteiros
import javafx.beans.property.SimpleStringProperty;   // Propriedade observável para strings
import javafx.collections.FXCollections;             // Cria listas observáveis
import javafx.collections.ObservableList;            // Lista que notifica a TableView
import javafx.event.ActionEvent;                    // Evento de ação (botões)
import javafx.fxml.FXML;                            // Anotação para elementos da interface
import javafx.fxml.Initializable;                  // Interface para inicialização
import javafx.scene.control.Alert;                  // Diálogos de alerta
import javafx.scene.control.ComboBox;               // Caixa de seleção
import javafx.scene.control.Label;                  // Rótulo de texto
import javafx.scene.control.PasswordField;          // Campo de senha
import javafx.scene.control.TableColumn;            // Coluna da tabela
import javafx.scene.control.TableView;              // Tabela
import javafx.scene.control.TextField;              // Campo de texto
import javafx.stage.FileChooser;                    // Diálogo para escolher onde salvar
import javafx.stage.Stage;                          // Janela da aplicação

/**
 * Controlador da tela de Configurações e Administração do sistema. Permite: -
 * Gestão de usuários (adicionar/remover, apenas para perfis N2 e MASTER). -
 * Teste de latência da conexão com o banco. - Realizar backup do banco de dados
 * via pg_dump (apenas MASTER).
 */
public class FormConfiguracoesController implements Initializable {

    // ==================== ELEMENTOS DA INTERFACE ====================
    @FXML
    private TextField txtNovoEmail;              // E-mail do novo usuário a cadastrar
    @FXML
    private PasswordField txtNovaSenha;          // Senha para o novo usuário
    @FXML
    private ComboBox<String> cbNivelAcesso;      // Nível de acesso (TECNICO, N2, MASTER)
    @FXML
    private TableView<Usuario> tabelaUsuarios;   // Tabela de usuários cadastrados
    @FXML
    private TableColumn<Usuario, Integer> colUserId;       // Coluna ID
    @FXML
    private TableColumn<Usuario, String> colUserEmail;     // Coluna E-mail
    @FXML
    private TableColumn<Usuario, String> colUserPermissao; // Coluna Permissão
    @FXML
    private TableColumn<Usuario, String> colUserStatus;    // Coluna Status
    @FXML
    private Label lblResultadoLatencia;           // Exibe resultado do teste de latência
    @FXML
    private Label lblStatusBackup;                // Exibe status do backup

    // Lista observável que alimenta a tabela de usuários
    private ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();

    // Nível de acesso do usuário logado, obtido do banco (padrão TECNICO para segurança)
    private String nivelAcessoLogado = "TECNICO";

    // ==================== INICIALIZAÇÃO ====================
    /**
     * Chamado automaticamente pelo JavaFX após o FXML ser carregado. Configura
     * os combos e colunas, verifica permissão e carrega dados.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Popula o ComboBox de nível de acesso
        cbNivelAcesso.setItems(FXCollections.observableArrayList("TECNICO", "N2", "MASTER"));

        // Liga as colunas da tabela às propriedades da classe Usuario
        colUserId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colUserEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colUserPermissao.setCellValueFactory(cellData -> cellData.getValue().permissaoProperty());
        colUserStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Busca o nível de acesso real do usuário no banco
        obterNivelAcessoDoBanco();
        // Habilita/desabilita controles conforme permissão
        verificarPermissaoDeAcessoTela();
    }

    // ==================== CONEXÃO COM BANCO ====================
    /**
     * Cria e retorna uma nova conexão JDBC usando as configurações salvas.
     */
    private Connection conectar() throws SQLException {
        // Monta a URL JDBC com IP e nome do banco
        String urlConexao = String.format("jdbc:postgresql://%s:5432/%s",
                ConfigBancoController.getIpServidor(),
                ConfigBancoController.getNomeBanco());
        // Abre a conexão
        return DriverManager.getConnection(urlConexao,
                ConfigBancoController.getUsuarioBD(),
                ConfigBancoController.getSenhaBD());
    }

    // ==========================================
    // SEGURANÇA BASEADA NO BANCO DE DADOS
    // ==========================================
    /**
     * Consulta a tabela usuarios_sistema para obter o nível de acesso do e-mail
     * logado. O resultado é armazenado em nivelAcessoLogado.
     */
    private void obterNivelAcessoDoBanco() {
        String emailLogado = App.getUsuarioLogado();
        if (emailLogado == null || emailLogado.isEmpty()) {
            return; // Nenhum usuário logado, mantém padrão TECNICO
        }

        String sql = "SELECT nivel_acesso FROM usuarios_sistema WHERE email = ?";
        try (Connection conn = conectar(); PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, emailLogado.toLowerCase());
            try (ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    nivelAcessoLogado = rs.getString("nivel_acesso").toUpperCase();
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao validar credenciais do banco: " + e.getMessage());
        }
    }

    /**
     * Bloqueia ou libera os controles de gestão de usuários conforme o perfil.
     * Apenas MASTER e N2 podem ver/gerenciar usuários.
     */
    private void verificarPermissaoDeAcessoTela() {
        if (nivelAcessoLogado.equals("MASTER") || nivelAcessoLogado.equals("N2")) {
            // Tem permissão: carrega a lista de usuários
            carregarUsuarios();
        } else {
            // Apenas TECNICO: desabilita a tabela e os campos de cadastro
            tabelaUsuarios.setDisable(true);
            txtNovoEmail.setDisable(true);
            txtNovaSenha.setDisable(true);
            cbNivelAcesso.setDisable(true);
        }
    }

    /**
     * Carrega todos os usuários da tabela usuarios_sistema e os exibe na
     * TableView.
     */
    private void carregarUsuarios() {
        listaUsuarios.clear();
        String sql = "SELECT id, email, nivel_acesso, status FROM usuarios_sistema ORDER BY id ASC";
        try (Connection conn = conectar(); PreparedStatement cmd = conn.prepareStatement(sql); ResultSet rs = cmd.executeQuery()) {

            while (rs.next()) {
                // Adiciona cada registro à lista observável
                listaUsuarios.add(new Usuario(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("nivel_acesso"),
                        rs.getString("status")));
            }
            tabelaUsuarios.setItems(listaUsuarios);
        } catch (SQLException e) {
            System.out.println("Erro ao carregar usuários: " + e.getMessage());
        }
    }

    // ==========================================
    // GESTÃO DE USUÁRIOS
    // ==========================================
    /**
     * Adiciona um novo usuário ao sistema. Valida permissão, domínio do e-mail
     * e nível máximo permitido.
     */
    @FXML
    private void adicionarUsuario(ActionEvent event) {
        // Verifica se o usuário logado tem permissão para criar contas
        if (!nivelAcessoLogado.equals("MASTER") && !nivelAcessoLogado.equals("N2")) {
            exibirAlerta("Permissão Negada",
                    "Apenas contas N2 ou Master podem adicionar novos usuários.",
                    Alert.AlertType.ERROR);
            return;
        }

        String email = txtNovoEmail.getText().trim();
        String senha = txtNovaSenha.getText();
        String nivel = cbNivelAcesso.getValue();

        // Validação de preenchimento
        if (email.isEmpty() || senha.isEmpty() || nivel == null) {
            exibirAlerta("Aviso", "Preencha todos os dados.", Alert.AlertType.WARNING);
            return;
        }
        // Restrição de domínio corporativo
        if (!email.endsWith("@fortestecnologia.com.br")) {
            exibirAlerta("Restrição Corporativa",
                    "Apenas contas corporativas Fortes são permitidas.",
                    Alert.AlertType.ERROR);
            return;
        }
        // N2 não pode criar MASTER
        if (nivelAcessoLogado.equals("N2") && nivel.equals("MASTER")) {
            exibirAlerta("Acesso Restrito",
                    "Você não tem privilégios para criar contas MASTER.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Inserção no banco
        String sql = "INSERT INTO usuarios_sistema (email, senha, nivel_acesso) VALUES (?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setString(1, email);
            String senhaHash = Seguranca.hashSenha(senha);
            cmd.setString(2, senhaHash);
            cmd.setString(3, nivel);

            cmd.executeUpdate();            // Executa o INSERT

            // Limpa os campos após sucesso
            txtNovoEmail.clear();
            txtNovaSenha.clear();
            cbNivelAcesso.getSelectionModel().clearSelection();

            exibirAlerta("Sucesso", "Usuário cadastrado com a senha temporária.",
                    Alert.AlertType.INFORMATION);
            carregarUsuarios();             // Atualiza a tabela
        } catch (SQLException e) {
            exibirAlerta("Erro BD", "Erro ao salvar: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Remove permanentemente um usuário selecionado na tabela. Apenas MASTER
     * pode executar, e não pode excluir a si mesmo.
     */
    @FXML
    private void removerUsuario(ActionEvent event) {
        if (!nivelAcessoLogado.equals("MASTER")) {
            exibirAlerta("Permissão Negada",
                    "Somente um Master pode remover permanentemente uma conta do sistema.",
                    Alert.AlertType.ERROR);
            return;
        }

        Usuario selecionado = tabelaUsuarios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return; // Nada selecionado
        }
        if (selecionado.getEmail().equalsIgnoreCase(App.getUsuarioLogado())) {
            exibirAlerta("Ação Bloqueada",
                    "Você não pode excluir sua própria conta enquanto estiver em uso.",
                    Alert.AlertType.ERROR);
            return;
        }

        String sql = "DELETE FROM usuarios_sistema WHERE id = ?";
        try (Connection conn = conectar(); PreparedStatement cmd = conn.prepareStatement(sql)) {
            cmd.setInt(1, selecionado.getId());
            cmd.executeUpdate();
            carregarUsuarios(); // Atualiza a tabela
            exibirAlerta("Removido", "O acesso do usuário foi bloqueado e excluído.",
                    Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            exibirAlerta("Erro BD", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ==========================================
    // MÓDULO DE INFRAESTRUTURA
    // ==========================================
    /**
     * Testa a latência da conexão com o banco de dados. Mede o tempo para
     * validar a conexão e exibe o resultado com cor indicativa.
     */
    @FXML
    private void testarLatencia(ActionEvent event) {
        lblResultadoLatencia.setText("Testando conexão...");
        long tempoInicial = System.currentTimeMillis();
        try (Connection conn = conectar()) {
            boolean isValid = conn.isValid(2); // Timeout de 2 segundos
            long latencia = System.currentTimeMillis() - tempoInicial;
            if (isValid) {
                lblResultadoLatencia.setText("Servidor Online | Latência: " + latencia + " ms");
                // Define cor verde se latência < 50ms, caso contrário laranja
                if (latencia < 50) {
                    lblResultadoLatencia.setStyle("-fx-text-fill: #198754;");
                } else {
                    lblResultadoLatencia.setStyle("-fx-text-fill: #fd7e14;");
                }
            }
        } catch (SQLException e) {
            lblResultadoLatencia.setText("Falha na comunicação (Offline).");
            lblResultadoLatencia.setStyle("-fx-text-fill: #dc3545;");
        }
    }

    /**
     * Realiza o backup do banco de dados utilizando a ferramenta pg_dump.
     *
     * CORREÇÕES E MELHORIAS APLICADAS: - Caminho absoluto do pg_dump é sugerido
     * (caso não esteja no PATH). - Captura da saída de erro (stderr) para
     * diagnóstico. - Exibição da saída de erro no label em caso de falha. - Uso
     * de .redirectErrorStream(true) para facilitar leitura.
     *
     * Pré‑requisito: pg_dump deve estar instalado e acessível. No Windows, pode
     * ser necessário informar o caminho completo (ex: "C:\\Program
     * Files\\PostgreSQL\\16\\bin\\pg_dump.exe").
     */
    @FXML
    private void realizarBackup(ActionEvent event) {
        // Apenas MASTER pode gerar backup
        if (!nivelAcessoLogado.equals("MASTER")) {
            exibirAlerta("Acesso Restrito",
                    "Você não tem privilégios de banco para exportar arquivos Dump de segurança.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Abre o diálogo para escolher onde salvar o arquivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Backup do Banco");
        fileChooser.setInitialFileName("backup_proton_" + LocalDate.now() + ".sql");
        File arquivo = fileChooser.showSaveDialog(txtNovoEmail.getScene().getWindow());

        if (arquivo != null) {
            lblStatusBackup.setText("Iniciando extração via pg_dump...");

            // ⚠️ Ajuste o caminho do pg_dump conforme sua instalação,
            // ou mantenha apenas "pg_dump" se ele estiver no PATH do sistema.
            String pgDumpPath = "pg_dump";  // Altere para o caminho completo se necessário

            try {
                // Cria o processo com os argumentos corretos
                ProcessBuilder pb = new ProcessBuilder(
                        pgDumpPath,
                        "-U", ConfigBancoController.getUsuarioBD(),
                        "-h", ConfigBancoController.getIpServidor(),
                        "-p", "5432",
                        "-F", "c", // Formato customizado (pode restaurar com pg_restore)
                        "-b", // Inclui objetos grandes (blobs)
                        "-v", // Modo verboso (progresso)
                        "-f", arquivo.getAbsolutePath(), // Arquivo de saída
                        ConfigBancoController.getNomeBanco()
                );

                // Define a senha via variável de ambiente (funciona na maioria dos sistemas)
                pb.environment().put("PGPASSWORD", ConfigBancoController.getSenhaBD());

                // Redireciona a saída de erro para a saída padrão (facilita captura)
                pb.redirectErrorStream(true);

                // Inicia o processo
                Process processo = pb.start();

                // Lê a saída combinada (stdout + stderr) para diagnóstico
                StringBuilder saida = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(processo.getInputStream()))) {
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        saida.append(linha).append("\n");
                    }
                }

                // Aguarda o término do processo (com timeout de 5 minutos)
                boolean terminou = processo.waitFor(5, java.util.concurrent.TimeUnit.MINUTES);
                if (terminou && processo.exitValue() == 0) {
                    lblStatusBackup.setText("✔ Backup gerado em segurança!");
                } else {
                    // Exibe o motivo do erro no label e no console
                    String erro = saida.length() > 0 ? saida.toString() : "Código de saída: " + processo.exitValue();
                    lblStatusBackup.setText("❌ Falha no pg_dump: " + erro);
                    System.err.println("Erro pg_dump: " + erro);
                }
            } catch (Exception e) {
                lblStatusBackup.setText("❌ Erro sistêmico: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Fecha a janela atual.
     */
    @FXML
    private void fecharJanela(ActionEvent event) {
        ((Stage) txtNovoEmail.getScene().getWindow()).close();
    }

    /**
     * Exibe um diálogo de alerta com título, mensagem e tipo.
     */
    private void exibirAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ==================== CLASSE INTERNA: REPRESENTA UM USUÁRIO ====================
    public static class Usuario {

        private final SimpleIntegerProperty id;
        private final SimpleStringProperty email;
        private final SimpleStringProperty permissao;
        private final SimpleStringProperty status;

        public Usuario(int id, String email, String permissao, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.email = new SimpleStringProperty(email);
            this.permissao = new SimpleStringProperty(permissao);
            this.status = new SimpleStringProperty(status);
        }

        // Propriedades observáveis (necessárias para a TableView)
        public SimpleIntegerProperty idProperty() {
            return id;
        }

        public SimpleStringProperty emailProperty() {
            return email;
        }

        public SimpleStringProperty permissaoProperty() {
            return permissao;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        // Getters comuns
        public int getId() {
            return id.get();
        }

        public String getEmail() {
            return email.get();
        }
    }
}

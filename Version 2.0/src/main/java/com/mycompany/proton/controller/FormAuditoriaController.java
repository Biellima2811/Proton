package com.mycompany.proton.controller;

import com.mycompany.proton.controller.ConfigBancoController;
import java.net.URL;                      // Representa a localização do FXML
import java.sql.Connection;               // Conexão JDBC com o banco
import java.sql.DriverManager;            // Gerencia conexões JDBC
import java.sql.PreparedStatement;        // Executa SQL parametrizado
import java.sql.ResultSet;                // Resultado de consultas
import java.sql.SQLException;             // Exceção de SQL
import java.sql.Timestamp;                // Importação esquecida
import java.time.format.DateTimeFormatter;// Importação esquecida
import java.util.ResourceBundle;          // Pacote de recursos (idioma)
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;  // Cria listas observáveis
import javafx.collections.ObservableList; // Lista que notifica a UI
import javafx.collections.transformation.FilteredList;   // Lista com filtro
import javafx.collections.transformation.SortedList;     // Lista ordenada
import javafx.fxml.FXML;                 // Anotação para vincular FXML
import javafx.fxml.Initializable;        // Interface para inicialização
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn; // Coluna de tabela
import javafx.scene.control.TableView;   // Tabela
import javafx.scene.control.TextField;   // Campo de texto

/**
 * Controlador da tela de Auditoria (logs do sistema).
 *
 * Exibe registros da tabela 'logs_auditoria' em uma TableView, ordenados do
 * mais recente para o mais antigo. Cada linha representa uma ação realizada por
 * um usuário no sistema Proton.
 */
public class FormAuditoriaController implements Initializable {

    // ==================== COMPONENTES VISUAIS (ligados ao FXML) ====================
    @FXML
    private TableView<LogItem> tabelaLogs;   // Tabela que exibirá os registros de log

    @FXML
    private TableColumn<LogItem, String> colDataHora;  // Coluna para data/hora do evento

    @FXML
    private TableColumn<LogItem, String> colUsuario;   // Coluna para e-mail do usuário

    @FXML
    private TableColumn<LogItem, String> colAcao;      // Coluna para a ação executada

    @FXML
    private TableColumn<LogItem, String> colDetalhes;  // Coluna para detalhes adicionais

    @FXML
    private DatePicker dpFiltroData;
    @FXML
    private TextField txtFiltroTecnico;
    @FXML
    private TextField txtFiltroDetalhes;

    // Lista observável que alimenta a tabela (mudanças refletem automaticamente na UI)
    private ObservableList<LogItem> listaLogs = FXCollections.observableArrayList();
    private FilteredList<LogItem> filteredData;

    // ==================== INICIALIZAÇÃO DA TELA ====================
    /**
     * Chamado automaticamente pelo JavaFX após a injeção dos campos @FXML.
     * Configura as colunas e carrega os dados do banco.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Liga cada coluna ao atributo correspondente da classe LogItem
        colDataHora.setCellValueFactory(cellData -> cellData.getValue().dataHoraProperty());
        colUsuario.setCellValueFactory(cellData -> cellData.getValue().usuarioProperty());
        colAcao.setCellValueFactory(cellData -> cellData.getValue().acaoProperty());
        colDetalhes.setCellValueFactory(cellData -> cellData.getValue().detalhesProperty());

        // 2. Preenche a tabela com os logs existentes
        carregarLogs();

        //3. Ativação dos Filtros dianâmicos
        filteredData = new FilteredList<>(listaLogs, b -> true);

        txtFiltroTecnico.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        txtFiltroDetalhes.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        dpFiltroData.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        SortedList<LogItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tabelaLogs.comparatorProperty());
        tabelaLogs.setItems(sortedData);
    }

    // ==================== FILTROS ====================
    private void aplicarFiltros() {
        filteredData.setPredicate(log -> {
            String txtTecnico = txtFiltroTecnico.getText() == null ? "" : txtFiltroTecnico.getText().toLowerCase();
            String txtDetalhes = txtFiltroDetalhes.getText() == null ? "" : txtFiltroDetalhes.getText().toLowerCase();

            boolean matchTecnico = txtTecnico.isEmpty() || log.getUsuario().toLowerCase().contains(txtTecnico);
            boolean matchDetalhes = txtDetalhes.isEmpty() || log.getDetalhes().toLowerCase().contains(txtDetalhes) || log.getAcao().toLowerCase().contains(txtDetalhes);

            boolean matchData = true;
            if (dpFiltroData.getValue() != null) {
                String dataFormatadaBR = dpFiltroData.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String dataFormatadaEUA = dpFiltroData.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                matchData = log.getDataHora().contains(dataFormatadaBR) || log.getDataHora().contains(dataFormatadaEUA);
            }
            return matchTecnico && matchDetalhes && matchData;
        });
    }

    @FXML
    private void limparFiltros() {
        txtFiltroTecnico.clear();
        txtFiltroDetalhes.clear();
        dpFiltroData.setValue(null);
    }

    // ==================== CARREGAMENTO DOS DADOS ====================
    /**
     * Consulta a tabela 'logs_auditoria' e popula a lista observável. Ordena os
     * registros pela data/hora em ordem decrescente (mais recente primeiro).
     */
    private void carregarLogs() {
        // SQL para selecionar todos os logs, do mais novo para o mais antigo
        String sql = "SELECT * FROM logs_auditoria ORDER BY data_hora DESC";

        // Obtém dados da configuração do banco (gerenciada por ConfigBancoController)
        String ip = ConfigBancoController.getIpServidor();
        String nomeBanco = ConfigBancoController.getNomeBanco();
        String usuario = ConfigBancoController.getUsuarioBD();
        String senha = ConfigBancoController.getSenhaBD();

        // Monta a URL de conexão JDBC para PostgreSQL
        String urlConexao = String.format("jdbc:postgresql://%s:5432/%s", ip, nomeBanco);

        // Bloco try-with-resources
        try (Connection conn = DriverManager.getConnection(urlConexao, usuario, senha); PreparedStatement cmd = conn.prepareStatement(sql); ResultSet rs = cmd.executeQuery()) {

            // Limpa a lista atual antes de adicionar novos registros
            listaLogs.clear();

            // Itera sobre cada linha retornada pela consulta
            while (rs.next()) {
                // Obtém o timestamp do banco; converte para String (ou "N/A" se nulo)
                Timestamp ts = rs.getTimestamp("data_hora");
                String dHora = (ts != null) ? ts.toString() : "N/A";

                // Lê os demais campos
                String usuarioEmail = rs.getString("usuario_email");
                String acao = rs.getString("acao");
                String detalhes = rs.getString("detalhes");

                // Cria um objeto LogItem e adiciona à lista observável
                listaLogs.add(new LogItem(dHora, usuarioEmail, acao, detalhes));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao carregar auditoria: " + e.getMessage());
        }
    }

    // ==================== CLASSE INTERNA: REPRESENTA UM REGISTRO DE LOG ====================
    /**
     * Classe auxiliar que representa uma linha da tabela de logs.
     */
    public static class LogItem {

        private final SimpleStringProperty dataHora;   // Data e hora do evento
        private final SimpleStringProperty usuario;    // E-mail do usuário que executou a ação
        private final SimpleStringProperty acao;       // Tipo de ação (ex.: "LOGIN", "CADASTRO")
        private final SimpleStringProperty detalhes;   // Informações complementares

        public LogItem(String dh, String usr, String ac, String det) {
            this.dataHora = new SimpleStringProperty(dh);
            this.usuario = new SimpleStringProperty(usr);
            this.acao = new SimpleStringProperty(ac);
            this.detalhes = new SimpleStringProperty(det);
        }

        // Métodos de Propriedade
        public SimpleStringProperty dataHoraProperty() {
            return dataHora;
        }

        public SimpleStringProperty usuarioProperty() {
            return usuario;
        }

        public SimpleStringProperty acaoProperty() {
            return acao;
        }

        public SimpleStringProperty detalhesProperty() {
            return detalhes;
        }

        // [CORREÇÃO] Os getters devem ficar DENTRO da classe LogItem
        public String getDataHora() {
            return dataHora.get();
        }

        public String getUsuario() {
            return usuario.get();
        }

        public String getAcao() {
            return acao.get();
        }

        public String getDetalhes() {
            return detalhes.get();
        }
    }
}

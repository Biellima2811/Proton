package com.mycompany.proton.controller;

import com.mycompany.proton.App;
import com.mycompany.proton.util.LoggerAuditoria;
import com.mycompany.proton.model.ClienteCompartilhado;
import com.mycompany.proton.model.Cliente;
import com.mycompany.proton.model.ClienteCancelado;
import com.mycompany.proton.model.FortesRH;
import java.net.URL;                      // Representa a localização do FXML
import java.sql.Connection;               // Conexão JDBC com o banco
import java.sql.DriverManager;            // Gerencia conexões JDBC
import java.sql.PreparedStatement;        // Executa SQL parametrizado
import java.sql.ResultSet;                // Resultado de consultas
import java.sql.SQLException;             // Exceção de SQL
import java.util.Optional;                // Container para valor opcional
import java.util.ResourceBundle;          // Pacote de recursos (idioma)
import javafx.animation.KeyFrame;         // Define um frame da animação
import javafx.animation.Timeline;         // Linha do tempo para animações
import javafx.collections.FXCollections;  // Cria listas observáveis
import javafx.collections.ObservableList; // Lista que notifica a UI
import javafx.collections.transformation.FilteredList;   // Lista com filtro
import javafx.collections.transformation.SortedList;     // Lista ordenada
import javafx.event.ActionEvent;          // Evento de ação (botões)
import javafx.fxml.FXML;                 // Anotação para vincular FXML
import javafx.fxml.FXMLLoader;           // Carrega arquivos FXML
import javafx.fxml.Initializable;        // Interface para inicialização
import javafx.scene.Parent;              // Nó raiz de uma cena
import javafx.scene.Scene;               // Representa a cena (conteúdo da janela)
import javafx.scene.chart.BarChart;      // Gráfico de barras
import javafx.scene.chart.PieChart;      // Gráfico de pizza
import javafx.scene.chart.XYChart;       // Série de dados para gráfico XY
import javafx.scene.control.Alert;       // Diálogos de alerta
import javafx.scene.control.Button;      // Componente botão
import javafx.scene.control.ButtonType;  // Tipos de botão para Alert
import javafx.scene.control.Label;       // Rótulo de texto
import javafx.scene.control.TableColumn; // Coluna de tabela
import javafx.scene.control.TableView;   // Tabela
import javafx.scene.control.TextField;   // Campo de texto
import javafx.scene.control.cell.PropertyValueFactory; // Vincula coluna a propriedade
import javafx.scene.layout.AnchorPane;   // Painel âncora (dashboard)
import javafx.scene.paint.Color;         // Cores
import javafx.scene.shape.Circle;        // Círculo (indicador de status)
import javafx.stage.Modality;            // Modalidade da janela
import javafx.stage.Popup;               // Popup de notificação
import javafx.stage.Stage;               // Janela principal
import javafx.util.Duration;             // Duração para Timeline

/**
 * Controlador da tela principal (Dashboard / Painel de Gestão).
 *
 * Responsável por: - Exibir e gerenciar as abas de clientes (Dedicados,
 * Compartilhados, Fortes RH, Cancelados). - Fornecer navegação entre as abas e
 * ações de detalhes, edição e exclusão. - Atualizar gráficos do dashboard com
 * dados do banco. - Controlar permissões de exclusão (apenas N2 e MASTER). -
 * Exibir notificações toast ao realizar ações.
 */
public class PrimaryController implements Initializable {

    // ==================== RÓTULO DA ABA ATIVA ====================
    @FXML
    private Label lblTituloAba;          // Exibe o título da aba selecionada
    @FXML
    private TextField txtPesquisa;       // Campo de pesquisa/filtro das tabelas

    // ==================== BOTÕES DE NAVEGAÇÃO LATERAL ====================
    @FXML
    private Button btnDedicados;         // Aba Clientes Dedicados
    @FXML
    private Button btnCompartilhados;    // Aba Clientes Compartilhados
    @FXML
    private Button btnFortesRH;          // Aba Fortes RH
    @FXML
    private Button btnCancelados;        // Aba Cancelados
    @FXML
    private Button btnDashboard;         // Aba Dashboard
    @FXML
    private Button btnConfiguracoes;     // Botão de engrenagem (abre configurações)

    // ==================== BOTÕES DE AÇÃO NA BARRA SUPERIOR ====================
    @FXML
    private Button btnDetalhes;          // Botão "Visualizar"
    @FXML
    private Button btnEditar;            // Botão "Editar"
    @FXML
    private Button btnDeletar;           // Botão "Excluir"

    // ==================== TABELA DE CLIENTES DEDICADOS ====================
    @FXML
    private TableView<Cliente> tabelaDedicados;
    @FXML
    private TableColumn<Cliente, Integer> colId;
    @FXML
    private TableColumn<Cliente, String> colNome;
    @FXML
    private TableColumn<Cliente, String> colCnpj;
    @FXML
    private TableColumn<Cliente, Integer> colQntServer;
    @FXML
    private TableColumn<Cliente, String> colAd;
    @FXML
    private TableColumn<Cliente, String> colAmbiente;
    @FXML
    private TableColumn<Cliente, Boolean> colVpn;
    @FXML
    private TableColumn<Cliente, String> colSgbd;

    // ==================== TABELA DE CLIENTES COMPARTILHADOS ====================
    @FXML
    private TableView<ClienteCompartilhado> tabelaCompartilhados;
    @FXML
    private TableColumn<ClienteCompartilhado, Integer> colCId;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCTipoNuvem;
    @FXML
    private TableColumn<ClienteCompartilhado, Integer> colCPod;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCData;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCRazao;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCCnpj;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCAg;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCPasta;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCContato;
    @FXML
    private TableColumn<ClienteCompartilhado, Integer> colCUsuarios;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCSistemas;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCStatus;
    @FXML
    private TableColumn<ClienteCompartilhado, String> colCBanco;

    // ==================== TABELA DE FORTES RH ====================
    @FXML
    private TableView<FortesRH> tabelaFortesRH;
    @FXML
    private TableColumn<FortesRH, Integer> colRhId;
    @FXML
    private TableColumn<FortesRH, String> colRhAmbiente;
    @FXML
    private TableColumn<FortesRH, String> colRhCliente;
    @FXML
    private TableColumn<FortesRH, String> colRhCnpj;
    @FXML
    private TableColumn<FortesRH, String> colRhUrl;

    // ==================== TABELA DE CANCELADOS ====================
    @FXML
    private TableView<ClienteCancelado> tabelaCancelados;
    @FXML
    private TableColumn<ClienteCancelado, Integer> colCancId;
    @FXML
    private TableColumn<ClienteCancelado, String> colCancCliente;
    @FXML
    private TableColumn<ClienteCancelado, String> colCancDataCriacao;
    @FXML
    private TableColumn<ClienteCancelado, String> colCancInicio;
    @FXML
    private TableColumn<ClienteCancelado, String> colCancFim;
    @FXML
    private TableColumn<ClienteCancelado, String> colCancChamado;
    @FXML
    private TableColumn<ClienteCancelado, String> colCancTecnico;

    // ==================== ELEMENTOS DO DASHBOARD ====================
    @FXML
    private AnchorPane paneDashboard;        // Painel que contém os gráficos
    @FXML
    private PieChart graficoNuvem;           // Gráfico de pizza (Dedicados x Compartilhados)
    @FXML
    private BarChart<String, Number> graficoSegmentos; // Gráfico de barras por segmento
    @FXML
    private Label lblTotalCancelados;        // Total de cancelamentos

    @FXML
    private TableView<NovoClienteHoje> tabelaNovosHoje;       // Tabela de clientes cadastrados hoje
    @FXML
    private TableColumn<NovoClienteHoje, String> colHojeHora;
    @FXML
    private TableColumn<NovoClienteHoje, String> colHojeTecnico;
    @FXML
    private TableColumn<NovoClienteHoje, String> colHojeCliente;
    @FXML
    private TableColumn<NovoClienteHoje, String> colHojeNuvem;

    @FXML
    private Label lblUsuarioLogado;          // Exibe o e-mail do usuário logado
    @FXML
    private Circle circleStatus;             // Indicador visual (verde = online, amarelo = standby)
    
    @FXML private Label lblCanceladosHoje;
    
    // Tabela nova de Técnicos
    @FXML private TableView<TecnicoOnline> tabelaTecnicos;
    @FXML private TableColumn<TecnicoOnline, String> colTecUsuario;
    @FXML private TableColumn<TecnicoOnline, String> colTecSetor;
    @FXML private TableColumn<TecnicoOnline, String> colTecAcesso;
    @FXML private TableColumn<TecnicoOnline, String> colTecStatus;

    // ==================== LISTAS OBSERVÁVEIS ====================
    private ObservableList<Cliente> listaDedicados = FXCollections.observableArrayList();
    private ObservableList<ClienteCompartilhado> listaCompartilhados = FXCollections.observableArrayList();
    private ObservableList<FortesRH> listaFortesRH = FXCollections.observableArrayList();
    private ObservableList<ClienteCancelado> listaCancelados = FXCollections.observableArrayList();

    // Controle da aba atualmente visível (1=Dedicados, 2=Comp., 3=FortesRH, 4=Cancelados, 5=Dashboard)
    private int abaAtiva = 5;

    /**
     * Inicializa o controlador após o carregamento do FXML. Configura as
     * colunas das tabelas, listeners de seleção, carrega dados e exibe o
     * dashboard como aba inicial.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Desabilita os botões de ação (Detalhes, Editar, Excluir) até que algo seja selecionado
        deligarBotoesAcao();

        // ==================== MAPEAMENTO DAS COLUNAS (DEDICADOS) ====================
        // Cada coluna é vinculada a um getter da classe modelo via PropertyValueFactory
        if (colId != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colNome != null) {
            colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        }
        if (colCnpj != null) {
            colCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpj"));
        }
        if (colQntServer != null) {
            colQntServer.setCellValueFactory(new PropertyValueFactory<>("qnt_server"));
        }
        if (colAd != null) {
            colAd.setCellValueFactory(new PropertyValueFactory<>("active_directory"));
        }
        if (colAmbiente != null) {
            colAmbiente.setCellValueFactory(new PropertyValueFactory<>("ambiente"));
        }
        if (colVpn != null) {
            colVpn.setCellValueFactory(new PropertyValueFactory<>("vpn"));
        }
        if (colSgbd != null) {
            colSgbd.setCellValueFactory(new PropertyValueFactory<>("sgbd"));
        }

        // ==================== MAPEAMENTO DAS COLUNAS (COMPARTILHADOS) ====================
        if (colCId != null) {
            colCId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colCTipoNuvem != null) {
            colCTipoNuvem.setCellValueFactory(new PropertyValueFactory<>("tipoNuvem"));
        }
        if (colCPod != null) {
            colCPod.setCellValueFactory(new PropertyValueFactory<>("pod"));
        }
        if (colCData != null) {
            colCData.setCellValueFactory(new PropertyValueFactory<>("dataCriacao"));
        }
        if (colCRazao != null) {
            colCRazao.setCellValueFactory(new PropertyValueFactory<>("razaoSocial"));
        }
        if (colCCnpj != null) {
            colCCnpj.setCellValueFactory(new PropertyValueFactory<>("cpfCnpj"));
        }
        if (colCAg != null) {
            colCAg.setCellValueFactory(new PropertyValueFactory<>("codAg"));
        }
        if (colCPasta != null) {
            colCPasta.setCellValueFactory(new PropertyValueFactory<>("pastaRede"));
        }
        if (colCContato != null) {
            colCContato.setCellValueFactory(new PropertyValueFactory<>("contato"));
        }
        if (colCUsuarios != null) {
            colCUsuarios.setCellValueFactory(new PropertyValueFactory<>("usuarios"));
        }
        if (colCSistemas != null) {
            colCSistemas.setCellValueFactory(new PropertyValueFactory<>("sistemas"));
        }
        if (colCStatus != null) {
            colCStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        }
        if (colCBanco != null) {
            colCBanco.setCellValueFactory(new PropertyValueFactory<>("bancoDados"));
        }

        // ==================== MAPEAMENTO DAS COLUNAS (FORTES RH) ====================
        if (colRhId != null) {
            colRhId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colRhAmbiente != null) {
            colRhAmbiente.setCellValueFactory(new PropertyValueFactory<>("tipo_ambiente"));
        }
        if (colRhCliente != null) {
            colRhCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        }
        if (colRhCnpj != null) {
            colRhCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpj_cpf"));
        }
        if (colRhUrl != null) {
            colRhUrl.setCellValueFactory(new PropertyValueFactory<>("url_acesso"));
        }

        // ==================== MAPEAMENTO DAS COLUNAS (CANCELADOS) ====================
        if (colCancId != null) {
            colCancId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colCancCliente != null) {
            colCancCliente.setCellValueFactory(new PropertyValueFactory<>("cliente_razao"));
        }
        if (colCancDataCriacao != null) {
            colCancDataCriacao.setCellValueFactory(new PropertyValueFactory<>("data_criacao"));
        }
        if (colCancInicio != null) {
            colCancInicio.setCellValueFactory(new PropertyValueFactory<>("inicio_cancelamento"));
        }
        if (colCancFim != null) {
            colCancFim.setCellValueFactory(new PropertyValueFactory<>("final_cancelamento"));
        }
        if (colCancChamado != null) {
            colCancChamado.setCellValueFactory(new PropertyValueFactory<>("chamado"));
        }
        if (colCancTecnico != null) {
            colCancTecnico.setCellValueFactory(new PropertyValueFactory<>("tecnico_responsavel"));
        }

        // Mapeamento da tabela de "Novos Hoje"
        if (colHojeHora != null) {
            colHojeHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        }
        if (colHojeTecnico != null) {
            colHojeTecnico.setCellValueFactory(new PropertyValueFactory<>("tecnico"));
        }
        if (colHojeCliente != null) {
            colHojeCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        }
        if (colHojeNuvem != null) {
            colHojeNuvem.setCellValueFactory(new PropertyValueFactory<>("nuvem"));
        }

        // Exibe o nome do usuário logado e a bolinha de status
        String user = App.getUsuarioLogado();
        if (user != null && !user.isEmpty()) {
            lblUsuarioLogado.setText(user);
            circleStatus.setFill(Color.web("#198754")); // Verde (online)
        } else {
            lblUsuarioLogado.setText("Desconectado / Standby");
            circleStatus.setFill(Color.web("#ffc107")); // Amarelo (standby)
        }
        
        // Mapeamento da tabela "Técnicos Online"
        if (colTecUsuario != null) colTecUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        if (colTecSetor != null) colTecSetor.setCellValueFactory(new PropertyValueFactory<>("setor"));
        if (colTecAcesso != null) colTecAcesso.setCellValueFactory(new PropertyValueFactory<>("acesso"));
        if (colTecStatus != null) colTecStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Configura listeners de seleção para cada tabela (habilita/desabilita botões)
        configurarOuvintesSelecao(tabelaDedicados);
        configurarOuvintesSelecao(tabelaCompartilhados);
        configurarOuvintesSelecao(tabelaFortesRH);
        configurarOuvintesSelecao(tabelaCancelados);

        // Carrega todos os dados do banco e configura o filtro de pesquisa
        carregarTodasAsTabelas();
        configurarFiltroPesquisa();

        // Inicia na aba do Dashboard
        abaDashboard(null);
    }

    /**
     * Adiciona listeners à tabela para: - Habilitar/desabilitar botões de ação
     * conforme a seleção. - Abrir o modal de detalhes ao clicar duas vezes em
     * uma linha.
     */
    private void configurarOuvintesSelecao(TableView<?> tabela) {
        if (tabela != null) {
            // Listener de seleção: liga ou desliga os botões de ação
            tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
                if (novo != null) {
                    ligarBotoesAcao();   // Habilita Detalhes, Editar, Excluir
                } else {
                    deligarBotoesAcao(); // Desabilita se nada estiver selecionado
                }
            });
            // Duplo clique abre o modal de detalhes
            tabela.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && tabela.getSelectionModel().getSelectedItem() != null) {
                    acaoDetalhes(null);
                }
            });
        }
    }

    /**
     * Habilita os botões Detalhes, Editar e Excluir.
     */
    private void ligarBotoesAcao() {
        if (btnDetalhes != null) {
            btnDetalhes.setDisable(false);
        }
        if (btnEditar != null) {
            btnEditar.setDisable(false);
        }
        if (btnDeletar != null) {
            btnDeletar.setDisable(false);
        }
    }

    /**
     * Desabilita os botões Detalhes, Editar e Excluir.
     */
    private void deligarBotoesAcao() {
        if (btnDetalhes != null) {
            btnDetalhes.setDisable(true);
        }
        if (btnEditar != null) {
            btnEditar.setDisable(true);
        }
        if (btnDeletar != null) {
            btnDeletar.setDisable(true);
        }
    }

    /**
     * Exibe uma notificação temporária (toast) no canto inferior direito da
     * janela.
     *
     * @param cliente Nome do cliente ou registro afetado.
     * @param tipoAcao Descrição da ação (ex.: "Inclusão", "Atualização").
     */
    private void exibirNotificacaoToast(String cliente, String tipoAcao) {
        String usuarioAtual = App.getUsuarioLogado() != null ? App.getUsuarioLogado() : "Técnico Oculto";
        String mensagem = "🔔 " + usuarioAtual + "\n" + tipoAcao + ": " + cliente;

        Popup popup = new Popup();
        popup.setAutoFix(true);  // Mantém o popup visível mesmo com foco em outro lugar

        Label label = new Label(mensagem);
        label.setStyle("-fx-background-color: #2b3035; -fx-text-fill: #198754; -fx-padding: 15px; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);");

        popup.getContent().add(label);

        // Posiciona o popup no canto inferior direito da janela principal
        Stage stage = (Stage) btnDashboard.getScene().getWindow();
        popup.show(stage, stage.getX() + stage.getWidth() - 350, stage.getY() + stage.getHeight() - 120);

        // Remove o popup automaticamente após 4 segundos
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> popup.hide()));
        timeline.play();
    }

    // ==========================================================
    // NAVEGAÇÃO DE ABAS
    // ==========================================================
    /**
     * Reseta todos os botões do menu lateral para o estilo inativo e esconde
     * todas as tabelas e o painel do dashboard.
     */
    private void resetarBotoesMenu() {
        String inativo = "-fx-background-color: transparent; -fx-text-fill: #adb5bd; "
                + "-fx-alignment: center-left; -fx-font-weight: bold;";
        if (btnDedicados != null) {
            btnDedicados.setStyle(inativo);
        }
        if (btnCompartilhados != null) {
            btnCompartilhados.setStyle(inativo);
        }
        if (btnFortesRH != null) {
            btnFortesRH.setStyle(inativo);
        }
        if (btnCancelados != null) {
            btnCancelados.setStyle(inativo);
        }
        if (btnDashboard != null) {
            btnDashboard.setStyle(inativo);
        }

        // Esconde todas as tabelas e o dashboard
        if (tabelaDedicados != null) {
            tabelaDedicados.setVisible(false);
        }
        if (tabelaCompartilhados != null) {
            tabelaCompartilhados.setVisible(false);
        }
        if (tabelaFortesRH != null) {
            tabelaFortesRH.setVisible(false);
        }
        if (tabelaCancelados != null) {
            tabelaCancelados.setVisible(false);
        }
        if (paneDashboard != null) {
            paneDashboard.setVisible(false);
        }

        // Limpa o campo de pesquisa ao trocar de aba
        if (txtPesquisa != null) {
            txtPesquisa.clear();
        }
        deligarBotoesAcao(); // Nenhuma seleção ativa ao trocar de aba
    }

    @FXML
    private void abaClientesDedicados(ActionEvent event) {
        abaAtiva = 1;
        resetarBotoesMenu();
        if (lblTituloAba != null) {
            lblTituloAba.setText("Clientes Dedicados");
        }
        if (btnDedicados != null) {
            btnDedicados.setStyle("-fx-background-color: #3b4248; -fx-text-fill: white; "
                    + "-fx-border-color: #0d6efd; -fx-border-width: 0 0 0 4; "
                    + "-fx-alignment: center-left; -fx-font-weight: bold;");
        }
        if (tabelaDedicados != null) {
            tabelaDedicados.setVisible(true);
        }
    }

    @FXML
    private void abaClientesCompartilhados(ActionEvent event) {
        abaAtiva = 2;
        resetarBotoesMenu();
        if (lblTituloAba != null) {
            lblTituloAba.setText("Clientes Compartilhados");
        }
        if (btnCompartilhados != null) {
            btnCompartilhados.setStyle("-fx-background-color: #3b4248; -fx-text-fill: white; "
                    + "-fx-border-color: #0d6efd; -fx-border-width: 0 0 0 4; "
                    + "-fx-alignment: center-left; -fx-font-weight: bold;");
        }
        if (tabelaCompartilhados != null) {
            tabelaCompartilhados.setVisible(true);
        }
    }

    @FXML
    private void abaFortesRH(ActionEvent event) {
        abaAtiva = 3;
        resetarBotoesMenu();
        if (lblTituloAba != null) {
            lblTituloAba.setText("Gestão FortesRH (Unificado)");
        }
        if (btnFortesRH != null) {
            btnFortesRH.setStyle("-fx-background-color: #3b4248; -fx-text-fill: white; "
                    + "-fx-border-color: #ffc107; -fx-border-width: 0 0 0 4; "
                    + "-fx-alignment: center-left; -fx-font-weight: bold;");
        }
        if (tabelaFortesRH != null) {
            tabelaFortesRH.setVisible(true);
        }
    }

    @FXML
    private void abaCancelados(ActionEvent event) {
        abaAtiva = 4;
        resetarBotoesMenu();
        if (lblTituloAba != null) {
            lblTituloAba.setText("Cancelamentos");
        }
        if (btnCancelados != null) {
            btnCancelados.setStyle("-fx-background-color: #3b4248; -fx-text-fill: white; "
                    + "-fx-border-color: #dc3545; -fx-border-width: 0 0 0 4; "
                    + "-fx-alignment: center-left; -fx-font-weight: bold;");
        }
        if (tabelaCancelados != null) {
            tabelaCancelados.setVisible(true);
        }
    }

    @FXML
    private void abaDashboard(ActionEvent event) {
        abaAtiva = 5;
        resetarBotoesMenu();
        if (lblTituloAba != null) {
            lblTituloAba.setText("Dashboard de Inteligência (BI)");
        }
        if (btnDashboard != null) {
            btnDashboard.setStyle("-fx-background-color: #3b4248; -fx-text-fill: white; "
                    + "-fx-border-color: #6f42c1; -fx-border-width: 0 0 0 4; "
                    + "-fx-alignment: center-left; -fx-font-weight: bold;");
        }
        if (paneDashboard != null) {
            paneDashboard.setVisible(true);
        }
        atualizarDashboard(); // Atualiza os gráficos e tabela "Novos Hoje"
    }

    // ==========================================================
    // CARGA DE DADOS DO BANCO
    // ==========================================================
    /**
     * Cria e retorna uma conexão JDBC usando as configurações do
     * ConfigBancoController.
     */
    private Connection conectar() throws SQLException {
        String urlConexao = String.format("jdbc:postgresql://%s:5432/%s",
                ConfigBancoController.getIpServidor(),
                ConfigBancoController.getNomeBanco());
        return DriverManager.getConnection(urlConexao,
                ConfigBancoController.getUsuarioBD(),
                ConfigBancoController.getSenhaBD());
    }

    /**
     * Limpa as listas e recarrega todos os dados das tabelas do banco.
     */
    private void carregarTodasAsTabelas() {
        listaDedicados.clear();
        listaCompartilhados.clear();
        listaFortesRH.clear();
        listaCancelados.clear();

        try (Connection conexao = conectar()) {
            // --- Clientes Dedicados ---
            try (PreparedStatement cmd = conexao.prepareStatement("SELECT * FROM clientes_dedicados ORDER BY id ASC"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    listaDedicados.add(new Cliente(
                            rs.getInt("id"),
                            rs.getString("cliente"),
                            rs.getString("cnpj_cpf"),
                            rs.getInt("qnt_de_servs"),
                            rs.getString("ad"),
                            rs.getString("ambiente"),
                            rs.getBoolean("vpn"),
                            "Ver Servidores" // valor genérico para coluna SGBD
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Erro Dedicados: " + e.getMessage());
            }

            // --- Clientes Compartilhados (tratamento defensivo contra nulos) ---
            try (PreparedStatement cmd = conexao.prepareStatement("SELECT * FROM clientes_compartilhados ORDER BY id ASC"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    listaCompartilhados.add(new ClienteCompartilhado(
                            rs.getInt("id"),
                            rs.getString("tipo_nuvem") != null ? rs.getString("tipo_nuvem") : "",
                            rs.getInt("pod"),
                            rs.getString("data_criacao") != null ? rs.getString("data_criacao") : "",
                            rs.getString("razao_social") != null ? rs.getString("razao_social") : "",
                            rs.getString("cpf_cnpj") != null ? rs.getString("cpf_cnpj") : "",
                            rs.getString("razao_cnpj_antigos") != null ? rs.getString("razao_cnpj_antigos") : "",
                            rs.getString("cod_ag") != null ? rs.getString("cod_ag") : "",
                            rs.getString("pasta_rede") != null ? rs.getString("pasta_rede") : "",
                            rs.getString("contato") != null ? rs.getString("contato") : "",
                            rs.getInt("usuarios"),
                            rs.getString("origem") != null ? rs.getString("origem") : "",
                            rs.getString("telefone") != null ? rs.getString("telefone") : "",
                            rs.getString("email") != null ? rs.getString("email") : "",
                            rs.getString("sistemas") != null ? rs.getString("sistemas") : "",
                            rs.getString("status") != null ? rs.getString("status") : "",
                            rs.getString("banco") != null ? rs.getString("banco") : "",
                            "N/A" // valor seguro (não há coluna correspondente)
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Erro Compartilhados: " + e.getMessage());
            }

            // --- Fortes RH ---
            try (PreparedStatement cmd = conexao.prepareStatement("SELECT * FROM fortesrh ORDER BY id ASC"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    listaFortesRH.add(new FortesRH(
                            rs.getInt("id"),
                            rs.getString("tipo_ambiente"),
                            rs.getString("cliente"),
                            rs.getString("cnpj_cpf"),
                            rs.getString("url_acesso"),
                            rs.getString("servidor_app"),
                            rs.getString("banco_dados"),
                            rs.getString("pasta_web"),
                            rs.getString("usuario_db"),
                            rs.getString("senha_db"),
                            rs.getString("load_balance"),
                            rs.getString("ip_load_balance")
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Erro FortesRH: " + e.getMessage());
            }

            // --- Cancelados ---
            try (PreparedStatement cmd = conexao.prepareStatement("SELECT * FROM clientes_cancelados ORDER BY id ASC"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    listaCancelados.add(new ClienteCancelado(
                            rs.getInt("id"),
                            rs.getString("tipo_nuvem"),
                            rs.getInt("pod"),
                            rs.getString("data_criacao"),
                            rs.getString("cliente_razao"),
                            rs.getString("status_antigo"),
                            rs.getString("inicio_cancelamento"),
                            rs.getString("final_cancelamento"),
                            rs.getString("chamado"),
                            rs.getString("tecnico_responsavel")
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Erro Cancelados: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Erro fatal de conexão geral: " + e.getMessage());
        }
    }

    /**
     * Atualiza os gráficos e a tabela de "Novos Hoje" no dashboard. Consulta
     * totais de clientes, segmentos, cancelamentos e cadastros do dia.
     */
    private void atualizarDashboard() {
        if (graficoNuvem == null || graficoSegmentos == null) {
            return;
        }
        graficoNuvem.getData().clear();
        graficoSegmentos.getData().clear();

        try (Connection conexao = conectar()) {
            // Conta clientes dedicados e compartilhados
            int totalDedicados = 0, totalCompartilhados = 0;
            try (PreparedStatement cmd = conexao.prepareStatement("SELECT COUNT(*) FROM clientes_dedicados"); ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    totalDedicados = rs.getInt(1);
                }
            }
            try (PreparedStatement cmd = conexao.prepareStatement("SELECT COUNT(*) FROM clientes_compartilhados"); ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    totalCompartilhados = rs.getInt(1);
                }
            }

            // Gráfico de pizza
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Dedicados (" + totalDedicados + ")", totalDedicados),
                    new PieChart.Data("Compartilhados (" + totalCompartilhados + ")", totalCompartilhados)
            );
            graficoNuvem.setData(pieData);

            // Gráfico de barras por segmento (apenas registros com segmento válido)
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Qtd por Segmento");
            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT segmento, COUNT(*) FROM bancos_nuvem_compartilhada WHERE segmento != 'N/A' GROUP BY segmento"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    series.getData().add(new XYChart.Data<>(rs.getString(1), rs.getInt(2)));
                }
            }
            graficoSegmentos.getData().add(series);

            // Total e Hoje de cancelamentos
            try (PreparedStatement cmd = conexao.prepareStatement("SELECT COUNT(*) FROM clientes_cancelados"); ResultSet rs = cmd.executeQuery()) {
                if (rs.next() && lblTotalCancelados != null) {
                    lblTotalCancelados.setText(String.valueOf(rs.getInt(1)));
                }
            }
            try (PreparedStatement cmd = conexao.prepareStatement("SELECT COUNT(*) FROM clientes_cancelados WHERE inicio_cancelamento = CURRENT_DATE"); ResultSet rs = cmd.executeQuery()) {
                if (rs.next() && lblCanceladosHoje != null) {
                    lblCanceladosHoje.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // Clientes cadastrados hoje (união das 3 tabelas)
            ObservableList<NovoClienteHoje> clientesHoje = FXCollections.observableArrayList();
            String sqlHoje = "SELECT COALESCE(hora_criacao, CURRENT_TIME)::text AS hora, "
                    + "COALESCE(criado_por, 'Sistema') AS tec, razao_social AS cli, 'Compartilhado' AS env "
                    + "FROM clientes_compartilhados WHERE data_criacao = CURRENT_DATE "
                    + "UNION ALL "
                    + "SELECT COALESCE(hora_criacao, CURRENT_TIME)::text AS hora, "
                    + "COALESCE(criado_por, 'Sistema') AS tec, cliente AS cli, 'FortesRH' AS env "
                    + "FROM fortesrh WHERE data_criacao = CURRENT_DATE "
                    + "UNION ALL "
                    + "SELECT COALESCE(hora_criacao, CURRENT_TIME)::text AS hora, "
                    + "COALESCE(criado_por, 'Sistema') AS tec, cliente AS cli, 'Dedicado' AS env "
                    + "FROM clientes_dedicados WHERE data_criacao = CURRENT_DATE";

            try (PreparedStatement cmd = conexao.prepareStatement(sqlHoje); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    // Pega apenas HH:mm dos primeiros 5 caracteres
                    String tempoLimpo = rs.getString("hora").substring(0, 5);
                    clientesHoje.add(new NovoClienteHoje(
                            tempoLimpo,
                            rs.getString("tec"),
                            rs.getString("cli"),
                            rs.getString("env")
                    ));
                }
            }
            if (tabelaNovosHoje != null) {
                tabelaNovosHoje.setItems(clientesHoje);
            }
            // Popula os dados estáticos dos Técnicos Online para combinar com o layout
            if (tabelaTecnicos != null) {
                ObservableList<TecnicoOnline> tecnicos = FXCollections.observableArrayList();
                tabelaTecnicos.setItems(tecnicos);
            }

        } catch (SQLException e) {
            System.out.println("Erro BI: " + e.getMessage());
        }
    }

    // ==========================================================
    // FILTRO DE PESQUISA EM TEMPO REAL
    // ==========================================================
    /**
     * Configura o campo txtPesquisa para filtrar as tabelas conforme o texto
     * digitado. Cada tabela possui sua FilteredList e SortedList para permitir
     * ordenação.
     */
    private void configurarFiltroPesquisa() {
        FilteredList<Cliente> fDed = new FilteredList<>(listaDedicados, b -> true);
        FilteredList<ClienteCompartilhado> fComp = new FilteredList<>(listaCompartilhados, b -> true);
        FilteredList<FortesRH> fRh = new FilteredList<>(listaFortesRH, b -> true);
        FilteredList<ClienteCancelado> fCanc = new FilteredList<>(listaCancelados, b -> true);

        if (txtPesquisa != null) {
            txtPesquisa.textProperty().addListener((observable, oldValue, newValue) -> {
                String ft = (newValue == null) ? "" : newValue.toLowerCase();
                // Aplica o filtro em cada lista
                fDed.setPredicate(c -> ft.isEmpty() || (c.getNome() != null && c.getNome().toLowerCase().contains(ft)));
                fComp.setPredicate(c -> ft.isEmpty() || (c.getRazaoSocial() != null && c.getRazaoSocial().toLowerCase().contains(ft)));
                fRh.setPredicate(c -> ft.isEmpty() || (c.getCliente() != null && c.getCliente().toLowerCase().contains(ft)));
                fCanc.setPredicate(c -> ft.isEmpty() || (c.getCliente_razao() != null && c.getCliente_razao().toLowerCase().contains(ft)));
            });
        }

        // Associa cada tabela à sua SortedList (mantém ordenação mesmo com filtro)
        if (tabelaDedicados != null) {
            SortedList<Cliente> sDed = new SortedList<>(fDed);
            sDed.comparatorProperty().bind(tabelaDedicados.comparatorProperty());
            tabelaDedicados.setItems(sDed);
        }
        if (tabelaCompartilhados != null) {
            SortedList<ClienteCompartilhado> sComp = new SortedList<>(fComp);
            sComp.comparatorProperty().bind(tabelaCompartilhados.comparatorProperty());
            tabelaCompartilhados.setItems(sComp);
        }
        if (tabelaFortesRH != null) {
            SortedList<FortesRH> sRh = new SortedList<>(fRh);
            sRh.comparatorProperty().bind(tabelaFortesRH.comparatorProperty());
            tabelaFortesRH.setItems(sRh);
        }
        if (tabelaCancelados != null) {
            SortedList<ClienteCancelado> sCanc = new SortedList<>(fCanc);
            sCanc.comparatorProperty().bind(tabelaCancelados.comparatorProperty());
            tabelaCancelados.setItems(sCanc);
        }
    }

    // ==========================================================
    // AÇÕES DA BARRA SUPERIOR E MODAIS
    // ==========================================================
    @FXML
    private void abrirConfiguracoes(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/proton/views/FormConfiguracoes.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Configurações Corporativas");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            System.out.println("Erro ao abrir configurações: " + e.getMessage());
        }
    }

    /**
     * Abre a janela modal para cadastrar um novo registro. Dependendo da aba
     * ativa, decide qual FXML carregar. No Dashboard (5) ou FortesRH (3),
     * pergunta ao usuário que tipo deseja cadastrar.
     */
    @FXML
    private void abrirModalNovoCliente(ActionEvent event) {
        try {
            String caminhoFxml = null;
            String tituloTela = "";

            if (abaAtiva == 5 || abaAtiva == 3) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Novo Cadastro (Proton)");
                alert.setHeaderText("O que você deseja cadastrar?");

                ButtonType btnDed = new ButtonType("Dedicado");
                ButtonType btnComp = new ButtonType("Compartilhado");
                ButtonType btnRh = new ButtonType("FortesRH");
                ButtonType btnCanc = new ButtonType("Cancelamento");
                ButtonType btnSair = new ButtonType("Cancelar Ação", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

                if (abaAtiva == 3) {
                    // Aba FortesRH: pergunta se é Compartilhado ou Dedicado
                    caminhoFxml = escolherTipoFortesRH();
                    tituloTela = "Novo Registro FortesRH";
                } else {
                    // Dashboard: oferece todas as opções
                    alert.getButtonTypes().setAll(btnDed, btnComp, btnRh, btnCanc, btnSair);
                    Optional<ButtonType> escolha = alert.showAndWait();

                    if (!escolha.isPresent() || escolha.get() == btnSair) {
                        return;
                    }

                    if (escolha.get() == btnDed) {
                        caminhoFxml = "/com/mycompany/proton/views/FormCliente.fxml";
                        tituloTela = "Novo Cliente Dedicado";
                    } else if (escolha.get() == btnComp) {
                        caminhoFxml = "/com/mycompany/proton/views/FormClienteCompartilhado.fxml";
                        tituloTela = "Novo Cliente Compartilhado";
                    } else if (escolha.get() == btnRh) {
                        caminhoFxml = escolherTipoFortesRH();
                        tituloTela = "Novo Registro FortesRH";
                    } else if (escolha.get() == btnCanc) {
                        caminhoFxml = "/com/mycompany/proton/views/FormCancelado.fxml";
                        tituloTela = "Registrar Cancelamento";
                    }
                }
            } else if (abaAtiva == 1) {
                caminhoFxml = "/com/mycompany/proton/views/FormCliente.fxml";
                tituloTela = "Novo Cliente Dedicado";
            } else if (abaAtiva == 2) {
                caminhoFxml = "/com/mycompany/proton/views/FormClienteCompartilhado.fxml";
                tituloTela = "Novo Cliente Compartilhado";
            } else if (abaAtiva == 4) {
                caminhoFxml = "/com/mycompany/proton/views/FormCancelado.fxml";
                tituloTela = "Registrar Cancelamento";
            }

            if (caminhoFxml == null) {
                return;
            }

            // Carrega o FXML e exibe a janela modal
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle(tituloTela);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Após fechar o modal, notifica e recarrega dados
            exibirNotificacaoToast("Novo Registro Efetuado", "Inclusão");
            carregarTodasAsTabelas();
            atualizarDashboard();

        } catch (Exception e) {
            System.out.println("Erro ao abrir a tela de cadastro: " + e.getMessage());
        }
    }

    /**
     * Para o FortesRH, pergunta se o ambiente é Compartilhado ou Dedicado,
     * retornando o nome do FXML correspondente.
     */
    private String escolherTipoFortesRH() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Escolha o Ambiente");
        alert.setHeaderText("Cadastrar Novo FortesRH");
        alert.setContentText("Qual ambiente do FortesRH você deseja registrar?");

        ButtonType btnCompartilhado = new ButtonType("Compartilhado");
        ButtonType btnDedicado = new ButtonType("Dedicado");
        ButtonType btnCancelar = new ButtonType("Cancelar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnCompartilhado, btnDedicado, btnCancelar);
        Optional<ButtonType> escolha = alert.showAndWait();

        if (escolha.isPresent()) {
            if (escolha.get() == btnCompartilhado) {
                return "/com/mycompany/proton/views/FormFortesRH.fxml";
            }
            if (escolha.get() == btnDedicado) {
                return "/com/mycompany/proton/views/FormFortesRH_Dedicado.fxml";
            }
        }
        return null;
    }

    // ==================== AÇÕES: DETALHES, EDITAR, EXCLUIR ====================
    @FXML
    private void acaoDetalhes(ActionEvent event) {
        if (abaAtiva == 1) {
            Cliente sel = tabelaDedicados.getSelectionModel().getSelectedItem();
            if (sel != null) {
                abrirModalDetalhesDedicados(sel);
            }
        } else if (abaAtiva == 2) {
            ClienteCompartilhado sel = tabelaCompartilhados.getSelectionModel().getSelectedItem();
            if (sel != null) {
                abrirModalDetalhesCompartilhados(sel);
            }
        } else if (abaAtiva == 3) {
            FortesRH sel = tabelaFortesRH.getSelectionModel().getSelectedItem();
            if (sel != null) {
                abrirModalFortesRH(sel, true);
            }
        } else if (abaAtiva == 4) {
            ClienteCancelado sel = tabelaCancelados.getSelectionModel().getSelectedItem();
            if (sel != null) {
                abrirModalCancelados(sel, true);
            }
        }
    }

    @FXML
    private void acaoEditar(ActionEvent event) {
        if (abaAtiva == 1) {
            Cliente sel = tabelaDedicados.getSelectionModel().getSelectedItem();
            if (sel != null) {
                abrirModalEdicaoDedicados(sel);
            }
        } else if (abaAtiva == 2) {
            ClienteCompartilhado sel = tabelaCompartilhados.getSelectionModel().getSelectedItem();
            if (sel != null) {
                abrirModalEdicaoCompartilhados(sel);
            }
        } else if (abaAtiva == 3) {
            FortesRH sel = tabelaFortesRH.getSelectionModel().getSelectedItem();
            if (sel != null) {
                abrirModalFortesRH(sel, false);
            }
        } else if (abaAtiva == 4) {
            ClienteCancelado sel = tabelaCancelados.getSelectionModel().getSelectedItem();
            if (sel != null) {
                abrirModalCancelados(sel, false);
            }
        }
    }

    /**
     * Exclui permanentemente um registro. Apenas usuários com nível MASTER ou
     * N2 podem executar essa ação. Confirmação é solicitada antes da exclusão.
     */
    @FXML
    private void acaoDeletar(ActionEvent event) {
        String logado = App.getUsuarioLogado();
        if (logado == null || logado.isEmpty()) {
            return;
        }

        // Verifica se o usuário tem permissão (MASTER ou N2)
        boolean isAutorizado = false;
        String sqlAuth = "SELECT nivel_acesso FROM usuarios_sistema WHERE email = ?";
        try (Connection conn = conectar(); PreparedStatement cmd = conn.prepareStatement(sqlAuth)) {
            cmd.setString(1, logado.toLowerCase());
            ResultSet rs = cmd.executeQuery();
            if (rs.next()) {
                String nivel = rs.getString("nivel_acesso").toUpperCase();
                if (nivel.equals("MASTER") || nivel.equals("N2")) {
                    isAutorizado = true;
                }
            }
        } catch (SQLException e) {
            /* ignora e mantém false */ }

        if (!isAutorizado) {
            exibirAlerta("Acesso Restrito",
                    "Apenas contas N2 ou Master podem excluir registros permanentemente.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Identifica qual registro excluir baseado na aba ativa
        String nomeCliente = "";
        int idCliente = -1;
        String tabelaBanco = "";

        if (abaAtiva == 1) {
            Cliente selecionado = tabelaDedicados.getSelectionModel().getSelectedItem();
            if (selecionado == null) {
                return;
            }
            nomeCliente = selecionado.getNome();
            idCliente = selecionado.getId();
            tabelaBanco = "clientes_dedicados";
        } else if (abaAtiva == 2) {
            ClienteCompartilhado selecionado = tabelaCompartilhados.getSelectionModel().getSelectedItem();
            if (selecionado == null) {
                return;
            }
            nomeCliente = selecionado.getRazaoSocial();
            idCliente = selecionado.getId();
            tabelaBanco = "clientes_compartilhados";
        } else if (abaAtiva == 3) {
            FortesRH selecionado = tabelaFortesRH.getSelectionModel().getSelectedItem();
            if (selecionado == null) {
                return;
            }
            nomeCliente = selecionado.getCliente();
            idCliente = selecionado.getId();
            tabelaBanco = "fortesrh";
        } else if (abaAtiva == 4) {
            ClienteCancelado selecionado = tabelaCancelados.getSelectionModel().getSelectedItem();
            if (selecionado == null) {
                return;
            }
            nomeCliente = selecionado.getCliente_razao();
            idCliente = selecionado.getId();
            tabelaBanco = "clientes_cancelados";
        } else {
            return;
        }

        // Confirmação
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Exclusão Crítica");
        alert.setHeaderText("Atenção, " + logado + "!");
        alert.setContentText("Tem certeza que deseja DELETAR o registro permanentemente:\n" + nomeCliente);

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try (Connection conexao = conectar(); PreparedStatement comando = conexao.prepareStatement("DELETE FROM " + tabelaBanco + " WHERE id = ?")) {
                comando.setInt(1, idCliente);
                comando.executeUpdate();
                exibirAlerta("Sucesso", "Registro excluído permanentemente.", Alert.AlertType.INFORMATION);
                exibirNotificacaoToast(nomeCliente, "Exclusão Realizada");
                LoggerAuditoria.registrar("EXCLUSÃO", "Deletou permanentemente o cliente ID " + idCliente + " (" + nomeCliente + ") da tabela " + tabelaBanco);
                carregarTodasAsTabelas();
                atualizarDashboard();
            } catch (Exception e) {
                exibirAlerta("Erro", "Falha ao excluir: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void fazerLogoff(ActionEvent event) {
        try {
            App.setUsuarioLogado(null); // Limpa a sessão do usuário atual
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(App.class.getResource("/com/mycompany/proton/views/login.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Proton - Acesso Restrito");
            stage.setMaximized(false);
            stage.setWidth(500);
            stage.setHeight(450);
            stage.centerOnScreen();
        } catch (Exception e) {
            exibirAlerta("Erro", "Falha ao deslogar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void abrirSobre(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sobre o Sistema");
        alert.setHeaderText("Proton - Gestão e Triagem");
        alert.setContentText("Versão da Aplicação: 1.0\n\n"
                + "Sistema de gestão interna exclusivo para controle de clientes em nuvem dedicada e compartilhada, "
                + "integração de banco de dados e auditoria de processos.\n\n");
        alert.showAndWait();
    }

    // ==================== MÉTODOS AUXILIARES PARA ABRIR MODAIS ====================
    private void abrirModalFortesRH(FortesRH rh, boolean apenasVisualizar) {
        try {
            String fxml = "Dedicado".equalsIgnoreCase(rh.getTipo_ambiente())
                    ? "/com/mycompany/proton/views/FormFortesRH_Dedicado.fxml" : "/com/mycompany/proton/views/FormFortesRH.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            FormFortesRHController controller = loader.getController();
            if (apenasVisualizar) {
                controller.carregarDadosParaVisualizacao(rh);
            } else {
                controller.carregarDadosParaEdicao(rh);
            }
            Stage stage = new Stage();
            stage.setTitle((apenasVisualizar ? "Visualizando: " : "Editando: ") + rh.getCliente());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            carregarTodasAsTabelas();
            atualizarDashboard();
        } catch (Exception e) {
            /* ignora */ }
    }

    private void abrirModalCancelados(ClienteCancelado cliente, boolean apenasVisualizar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/proton/views/FormCancelado.fxml"));
            Parent root = loader.load();
            FormCanceladoController controller = loader.getController();
            if (apenasVisualizar) {
                controller.carregarDadosParaVisualizacao(cliente);
            } else {
                controller.carregarDadosParaEdicao(cliente);
            }
            Stage stage = new Stage();
            stage.setTitle((apenasVisualizar ? "Ficha Histórica: " : "Ajustando Histórico: ") + cliente.getCliente_razao());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            carregarTodasAsTabelas();
            atualizarDashboard();
        } catch (Exception e) {
            /* ignora */ }
    }

    private void abrirModalDetalhesCompartilhados(ClienteCompartilhado cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/proton/views/FormClienteCompartilhado.fxml"));
            Parent root = loader.load();
            FormClienteCompartilhadoController controller = loader.getController();
            controller.carregarDadosParaVisualizacao(cliente);
            Stage stage = new Stage();
            stage.setTitle("Visualização: " + cliente.getRazaoSocial());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            /* ignora */ }
    }

    private void abrirModalDetalhesDedicados(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/proton/views/FormCliente.fxml"));
            Parent root = loader.load();
            FormClienteController controller = loader.getController();
            controller.carregarDadosParaVisualizacao(cliente);
            Stage stage = new Stage();
            stage.setTitle("Visualização: " + cliente.getNome());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            /* ignora */ }
    }

    private void abrirModalEdicaoDedicados(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/proton/views/FormCliente.fxml"));
            Parent root = loader.load();
            FormClienteController controller = loader.getController();
            controller.carregarDadosParaEdicao(cliente);
            Stage stage = new Stage();
            stage.setTitle("Editando: " + cliente.getNome());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            exibirNotificacaoToast(cliente.getNome(), "Atualização");
            carregarTodasAsTabelas();
            atualizarDashboard();
        } catch (Exception e) {
            /* ignora */ }
    }

    private void abrirModalEdicaoCompartilhados(ClienteCompartilhado cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/proton/views/FormClienteCompartilhado.fxml"));
            Parent root = loader.load();
            FormClienteCompartilhadoController controller = loader.getController();
            controller.carregarDadosParaEdicao(cliente);
            Stage stage = new Stage();
            stage.setTitle("Editando: " + cliente.getRazaoSocial());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            exibirNotificacaoToast(cliente.getRazaoSocial(), "Atualização");
            carregarTodasAsTabelas();
            atualizarDashboard();
        } catch (Exception e) {
            /* ignora */ }
    }

    @FXML
    private void abrirModalAuditoria(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mycompany/proton/views/FormAuditoria.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Relatório de Auditoria e Conformidade");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            exibirAlerta("Erro", "Falha ao abrir Auditoria: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    // ==================== CLASSE INTERNA: MODELO PARA TABELA "NOVOS HOJE" ====================
    public static class NovoClienteHoje {

        private String hora;
        private String tecnico;
        private String cliente;
        private String nuvem;

        public NovoClienteHoje(String hora, String tecnico, String cliente, String nuvem) {
            this.hora = hora;
            this.tecnico = tecnico;
            this.cliente = cliente;
            this.nuvem = nuvem;
        }

        public String getHora() {
            return hora;
        }

        public String getTecnico() {
            return tecnico;
        }

        public String getCliente() {
            return cliente;
        }

        public String getNuvem() {
            return nuvem;
        }
    }
    // ==================== CLASSE INTERNA: MODELO PARA TABELA "TÉCNICOS ONLINE" ====================
    public static class TecnicoOnline {
        private String usuario, setor, acesso, status;

        public TecnicoOnline(String usuario, String setor, String acesso, String status) {
            this.usuario = usuario;
            this.setor = setor;
            this.acesso = acesso;
            this.status = status;
        }
        public String getUsuario() { return usuario; }
        public String getSetor() { return setor; }
        public String getAcesso() { return acesso; }
        public String getStatus() { return status; }
    }
}

package com.mycompany.proton.controller;

import com.mycompany.proton.App;
import com.mycompany.proton.model.ClienteCancelado;
import com.mycompany.proton.model.ClienteCompartilhado;
import com.mycompany.proton.model.Cliente;
import com.mycompany.proton.model.FortesRH;
import com.mycompany.proton.util.DatabaseConnection;
import com.mycompany.proton.util.LoggerAuditoria;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controlador da tela principal (Dashboard / Painel de Gestão).
 *
 * CORREÇÕES APLICADAS: 1. Método conectar() agora chama
 * DatabaseConnection.conectar() que existe após a correção de
 * DatabaseConnection.java. Antes chamava DatabaseConnection.conectar() que não
 * existia, causando erro de compilação "cannot find symbol". 2. Instanciação de
 * FortesRH no carregarTodasAsTabelas() agora usa o construtor com 12 parâmetros
 * (original) — mantém compatibilidade. 3. Leitura de colunas do FortesRH no
 * dashboard usa safeString() para evitar SQLException quando colunas como
 * url_acesso são NULL no banco. 4. Método safeString() adicionado para leitura
 * segura de colunas.
 */
public class PrimaryController implements Initializable {

    // ==================== RÓTULOS E CAMPOS ====================
    @FXML
    private Label lblTituloAba;
    @FXML
    private TextField txtPesquisa;

    // ==================== BOTÕES DE NAVEGAÇÃO ====================
    @FXML
    private Button btnDedicados;
    @FXML
    private Button btnCompartilhados;
    @FXML
    private Button btnFortesRH;
    @FXML
    private Button btnCancelados;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnConfiguracoes;

    // ==================== BOTÕES DE AÇÃO ====================
    @FXML
    private Button btnDetalhes;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnDeletar;

    // ==================== TABELA DEDICADOS ====================
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

    // ==================== TABELA COMPARTILHADOS ====================
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

    // ==================== TABELA FORTES RH ====================
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

    // ==================== TABELA CANCELADOS ====================
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

    // ==================== DASHBOARD ====================
    @FXML
    private AnchorPane paneDashboard;
    @FXML
    private PieChart graficoNuvem;
    @FXML
    private BarChart<String, Number> graficoSegmentos;
    @FXML
    private Label lblTotalCancelados;
    @FXML
    private Label lblCanceladosHoje;
    @FXML
    private TableView<NovoClienteHoje> tabelaNovosHoje;
    @FXML
    private TableColumn<NovoClienteHoje, String> colHojeHora;
    @FXML
    private TableColumn<NovoClienteHoje, String> colHojeTecnico;
    @FXML
    private TableColumn<NovoClienteHoje, String> colHojeCliente;
    @FXML
    private TableColumn<NovoClienteHoje, String> colHojeNuvem;
    @FXML
    private Label lblUsuarioLogado;
    @FXML
    private Circle circleStatus;
    @FXML
    private TableView<TecnicoOnline> tabelaTecnicos;
    @FXML
    private TableColumn<TecnicoOnline, String> colTecUsuario;
    @FXML
    private TableColumn<TecnicoOnline, String> colTecSetor;
    @FXML
    private TableColumn<TecnicoOnline, String> colTecAcesso;
    @FXML
    private TableColumn<TecnicoOnline, String> colTecStatus;

    // ==================== LISTAS OBSERVÁVEIS ====================
    private ObservableList<Cliente> listaDedicados = FXCollections.observableArrayList();
    private ObservableList<ClienteCompartilhado> listaCompartilhados = FXCollections.observableArrayList();
    private ObservableList<FortesRH> listaFortesRH = FXCollections.observableArrayList();
    private ObservableList<ClienteCancelado> listaCancelados = FXCollections.observableArrayList();

    private int abaAtiva = 5;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        deligarBotoesAcao();

        // Colunas Dedicados
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

        // Colunas Compartilhados
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

        // Colunas FortesRH
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

        // Colunas Cancelados
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

        // Colunas Dashboard
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
        if (colTecUsuario != null) {
            colTecUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        }
        if (colTecSetor != null) {
            colTecSetor.setCellValueFactory(new PropertyValueFactory<>("setor"));
        }
        if (colTecAcesso != null) {
            colTecAcesso.setCellValueFactory(new PropertyValueFactory<>("acesso"));
        }
        if (colTecStatus != null) {
            colTecStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        // Indicador de sessão
        String user = App.getUsuarioLogado();
        if (lblUsuarioLogado != null) {
            if (user != null && !user.isEmpty()) {
                lblUsuarioLogado.setText(user);
                if (circleStatus != null) {
                    circleStatus.setFill(Color.web("#198754"));
                }
            } else {
                lblUsuarioLogado.setText("Desconectado / Standby");
                if (circleStatus != null) {
                    circleStatus.setFill(Color.web("#ffc107"));
                }
            }
        }

        configurarOuvintesSelecao(tabelaDedicados);
        configurarOuvintesSelecao(tabelaCompartilhados);
        configurarOuvintesSelecao(tabelaFortesRH);
        configurarOuvintesSelecao(tabelaCancelados);

        carregarTodasAsTabelas();
        configurarFiltroPesquisa();
        abaDashboard(null);
    }

    // ==========================================================
    // CONEXÃO
    // ==========================================================
    /**
     * CORREÇÃO: Chama DatabaseConnection.conectar() que agora existe após a
     * correção de DatabaseConnection.java. Antes: DatabaseConnection.conectar()
     * não existia → erro de compilação.
     */
    private Connection conectar() throws SQLException {
        return DatabaseConnection.conectar();
    }

    // ==========================================================
    // LISTENERS E CONTROLE DE BOTÕES
    // ==========================================================
    private void configurarOuvintesSelecao(TableView<?> tabela) {
        if (tabela != null) {
            tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
                if (novo != null) {
                    ligarBotoesAcao();
                } else {
                    deligarBotoesAcao();
                }
            });
            tabela.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && tabela.getSelectionModel().getSelectedItem() != null) {
                    acaoDetalhes(null);
                }
            });
        }
    }

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

    // ==========================================================
    // NOTIFICAÇÃO TOAST
    // ==========================================================
    private void exibirNotificacaoToast(String cliente, String tipoAcao) {
        String usuarioAtual = App.getUsuarioLogado() != null ? App.getUsuarioLogado() : "Técnico";
        String mensagem = "🔔 " + usuarioAtual + "\n" + tipoAcao + ": " + cliente;

        Popup popup = new Popup();
        popup.setAutoFix(true);

        Label label = new Label(mensagem);
        label.setStyle("-fx-background-color: #2b3035; -fx-text-fill: #198754; -fx-padding: 15px; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);");
        popup.getContent().add(label);

        Stage stage = (Stage) btnDashboard.getScene().getWindow();
        popup.show(stage, stage.getX() + stage.getWidth() - 350, stage.getY() + stage.getHeight() - 120);
        new Timeline(new KeyFrame(Duration.seconds(4), e -> popup.hide())).play();
    }

    // ==========================================================
    // NAVEGAÇÃO DE ABAS
    // ==========================================================
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
        if (txtPesquisa != null) {
            txtPesquisa.clear();
        }
        deligarBotoesAcao();
    }

    @FXML
    private void abaClientesDedicados(ActionEvent event) {
        abaAtiva = 1;
        resetarBotoesMenu();
        if (lblTituloAba != null) {
            lblTituloAba.setText("Clientes Dedicados");
        }
        if (btnDedicados != null) {
            btnDedicados.setStyle(
                    "-fx-background-color: #3b4248; -fx-text-fill: white; "
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
            btnCompartilhados.setStyle(
                    "-fx-background-color: #3b4248; -fx-text-fill: white; "
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
            btnFortesRH.setStyle(
                    "-fx-background-color: #3b4248; -fx-text-fill: white; "
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
            btnCancelados.setStyle(
                    "-fx-background-color: #3b4248; -fx-text-fill: white; "
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
            btnDashboard.setStyle(
                    "-fx-background-color: #3b4248; -fx-text-fill: white; "
                    + "-fx-border-color: #6f42c1; -fx-border-width: 0 0 0 4; "
                    + "-fx-alignment: center-left; -fx-font-weight: bold;");
        }
        if (paneDashboard != null) {
            paneDashboard.setVisible(true);
        }
        atualizarDashboard();
    }

    // ==========================================================
    // CARGA DE DADOS
    // ==========================================================
    private void carregarTodasAsTabelas() {
        listaDedicados.clear();
        listaCompartilhados.clear();
        listaFortesRH.clear();
        listaCancelados.clear();

        try (Connection conexao = conectar()) {

            // Clientes Dedicados
            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT * FROM clientes_dedicados ORDER BY id ASC"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    listaDedicados.add(new Cliente(
                            rs.getInt("id"),
                            safeString(rs, "cliente"),
                            safeString(rs, "cnpj_cpf"),
                            rs.getInt("qnt_de_servs"),
                            safeString(rs, "ad"),
                            safeString(rs, "ambiente"),
                            rs.getBoolean("vpn"),
                            "Ver Servidores"
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Erro Dedicados: " + e.getMessage());
            }

            // Clientes Compartilhados
            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT * FROM clientes_compartilhados ORDER BY id ASC"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    listaCompartilhados.add(new ClienteCompartilhado(
                            rs.getInt("id"),
                            safeString(rs, "tipo_nuvem"),
                            rs.getInt("pod"),
                            safeString(rs, "data_criacao"),
                            safeString(rs, "razao_social"),
                            safeString(rs, "cpf_cnpj"),
                            safeString(rs, "razao_cnpj_antigos"),
                            safeString(rs, "cod_ag"),
                            safeString(rs, "pasta_rede"),
                            safeString(rs, "contato"),
                            rs.getInt("usuarios"),
                            safeString(rs, "origem"),
                            safeString(rs, "telefone"),
                            safeString(rs, "email"),
                            safeString(rs, "sistemas"),
                            safeString(rs, "status"),
                            safeString(rs, "banco"),
                            "N/A"
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Erro Compartilhados: " + e.getMessage());
            }

            // FortesRH — CORREÇÃO: construtor com 12 parâmetros (original mantido)
            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT * FROM fortesrh ORDER BY id ASC"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    listaFortesRH.add(new FortesRH(
                            rs.getInt("id"),
                            safeString(rs, "tipo_ambiente"),
                            safeString(rs, "cliente"),
                            safeString(rs, "cnpj_cpf"),
                            safeString(rs, "url_acesso"),
                            safeString(rs, "servidor_app"),
                            safeString(rs, "banco_dados"),
                            safeString(rs, "pasta_web"),
                            safeString(rs, "usuario_db"),
                            safeString(rs, "senha_db"),
                            safeString(rs, "load_balance"),
                            safeString(rs, "ip_load_balance")
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Erro FortesRH: " + e.getMessage());
            }

            // Cancelados
            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT * FROM clientes_cancelados ORDER BY id ASC"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    listaCancelados.add(new ClienteCancelado(
                            rs.getInt("id"),
                            safeString(rs, "tipo_nuvem"),
                            rs.getInt("pod"),
                            safeString(rs, "data_criacao"),
                            safeString(rs, "cliente_razao"),
                            safeString(rs, "status_antigo"),
                            safeString(rs, "inicio_cancelamento"),
                            safeString(rs, "final_cancelamento"),
                            safeString(rs, "chamado"),
                            safeString(rs, "tecnico_responsavel")
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Erro Cancelados: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro fatal de conexão: " + e.getMessage());
        }
    }

    /**
     * CORREÇÃO: Leitura segura de coluna String do ResultSet. Retorna string
     * vazia se a coluna for NULL, evitando NPE ao popular os modelos.
     */
    private String safeString(ResultSet rs, String coluna) {
        try {
            String v = rs.getString(coluna);
            return v != null ? v : "";
        } catch (SQLException e) {
            return "";
        }
    }

    private void atualizarDashboard() {
        if (graficoNuvem == null || graficoSegmentos == null) {
            return;
        }
        graficoNuvem.getData().clear();
        graficoSegmentos.getData().clear();

        try (Connection conexao = conectar()) {
            int totalDedicados = 0, totalCompartilhados = 0;

            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM clientes_dedicados"); ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    totalDedicados = rs.getInt(1);
                }
            }
            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM clientes_compartilhados"); ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    totalCompartilhados = rs.getInt(1);
                }
            }

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Dedicados (" + totalDedicados + ")", totalDedicados),
                    new PieChart.Data("Compartilhados (" + totalCompartilhados + ")", totalCompartilhados)
            );
            graficoNuvem.setData(pieData);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Qtd por Segmento");
            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT segmento, COUNT(*) FROM bancos_nuvem_compartilhada "
                    + "WHERE segmento != 'N/A' GROUP BY segmento"); ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    series.getData().add(new XYChart.Data<>(rs.getString(1), rs.getInt(2)));
                }
            }
            graficoSegmentos.getData().add(series);

            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM clientes_cancelados"); ResultSet rs = cmd.executeQuery()) {
                if (rs.next() && lblTotalCancelados != null) {
                    lblTotalCancelados.setText(String.valueOf(rs.getInt(1)));
                }
            }

            try (PreparedStatement cmd = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM clientes_cancelados WHERE inicio_cancelamento = CURRENT_DATE"); ResultSet rs = cmd.executeQuery()) {
                if (rs.next() && lblCanceladosHoje != null) {
                    lblCanceladosHoje.setText(String.valueOf(rs.getInt(1)));
                }
            }

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
                    String horaBruta = rs.getString("hora");
                    String tempoLimpo = (horaBruta != null && horaBruta.length() >= 5)
                            ? horaBruta.substring(0, 5) : "00:00";
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
            if (tabelaTecnicos != null) {
                tabelaTecnicos.setItems(FXCollections.observableArrayList());
            }

        } catch (SQLException e) {
            System.out.println("Erro BI: " + e.getMessage());
        }
    }

    // ==========================================================
    // FILTRO DE PESQUISA
    // ==========================================================
    private void configurarFiltroPesquisa() {
        FilteredList<Cliente> fDed = new FilteredList<>(listaDedicados, b -> true);
        FilteredList<ClienteCompartilhado> fComp = new FilteredList<>(listaCompartilhados, b -> true);
        FilteredList<FortesRH> fRh = new FilteredList<>(listaFortesRH, b -> true);
        FilteredList<ClienteCancelado> fCanc = new FilteredList<>(listaCancelados, b -> true);

        if (txtPesquisa != null) {
            txtPesquisa.textProperty().addListener((observable, oldValue, newValue) -> {
                String ft = (newValue == null) ? "" : newValue.toLowerCase();
                fDed.setPredicate(c -> ft.isEmpty() || (c.getNome() != null
                        && c.getNome().toLowerCase().contains(ft)));
                fComp.setPredicate(c -> ft.isEmpty() || (c.getRazaoSocial() != null
                        && c.getRazaoSocial().toLowerCase().contains(ft)));
                fRh.setPredicate(c -> ft.isEmpty() || (c.getCliente() != null
                        && c.getCliente().toLowerCase().contains(ft)));
                fCanc.setPredicate(c -> ft.isEmpty() || (c.getCliente_razao() != null
                        && c.getCliente_razao().toLowerCase().contains(ft)));
            });
        }

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
    // AÇÕES
    // ==========================================================
    @FXML
    private void abrirConfiguracoes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/mycompany/proton/views/FormConfiguracoes.fxml"));
            Parent root = loader.load();
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

    @FXML
    private void abrirModalNovoCliente(ActionEvent event) {
        try {
            String caminhoFxml = null;
            String tituloTela = "";

            if (abaAtiva == 5 || abaAtiva == 3) {
                if (abaAtiva == 3) {
                    caminhoFxml = escolherTipoFortesRH();
                    tituloTela = "Novo Registro FortesRH";
                } else {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Novo Cadastro (Proton)");
                    alert.setHeaderText("O que você deseja cadastrar?");
                    ButtonType btnDed = new ButtonType("Dedicado");
                    ButtonType btnComp = new ButtonType("Compartilhado");
                    ButtonType btnRh = new ButtonType("FortesRH");
                    ButtonType btnCanc = new ButtonType("Cancelamento");
                    ButtonType btnSair = new ButtonType("Cancelar",
                            javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
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

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle(tituloTela);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            exibirNotificacaoToast("Novo Registro Efetuado", "Inclusão");
            carregarTodasAsTabelas();
            atualizarDashboard();

        } catch (Exception e) {
            System.out.println("Erro ao abrir cadastro: " + e.getMessage());
        }
    }

    private String escolherTipoFortesRH() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Escolha o Ambiente");
        alert.setHeaderText("Cadastrar Novo FortesRH");
        alert.setContentText("Qual ambiente do FortesRH você deseja registrar?");
        ButtonType btnCompartilhado = new ButtonType("Compartilhado");
        ButtonType btnDedicado = new ButtonType("Dedicado");
        ButtonType btnCancelar = new ButtonType("Cancelar",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
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

    @FXML
    private void acaoDeletar(ActionEvent event) {
        String logado = App.getUsuarioLogado();
        if (logado == null || logado.isEmpty()) {
            return;
        }

        boolean isAutorizado = false;
        String sqlAuth = "SELECT nivel_acesso FROM usuarios_sistema WHERE email = ?";
        try (Connection conn = conectar(); PreparedStatement cmd = conn.prepareStatement(sqlAuth)) {
            cmd.setString(1, logado.toLowerCase());
            try (ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    String nivel = rs.getString("nivel_acesso").toUpperCase();
                    if (nivel.equals("MASTER") || nivel.equals("N2")) {
                        isAutorizado = true;
                    }
                }
            }
        } catch (SQLException e) {
            /* mantém false */ }

        if (!isAutorizado) {
            exibirAlerta("Acesso Restrito",
                    "Apenas contas N2 ou Master podem excluir registros.", Alert.AlertType.ERROR);
            return;
        }

        String nomeCliente = "";
        int idCliente = -1;
        String tabelaBanco = "";

        if (abaAtiva == 1) {
            Cliente s = tabelaDedicados.getSelectionModel().getSelectedItem();
            if (s == null) {
                return;
            }
            nomeCliente = s.getNome();
            idCliente = s.getId();
            tabelaBanco = "clientes_dedicados";
        } else if (abaAtiva == 2) {
            ClienteCompartilhado s = tabelaCompartilhados.getSelectionModel().getSelectedItem();
            if (s == null) {
                return;
            }
            nomeCliente = s.getRazaoSocial();
            idCliente = s.getId();
            tabelaBanco = "clientes_compartilhados";
        } else if (abaAtiva == 3) {
            FortesRH s = tabelaFortesRH.getSelectionModel().getSelectedItem();
            if (s == null) {
                return;
            }
            nomeCliente = s.getCliente();
            idCliente = s.getId();
            tabelaBanco = "fortesrh";
        } else if (abaAtiva == 4) {
            ClienteCancelado s = tabelaCancelados.getSelectionModel().getSelectedItem();
            if (s == null) {
                return;
            }
            nomeCliente = s.getCliente_razao();
            idCliente = s.getId();
            tabelaBanco = "clientes_cancelados";
        } else {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Exclusão");
        alert.setHeaderText("Atenção, " + logado + "!");
        alert.setContentText("Deseja DELETAR permanentemente: " + nomeCliente);
        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try (Connection conexao = conectar(); PreparedStatement comando = conexao.prepareStatement(
                    "DELETE FROM " + tabelaBanco + " WHERE id = ?")) {
                comando.setInt(1, idCliente);
                comando.executeUpdate();
                exibirAlerta("Sucesso", "Registro excluído.", Alert.AlertType.INFORMATION);
                exibirNotificacaoToast(nomeCliente, "Exclusão");
                LoggerAuditoria.registrar("EXCLUSÃO", "Deletou ID " + idCliente
                        + " (" + nomeCliente + ") de " + tabelaBanco);
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
            App.setUsuarioLogado(null);
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/com/mycompany/proton/views/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(new Scene(root));
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
        alert.setContentText("Versão: 2.0\nSistema de gestão interna para controle de clientes em nuvem.");
        alert.showAndWait();
    }

    // ==================== MODAIS ====================
    private void abrirModalFortesRH(FortesRH rh, boolean apenasVisualizar) {
        try {
            String fxml = "Dedicado".equalsIgnoreCase(rh.getTipo_ambiente())
                    ? "/com/mycompany/proton/views/FormFortesRH_Dedicado.fxml"
                    : "/com/mycompany/proton/views/FormFortesRH.fxml";
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
            System.out.println("Erro ao abrir FortesRH: " + e.getMessage());
        }
    }

    private void abrirModalCancelados(ClienteCancelado cliente, boolean apenasVisualizar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/mycompany/proton/views/FormCancelado.fxml"));
            Parent root = loader.load();
            FormCanceladoController controller = loader.getController();
            if (apenasVisualizar) {
                controller.carregarDadosParaVisualizacao(cliente);
            } else {
                controller.carregarDadosParaEdicao(cliente);
            }
            Stage stage = new Stage();
            stage.setTitle((apenasVisualizar ? "Ficha: " : "Editando: ") + cliente.getCliente_razao());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            carregarTodasAsTabelas();
            atualizarDashboard();
        } catch (Exception e) {
            System.out.println("Erro ao abrir Cancelado: " + e.getMessage());
        }
    }

    private void abrirModalDetalhesCompartilhados(ClienteCompartilhado cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/mycompany/proton/views/FormClienteCompartilhado.fxml"));
            Parent root = loader.load();
            FormClienteCompartilhadoController controller = loader.getController();
            controller.carregarDadosParaVisualizacao(cliente);
            Stage stage = new Stage();
            stage.setTitle("Visualização: " + cliente.getRazaoSocial());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            System.out.println("Erro ao abrir compartilhado: " + e.getMessage());
        }
    }

    private void abrirModalDetalhesDedicados(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/mycompany/proton/views/FormCliente.fxml"));
            Parent root = loader.load();
            FormClienteController controller = loader.getController();
            controller.carregarDadosParaVisualizacao(cliente);
            Stage stage = new Stage();
            stage.setTitle("Visualização: " + cliente.getNome());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            System.out.println("Erro ao abrir dedicado: " + e.getMessage());
        }
    }

    private void abrirModalEdicaoDedicados(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/mycompany/proton/views/FormCliente.fxml"));
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
            System.out.println("Erro ao abrir edição dedicado: " + e.getMessage());
        }
    }

    private void abrirModalEdicaoCompartilhados(ClienteCompartilhado cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/mycompany/proton/views/FormClienteCompartilhado.fxml"));
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
            System.out.println("Erro ao abrir edição compartilhado: " + e.getMessage());
        }
    }

    @FXML
    private void abrirModalAuditoria(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                    "/com/mycompany/proton/views/FormAuditoria.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Relatório de Auditoria");
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

    // ==================== CLASSES INTERNAS ====================
    public static class NovoClienteHoje {

        private String hora, tecnico, cliente, nuvem;

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

    public static class TecnicoOnline {

        private String usuario, setor, acesso, status;

        public TecnicoOnline(String usuario, String setor, String acesso, String status) {
            this.usuario = usuario;
            this.setor = setor;
            this.acesso = acesso;
            this.status = status;
        }

        public String getUsuario() {
            return usuario;
        }

        public String getSetor() {
            return setor;
        }

        public String getAcesso() {
            return acesso;
        }

        public String getStatus() {
            return status;
        }
    }
}

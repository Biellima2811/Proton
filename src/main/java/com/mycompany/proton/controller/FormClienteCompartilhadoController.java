package com.mycompany.proton.controller;

import com.mycompany.proton.util.LoggerAuditoria;
import com.mycompany.proton.controller.ConfigBancoController;
import com.mycompany.proton.model.ClienteCompartilhado;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador do formulário de Cliente Compartilhado.
 */
public class FormClienteCompartilhadoController implements Initializable {

    // ==================== ABA 1: DADOS GERAIS ====================
    @FXML
    private ComboBox<String> cbSegmento;
    @FXML
    private ComboBox<String> cbTipoNuvem;
    @FXML
    private DatePicker dpDataCriacao;
    @FXML
    private ComboBox<String> cbPod;
    @FXML
    private TextField txtRazaoSocial;
    @FXML
    private TextField txtCpfCnpj;
    @FXML
    private TextField txtCodAg;
    @FXML
    private TextField txtPastaRede;
    @FXML
    private TextField txtSistemas;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private ComboBox<String> cbBanco;
    @FXML
    private TextField txtContato;
    @FXML
    private TextField txtTelefone;
    @FXML
    private ComboBox<String> cbOrigem;
    @FXML
    private TextField txtUsuarios;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtRazaoAntiga;

    // ==================== ABA 2: CADASTRO DE EMAILS ====================
    @FXML
    private TextField txtEmailUsuario;
    @FXML
    private TableView<EmailItem> tabelaEmails;
    @FXML
    private TableColumn<EmailItem, Integer> colUserNum;
    @FXML
    private TableColumn<EmailItem, String> colUserEmail;
    private ObservableList<EmailItem> listaEmailsTemporaria = FXCollections.observableArrayList();

    // ==================== ABA 3: DADOS DE BANCO DE DADOS ====================
    @FXML
    private TextField txtDbServidor;
    @FXML
    private TextField txtDbNome;
    @FXML
    private TextField txtDbConexao;
    @FXML
    private ComboBox<String> cbDbSgbd;
    @FXML
    private ComboBox<String> cbUserBanco;
    @FXML
    private TextField txtSenhaBanco;

    @FXML
    private TableView<BancoItem> tabelaBancos;
    @FXML
    private TableColumn<BancoItem, String> colBancoServidor;
    @FXML
    private TableColumn<BancoItem, String> colBancoNome;
    @FXML
    private TableColumn<BancoItem, String> colBancoSgbd;
    @FXML
    private TableColumn<BancoItem, String> colBancoCaminho;
    @FXML
    private TableColumn<BancoItem, String> colBancoUsuario;
    @FXML
    private TableColumn<BancoItem, String> colBancoSenha;
    private ObservableList<BancoItem> listaBancosTemporaria = FXCollections.observableArrayList();

    // ==================== ABA 4: WEBSERVICES E INTEGRAÇÃO ====================
    @FXML
    private TextField txtWsIntegracao;
    @FXML
    private ComboBox<String> cbWsServidor;
    @FXML
    private TextField txtWsPorta;
    @FXML
    private TextField txtWsSoap;
    @FXML
    private TextField txtWsWsdl;
    @FXML
    private TextField txtWsStringBd;
    @FXML
    private TextField txtWsUserBd;
    @FXML
    private TextField txtWsSenhaBd;

    // ==================== CONTROLE DE MODO ====================
    private boolean modoVisualizacao = false;
    private boolean modoEdicao = false;
    private Integer idClienteEdicao = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (cbSegmento != null) {
            cbSegmento.setItems(FXCollections.observableArrayList("Contábil", "Corporativo"));
        }
        if (cbTipoNuvem != null) {
            cbTipoNuvem.setItems(FXCollections.observableArrayList("Compartilhada", "Dedicada"));
        }
        if (cbStatus != null) {
            cbStatus.setItems(FXCollections.observableArrayList("Ativo", "Ativando", "Desativado", "Pendente"));
        }
        if (cbBanco != null) {
            cbBanco.setItems(FXCollections.observableArrayList("SQL", "Firebird", "FB e SQL"));
        }
        if (cbOrigem != null) {
            cbOrigem.setItems(FXCollections.observableArrayList("Novo", "Base", "Novo Esocial"));
        }
        if (cbPod != null) {
            cbPod.setItems(FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8"));
        }
        if (cbDbSgbd != null) {
            cbDbSgbd.setItems(FXCollections.observableArrayList("Firebird", "MSSQL"));
        }
        if (cbUserBanco != null) {
            cbUserBanco.setItems(FXCollections.observableArrayList("SYSDBA", "cliente.sql", "sa"));
        }

        if (colUserNum != null) {
            colUserNum.setCellValueFactory(cellData -> cellData.getValue().numeroProperty().asObject());
        }
        if (colUserEmail != null) {
            colUserEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        }
        if (tabelaEmails != null) {
            tabelaEmails.setItems(listaEmailsTemporaria);
        }

        if (colBancoServidor != null) {
            colBancoServidor.setCellValueFactory(cellData -> cellData.getValue().servidorProperty());
        }
        if (colBancoNome != null) {
            colBancoNome.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        }
        if (colBancoSgbd != null) {
            colBancoSgbd.setCellValueFactory(cellData -> cellData.getValue().sgbdProperty());
        }
        if (colBancoCaminho != null) {
            colBancoCaminho.setCellValueFactory(cellData -> cellData.getValue().conexaoProperty());
        }
        if (colBancoUsuario != null) {
            colBancoUsuario.setCellValueFactory(cellData -> cellData.getValue().usuarioProperty());
        }
        if (colBancoSenha != null) {
            colBancoSenha.setCellValueFactory(cellData -> cellData.getValue().senhaProperty());
        }
        if (tabelaBancos != null) {
            tabelaBancos.setItems(listaBancosTemporaria);
        }

        // Carrega a lista de servidores WS da Aba 4
        carregarServidoresWsExistentes();
    }

    private Connection conectar() throws SQLException {
        String urlConexao = String.format("jdbc:postgresql://%s:5432/%s", ConfigBancoController.getIpServidor(), ConfigBancoController.getNomeBanco());
        return DriverManager.getConnection(urlConexao, ConfigBancoController.getUsuarioBD(), ConfigBancoController.getSenhaBD());
    }

    /**
     * Consulta o banco de dados e carrega apenas os Servidores WS distintos já
     * cadastrados.
     */
    private void carregarServidoresWsExistentes() {
        ObservableList<String> servidores = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT servidor_ws FROM webservices_compartilhados WHERE servidor_ws IS NOT NULL AND servidor_ws <> 'N/A' AND servidor_ws <> '' ORDER BY servidor_ws";

        try (Connection conexao = conectar(); PreparedStatement cmd = conexao.prepareStatement(sql); ResultSet rs = cmd.executeQuery()) {
            while (rs.next()) {
                servidores.add(rs.getString("servidor_ws"));
            }
            if (cbWsServidor != null) {
                cbWsServidor.setItems(servidores);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar lista de Servidores WS: " + e.getMessage());
        }
    }

    @FXML
    private void adicionarBanco(ActionEvent event) {
        if (txtDbServidor.getText() == null || txtDbServidor.getText().trim().isEmpty()
                || txtDbNome.getText() == null || txtDbNome.getText().trim().isEmpty()
                || cbDbSgbd.getValue() == null || cbDbSgbd.getValue().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha IP do Servidor, Nome do Banco e SGBD para adicionar à lista.", Alert.AlertType.WARNING);
            return;
        }

        listaBancosTemporaria.add(new BancoItem(
                txtDbServidor.getText().trim(),
                txtDbNome.getText().trim(),
                txtDbConexao.getText() != null ? txtDbConexao.getText().trim() : "",
                cbDbSgbd.getValue().trim(),
                cbUserBanco.getValue() != null ? cbUserBanco.getValue().trim() : "N/A",
                txtSenhaBanco.getText() != null ? txtSenhaBanco.getText().trim() : "",
                cbSegmento.getValue() != null ? cbSegmento.getValue().trim() : "Corporativo"
        ));

        txtDbServidor.clear();
        txtDbNome.clear();
        txtDbConexao.clear();
        txtSenhaBanco.clear();
    }

    @FXML
    private void removerBanco(ActionEvent event) {
        BancoItem selecionado = tabelaBancos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            listaBancosTemporaria.remove(selecionado);
        }
    }

    @FXML
    private void inserirEmail(ActionEvent event) {
        String emailInserido = txtEmailUsuario.getText().trim();
        int limite = obterLimiteUsuarios();

        if (limite <= 0) {
            exibirAlerta("Atenção", "Por favor, preencha primeiro a Qtd. de Usuários (aba 1) antes de inserir emails.", Alert.AlertType.WARNING);
            return;
        }

        if (!emailInserido.isEmpty()) {
            if (listaEmailsTemporaria.size() < limite) {
                listaEmailsTemporaria.add(new EmailItem(listaEmailsTemporaria.size() + 1, emailInserido));
                txtEmailUsuario.clear();
            } else {
                exibirAlerta("Limite Excedido", "Você já cadastrou o número máximo de usuários permitido (" + limite + ").", Alert.AlertType.WARNING);
            }
        }
    }

    @FXML
    private void importarCsv(ActionEvent event) {
        exibirAlerta("Aviso", "A funcionalidade de importação de CSV será implementada em uma versão futura.", Alert.AlertType.INFORMATION);
    }

    private int obterLimiteUsuarios() {
        if (txtUsuarios == null || txtUsuarios.getText() == null || txtUsuarios.getText().trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(txtUsuarios.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String getValorSeguro(ResultSet rs, String nomeColuna) {
        try {
            String v = rs.getString(nomeColuna);
            return v != null ? v : "N/A";
        } catch (SQLException e) {
            return "N/A";
        }
    }

    public void carregarDadosParaVisualizacao(ClienteCompartilhado cliente) {
        this.modoVisualizacao = true;
        preencherCamposComuns(cliente);
        bloquearCampos();
    }

    public void carregarDadosParaEdicao(ClienteCompartilhado cliente) {
        this.modoEdicao = true;
        this.idClienteEdicao = cliente.getId();
        preencherCamposComuns(cliente);
    }

    private void bloquearCampos() {
        if (txtRazaoSocial != null) {
            txtRazaoSocial.setEditable(false);
        }
        if (txtCpfCnpj != null) {
            txtCpfCnpj.setEditable(false);
        }
        if (txtCodAg != null) {
            txtCodAg.setEditable(false);
        }
        if (txtPastaRede != null) {
            txtPastaRede.setEditable(false);
        }
        if (txtSistemas != null) {
            txtSistemas.setEditable(false);
        }
        if (txtContato != null) {
            txtContato.setEditable(false);
        }
        if (txtTelefone != null) {
            txtTelefone.setEditable(false);
        }
        if (txtUsuarios != null) {
            txtUsuarios.setEditable(false);
        }
        if (txtEmail != null) {
            txtEmail.setEditable(false);
        }
        if (txtRazaoAntiga != null) {
            txtRazaoAntiga.setEditable(false);
        }

        if (cbSegmento != null) {
            cbSegmento.setDisable(true);
        }
        if (cbTipoNuvem != null) {
            cbTipoNuvem.setDisable(true);
        }
        if (cbStatus != null) {
            cbStatus.setDisable(true);
        }
        if (cbBanco != null) {
            cbBanco.setDisable(true);
        }
        if (cbOrigem != null) {
            cbOrigem.setDisable(true);
        }
        if (cbPod != null) {
            cbPod.setDisable(true);
        }
        if (dpDataCriacao != null) {
            dpDataCriacao.setDisable(true);
        }

        // Webservices
        if (txtWsIntegracao != null) {
            txtWsIntegracao.setEditable(false);
        }
        if (cbWsServidor != null) {
            cbWsServidor.setDisable(true);
        }
        if (txtWsPorta != null) {
            txtWsPorta.setEditable(false);
        }
        if (txtWsSoap != null) {
            txtWsSoap.setEditable(false);
        }
        if (txtWsWsdl != null) {
            txtWsWsdl.setEditable(false);
        }
        if (txtWsStringBd != null) {
            txtWsStringBd.setEditable(false);
        }
        if (txtWsUserBd != null) {
            txtWsUserBd.setEditable(false);
        }
        if (txtWsSenhaBd != null) {
            txtWsSenhaBd.setEditable(false);
        }
    }

    private void preencherCamposComuns(ClienteCompartilhado cliente) {
        if (cliente == null) {
            return;
        }

        try {
            if (cbTipoNuvem != null && cliente.getTipoNuvem() != null) {
                cbTipoNuvem.getEditor().setText(cliente.getTipoNuvem());
            }
        } catch (Exception e) {
        }
        try {
            if (cbPod != null) {
                cbPod.getEditor().setText(String.valueOf(cliente.getPod()));
            }
        } catch (Exception e) {
        }
        try {
            if (txtRazaoSocial != null) {
                txtRazaoSocial.setText(cliente.getRazaoSocial() != null ? cliente.getRazaoSocial() : "");
            }
        } catch (Exception e) {
        }
        try {
            if (txtCpfCnpj != null) {
                txtCpfCnpj.setText(cliente.getCpfCnpj() != null ? cliente.getCpfCnpj() : "");
            }
        } catch (Exception e) {
        }
        try {
            if (txtCodAg != null) {
                txtCodAg.setText(cliente.getCodAg() != null ? cliente.getCodAg() : "");
            }
        } catch (Exception e) {
        }
        try {
            if (txtPastaRede != null) {
                txtPastaRede.setText(cliente.getPastaRede() != null ? cliente.getPastaRede() : "");
            }
        } catch (Exception e) {
        }
        try {
            if (txtSistemas != null) {
                txtSistemas.setText(cliente.getSistemas() != null ? cliente.getSistemas() : "");
            }
        } catch (Exception e) {
        }
        try {
            if (cbStatus != null && cliente.getStatus() != null) {
                cbStatus.getEditor().setText(cliente.getStatus());
            }
        } catch (Exception e) {
        }
        try {
            if (cbBanco != null && cliente.getBancoDados() != null) {
                cbBanco.getEditor().setText(cliente.getBancoDados());
            }
        } catch (Exception e) {
        }
        try {
            if (txtContato != null) {
                txtContato.setText(cliente.getContato() != null ? cliente.getContato() : "");
            }
        } catch (Exception e) {
        }
        try {
            if (txtTelefone != null) {
                txtTelefone.setText(cliente.getTelefone() != null ? cliente.getTelefone() : "");
            }
        } catch (Exception e) {
        }
        try {
            if (txtEmail != null) {
                txtEmail.setText(cliente.getEmail() != null ? cliente.getEmail() : "");
            }
        } catch (Exception e) {
        }
        try {
            if (cbOrigem != null && cliente.getOrigem() != null) {
                cbOrigem.getEditor().setText(cliente.getOrigem());
            }
        } catch (Exception e) {
        }
        try {
            if (txtUsuarios != null) {
                txtUsuarios.setText(String.valueOf(cliente.getUsuarios()));
            }
        } catch (Exception e) {
        }

        if (cliente.getDataCriacao() != null && !cliente.getDataCriacao().isEmpty() && !cliente.getDataCriacao().equals("N/A") && dpDataCriacao != null) {
            try {
                dpDataCriacao.setValue(LocalDate.parse(cliente.getDataCriacao().split(" ")[0]));
            } catch (Exception e) {
            }
        }

        try (Connection conexao = conectar()) {
            try {
                String sqlBanco = "SELECT * FROM bancos_nuvem_compartilhada WHERE id_cliente = ?";
                try (PreparedStatement cmd = conexao.prepareStatement(sqlBanco)) {
                    cmd.setInt(1, cliente.getId());
                    try (ResultSet rs = cmd.executeQuery()) {
                        listaBancosTemporaria.clear();
                        while (rs.next()) {
                            listaBancosTemporaria.add(new BancoItem(
                                    getValorSeguro(rs, "ip_servidor"),
                                    getValorSeguro(rs, "nome_banco"),
                                    getValorSeguro(rs, "caminho_conexao"),
                                    getValorSeguro(rs, "sgbd"),
                                    getValorSeguro(rs, "usuario_banco"),
                                    getValorSeguro(rs, "senha_banco"),
                                    getValorSeguro(rs, "segmento")
                            ));
                        }
                    }
                }
            } catch (SQLException eBanco) {
            }

            if (!listaBancosTemporaria.isEmpty() && cbSegmento != null) {
                cbSegmento.getEditor().setText(listaBancosTemporaria.get(0).getSegmento());
            }

            try {
                String sqlAcessos = "SELECT email_usuario FROM usuarios_nuvem_compartilhada WHERE id_cliente = ?";
                try (PreparedStatement cmd = conexao.prepareStatement(sqlAcessos)) {
                    cmd.setInt(1, cliente.getId());
                    try (ResultSet rs = cmd.executeQuery()) {
                        int index = 1;
                        listaEmailsTemporaria.clear();
                        while (rs.next()) {
                            listaEmailsTemporaria.add(new EmailItem(index++, getValorSeguro(rs, "email_usuario")));
                        }
                    }
                }
            } catch (SQLException eAcesso) {
            }

            try {
                String sqlWs = "SELECT * FROM webservices_compartilhados WHERE id_cliente = ?";
                try (PreparedStatement cmdWs = conexao.prepareStatement(sqlWs)) {
                    cmdWs.setInt(1, cliente.getId());
                    try (ResultSet rsWs = cmdWs.executeQuery()) {
                        if (rsWs.next()) {
                            if (txtWsIntegracao != null) {
                                txtWsIntegracao.setText(getValorSeguro(rsWs, "integracao"));
                            }
                            if (cbWsServidor != null) {
                                cbWsServidor.getEditor().setText(getValorSeguro(rsWs, "servidor_ws"));
                            }
                            if (txtWsPorta != null) {
                                txtWsPorta.setText(getValorSeguro(rsWs, "porta"));
                            }
                            if (txtWsSoap != null) {
                                txtWsSoap.setText(getValorSeguro(rsWs, "endereco_soap"));
                            }
                            if (txtWsWsdl != null) {
                                txtWsWsdl.setText(getValorSeguro(rsWs, "endereco_wsdl"));
                            }
                            if (txtWsStringBd != null) {
                                txtWsStringBd.setText(getValorSeguro(rsWs, "string_conexao_bd"));
                            }
                            if (txtWsUserBd != null) {
                                txtWsUserBd.setText(getValorSeguro(rsWs, "usuario_bd"));
                            }
                            if (txtWsSenhaBd != null) {
                                txtWsSenhaBd.setText(getValorSeguro(rsWs, "senha_bd"));
                            }
                        }
                    }
                }
            } catch (SQLException eWs) {
                System.out.println("Erro ao ler tabela de Webservices: " + eWs.getMessage());
            }
        } catch (SQLException e) {
        }
    }

    private String calcularProximaPorta(Connection conexao, String servidorWs) {
        String sql = "SELECT MAX(CAST(NULLIF(porta, 'N/A') AS INTEGER)) AS max_porta FROM webservices_compartilhados WHERE servidor_ws = ?";
        try (PreparedStatement cmd = conexao.prepareStatement(sql)) {
            cmd.setString(1, servidorWs);
            try (ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    int maxPorta = rs.getInt("max_porta");
                    if (maxPorta > 0) {
                        return String.valueOf(maxPorta + 10);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao calcular porta: " + e.getMessage());
        }
        return "1030";
    }

    @FXML
    private void salvarCliente(ActionEvent event) {
        if (modoVisualizacao) {
            fecharJanela();
            return;
        }

        if (cbSegmento.getValue() == null || cbSegmento.getValue().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o campo Segmento.", Alert.AlertType.WARNING);
            return;
        }
        if (cbTipoNuvem.getValue() == null || cbTipoNuvem.getValue().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o Tipo de Nuvem.", Alert.AlertType.WARNING);
            return;
        }
        if (dpDataCriacao.getValue() == null) {
            exibirAlerta("Aviso", "Preencha a Data de Criação.", Alert.AlertType.WARNING);
            return;
        }
        if (cbPod.getValue() == null || cbPod.getValue().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o POD.", Alert.AlertType.WARNING);
            return;
        }
        if (txtRazaoSocial.getText() == null || txtRazaoSocial.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha a Razão Social.", Alert.AlertType.WARNING);
            return;
        }
        if (txtCpfCnpj.getText() == null || txtCpfCnpj.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o CPF/CNPJ.", Alert.AlertType.WARNING);
            return;
        }
        if (txtCodAg.getText() == null || txtCodAg.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o Código AG.", Alert.AlertType.WARNING);
            return;
        }
        if (txtPastaRede.getText() == null || txtPastaRede.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha a Pasta de Rede.", Alert.AlertType.WARNING);
            return;
        }
        if (txtSistemas.getText() == null || txtSistemas.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha os Sistemas.", Alert.AlertType.WARNING);
            return;
        }
        if (cbStatus.getValue() == null || cbStatus.getValue().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o Status.", Alert.AlertType.WARNING);
            return;
        }
        if (cbBanco.getValue() == null || cbBanco.getValue().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o Tipo de Base (Banco).", Alert.AlertType.WARNING);
            return;
        }
        if (txtContato.getText() == null || txtContato.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o Contato Responsável.", Alert.AlertType.WARNING);
            return;
        }
        if (txtTelefone.getText() == null || txtTelefone.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o Telefone.", Alert.AlertType.WARNING);
            return;
        }
        if (cbOrigem.getValue() == null || cbOrigem.getValue().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha a Origem.", Alert.AlertType.WARNING);
            return;
        }
        if (txtUsuarios.getText() == null || txtUsuarios.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha a Qtd. de Usuários.", Alert.AlertType.WARNING);
            return;
        }
        if (txtEmail.getText() == null || txtEmail.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o E-mail.", Alert.AlertType.WARNING);
            return;
        }

        int pod = 0, usuarios = 0;
        try {
            pod = Integer.parseInt(cbPod.getValue().trim());
        } catch (Exception e) {
            exibirAlerta("Erro", "P.O.D deve ser numérico.", Alert.AlertType.ERROR);
            return;
        }
        try {
            usuarios = Integer.parseInt(txtUsuarios.getText().trim());
        } catch (Exception e) {
            exibirAlerta("Erro", "Qtd. Usuários deve ser numérica.", Alert.AlertType.ERROR);
            return;
        }

        String sqlCliente = modoEdicao
                ? "UPDATE clientes_compartilhados SET tipo_nuvem=?, pod=?, data_criacao=?, razao_social=?, cpf_cnpj=?, razao_cnpj_antigos=?, cod_ag=?, pasta_rede=?, contato=?, usuarios=?, origem=?, telefone=?, email=?, sistemas=?, status=?, banco=? WHERE id=?"
                : "INSERT INTO clientes_compartilhados (tipo_nuvem, pod, data_criacao, razao_social, cpf_cnpj, razao_cnpj_antigos, cod_ag, pasta_rede, contato, usuarios, origem, telefone, email, sistemas, status, banco) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conexao = conectar()) {
            conexao.setAutoCommit(false);
            try {
                int idClienteAplicado = modoEdicao ? idClienteEdicao : -1;

                try (PreparedStatement cmdCliente = conexao.prepareStatement(sqlCliente, modoEdicao ? java.sql.Statement.NO_GENERATED_KEYS : java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    cmdCliente.setString(1, cbTipoNuvem.getValue().trim());
                    cmdCliente.setInt(2, pod);
                    cmdCliente.setDate(3, java.sql.Date.valueOf(dpDataCriacao.getValue()));
                    cmdCliente.setString(4, txtRazaoSocial.getText().trim());
                    cmdCliente.setString(5, txtCpfCnpj.getText().trim());
                    cmdCliente.setString(6, txtRazaoAntiga.getText() != null ? txtRazaoAntiga.getText().trim() : "");
                    cmdCliente.setString(7, txtCodAg.getText().trim());
                    cmdCliente.setString(8, txtPastaRede.getText().trim());
                    cmdCliente.setString(9, txtContato.getText().trim());
                    cmdCliente.setInt(10, usuarios);
                    cmdCliente.setString(11, cbOrigem.getValue().trim());
                    cmdCliente.setString(12, txtTelefone.getText().trim());
                    cmdCliente.setString(13, txtEmail.getText().trim());
                    cmdCliente.setString(14, txtSistemas.getText().trim());
                    cmdCliente.setString(15, cbStatus.getValue().trim());
                    cmdCliente.setString(16, cbBanco.getValue().trim());

                    if (modoEdicao) {
                        cmdCliente.setInt(17, idClienteEdicao);
                        cmdCliente.executeUpdate();
                    } else {
                        cmdCliente.executeUpdate();
                        try (ResultSet chaves = cmdCliente.getGeneratedKeys()) {
                            if (chaves.next()) {
                                idClienteAplicado = chaves.getInt(1);
                            }
                        }
                    }
                }

                if (idClienteAplicado == -1) {
                    throw new SQLException("ID do Cliente não obtido.");
                }

                if (modoEdicao) {
                    try (PreparedStatement cmdDelB = conexao.prepareStatement("DELETE FROM bancos_nuvem_compartilhada WHERE id_cliente = ?")) {
                        cmdDelB.setInt(1, idClienteAplicado);
                        cmdDelB.executeUpdate();
                    }
                    try (PreparedStatement cmdDelA = conexao.prepareStatement("DELETE FROM usuarios_nuvem_compartilhada WHERE id_cliente = ?")) {
                        cmdDelA.setInt(1, idClienteAplicado);
                        cmdDelA.executeUpdate();
                    }
                    try (PreparedStatement cmdDelWs = conexao.prepareStatement("DELETE FROM webservices_compartilhados WHERE id_cliente = ?")) {
                        cmdDelWs.setInt(1, idClienteAplicado);
                        cmdDelWs.executeUpdate();
                    }
                }

                if (!listaBancosTemporaria.isEmpty()) {
                    String sqlBancoQ = "INSERT INTO bancos_nuvem_compartilhada (id_cliente, segmento, razao_social, ip_servidor, nome_banco, caminho_conexao, caminho_banco, sgbd, usuario_banco, senha_banco) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement cmdBanco = conexao.prepareStatement(sqlBancoQ)) {
                        for (BancoItem b : listaBancosTemporaria) {
                            cmdBanco.setInt(1, idClienteAplicado);
                            cmdBanco.setString(2, b.getSegmento());
                            cmdBanco.setString(3, txtRazaoSocial.getText().trim());
                            cmdBanco.setString(4, b.getServidor());
                            cmdBanco.setString(5, b.getNome());
                            cmdBanco.setString(6, b.getConexao());
                            cmdBanco.setString(7, b.getConexao());
                            cmdBanco.setString(8, b.getSgbd());
                            cmdBanco.setString(9, b.getUsuario());
                            cmdBanco.setString(10, b.getSenha());
                            cmdBanco.addBatch();
                        }
                        cmdBanco.executeBatch();
                    }
                }

                if (!listaEmailsTemporaria.isEmpty()) {
                    try (PreparedStatement cmdAcesso = conexao.prepareStatement("INSERT INTO usuarios_nuvem_compartilhada (id_cliente, email_usuario) VALUES (?, ?)")) {
                        for (EmailItem item : listaEmailsTemporaria) {
                            cmdAcesso.setInt(1, idClienteAplicado);
                            cmdAcesso.setString(2, item.getEmail());
                            cmdAcesso.addBatch();
                        }
                        cmdAcesso.executeBatch();
                    }
                }

                String wsServidor = "";
                if (cbWsServidor != null) {
                    if (cbWsServidor.getValue() != null && !cbWsServidor.getValue().trim().isEmpty()) {
                        wsServidor = cbWsServidor.getValue().trim();
                    } else if (cbWsServidor.getEditor().getText() != null && !cbWsServidor.getEditor().getText().trim().isEmpty()) {
                        wsServidor = cbWsServidor.getEditor().getText().trim();
                    }
                }

                if (!wsServidor.isEmpty()) {
                    String wsPorta = txtWsPorta.getText() != null ? txtWsPorta.getText().trim() : "";

                    if (wsPorta.isEmpty() || wsPorta.equals("N/A")) {
                        wsPorta = calcularProximaPorta(conexao, wsServidor);
                    }

                    String sqlWsIns = "INSERT INTO webservices_compartilhados (id_cliente, integracao, servidor_ws, porta, endereco_soap, endereco_wsdl, string_conexao_bd, usuario_bd, senha_bd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement cmdWsIns = conexao.prepareStatement(sqlWsIns)) {
                        cmdWsIns.setInt(1, idClienteAplicado);
                        cmdWsIns.setString(2, txtWsIntegracao.getText() != null && !txtWsIntegracao.getText().isEmpty() ? txtWsIntegracao.getText().trim() : "N/A");
                        cmdWsIns.setString(3, wsServidor);
                        cmdWsIns.setString(4, wsPorta);
                        cmdWsIns.setString(5, txtWsSoap.getText() != null && !txtWsSoap.getText().isEmpty() ? txtWsSoap.getText().trim() : "N/A");
                        cmdWsIns.setString(6, txtWsWsdl.getText() != null && !txtWsWsdl.getText().isEmpty() ? txtWsWsdl.getText().trim() : "N/A");
                        cmdWsIns.setString(7, txtWsStringBd.getText() != null && !txtWsStringBd.getText().isEmpty() ? txtWsStringBd.getText().trim() : "N/A");
                        cmdWsIns.setString(8, txtWsUserBd.getText() != null && !txtWsUserBd.getText().isEmpty() ? txtWsUserBd.getText().trim() : "N/A");
                        cmdWsIns.setString(9, txtWsSenhaBd.getText() != null && !txtWsSenhaBd.getText().isEmpty() ? txtWsSenhaBd.getText().trim() : "N/A");
                        cmdWsIns.executeUpdate();
                    }
                }

                conexao.commit();
                exibirAlerta("Sucesso", modoEdicao ? "Cliente atualizado!" : "Gravado com êxito!", Alert.AlertType.INFORMATION);

                String acaoLog = modoEdicao ? "EDIÇÃO" : "INCLUSÃO";
                LoggerAuditoria.registrar(acaoLog, "O cliente compartilhado " + txtRazaoSocial.getText().trim() + " (ID: " + idClienteAplicado + ") foi " + (modoEdicao ? "alterado." : "criado."));
                fecharJanela();

            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            } finally {
                conexao.setAutoCommit(true);
            }
        } catch (SQLException e) {
            exibirAlerta("Erro", "Falha DB: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancelar(ActionEvent event) {
        fecharJanela();
    }

    private void fecharJanela() {
        ((Stage) txtRazaoSocial.getScene().getWindow()).close();
    }

    private void exibirAlerta(String t, String m, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    public static class EmailItem {

        private final SimpleIntegerProperty numero;
        private final SimpleStringProperty email;

        public EmailItem(int n, String e) {
            this.numero = new SimpleIntegerProperty(n);
            this.email = new SimpleStringProperty(e);
        }

        public SimpleIntegerProperty numeroProperty() {
            return numero;
        }

        public SimpleStringProperty emailProperty() {
            return email;
        }

        public String getEmail() {
            return email.get();
        }
    }

    public static class BancoItem {

        private final SimpleStringProperty servidor;
        private final SimpleStringProperty nome;
        private final SimpleStringProperty conexao;
        private final SimpleStringProperty sgbd;
        private final SimpleStringProperty usuario;
        private final SimpleStringProperty senha;
        private final SimpleStringProperty segmento;

        public BancoItem(String s, String n, String c, String sg, String u, String se, String seg) {
            this.servidor = new SimpleStringProperty(s);
            this.nome = new SimpleStringProperty(n);
            this.conexao = new SimpleStringProperty(c);
            this.sgbd = new SimpleStringProperty(sg);
            this.usuario = new SimpleStringProperty(u);
            this.senha = new SimpleStringProperty(se);
            this.segmento = new SimpleStringProperty(seg);
        }

        public String getServidor() {
            return servidor.get();
        }

        public String getNome() {
            return nome.get();
        }

        public String getConexao() {
            return conexao.get();
        }

        public String getSgbd() {
            return sgbd.get();
        }

        public String getUsuario() {
            return usuario.get();
        }

        public String getSenha() {
            return senha.get();
        }

        public String getSegmento() {
            return segmento.get();
        }

        public SimpleStringProperty servidorProperty() {
            return servidor;
        }

        public SimpleStringProperty nomeProperty() {
            return nome;
        }

        public SimpleStringProperty conexaoProperty() {
            return conexao;
        }

        public SimpleStringProperty sgbdProperty() {
            return sgbd;
        }

        public SimpleStringProperty usuarioProperty() {
            return usuario;
        }

        public SimpleStringProperty senhaProperty() {
            return senha;
        }
    }
}

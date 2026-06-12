package com.mycompany.proton.controller;

import com.mycompany.proton.App;
import com.mycompany.proton.util.LoggerAuditoria;
import com.mycompany.proton.controller.ConfigBancoController;
import com.mycompany.proton.model.Cliente;
import com.mycompany.proton.model.ServidorDedicado;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;      // Cria listas observáveis para os ComboBoxes
import javafx.collections.ObservableList;      // Lista que notifica automaticamente a TableView
import javafx.event.ActionEvent;               // Evento disparado por botões
import javafx.fxml.FXML;                       // Anotação para vincular elementos da interface FXML
import javafx.fxml.Initializable;              // Interface para inicialização do controlador
import javafx.scene.control.Alert;             // Diálogos de alerta (aviso, erro, informação)
import javafx.scene.control.ComboBox;          // Caixa de seleção suspensa
import javafx.scene.control.TableColumn;       // Coluna de uma TableView
import javafx.scene.control.TableView;         // Componente visual de tabela
import javafx.scene.control.TextField;         // Campo de texto comum
import javafx.stage.Stage;                     // Representa a janela atual

/**
 * Controlador do formulário de Cliente Dedicado (Dedicados).
 *
 * A tela é dividida em duas abas: Aba 1 – Dados Mestres (Nome, CNPJ/CPF,
 * Ambiente, VPN, Active Directory) Aba 2 – Gestão de Servidores (mini tabela
 * com tipo, IP, usuário e senha)
 *
 * Permite cadastrar, editar e visualizar (somente leitura) um cliente dedicado.
 * As informações são persistidas nas tabelas: clientes_dedicados
 * servidores_clientes_dedicados
 */
public class FormClienteController implements Initializable {

    // ==================== ABA 1: DADOS MESTRES ====================
    @FXML
    private TextField txtNome;            // Razão Social ou nome do cliente
    @FXML
    private TextField txtCpfCnpj;         // CPF ou CNPJ do cliente
    @FXML
    private ComboBox<String> cbAmbiente;  // Ambiente (Scaling, Micro)
    @FXML
    private ComboBox<String> cbVpn;       // VPN (Sim / Não)
    @FXML
    private TextField txtAd;              // Informações do Active Directory (domínio etc.)

    // ==================== ABA 2: SERVIDORES (MINI TABELA) ====================
    @FXML
    private ComboBox<String> cbTipoServidor;    // Tipo do servidor (APP, Banco de Dados, etc.)
    @FXML
    private TextField txtIpServidor;            // IP ou hostname do servidor
    @FXML
    private TextField txtUsuarioServidor;       // Nome de usuário para acesso ao servidor
    @FXML
    private TextField txtSenhaServidor;         // Senha de acesso ao servidor

    @FXML
    private TableView<ServidorDedicado> tabelaServidores;         // Tabela que lista os servidores adicionados
    @FXML
    private TableColumn<ServidorDedicado, String> colServTipo;    // Coluna: Tipo
    @FXML
    private TableColumn<ServidorDedicado, String> colServIp;      // Coluna: IP
    @FXML
    private TableColumn<ServidorDedicado, String> colServUser;    // Coluna: Usuário
    @FXML
    private TableColumn<ServidorDedicado, String> colServPass;    // Coluna: Senha

    // Lista observável que alimenta a mini tabela (mudanças refletem automaticamente na UI)
    private ObservableList<ServidorDedicado> listaServidoresMini = FXCollections.observableArrayList();

    // Controle de modo da tela
    private boolean modoVisualizacao = false;   // true = campos travados (somente leitura)
    private boolean modoEdicao = false;         // true = alterando registro existente
    private Integer idClienteEdicao = null;     // ID do cliente quando estiver editando

    /**
     * Inicializa os ComboBoxes com os valores permitidos. Configura as colunas
     * da mini tabela.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Preenche os itens do ComboBox de Ambiente
        if (cbAmbiente != null) {
            cbAmbiente.setItems(FXCollections.observableArrayList("Scaling", "Micro"));
        }
        // Preenche os itens do ComboBox de VPN
        if (cbVpn != null) {
            cbVpn.setItems(FXCollections.observableArrayList("Sim", "Não"));
        }
        // Preenche os tipos de servidor disponíveis
        if (cbTipoServidor != null) {
            cbTipoServidor.setItems(FXCollections.observableArrayList(
                    "APP", "Banco de Dados (SGBD)", "Repositório", "Active Directory", "Web"));
        }

        // Mapeamento das colunas da mini tabela para as propriedades da classe ServidorDedicado
        if (colServTipo != null) {
            colServTipo.setCellValueFactory(cell -> cell.getValue().tipoProperty());
        }
        if (colServIp != null) {
            colServIp.setCellValueFactory(cell -> cell.getValue().ipProperty());
        }
        if (colServUser != null) {
            colServUser.setCellValueFactory(cell -> cell.getValue().usuarioProperty());
        }
        if (colServPass != null) {
            colServPass.setCellValueFactory(cell -> cell.getValue().senhaProperty());
        }
        // Associa a lista observável à tabela
        if (tabelaServidores != null) {
            tabelaServidores.setItems(listaServidoresMini);
        }
    }

    /**
     * Cria e retorna uma conexão JDBC com o banco de dados PostgreSQL. Utiliza
     * os dados de configuração do ConfigBancoController.
     */
    private Connection conectar() throws SQLException {
        // Monta a URL de conexão no formato jdbc:postgresql://<ip>:5432/<nome_banco>
        String urlConexao = String.format("jdbc:postgresql://%s:5432/%s",
                ConfigBancoController.getIpServidor(),
                ConfigBancoController.getNomeBanco());
        // Abre a conexão com usuário e senha
        return DriverManager.getConnection(urlConexao,
                ConfigBancoController.getUsuarioBD(),
                ConfigBancoController.getSenhaBD());
    }

    // =========================================================
    // GERENCIAMENTO DA MINI-TABELA DE SERVIDORES
    // =========================================================
    /**
     * Adiciona um novo servidor à lista temporária. Valida se os campos Tipo e
     * IP foram preenchidos.
     */
    @FXML
    private void adicionarServidorNaLista(ActionEvent event) {
        String tipo = cbTipoServidor.getValue();  // Lê o tipo selecionado
        String ip = txtIpServidor.getText();       // Lê o IP digitado
        String user = txtUsuarioServidor.getText(); // Lê o usuário (opcional)
        String pass = txtSenhaServidor.getText();   // Lê a senha (opcional)

        // Validação: Tipo e IP são obrigatórios
        if (tipo == null || tipo.trim().isEmpty() || ip == null || ip.trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha o Tipo e o IP para adicionar a máquina à lista.",
                    Alert.AlertType.WARNING);
            return;
        }

        // Cria um novo objeto ServidorDedicado e adiciona à lista
        listaServidoresMini.add(new ServidorDedicado(
                tipo,
                ip,
                user != null ? user : "", // Se não informado, grava string vazia
                pass != null ? pass : ""));

        // Limpa os campos para permitir nova digitação
        txtIpServidor.clear();
        txtUsuarioServidor.clear();
        txtSenhaServidor.clear();
        cbTipoServidor.getSelectionModel().clearSelection(); // Desmarca o ComboBox
    }

    /**
     * Remove o servidor selecionado na tabela da lista temporária.
     */
    @FXML
    private void removerServidorDaLista(ActionEvent event) {
        // Obtém o item selecionado na tabela
        ServidorDedicado selecionado = tabelaServidores.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            listaServidoresMini.remove(selecionado); // Remove da lista observável
        }
    }

    // =========================================================
    // PERSISTÊNCIA TRANSACIONAL (Salva o Cliente e seus servidores)
    // =========================================================
    /**
     * Acionado ao clicar no botão "Salvar". Valida os campos obrigatórios e
     * persiste os dados no banco. Se estiver em modo edição, atualiza o
     * registro existente; caso contrário, insere um novo cliente e seus
     * servidores. Tudo é executado dentro de uma transação para garantir
     * consistência.
     */
    @FXML
    private void salvarCliente(ActionEvent event) {
        // Se estiver apenas visualizando, fecha a janela sem salvar
        if (modoVisualizacao) {
            fecharJanela();
            return;
        }

        // ---- Validações dos campos obrigatórios ----
        if (txtNome.getText() == null || txtNome.getText().trim().isEmpty()) {
            exibirAlerta("Atenção", "Razão Social é obrigatória!", Alert.AlertType.WARNING);
            return;
        }
        if (txtCpfCnpj.getText() == null || txtCpfCnpj.getText().trim().isEmpty()) {
            exibirAlerta("Atenção", "CNPJ/CPF é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (listaServidoresMini.isEmpty()) {
            exibirAlerta("Atenção",
                    "Adicione pelo menos UM Servidor na aba 'Gestão de Servidores' antes de salvar o cliente.",
                    Alert.AlertType.WARNING);
            return;
        }

        // Prepara os valores que podem estar nulos ou precisam de tratamento
        String valorAmbiente = (cbAmbiente.getValue() != null) ? cbAmbiente.getValue() : "N/A";
        boolean vpnHabilitada = cbVpn.getValue() != null && cbVpn.getValue().equalsIgnoreCase("Sim");
        String adStr = txtAd.getText() != null ? txtAd.getText().trim() : "";

        // SQL que será executado: UPDATE (edição) ou INSERT (novo)
        String sqlCliente;
        if (modoEdicao) {
            sqlCliente = "UPDATE clientes_dedicados SET cliente=?, cnpj_cpf=?, qnt_de_servs=?, ad=?, ambiente=?, vpn=? WHERE id=?";
        } else {
            sqlCliente = "INSERT INTO clientes_dedicados (cliente, cnpj_cpf, qnt_de_servs, ad, ambiente, vpn, criado_por) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        }

        try (Connection conexao = conectar()) {
            conexao.setAutoCommit(false); // Inicia transação manual

            try {
                int idClienteAplicado = modoEdicao ? idClienteEdicao : -1;

                // 1. Salva ou atualiza os dados mestre do cliente (clientes_dedicados)
                try (PreparedStatement cmdCliente = conexao.prepareStatement(sqlCliente,
                        modoEdicao ? java.sql.Statement.NO_GENERATED_KEYS : java.sql.Statement.RETURN_GENERATED_KEYS)) {

                    // Preenche os parâmetros da consulta
                    cmdCliente.setString(1, txtNome.getText().trim());          // Nome / Razão Social
                    cmdCliente.setString(2, txtCpfCnpj.getText().trim());        // CPF/CNPJ
                    cmdCliente.setInt(3, listaServidoresMini.size());            // Quantidade de servidores = tamanho da lista
                    cmdCliente.setString(4, adStr);                              // Active Directory
                    cmdCliente.setString(5, valorAmbiente);                      // Ambiente
                    cmdCliente.setBoolean(6, vpnHabilitada);                     // VPN (true/false)

                    if (modoEdicao) {
                        cmdCliente.setInt(7, idClienteEdicao);  // WHERE id = ?
                        cmdCliente.executeUpdate();             // Executa o UPDATE
                    } else {
                        cmdCliente.setString(7, App.getUsuarioLogado()); // criado_por = e-mail do usuário logado
                        cmdCliente.executeUpdate();                       // Executa o INSERT
                        // Recupera o ID gerado pela cláusula RETURNING id
                        try (ResultSet chaves = cmdCliente.getGeneratedKeys()) {
                            if (chaves.next()) {
                                idClienteAplicado = chaves.getInt(1);
                            }
                        }
                    }
                }

                if (idClienteAplicado == -1) {
                    throw new SQLException("ID do Cliente não recuperado.");
                }

                // Se estiver editando, remove todos os servidores antigos para regravar a lista atualizada
                if (modoEdicao) {
                    try (PreparedStatement cmdDel = conexao.prepareStatement(
                            "DELETE FROM servidores_clientes_dedicados WHERE cliente_id = ?")) {
                        cmdDel.setInt(1, idClienteAplicado);
                        cmdDel.executeUpdate();
                    }
                }

                // 2. Insere todos os servidores da lista (em lote)
                String sqlServidor = "INSERT INTO servidores_clientes_dedicados "
                        + "(cliente_id, tipo_servidor, ip_servidor, usuario, senha) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement cmdServ = conexao.prepareStatement(sqlServidor)) {
                    for (ServidorDedicado serv : listaServidoresMini) {
                        cmdServ.setInt(1, idClienteAplicado);        // ID do cliente pai
                        cmdServ.setString(2, serv.getTipo());        // Tipo do servidor
                        cmdServ.setString(3, serv.getIp());          // IP
                        cmdServ.setString(4, serv.getUsuario());     // Usuário
                        cmdServ.setString(5, serv.getSenha());       // Senha
                        cmdServ.addBatch(); // Adiciona ao lote
                    }
                    cmdServ.executeBatch(); // Executa todas as inserções de uma vez
                }

                conexao.commit(); // Confirma a transação (cliente + servidores)
                exibirAlerta("Sucesso", modoEdicao ? "Cliente atualizado!" : "Cliente Dedicado e seus Servidores salvos!",
                        Alert.AlertType.INFORMATION);
                String acaoLog = modoEdicao ? "EDIÇÃO" : "INCLUSÃO";
                LoggerAuditoria.registrar(acaoLog, "Cliente Dedicado: " + txtNome.getText().trim() + " foi " + (modoEdicao ? "alterado." : "criado."));
                fecharJanela();

            } catch (SQLException e) {
                conexao.rollback(); // Desfaz qualquer alteração em caso de erro
                throw e;
            } finally {
                conexao.setAutoCommit(true); // Restaura o modo de commit automático
            }
        } catch (SQLException e) {
            exibirAlerta("Erro", "Falha de persistência: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // =========================================================
    // VISUALIZAÇÃO E EDIÇÃO
    // =========================================================
    /**
     * Prepara a tela para visualização (somente leitura). Preenche os campos
     * com os dados do cliente e bloqueia a edição.
     */
    public void carregarDadosParaVisualizacao(Cliente cliente) {
        this.modoVisualizacao = true;
        preencherMestre(cliente);
        travarInterface();
    }

    /**
     * Prepara a tela para edição de um cliente existente. Preenche os campos e
     * mantém a edição habilitada.
     */
    public void carregarDadosParaEdicao(Cliente cliente) {
        this.modoEdicao = true;
        this.idClienteEdicao = cliente.getId();  // Guarda o ID para usar no UPDATE
        preencherMestre(cliente);
    }

    /**
     * Popula os campos da Aba 1 com os dados vindos do objeto Cliente. Também
     * carrega a lista de servidores associados do banco de dados.
     */
    private void preencherMestre(Cliente cliente) {
        // Preenche os campos mestre da Aba 1
        if (txtNome != null) {
            txtNome.setText(cliente.getNome());
        }
        if (txtCpfCnpj != null) {
            txtCpfCnpj.setText(cliente.getCnpj());
        }
        if (txtAd != null) {
            txtAd.setText(cliente.getActive_directory());
        }
        if (cbAmbiente != null) {
            cbAmbiente.setValue(cliente.getAmbiente());
        }
        if (cbVpn != null) {
            cbVpn.setValue(cliente.isVpn() ? "Sim" : "Não");
        }

        // Limpa a lista atual de servidores
        listaServidoresMini.clear();

        // Abordagem defensiva: lê a tabela de servidores correta (servidores_clientes_dedicados)
        try (Connection conn = conectar()) {

            // Consulta os servidores vinculados ao cliente
            String sqlNova = "SELECT tipo_servidor, ip_servidor, usuario, senha "
                    + "FROM servidores_clientes_dedicados WHERE cliente_id = ?";
            try (PreparedStatement cmdNova = conn.prepareStatement(sqlNova)) {
                cmdNova.setInt(1, cliente.getId());
                try (ResultSet rsNova = cmdNova.executeQuery()) {
                    while (rsNova.next()) {
                        // Adiciona cada servidor encontrado à lista temporária
                        listaServidoresMini.add(new ServidorDedicado(
                                rsNova.getString("tipo_servidor"),
                                rsNova.getString("ip_servidor"),
                                rsNova.getString("usuario"),
                                rsNova.getString("senha")
                        ));
                    }
                }
            } catch (SQLException eNova) {
                System.out.println("Erro Crítico ao preencher Servidores (Tabela não encontrada): "
                        + eNova.getMessage());
            }

        } catch (SQLException eGeral) {
            System.out.println("Erro na conexão ao preencher servidores: " + eGeral.getMessage());
        }
    }

    /**
     * Trava (desabilita) todos os campos para o modo de visualização.
     */
    private void travarInterface() {
        // Campos de texto: torna não editáveis
        if (txtNome != null) {
            txtNome.setEditable(false);
        }
        if (txtCpfCnpj != null) {
            txtCpfCnpj.setEditable(false);
        }
        if (txtAd != null) {
            txtAd.setEditable(false);
        }

        // ComboBoxes: desabilita
        if (cbAmbiente != null) {
            cbAmbiente.setDisable(true);
        }
        if (cbVpn != null) {
            cbVpn.setDisable(true);
        }

        // Campos de adição de servidores: desabilita (eles não apareceriam, mas por segurança)
        if (cbTipoServidor != null) {
            cbTipoServidor.setDisable(true);
        }
        if (txtIpServidor != null) {
            txtIpServidor.setDisable(true);
        }
        if (txtUsuarioServidor != null) {
            txtUsuarioServidor.setDisable(true);
        }
        if (txtSenhaServidor != null) {
            txtSenhaServidor.setDisable(true);
        }
    }

    /**
     * Ação do botão "Cancelar". Apenas fecha a janela.
     */
    @FXML
    private void cancelar(ActionEvent event) {
        fecharJanela();
    }

    /**
     * Fecha a janela atual usando o campo txtNome como referência.
     */
    private void fecharJanela() {
        ((Stage) txtNome.getScene().getWindow()).close();
    }

    /**
     * Exibe um diálogo (Alert) com título, mensagem e tipo especificados.
     */
    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);  // Sem cabeçalho extra
        alert.setContentText(mensagem);
        alert.showAndWait();        // Aguarda o usuário fechar
    }
}

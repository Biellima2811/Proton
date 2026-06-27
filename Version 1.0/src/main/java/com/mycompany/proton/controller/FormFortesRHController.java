package com.mycompany.proton.controller;

import com.mycompany.proton.util.LoggerAuditoria;
import com.mycompany.proton.controller.ConfigBancoController;
import com.mycompany.proton.model.FortesRH;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;                  // Representa uma data (usada nos DatePickers)
import java.util.ResourceBundle;
import javafx.collections.FXCollections;     // Cria listas observáveis para os ComboBoxes
import javafx.event.ActionEvent;             // Evento de ação (botões)
import javafx.fxml.FXML;                     // Anotação para vincular elementos da interface
import javafx.fxml.Initializable;            // Interface para inicialização do controlador
import javafx.scene.control.Alert;           // Diálogos de alerta
import javafx.scene.control.ComboBox;        // Caixa de seleção suspensa
import javafx.scene.control.DatePicker;      // Campo de seleção de data
import javafx.scene.control.TextField;       // Campo de texto comum
import javafx.stage.Stage;                   // Representa a janela atual

/**
 * Controlador do formulário de cadastro/edição de ambientes Fortes RH.
 *
 * O mesmo controlador atende tanto a tela de ambientes COMPARTILHADOS quanto a
 * tela de ambientes DEDICADOS. A diferenciação é feita dinamicamente pela
 * presença ou ausência de determinados campos visuais.
 *
 * Principais funcionalidades: - Inserir/editar/visualizar registros da tabela
 * "fortesrh". - Suporta dois layouts distintos com campos opcionais. -
 * Validação mínima antes da persistência.
 */
public class FormFortesRHController implements Initializable {

    // ==================== CAMPOS DA INTERFACE (AMBOS OS LAYOUTS) ====================
    // Obs: Nem todos os campos existirão em ambas as telas. O controlador trata a ausência com verificações null.
    @FXML
    private ComboBox<String> cbStatus;           // Status do ambiente (Ativo, Pendente, Desativado)
    @FXML
    private DatePicker dpDataCriacao;            // Data de criação do cadastro
    @FXML
    private ComboBox<String> cbTipoAmbiente;     // Tipo de ambiente (Compartilhado / Dedicado) – presente apenas na tela Compartilhada
    @FXML
    private TextField txtCliente;                // Razão social ou nome do cliente
    @FXML
    private TextField txtCnpjCpf;                // CPF ou CNPJ
    @FXML
    private TextField txtServidorApp;            // Endereço do servidor de aplicação
    @FXML
    private TextField txtUsuarioDb;              // Usuário do banco de dados
    @FXML
    private TextField txtSenhaDb;                // Senha do banco de dados
    @FXML
    private TextField txtBancoDados;             // Nome do banco de dados
    @FXML
    private TextField txtPastaWeb;               // Caminho da pasta web
    @FXML
    private TextField txtWebAplication;          // Nome da aplicação web
    @FXML
    private TextField txtUrlAcesso;              // URL de acesso (tela Compartilhada)
    @FXML
    private TextField txtIpPublico;              // IP público
    @FXML
    private TextField txtIpPrivado;              // IP privado
    @FXML
    private ComboBox<String> cbLoadBalance;      // Possui Load Balance? (Sim / Não)
    @FXML
    private TextField txtIpLoadBalance;          // IP do Load Balance
    @FXML
    private ComboBox<String> cbVersao;           // Versão do Fortes RH
    @FXML
    private TextField txtLinkAcesso;             // Link de acesso (tela Dedicada)

    // ==================== CONTROLE DE MODO ====================
    private boolean modoVisualizacao = false;    // true = campos bloqueados para leitura
    private boolean modoEdicao = false;          // true = alterando um registro existente
    private Integer idEdicao = null;             // ID do registro quando em modo de edição

    /**
     * Inicializa os ComboBoxes com os valores permitidos. Executado
     * automaticamente após o FXML ser carregado.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Preenche os itens do ComboBox de Status
        if (cbStatus != null) {
            cbStatus.setItems(FXCollections.observableArrayList("Ativo", "Pendente", "Desativado"));
        }
        // Tipo de ambiente (somente tela Compartilhada)
        if (cbTipoAmbiente != null) {
            cbTipoAmbiente.setItems(FXCollections.observableArrayList("Compartilhado", "Dedicado"));
        }
        // Load Balance
        if (cbLoadBalance != null) {
            cbLoadBalance.setItems(FXCollections.observableArrayList("Sim", "Não"));
        }
        // Versão
        if (cbVersao != null) {
            cbVersao.setItems(FXCollections.observableArrayList("Novo 2.0", "Atualizado", "Pendente"));
        }
    }

    /**
     * Cria e retorna uma conexão JDBC com o banco de dados. Utiliza as
     * configurações do ConfigBancoController.
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
     * Acionado ao clicar no botão "Salvar".
     *
     * Se estiver em modo de visualização, apenas fecha a janela. Caso
     * contrário, valida o campo obrigatório (Razão Social) e persiste os dados
     * na tabela "fortesrh".
     *
     * O SQL é ajustado dinamicamente: UPDATE se edição, INSERT se novo.
     */
    @FXML
    private void salvarFortesRH(ActionEvent event) {
        // Se for apenas visualização, não permite salvar – fecha a tela
        if (modoVisualizacao) {
            fecharJanela();
            return;
        }

        // Validação do campo obrigatório principal
        if (txtCliente.getText() == null || txtCliente.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Nome da Razão Social / Cliente é obrigatório!", Alert.AlertType.WARNING);
            return;
        }

        // Determina o tipo de ambiente.
        // Se a tela for Compartilhada, o ComboBox cbTipoAmbiente estará presente;
        // caso contrário, assume "Dedicado" como padrão (tela Dedicada).
        String tpAmbiente = "Dedicado";
        if (cbTipoAmbiente != null) {
            // Tenta obter o valor selecionado; se nulo, tenta o texto digitado (caso editável)
            tpAmbiente = (cbTipoAmbiente.getValue() != null)
                    ? cbTipoAmbiente.getValue()
                    : cbTipoAmbiente.getEditor().getText();
        }

        // Converte a data do DatePicker para java.sql.Date (pode ser nulo)
        LocalDate dataLocal = (dpDataCriacao != null) ? dpDataCriacao.getValue() : null;
        java.sql.Date dataSql = (dataLocal != null) ? java.sql.Date.valueOf(dataLocal) : null;

        // Define a instrução SQL conforme o modo (inserção ou atualização)
        String sql;
        if (modoEdicao) {
            sql = "UPDATE fortesrh SET tipo_ambiente=?, cliente=?, cnpj_cpf=?, url_acesso=?, "
                    + "servidor_app=?, banco_dados=?, pasta_web=?, usuario_db=?, senha_db=?, "
                    + "load_balance=?, ip_load_balance=?, status=?, data_criacao=?, ip_publico=?, "
                    + "ip_privado=?, versao=?, web_aplication=? WHERE id=?";
        } else {
            sql = "INSERT INTO fortesrh (tipo_ambiente, cliente, cnpj_cpf, url_acesso, "
                    + "servidor_app, banco_dados, pasta_web, usuario_db, senha_db, load_balance, "
                    + "ip_load_balance, status, data_criacao, ip_publico, ip_privado, versao, "
                    + "web_aplication) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        // Persiste no banco utilizando try-with-resources (fecha automaticamente conexão e statement)
        try (Connection conexao = conectar(); PreparedStatement cmd = conexao.prepareStatement(sql)) {

            // Preenche os parâmetros na ordem das interrogações
            cmd.setString(1, tpAmbiente);                                 // tipo_ambiente
            cmd.setString(2, txtCliente.getText().trim());                // cliente
            cmd.setString(3, getTexto(txtCnpjCpf));                       // cnpj_cpf

            // Inteligência de URL: tenta primeiro o campo da tela Compartilhada (txtUrlAcesso);
            // se não existir (retornou "N/A"), tenta o campo da tela Dedicada (txtLinkAcesso).
            String urlOuLink = getTexto(txtUrlAcesso);
            if (urlOuLink.equals("N/A")) {
                urlOuLink = getTexto(txtLinkAcesso);
            }
            cmd.setString(4, urlOuLink);                                  // url_acesso

            cmd.setString(5, getTexto(txtServidorApp));                   // servidor_app
            cmd.setString(6, getTexto(txtBancoDados));                    // banco_dados
            cmd.setString(7, getTexto(txtPastaWeb));                      // pasta_web
            cmd.setString(8, getTexto(txtUsuarioDb));                     // usuario_db
            cmd.setString(9, getTexto(txtSenhaDb));                       // senha_db

            String valLoadBalance = (cbLoadBalance != null && cbLoadBalance.getValue() != null)
                    ? cbLoadBalance.getValue() : "N/A";
            cmd.setString(10, valLoadBalance);                            // load_balance

            cmd.setString(11, getTexto(txtIpLoadBalance));                // ip_load_balance

            String valStatus = (cbStatus != null && cbStatus.getValue() != null)
                    ? cbStatus.getValue() : "N/A";
            cmd.setString(12, valStatus);                                 // status

            cmd.setDate(13, dataSql);                                     // data_criacao
            cmd.setString(14, getTexto(txtIpPublico));                    // ip_publico
            cmd.setString(15, getTexto(txtIpPrivado));                    // ip_privado

            String valVersao = (cbVersao != null && cbVersao.getValue() != null)
                    ? cbVersao.getValue() : "N/A";
            cmd.setString(16, valVersao);                                 // versao

            cmd.setString(17, getTexto(txtWebAplication));                // web_aplication

            if (modoEdicao) {
                cmd.setInt(18, idEdicao);                                 // WHERE id = ? (no UPDATE)
            }

            // Executa a inserção ou atualização
            cmd.executeUpdate();

            // Exibe mensagem de sucesso
            exibirAlerta("Sucesso",
                    modoEdicao ? "Registro RH atualizado com sucesso!" : "Novo ambiente RH cadastrado!",
                    Alert.AlertType.INFORMATION);
            String acaoLog = modoEdicao ? "EDIÇÃO" : "INCLUSÃO";
            LoggerAuditoria.registrar(acaoLog, "Ambiente FortesRH: " + txtCliente.getText().trim() + " foi " + (modoEdicao ? "alterado." : "criado."));
            fecharJanela();

        } catch (SQLException e) {
            exibirAlerta("Erro BD", "Falha ao salvar no PostgreSQL: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Método auxiliar para obter o texto de um TextField de forma segura. Se o
     * campo for nulo ou estiver vazio, retorna "N/A".
     *
     * @param campo O TextField a ser lido
     * @return O texto limpo ou "N/A"
     */
    private String getTexto(TextField campo) {
        if (campo != null && campo.getText() != null && !campo.getText().trim().isEmpty()) {
            return campo.getText().trim();
        }
        return "N/A";
    }

    // =========================================================
    // VISUALIZAÇÃO E EDIÇÃO
    // =========================================================
    /**
     * Prepara a tela para visualização (somente leitura). Preenche os campos
     * com os dados do objeto FortesRH e bloqueia a edição.
     *
     * @param rh Objeto FortesRH contendo os dados a serem exibidos
     */
    public void carregarDadosParaVisualizacao(FortesRH rh) {
        modoVisualizacao = true;
        preencherCampos(rh);

        // Desabilita todos os campos de texto
        if (txtCliente != null) {
            txtCliente.setEditable(false);
        }
        if (txtCnpjCpf != null) {
            txtCnpjCpf.setEditable(false);
        }
        if (txtServidorApp != null) {
            txtServidorApp.setEditable(false);
        }
        if (txtUsuarioDb != null) {
            txtUsuarioDb.setEditable(false);
        }
        if (txtSenhaDb != null) {
            txtSenhaDb.setEditable(false);
        }
        if (txtBancoDados != null) {
            txtBancoDados.setEditable(false);
        }
        if (txtPastaWeb != null) {
            txtPastaWeb.setEditable(false);
        }
        if (txtWebAplication != null) {
            txtWebAplication.setEditable(false);
        }
        if (txtUrlAcesso != null) {
            txtUrlAcesso.setEditable(false);
        }
        if (txtLinkAcesso != null) {
            txtLinkAcesso.setEditable(false);
        }
        if (txtIpPublico != null) {
            txtIpPublico.setEditable(false);
        }
        if (txtIpPrivado != null) {
            txtIpPrivado.setEditable(false);
        }
        if (txtIpLoadBalance != null) {
            txtIpLoadBalance.setEditable(false);
        }

        // Desabilita os ComboBoxes e DatePicker
        if (cbStatus != null) {
            cbStatus.setDisable(true);
        }
        if (dpDataCriacao != null) {
            dpDataCriacao.setDisable(true);
        }
        if (cbTipoAmbiente != null) {
            cbTipoAmbiente.setDisable(true);
        }
        if (cbLoadBalance != null) {
            cbLoadBalance.setDisable(true);
        }
        if (cbVersao != null) {
            cbVersao.setDisable(true);
        }
    }

    /**
     * Prepara a tela para edição de um registro existente. Preenche os campos
     * com os dados atuais e configura o modo de edição.
     *
     * @param rh Objeto FortesRH com os dados atuais
     */
    public void carregarDadosParaEdicao(FortesRH rh) {
        modoEdicao = true;
        idEdicao = rh.getId();  // Armazena o ID para o WHERE do UPDATE
        preencherCampos(rh);
    }

    /**
     * Popula os campos visuais com os dados vindos do objeto FortesRH. Alguns
     * campos adicionais (como status, versão, IPs) são obtidos diretamente do
     * banco através de uma nova consulta, garantindo que estejam atualizados.
     *
     * @param rh Objeto FortesRH base
     */
    private void preencherCampos(FortesRH rh) {
        // Preenche campos que existem no objeto FortesRH
        if (txtCliente != null) {
            txtCliente.setText(rh.getCliente());
        }
        if (txtCnpjCpf != null) {
            txtCnpjCpf.setText(rh.getCnpj_cpf());
        }
        if (txtUrlAcesso != null) {
            txtUrlAcesso.setText(rh.getUrl_acesso());
        }
        if (txtLinkAcesso != null) {
            txtLinkAcesso.setText(rh.getUrl_acesso()); // Mesmo campo em ambas as telas
        }
        if (txtServidorApp != null) {
            txtServidorApp.setText(rh.getServidor_app());
        }
        if (txtBancoDados != null) {
            txtBancoDados.setText(rh.getBanco_dados());
        }
        if (txtPastaWeb != null) {
            txtPastaWeb.setText(rh.getPasta_web());
        }
        if (txtUsuarioDb != null) {
            txtUsuarioDb.setText(rh.getUsuario_db());
        }
        if (txtSenhaDb != null) {
            txtSenhaDb.setText(rh.getSenha_db());
        }
        if (cbLoadBalance != null && rh.getLoad_balance() != null) {
            cbLoadBalance.getEditor().setText(rh.getLoad_balance());
        }
        if (txtIpLoadBalance != null) {
            txtIpLoadBalance.setText(rh.getIp_load_balance());
        }

        // Para os campos que podem não estar no objeto FortesRH,
        // faz uma nova consulta ao banco para garantir dados completos.
        try (Connection conexao = conectar(); PreparedStatement cmd = conexao.prepareStatement("SELECT * FROM fortesrh WHERE id = ?")) {
            cmd.setInt(1, rh.getId());
            try (ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    if (cbStatus != null) {
                        cbStatus.getEditor().setText(rs.getString("status"));
                    }
                    if (cbTipoAmbiente != null) {
                        cbTipoAmbiente.getEditor().setText(rs.getString("tipo_ambiente"));
                    }
                    if (txtWebAplication != null) {
                        txtWebAplication.setText(rs.getString("web_aplication"));
                    }
                    if (txtIpPublico != null) {
                        txtIpPublico.setText(rs.getString("ip_publico"));
                    }
                    if (txtIpPrivado != null) {
                        txtIpPrivado.setText(rs.getString("ip_privado"));
                    }
                    if (cbVersao != null) {
                        cbVersao.getEditor().setText(rs.getString("versao"));
                    }
                    // Data de criação (se disponível)
                    if (dpDataCriacao != null && rs.getDate("data_criacao") != null) {
                        dpDataCriacao.setValue(rs.getDate("data_criacao").toLocalDate());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar extras FortesRH: " + e.getMessage());
        }
    }

    /**
     * Ação do botão "Cancelar". Apenas fecha a janela sem salvar.
     */
    @FXML
    private void cancelar(ActionEvent event) {
        fecharJanela();
    }

    /**
     * Fecha a janela atual. Obtém o Stage a partir do campo txtCliente (sempre
     * presente).
     */
    private void fecharJanela() {
        ((Stage) txtCliente.getScene().getWindow()).close();
    }

    /**
     * Exibe um diálogo de alerta com título, mensagem e tipo.
     *
     * @param titulo Título da janela
     * @param msg Mensagem de conteúdo
     * @param tipo Tipo do alerta (ERROR, WARNING, INFORMATION, etc.)
     */
    private void exibirAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

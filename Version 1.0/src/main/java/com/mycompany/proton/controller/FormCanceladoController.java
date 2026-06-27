package com.mycompany.proton.controller;

import com.mycompany.proton.App;
import com.mycompany.proton.util.LoggerAuditoria;
import com.mycompany.proton.controller.ConfigBancoController;
import com.mycompany.proton.model.ClienteCancelado;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;               // Representa uma data (usada nos DatePickers)
import java.util.ResourceBundle;
import javafx.collections.FXCollections;  // Cria listas observáveis para os ComboBoxes
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;        // Exibe diálogos de aviso/erro/informação
import javafx.scene.control.ComboBox;      // Caixa de seleção suspensa
import javafx.scene.control.DatePicker;    // Campo para selecionar data
import javafx.scene.control.TextField;     // Campo de texto comum
import javafx.stage.Stage;                 // Representa a janela atual

/**
 * Controlador do formulário de cancelamento de cliente.
 *
 * A tela é dividida em duas abas (guias): ABA 1 – Dados do Cliente ABA 2 –
 * Triagem de Dados
 *
 * O formulário pode operar em três modos: - Novo cadastro de cancelamento -
 * Edição de um cancelamento existente - Visualização (somente leitura)
 */
public class FormCanceladoController implements Initializable {

    // ==================== ABA 1: DADOS DO CLIENTE ====================
    @FXML
    private ComboBox<String> cbSegmento;        // Segmento do cliente (ex.: Corporativo, Contábil)
    @FXML
    private ComboBox<String> cbTipoNuvem;       // Tipo de nuvem (Compartilhada / Dedicada)
    @FXML
    private DatePicker dpDataCriacao;           // Data de criação original do cliente
    @FXML
    private TextField txtClienteRazao;          // Razão social do cliente
    @FXML
    private TextField txtCnpjCpf;               // CPF ou CNPJ do cliente
    @FXML
    private TextField txtCodAg;                 // Código da agência / contrato
    @FXML
    private TextField txtRazaoAntiga;           // Razão social anterior (caso alterada)
    @FXML
    private TextField txtCnpjCpfAntigo;         // CNPJ/CPF anterior
    @FXML
    private TextField txtQtdUsuarios;           // Quantidade de usuários vinculados
    @FXML
    private TextField txtNomePasta;             // Nome da pasta de rede
    @FXML
    private ComboBox<String> cbOrigem;          // Origem do cliente (Base, Novo eSocial, etc.)
    @FXML
    private TextField txtSistemas;              // Sistemas utilizados pelo cliente

    // ==================== ABA 2: TRIAGEM DE DADOS ====================
    @FXML
    private ComboBox<String> cbTipoBanco;       // Tipo do banco de dados (Firebird, MSSQL, PostgreSQL)
    @FXML
    private DatePicker dpInicioCancelamento;    // Data em que o cancelamento foi iniciado
    @FXML
    private DatePicker dpFinalCancelamento;     // Data em que o cancelamento foi concluído
    @FXML
    private TextField txtChamado;               // Número do chamado que motivou o cancelamento
    @FXML
    private TextField txtCaminhoBanco;          // Caminho físico do banco de dados
    @FXML
    private TextField txtTecnico;               // Nome do técnico responsável pelo cancelamento
    @FXML
    private ComboBox<String> cbStatusCancelamento; // Status final (Desativado, Pendente)

    // ==================== CONTROLE DE MODO ====================
    private boolean modoVisualizacao = false;   // true = campos somente leitura
    private boolean modoEdicao = false;         // true = alterando um registro existente
    private Integer idEdicao = null;            // ID do registro sendo editado (nulo se novo)

    /**
     * Inicializa os ComboBoxes com as opções permitidas. Executado
     * automaticamente após o FXML ser carregado.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Preenche o combo de segmento
        if (cbSegmento != null) {
            cbSegmento.setItems(FXCollections.observableArrayList("Corporativo", "Contábil"));
        }
        // Preenche o combo de tipo de nuvem
        if (cbTipoNuvem != null) {
            cbTipoNuvem.setItems(FXCollections.observableArrayList("Compartilhada", "Dedicada"));
        }
        // Preenche o combo de origem
        if (cbOrigem != null) {
            cbOrigem.setItems(FXCollections.observableArrayList("Base", "Novo Esocial", "Novo"));
        }
        // Preenche o combo de tipo de banco
        if (cbTipoBanco != null) {
            cbTipoBanco.setItems(FXCollections.observableArrayList("Firebird", "MSSQL", "PostgreSQL"));
        }
        // Preenche o combo de status do cancelamento
        if (cbStatusCancelamento != null) {
            cbStatusCancelamento.setItems(FXCollections.observableArrayList("Desativado", "Pendente"));
        }
    }

    /**
     * Cria uma nova conexão com o banco de dados utilizando as configurações
     * salvas pelo ConfigBancoController.
     *
     * @return Objeto Connection pronto para uso.
     * @throws SQLException Se ocorrer falha na conexão.
     */
    private Connection conectar() throws SQLException {
        // Monta a URL JDBC com os dados do arquivo de configuração
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
     * contrário, valida todos os campos obrigatórios e insere/atualiza o
     * registro na tabela "clientes_cancelados".
     */
    @FXML
    private void salvarCancelamento(ActionEvent event) {
        // Se for apenas visualização, não salva – só fecha a tela
        if (modoVisualizacao) {
            fecharJanela();
            return;
        }

        // ===================== VALIDAÇÃO DOS CAMPOS OBRIGATÓRIOS =====================
        // Cada campo é verificado individualmente para garantir que o registro não
        // seja salvo com dados incompletos. Se faltar algo, exibe um alerta e interrompe.
        if (cbSegmento.getValue() == null) {
            exibirAlerta("Aviso", "O Segmento é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (cbTipoNuvem.getValue() == null) {
            exibirAlerta("Aviso", "O Tipo de Nuvem é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (dpDataCriacao.getValue() == null) {
            exibirAlerta("Aviso", "A Data de Criação é obrigatória!", Alert.AlertType.WARNING);
            return;
        }
        if (txtClienteRazao.getText() == null || txtClienteRazao.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "A Razão Social é obrigatória!", Alert.AlertType.WARNING);
            return;
        }
        if (txtCnpjCpf.getText() == null || txtCnpjCpf.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "O CPF/CNPJ é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (txtCodAg.getText() == null || txtCodAg.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "O Código AG é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (txtQtdUsuarios.getText() == null || txtQtdUsuarios.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "A Qtd. de Usuários é obrigatória!", Alert.AlertType.WARNING);
            return;
        }
        if (txtNomePasta.getText() == null || txtNomePasta.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "O Nome da Pasta é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (cbOrigem.getValue() == null) {
            exibirAlerta("Aviso", "A Origem é obrigatória!", Alert.AlertType.WARNING);
            return;
        }
        if (txtSistemas.getText() == null || txtSistemas.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Os Sistemas são obrigatórios!", Alert.AlertType.WARNING);
            return;
        }
        if (cbTipoBanco.getValue() == null) {
            exibirAlerta("Aviso", "O Tipo de Banco é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (dpInicioCancelamento.getValue() == null) {
            exibirAlerta("Aviso", "A Data de Início do Cancelamento é obrigatória!", Alert.AlertType.WARNING);
            return;
        }
        if (dpFinalCancelamento.getValue() == null) {
            exibirAlerta("Aviso", "A Data Final do Processo é obrigatória!", Alert.AlertType.WARNING);
            return;
        }
        if (txtChamado.getText() == null || txtChamado.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "O Nº do Chamado é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (txtCaminhoBanco.getText() == null || txtCaminhoBanco.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "O Caminho do Banco é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (txtTecnico.getText() == null || txtTecnico.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "O Técnico Responsável é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        if (cbStatusCancelamento.getValue() == null) {
            exibirAlerta("Aviso", "O Status do Cancelamento é obrigatório!", Alert.AlertType.WARNING);
            return;
        }
        // =============================================================================

        // Define a instrução SQL conforme o modo: UPDATE (edição) ou INSERT (novo)
        String sql;
        if (modoEdicao) {
            sql = "UPDATE clientes_cancelados SET tipo_nuvem=?, pod=?, data_criacao=?, "
                    + "cliente_razao=?, status_antigo=?, inicio_cancelamento=?, "
                    + "final_cancelamento=?, chamado=?, tecnico_responsavel=? WHERE id=?";
        } else {
            sql = "INSERT INTO clientes_cancelados (tipo_nuvem, pod, data_criacao, "
                    + "cliente_razao, status_antigo, inicio_cancelamento, final_cancelamento, "
                    + "chamado, tecnico_responsavel, criado_por) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        // Bloco try-with-resources garante que a conexão e o PreparedStatement sejam fechados
        try (Connection conexao = conectar(); PreparedStatement cmd = conexao.prepareStatement(sql)) {

            // Preenche os parâmetros da consulta na ordem dos "?"
            cmd.setString(1, cbTipoNuvem.getValue());          // tipo_nuvem
            cmd.setInt(2, 1);                                   // pod (valor padrão 1)
            cmd.setDate(3, java.sql.Date.valueOf(dpDataCriacao.getValue())); // data_criacao
            cmd.setString(4, txtClienteRazao.getText().trim()); // cliente_razao
            cmd.setString(5, cbStatusCancelamento.getValue());  // status_antigo
            cmd.setDate(6, java.sql.Date.valueOf(dpInicioCancelamento.getValue())); // inicio_cancelamento
            cmd.setDate(7, java.sql.Date.valueOf(dpFinalCancelamento.getValue()));  // final_cancelamento
            cmd.setString(8, txtChamado.getText().trim());      // chamado
            cmd.setString(9, txtTecnico.getText().trim());      // tecnico_responsavel

            // Obtém o e-mail do usuário logado (ou "Técnico" se não houver sessão)
            String tecnicoLogado = App.getUsuarioLogado() != null
                    ? App.getUsuarioLogado() : "Técnico";

            if (modoEdicao) {
                // No UPDATE, o décimo parâmetro é o ID do registro
                cmd.setInt(10, idEdicao);
            } else {
                // No INSERT, o décimo parâmetro é o campo "criado_por"
                cmd.setString(10, tecnicoLogado);
            }

            // Executa a inserção ou atualização no banco
            cmd.executeUpdate();

            // Exibe mensagem de sucesso
            exibirAlerta("Sucesso", modoEdicao ? "Registro atualizado!" : "Cancelamento registrado com sucesso!", Alert.AlertType.INFORMATION);
            String acaoLog = modoEdicao ? "EDIÇÃO" : "INCLUSÃO";
            LoggerAuditoria.registrar(acaoLog, "Registro de Cancelamento: " + txtClienteRazao.getText().trim() + " foi " + (modoEdicao ? "alterado." : "criado."));

            // Fecha a janela após salvar
            fecharJanela();

        } catch (SQLException e) {
            // Em caso de erro de banco, exibe o motivo
            exibirAlerta("Erro Banco", "Erro ao salvar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Prepara a tela para visualização (somente leitura). Preenche os campos e
     * bloqueia a edição.
     *
     * @param cliente Objeto ClienteCancelado com os dados a exibir.
     */
    public void carregarDadosParaVisualizacao(ClienteCancelado cliente) {
        this.modoVisualizacao = true;
        preencherCampos(cliente);
        travarCampos(); // Desabilita todos os campos
    }

    /**
     * Prepara a tela para edição de um cancelamento existente. Preenche os
     * campos e mantém a edição habilitada.
     *
     * @param cliente Objeto ClienteCancelado com os dados atuais.
     */
    public void carregarDadosParaEdicao(ClienteCancelado cliente) {
        this.modoEdicao = true;
        this.idEdicao = cliente.getId();   // Guarda o ID para usar no UPDATE
        preencherCampos(cliente);
    }

    /**
     * Popula os campos da interface com os dados vindos do objeto
     * ClienteCancelado. Converte datas de String para LocalDate usando parse
     * seguro.
     */
    private void preencherCampos(ClienteCancelado cliente) {
        // Campos de texto simples
        if (txtClienteRazao != null) {
            txtClienteRazao.setText(cliente.getCliente_razao());
        }
        if (cbTipoNuvem != null) {
            cbTipoNuvem.setValue(cliente.getTipo_nuvem());
        }
        if (cbStatusCancelamento != null) {
            cbStatusCancelamento.setValue(cliente.getStatus_antigo());
        }
        if (txtChamado != null) {
            txtChamado.setText(cliente.getChamado());
        }
        if (txtTecnico != null) {
            txtTecnico.setText(cliente.getTecnico_responsavel());
        }

        // Conversão de datas: tenta fazer o parse; se falhar, simplesmente ignora
        try {
            if (dpDataCriacao != null && cliente.getData_criacao() != null) {
                dpDataCriacao.setValue(LocalDate.parse(cliente.getData_criacao()));
            }
            if (dpInicioCancelamento != null && cliente.getInicio_cancelamento() != null) {
                dpInicioCancelamento.setValue(LocalDate.parse(cliente.getInicio_cancelamento()));
            }
            if (dpFinalCancelamento != null && cliente.getFinal_cancelamento() != null) {
                dpFinalCancelamento.setValue(LocalDate.parse(cliente.getFinal_cancelamento()));
            }
        } catch (Exception e) {
            // Se alguma data vier em formato inválido, os campos permanecem vazios
        }
    }

    /**
     * Desabilita todos os campos editáveis e combos, transformando a tela em
     * modo de visualização (não permite alterações).
     */
    private void travarCampos() {
        // Torna os TextField não editáveis
        if (txtClienteRazao != null) {
            txtClienteRazao.setEditable(false);
        }
        if (txtCnpjCpf != null) {
            txtCnpjCpf.setEditable(false);
        }
        if (txtCodAg != null) {
            txtCodAg.setEditable(false);
        }
        if (txtRazaoAntiga != null) {
            txtRazaoAntiga.setEditable(false);
        }
        if (txtCnpjCpfAntigo != null) {
            txtCnpjCpfAntigo.setEditable(false);
        }
        if (txtQtdUsuarios != null) {
            txtQtdUsuarios.setEditable(false);
        }
        if (txtNomePasta != null) {
            txtNomePasta.setEditable(false);
        }
        if (txtSistemas != null) {
            txtSistemas.setEditable(false);
        }
        if (txtChamado != null) {
            txtChamado.setEditable(false);
        }
        if (txtCaminhoBanco != null) {
            txtCaminhoBanco.setEditable(false);
        }
        if (txtTecnico != null) {
            txtTecnico.setEditable(false);
        }

        // Desabilita os ComboBox (não podem ser abertos)
        if (cbSegmento != null) {
            cbSegmento.setDisable(true);
        }
        if (cbTipoNuvem != null) {
            cbTipoNuvem.setDisable(true);
        }
        if (cbOrigem != null) {
            cbOrigem.setDisable(true);
        }
        if (cbTipoBanco != null) {
            cbTipoBanco.setDisable(true);
        }
        if (cbStatusCancelamento != null) {
            cbStatusCancelamento.setDisable(true);
        }

        // Desabilita os DatePicker
        if (dpDataCriacao != null) {
            dpDataCriacao.setDisable(true);
        }
        if (dpInicioCancelamento != null) {
            dpInicioCancelamento.setDisable(true);
        }
        if (dpFinalCancelamento != null) {
            dpFinalCancelamento.setDisable(true);
        }
    }

    /**
     * Acionado ao clicar em "Cancelar". Apenas fecha a janela sem salvar.
     */
    @FXML
    private void cancelar(ActionEvent event) {
        fecharJanela();
    }

    /**
     * Fecha a janela atual. Obtém o Stage a partir de um dos campos da tela.
     */
    private void fecharJanela() {
        // Usa o campo txtClienteRazao (sempre presente) para obter a janela
        ((Stage) txtClienteRazao.getScene().getWindow()).close();
    }

    /**
     * Exibe um diálogo (Alert) com a mensagem e o tipo especificados.
     *
     * @param titulo Título da janela do alerta
     * @param msg Mensagem de conteúdo
     * @param tipo Tipo do alerta (ERROR, WARNING, INFORMATION, etc.)
     */
    private void exibirAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);  // Sem cabeçalho extra
        alert.setContentText(msg);
        alert.showAndWait();        // Exibe e aguarda o usuário fechar
    }
}

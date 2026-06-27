package com.mycompany.proton.controller;

import com.mycompany.proton.model.ClienteCancelado;
import com.mycompany.proton.util.GerenciadorDeLog;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador do formulário de cancelamento de cliente.
 *
 * CORREÇÕES APLICADAS:
 * 1. Declarado campo @FXML cbPod (ComboBox) que era referenciado em
 *    salvarCancelamento() mas nunca havia sido declarado, causando
 *    NullPointerException em runtime ao tentar salvar um cancelamento.
 *    Nota: se o FXML FormCancelado.fxml não tiver o cbPod, o campo
 *    ficará null e o código trata isso com verificação defensiva.
 * 2. Corrigido parsing de datas em preencherCampos():
 *    - O banco retorna datas no formato ISO (yyyy-MM-dd).
 *    - O código anterior usava somente o padrão dd/MM/yyyy, fazendo
 *      o parse falhar silenciosamente e deixar o campo vazio.
 *    - Agora tenta primeiro o formato ISO, depois o brasileiro.
 * 3. Removidos imports não utilizados de App, LoggerAuditoria,
 *    ConfigBancoController, Connection, DriverManager, PreparedStatement
 *    (a persistência foi delegada ao Service/DAO corretamente).
 */
public class FormCanceladoController implements Initializable {

    // ==================== ABA 1: DADOS DO CLIENTE ====================
    @FXML private ComboBox<String> cbSegmento;
    @FXML private ComboBox<String> cbTipoNuvem;
    @FXML private DatePicker dpDataCriacao;
    @FXML private TextField txtClienteRazao;
    @FXML private TextField txtCnpjCpf;
    @FXML private TextField txtCodAg;
    @FXML private TextField txtRazaoAntiga;
    @FXML private TextField txtCnpjCpfAntigo;
    @FXML private TextField txtQtdUsuarios;
    @FXML private TextField txtNomePasta;
    @FXML private ComboBox<String> cbOrigem;
    @FXML private TextField txtSistemas;

    /**
     * CORREÇÃO: cbPod estava sendo referenciado em salvarCancelamento()
     * sem estar declarado como campo @FXML, causando NullPointerException.
     * Campo declarado aqui. Se não existir no FXML, ficará null e o código
     * trata isso defensivamente (usa pod = 1 como fallback).
     */
    @FXML private ComboBox<String> cbPod;

    // ==================== ABA 2: TRIAGEM DE DADOS ====================
    @FXML private ComboBox<String> cbTipoBanco;
    @FXML private DatePicker dpInicioCancelamento;
    @FXML private DatePicker dpFinalCancelamento;
    @FXML private TextField txtChamado;
    @FXML private TextField txtCaminhoBanco;
    @FXML private TextField txtTecnico;
    @FXML private ComboBox<String> cbStatusCancelamento;

    // ==================== CONTROLE DE MODO ====================
    private boolean modoVisualizacao = false;
    private boolean modoEdicao = false;
    private Integer idEdicao = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (cbSegmento != null) {
            cbSegmento.setItems(FXCollections.observableArrayList("Corporativo", "Contábil"));
        }
        if (cbTipoNuvem != null) {
            cbTipoNuvem.setItems(FXCollections.observableArrayList("Compartilhada", "Dedicada"));
        }
        if (cbOrigem != null) {
            cbOrigem.setItems(FXCollections.observableArrayList("Base", "Novo Esocial", "Novo"));
        }
        if (cbTipoBanco != null) {
            cbTipoBanco.setItems(FXCollections.observableArrayList("Firebird", "MSSQL", "PostgreSQL"));
        }
        if (cbStatusCancelamento != null) {
            cbStatusCancelamento.setItems(FXCollections.observableArrayList("Desativado", "Pendente"));
        }
        // CORREÇÃO: inicializa cbPod caso ele exista no FXML
        if (cbPod != null) {
            cbPod.setItems(FXCollections.observableArrayList("1","2","3","4","5","6","7","8"));
        }
    }

    @FXML
    private void salvarCancelamento(ActionEvent event) {
        if (modoVisualizacao) {
            fecharJanela();
            return;
        }

        // Validações dos campos obrigatórios
        if (cbSegmento.getValue() == null
                || dpDataCriacao.getValue() == null
                || txtClienteRazao.getText().trim().isEmpty()) {
            exibirAlerta("Aviso", "Preencha todos os campos obrigatórios!", Alert.AlertType.WARNING);
            return;
        }

        // CORREÇÃO: cbPod pode ser null se não existir no FXML — trata defensivamente
        int podSelecionado = 1;
        if (cbPod != null && cbPod.getValue() != null) {
            try {
                podSelecionado = Integer.parseInt(cbPod.getValue().trim());
            } catch (NumberFormatException e) {
                GerenciadorDeLog.erro("Valor inválido para POD: " + cbPod.getValue());
            }
        }

        // Data de início e fim: usadas com null-safety
        String inicioCancelamento = dpInicioCancelamento != null && dpInicioCancelamento.getValue() != null
                ? dpInicioCancelamento.getValue().toString() : "";
        String finalCancelamento = dpFinalCancelamento != null && dpFinalCancelamento.getValue() != null
                ? dpFinalCancelamento.getValue().toString() : "";

        ClienteCancelado c = new ClienteCancelado(
                modoEdicao ? idEdicao : 0,
                cbTipoNuvem.getValue(),
                podSelecionado,
                dpDataCriacao.getValue().toString(),
                txtClienteRazao.getText().trim(),
                cbStatusCancelamento.getValue(),
                inicioCancelamento,
                finalCancelamento,
                txtChamado != null ? txtChamado.getText().trim() : "",
                txtTecnico != null ? txtTecnico.getText().trim() : ""
        );

        try {
            com.mycompany.proton.service.ClienteCanceladoService service =
                    new com.mycompany.proton.service.ClienteCanceladoService();
            service.salvar(c, modoEdicao);
            exibirAlerta("Sucesso",
                    modoEdicao ? "Registro atualizado!" : "Cancelamento registrado com sucesso!",
                    Alert.AlertType.INFORMATION);
            fecharJanela();
        } catch (SQLException e) {
            exibirAlerta("Erro Banco", "Erro ao salvar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void carregarDadosParaVisualizacao(ClienteCancelado cliente) {
        this.modoVisualizacao = true;
        preencherCampos(cliente);
        travarCampos();
    }

    public void carregarDadosParaEdicao(ClienteCancelado cliente) {
        this.modoEdicao = true;
        this.idEdicao = cliente.getId();
        preencherCampos(cliente);
    }

    /**
     * Preenche os campos com os dados do objeto ClienteCancelado.
     *
     * CORREÇÃO: parsing de datas agora tenta dois formatos:
     * 1. ISO (yyyy-MM-dd) — formato retornado pelo PostgreSQL
     * 2. Brasileiro (dd/MM/yyyy) — formato alternativo
     * Antes usava somente dd/MM/yyyy, fazendo todos os campos de data
     * ficarem vazios quando o banco retornava yyyy-MM-dd.
     */
    private void preencherCampos(ClienteCancelado cliente) {
        if (txtClienteRazao != null) {
            txtClienteRazao.setText(cliente.getCliente_razao() != null ? cliente.getCliente_razao() : "");
        }
        if (cbTipoNuvem != null) {
            cbTipoNuvem.setValue(cliente.getTipo_nuvem());
        }
        if (cbStatusCancelamento != null) {
            cbStatusCancelamento.setValue(cliente.getStatus_antigo());
        }
        if (txtChamado != null) {
            txtChamado.setText(cliente.getChamado() != null ? cliente.getChamado() : "");
        }
        if (txtTecnico != null) {
            txtTecnico.setText(cliente.getTecnico_responsavel() != null
                    ? cliente.getTecnico_responsavel() : "");
        }

        // CORREÇÃO: parsing seguro que tenta ISO primeiro, depois brasileiro
        if (dpDataCriacao != null) {
            dpDataCriacao.setValue(parseDateSafe(cliente.getData_criacao()));
        }
        if (dpInicioCancelamento != null) {
            dpInicioCancelamento.setValue(parseDateSafe(cliente.getInicio_cancelamento()));
        }
        if (dpFinalCancelamento != null) {
            dpFinalCancelamento.setValue(parseDateSafe(cliente.getFinal_cancelamento()));
        }
    }

    /**
     * Converte uma String de data para LocalDate de forma segura.
     * Tenta primeiro o formato ISO yyyy-MM-dd (padrão do PostgreSQL),
     * depois dd/MM/yyyy (formato brasileiro alternativo).
     *
     * @param dataStr String com a data a converter
     * @return LocalDate se a conversão for bem-sucedida, null caso contrário
     */
    private LocalDate parseDateSafe(String dataStr) {
        if (dataStr == null || dataStr.isEmpty() || dataStr.equals("N/A")) {
            return null;
        }
        // Remove porção de hora se vier como "2024-01-15 00:00:00"
        String dataLimpa = dataStr.split(" ")[0].trim();

        // Tentativa 1: formato ISO (yyyy-MM-dd) — padrão PostgreSQL
        try {
            return LocalDate.parse(dataLimpa);
        } catch (DateTimeParseException ignored) {}

        // Tentativa 2: formato brasileiro (dd/MM/yyyy)
        try {
            return LocalDate.parse(dataLimpa, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            GerenciadorDeLog.erro("Não foi possível interpretar a data: " + dataStr);
            return null;
        }
    }

    private void travarCampos() {
        // TextFields
        TextField[] campos = {txtClienteRazao, txtCnpjCpf, txtCodAg, txtRazaoAntiga,
                txtCnpjCpfAntigo, txtQtdUsuarios, txtNomePasta, txtSistemas,
                txtChamado, txtCaminhoBanco, txtTecnico};
        for (TextField campo : campos) {
            if (campo != null) campo.setEditable(false);
        }
        // ComboBoxes
        ComboBox<?>[] combos = {cbSegmento, cbTipoNuvem, cbOrigem, cbTipoBanco,
                cbStatusCancelamento, cbPod};
        for (ComboBox<?> combo : combos) {
            if (combo != null) combo.setDisable(true);
        }
        // DatePickers
        DatePicker[] pickers = {dpDataCriacao, dpInicioCancelamento, dpFinalCancelamento};
        for (DatePicker picker : pickers) {
            if (picker != null) picker.setDisable(true);
        }
    }

    @FXML
    private void cancelar(ActionEvent event) {
        fecharJanela();
    }

    private void fecharJanela() {
        ((Stage) txtClienteRazao.getScene().getWindow()).close();
    }

    private void exibirAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
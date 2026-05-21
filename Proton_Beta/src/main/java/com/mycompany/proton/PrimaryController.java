package com.mycompany.proton;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PrimaryController implements Initializable {

    @FXML
    private Label lblTituloAba;
    @FXML
    private Button btnDedicados;
    @FXML
    private Button btnCompartilhados;

    // --- ELEMENTOS DA TABELA DE DEDICADOS ---
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

    // --- ELEMENTOS DA TABELA DE COMPARTILHADOS ---
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

    // Listas em memória para atualizar a interface rapidamente
    private ObservableList<Cliente> listaDedicados = FXCollections.observableArrayList();
    private ObservableList<ClienteCompartilhado> listaCompartilhados = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mapeamento das colunas de Dedicados
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpj"));
        colQntServer.setCellValueFactory(new PropertyValueFactory<>("qnt_server"));
        colAd.setCellValueFactory(new PropertyValueFactory<>("active_directory"));
        colAmbiente.setCellValueFactory(new PropertyValueFactory<>("ambiente"));
        colVpn.setCellValueFactory(new PropertyValueFactory<>("vpn"));
        colSgbd.setCellValueFactory(new PropertyValueFactory<>("sgbd"));

        // Mapeamento das colunas de Compartilhados
        colCId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCTipoNuvem.setCellValueFactory(new PropertyValueFactory<>("tipoNuvem"));
        colCPod.setCellValueFactory(new PropertyValueFactory<>("pod"));
        colCData.setCellValueFactory(new PropertyValueFactory<>("dataCriacao"));
        colCRazao.setCellValueFactory(new PropertyValueFactory<>("razaoSocial"));
        colCCnpj.setCellValueFactory(new PropertyValueFactory<>("cpfCnpj"));
        colCAg.setCellValueFactory(new PropertyValueFactory<>("codAg"));
        colCPasta.setCellValueFactory(new PropertyValueFactory<>("pastaRede"));
        colCContato.setCellValueFactory(new PropertyValueFactory<>("contato"));
        colCUsuarios.setCellValueFactory(new PropertyValueFactory<>("usuarios"));
        colCSistemas.setCellValueFactory(new PropertyValueFactory<>("sistemas"));
        colCStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCBanco.setCellValueFactory(new PropertyValueFactory<>("bancoDados"));

        // Ao abrir a tela, carrega a aba 1 por padrão
        carregarDadosDedicados();
    }

    // --- LÓGICA DOS BOTÕES (Alternar Abas) ---
    @FXML
    private void abaClientesDedicados(ActionEvent event) {
        lblTituloAba.setText("Clientes Dedicados (SKYONE)");

        // Estilização dos botões (Deixa o clicado azul)
        btnDedicados.setStyle("-fx-background-color: #3b4248; -fx-text-fill: white; -fx-alignment: center-left; -fx-border-color: #0d6efd; -fx-border-width: 0 0 0 4;");
        btnCompartilhados.setStyle("-fx-background-color: transparent; -fx-text-fill: #adb5bd; -fx-alignment: center-left;");

        tabelaCompartilhados.setVisible(false); // Esconde a 2
        tabelaDedicados.setVisible(true);       // Mostra a 1
        carregarDadosDedicados();
    }

    @FXML
    private void abaClientesCompartilhados(ActionEvent event) {
        lblTituloAba.setText("Clientes Compartilhados (NUVEM)");

        btnCompartilhados.setStyle("-fx-background-color: #3b4248; -fx-text-fill: white; -fx-alignment: center-left; -fx-border-color: #0d6efd; -fx-border-width: 0 0 0 4;");
        btnDedicados.setStyle("-fx-background-color: transparent; -fx-text-fill: #adb5bd; -fx-alignment: center-left;");

        tabelaDedicados.setVisible(false);      // Esconde a 1
        tabelaCompartilhados.setVisible(true);  // Mostra a 2
        carregarDadosCompartilhados();
    }

    // --- MÉTODOS DE BANCO DE DADOS ---
    private Connection conectar() throws SQLException {
        String ipServidor = "localhost";
        String nomeBanco = "proton"; // Coloque o nome do seu banco
        String usuario = "postgres";
        String senha = "tec@123"; // COLOQUE SUA SENHA AQUI

        String urlConexao = "jdbc:postgresql://" + ipServidor + ":5432/" + nomeBanco;
        return DriverManager.getConnection(urlConexao, usuario, senha);
    }

    private void carregarDadosDedicados() {
        listaDedicados.clear();
        String sql = "SELECT * FROM clientes_dedicados ORDER BY id ASC";

        try (Connection conexao = conectar(); PreparedStatement comando = conexao.prepareStatement(sql); ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                listaDedicados.add(new Cliente(
                        resultado.getInt("id"),
                        resultado.getString("nome"), 
                        resultado.getString("cnpj"),
                        resultado.getInt("qnt_server"), 
                        resultado.getString("active_directory"),
                        resultado.getString("ambiente"), 
                        resultado.getBoolean("vpn"), 
                        resultado.getString("sgbd")
                ));
            }
            tabelaDedicados.setItems(listaDedicados);
        } catch (SQLException e) {
            System.out.println("Erro Dedicados: " + e.getMessage());
        }
    }
    
    private void carregarDadosCompartilhados() {
        listaCompartilhados.clear(); 
        String sql = "SELECT * FROM clientes_compartilhados ORDER BY id ASC";

        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                listaCompartilhados.add(new ClienteCompartilhado(
                    resultado.getInt("id"), resultado.getString("tipo_nuvem"), resultado.getInt("pod"),
                    resultado.getString("data_criacao"), resultado.getString("razao_social"),
                    resultado.getString("cpf_cnpj"), resultado.getString("razao_cnpj_antigos"),
                    resultado.getString("cod_ag"), resultado.getString("pasta_rede"),
                    resultado.getString("contato"), resultado.getInt("usuarios"),
                    resultado.getString("origem"), resultado.getString("telefone"),
                    resultado.getString("email"), resultado.getString("sistemas"),
                    resultado.getString("status"), resultado.getString("banco_dados")
                ));
            }
            tabelaCompartilhados.setItems(listaCompartilhados);
        } catch (SQLException e) {
            System.out.println("Erro Compartilhados: " + e.getMessage());
        }
    }
}

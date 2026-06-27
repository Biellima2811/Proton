package com.mycompany.proton.controller;

import java.io.File;                // Representa o arquivo de configuração no disco
import java.io.FileInputStream;     // Lê bytes do arquivo (para carregar propriedades)
import java.io.FileOutputStream;    // Escreve bytes no arquivo (para salvar propriedades)
import java.io.IOException;         // Exceção de entrada/saída
import java.net.URL;                // Endereço do recurso FXML (usado no initialize)
import java.util.Properties;        // Manipula pares chave=valor do arquivo de configuração
import java.util.ResourceBundle;    // Pacote de recursos (usado no initialize)
import javafx.event.ActionEvent;    // Evento disparado por botões
import javafx.fxml.FXML;            // Anotação para vincular elementos do FXML
import javafx.fxml.Initializable;   // Interface para inicializar controladores após carregar FXML
import javafx.scene.control.Alert;  // Exibe diálogos de informação/erro
import javafx.scene.control.PasswordField; // Campo de texto para senha (mascarado)
import javafx.scene.control.TextField;     // Campo de texto comum
import javafx.stage.Stage;          // Janela (para fechar a tela atual)

/**
 * Controlador da tela de configuração do banco de dados.
 *
 * Permite ao usuário definir IP, nome do banco, usuário e senha do PostgreSQL.
 * As configurações são persistidas em um arquivo "config_banco.properties" e
 * podem ser acessadas por outras partes do sistema via métodos estáticos.
 */
public class ConfigBancoController implements Initializable {

    // ==================== CAMPOS VINCULADOS À INTERFACE (FXML) ====================
    // O fx:id de cada campo deve coincidir com o nome da variável no SceneBuilder.
    @FXML
    private TextField txtIpServidor;   // Campo para o IP ou hostname do servidor

    @FXML
    private TextField txtNomeBanco;    // Campo para o nome do banco de dados

    @FXML
    private TextField txtUsuarioBD;    // Campo para o nome de usuário do banco

    @FXML
    private PasswordField txtSenhaBD;  // Campo para a senha (dígitos ocultos)

    // ==================== CONFIGURAÇÕES PERSISTIDAS ====================
    // Nome do arquivo que guarda as configurações (ficará na pasta de execução)
    private static final String CONFIG_FILE = "config_banco.properties";

    // Objeto Properties em memória, carregado uma única vez (static)
    private static Properties props = new Properties();

    /**
     * Bloco estático: executa assim que a classe é carregada pela JVM. 1.
     * Carrega o driver do PostgreSQL (necessário para conexões JDBC). 2. Lê (ou
     * cria) o arquivo de configuração.
     */
    static {
        try {
            // Registra a classe do driver na memória
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver do PostgreSQL carregado com sucesso no .EXE!");
        } catch (ClassNotFoundException e) {
            // Se o driver não estiver no classpath, o sistema não conseguirá conectar
            System.out.println("ERRO FATAL: Driver do PostgreSQL não encontrado.");
        }
        // Carrega as propriedades do arquivo para o objeto props
        carregarConfiguracoes();
    }

    /**
     * Lê o arquivo de configuração e preenche o objeto Properties. Se o arquivo
     * não existir, cria um com valores padrão.
     */
    public static void carregarConfiguracoes() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            // Tenta abrir o arquivo e carregar os pares chave=valor
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);  // Lê e decodifica as propriedades do arquivo
            } catch (IOException e) {
                System.out.println("Erro ao carregar configurações.");
            }
        } else {
            // Arquivo não encontrado: cria um com os valores padrão
            salvarConfiguracoesPadrao();
        }
    }

    /**
     * Define valores padrão nas propriedades e salva no disco.
     */
    private static void salvarConfiguracoesPadrao() {
        props.setProperty("ip_servidor", "localhost");
        props.setProperty("nome_banco", "proton");
        props.setProperty("usuario_db", "postgres");
        props.setProperty("senha_db", com.mycompany.proton.util.Seguranca.criptografar("123456"));
        salvarArquivo();  // Persiste as alterações
    }

    /**
     * Grava o objeto Properties no arquivo de configuração (sobrescrevendo).
     */
    private static void salvarArquivo() {
        // Bloco try-with-resources fecha automaticamente o FileOutputStream
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Configurações de Conexão Proton"); // Escreve com comentário no cabeçalho
        } catch (IOException e) {
            System.out.println("Erro ao salvar arquivo.");
        }
    }

    // ==================== GETTERS ESTÁTICOS (USO GLOBAL) ====================
    // Outras classes (ex.: ConexaoBD) utilizam esses métodos para montar a URL JDBC.
    public static String getIpServidor() {
        return props.getProperty("ip_servidor", "localhost"); // Retorna "localhost" se a chave não existir
    }

    public static String getNomeBanco() {
        return props.getProperty("nome_banco", "proton");
    }

    public static String getUsuarioBD() {
        return props.getProperty("usuario_db", "postgres");
    }

    public static String getSenhaBD() {
        // Puxa do arquivo e descriptografa usando a chave da máquina
        return com.mycompany.proton.util.Seguranca.descriptografar(props.getProperty("senha_db", ""));
    }

    // ==================== INICIALIZAÇÃO DA TELA ====================
    /**
     * Método chamado automaticamente pelo JavaFX após a injeção dos @FXML.
     * Preenche os campos com os valores atualmente salvos no arquivo de
     * propriedades.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Verificação defensiva contra campos não injetados (null)
        if (txtIpServidor != null) {
            txtIpServidor.setText(getIpServidor());    // Exibe IP atual
        }
        if (txtNomeBanco != null) {
            txtNomeBanco.setText(getNomeBanco());      // Exibe nome do banco
        }
        if (txtUsuarioBD != null) {
            txtUsuarioBD.setText(getUsuarioBD());      // Exibe usuário
        }
        if (txtSenhaBD != null) {
            txtSenhaBD.setText(getSenhaBD());          // Exibe senha (mascarada pelo PasswordField)
        }
    }

    // ==================== AÇÕES DOS BOTÕES ====================
    /**
     * Disparado ao clicar no botão "Salvar" (associado no FXML). Atualiza as
     * propriedades com os valores digitados e persiste no arquivo. Exibe uma
     * mensagem de sucesso e fecha a janela.
     */
    @FXML
    private void salvarConfiguracao(ActionEvent event) {
        // Lê cada campo, removendo espaços extras
        if (txtIpServidor != null) {
            props.setProperty("ip_servidor", txtIpServidor.getText().trim());
        }
        if (txtNomeBanco != null) {
            props.setProperty("nome_banco", txtNomeBanco.getText().trim());
        }
        if (txtUsuarioBD != null) {
            props.setProperty("usuario_db", txtUsuarioBD.getText().trim());
        }
        if (txtSenhaBD != null) {
            // Criptografa o que o usuário digitou ANTES de salvar no arquivo .properties
            props.setProperty("senha_db", com.mycompany.proton.util.Seguranca.criptografar(txtSenhaBD.getText()));
        }

        // Persiste as alterações no arquivo
        salvarArquivo();

        // Monta um diálogo informativo
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Configuração Atualizada");
        alerta.setHeaderText(null); // Sem cabeçalho extra
        alerta.setContentText("Banco de Dados configurado com sucesso! Tente logar novamente.");
        alerta.showAndWait(); // Exibe e espera o usuário fechar

        // Fecha a tela de configuração
        fechar(event);
    }

    /**
     * Fecha a janela atual. Obtém o Stage a partir de qualquer campo presente.
     */
    @FXML
    private void fechar(ActionEvent event) {
        if (txtIpServidor != null) {
            // Obtém a janela (Stage) que contém o campo de texto e a fecha
            Stage stage = (Stage) txtIpServidor.getScene().getWindow();
            stage.close();
        }
        // Se nenhum campo estiver injetado, a tela permanece aberta (comportamento seguro)
    }
}

package com.mycompany.proton.model;

import javafx.beans.property.SimpleStringProperty;  // Propriedade observável que notifica a TableView automaticamente

/**
 * Representa um servidor dedicado associado a um cliente.
 *
 * Utilizado na mini-tabela dentro da Aba 2 (Gestão de Servidores) do formulário
 * de Cliente Dedicado. Cada instância contém o tipo, IP, usuário e senha do
 * servidor.
 *
 * O uso de {@link SimpleStringProperty} permite que a TableView seja atualizada
 * automaticamente sempre que o valor de uma célula for alterado (data binding).
 */
public class ServidorDedicado {

    // Propriedade observável para o tipo do servidor (APP, Banco de Dados, Repositório, etc.)
    private SimpleStringProperty tipo;

    // Propriedade observável para o endereço IP (ou hostname) do servidor
    private SimpleStringProperty ip;

    // Propriedade observável para o nome de usuário de acesso ao servidor
    private SimpleStringProperty usuario;

    // Propriedade observável para a senha de acesso ao servidor (armazenada em texto puro)
    private SimpleStringProperty senha;

    /**
     * Construtor que inicializa todas as propriedades do servidor.
     *
     * @param tipo Tipo do servidor (ex.: "APP", "Banco de Dados (SGBD)")
     * @param ip Endereço IP ou hostname
     * @param usuario Nome de usuário para login
     * @param senha Senha para login
     */
    public ServidorDedicado(String tipo, String ip, String usuario, String senha) {
        // Converte cada String recebida em uma SimpleStringProperty (observável)
        this.tipo = new SimpleStringProperty(tipo);
        this.ip = new SimpleStringProperty(ip);
        this.usuario = new SimpleStringProperty(usuario);
        this.senha = new SimpleStringProperty(senha);
    }

    // ----- Getters (retornam o valor puro) -----
    /**
     * @return O tipo do servidor
     */
    public String getTipo() {
        return tipo.get();     // .get() extrai o valor da SimpleStringProperty
    }

    /**
     * @return O IP ou hostname do servidor
     */
    public String getIp() {
        return ip.get();
    }

    /**
     * @return O nome de usuário de acesso
     */
    public String getUsuario() {
        return usuario.get();
    }

    /**
     * @return A senha de acesso
     */
    public String getSenha() {
        return senha.get();
    }

    // ----- Métodos Property (necessários para o CellValueFactory da TableView) -----
    /**
     * @return A propriedade observável do tipo
     */
    public SimpleStringProperty tipoProperty() {
        return tipo;
    }

    /**
     * @return A propriedade observável do IP
     */
    public SimpleStringProperty ipProperty() {
        return ip;
    }

    /**
     * @return A propriedade observável do usuário
     */
    public SimpleStringProperty usuarioProperty() {
        return usuario;
    }

    /**
     * @return A propriedade observável da senha
     */
    public SimpleStringProperty senhaProperty() {
        return senha;
    }
}

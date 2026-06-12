package com.mycompany.proton.model;

/**
 * Representa um registro de ambiente Fortes RH no sistema Proton.
 *
 * Armazena informações sobre o cliente, tipo de ambiente
 * (compartilhado/dedicado), dados de acesso ao banco, servidores, load balance
 * e URL de acesso.
 *
 * Funciona como modelo de dados (POJO) para transportar informações entre o
 * banco de dados e as telas de cadastro/edição.
 */
public class FortesRH {

    // Identificador único do registro (chave primária)
    private int id;

    // Tipo de ambiente: "Compartilhado" ou "Dedicado"
    private String tipo_ambiente;

    // Nome ou razão social do cliente
    private String cliente;

    // CPF ou CNPJ do cliente
    private String cnpj_cpf;

    // URL de acesso ao sistema Fortes RH
    private String url_acesso;

    // Endereço do servidor de aplicação (onde o sistema está hospedado)
    private String servidor_app;

    // Nome do banco de dados utilizado
    private String banco_dados;

    // Caminho da pasta web (publicação da aplicação)
    private String pasta_web;

    // Usuário de conexão com o banco de dados
    private String usuario_db;

    // Senha de conexão com o banco de dados
    private String senha_db;

    // Indica se o ambiente possui load balance ("Sim" / "Não")
    private String load_balance;

    // Endereço IP do load balance (se houver)
    private String ip_load_balance;

    /**
     * Construtor completo do modelo FortesRH.
     *
     * @param id Identificador único
     * @param tipo_ambiente Tipo de ambiente (Compartilhado / Dedicado)
     * @param cliente Nome do cliente
     * @param cnpj_cpf CPF ou CNPJ
     * @param url_acesso URL de acesso ao sistema
     * @param servidor_app Servidor de aplicação
     * @param banco_dados Banco de dados
     * @param pasta_web Pasta web
     * @param usuario_db Usuário do banco
     * @param senha_db Senha do banco
     * @param load_balance Possui load balance?
     * @param ip_load_balance IP do load balance
     */
    public FortesRH(int id, String tipo_ambiente, String cliente, String cnpj_cpf,
            String url_acesso, String servidor_app, String banco_dados,
            String pasta_web, String usuario_db, String senha_db,
            String load_balance, String ip_load_balance) {
        // Atribui cada parâmetro recebido ao campo correspondente da classe
        this.id = id;
        this.tipo_ambiente = tipo_ambiente;
        this.cliente = cliente;
        this.cnpj_cpf = cnpj_cpf;
        this.url_acesso = url_acesso;
        this.servidor_app = servidor_app;
        this.banco_dados = banco_dados;
        this.pasta_web = pasta_web;
        this.usuario_db = usuario_db;
        this.senha_db = senha_db;
        this.load_balance = load_balance;
        this.ip_load_balance = ip_load_balance;
    }

    // ==================== GETTERS (métodos de acesso) ====================
    /**
     * @return O ID do registro
     */
    public int getId() {
        return id;
    }

    /**
     * @return O tipo de ambiente (Compartilhado / Dedicado)
     */
    public String getTipo_ambiente() {
        return tipo_ambiente;
    }

    /**
     * @return O nome do cliente
     */
    public String getCliente() {
        return cliente;
    }

    /**
     * @return O CPF/CNPJ do cliente
     */
    public String getCnpj_cpf() {
        return cnpj_cpf;
    }

    /**
     * @return A URL de acesso ao sistema
     */
    public String getUrl_acesso() {
        return url_acesso;
    }

    /**
     * @return O endereço do servidor de aplicação
     */
    public String getServidor_app() {
        return servidor_app;
    }

    /**
     * @return O nome do banco de dados
     */
    public String getBanco_dados() {
        return banco_dados;
    }

    /**
     * @return O caminho da pasta web
     */
    public String getPasta_web() {
        return pasta_web;
    }

    /**
     * @return O usuário de conexão com o banco
     */
    public String getUsuario_db() {
        return usuario_db;
    }

    /**
     * @return A senha do banco de dados
     */
    public String getSenha_db() {
        return senha_db;
    }

    /**
     * @return Se possui load balance ("Sim" / "Não")
     */
    public String getLoad_balance() {
        return load_balance;
    }

    /**
     * @return O IP do load balance
     */
    public String getIp_load_balance() {
        return ip_load_balance;
    }
}

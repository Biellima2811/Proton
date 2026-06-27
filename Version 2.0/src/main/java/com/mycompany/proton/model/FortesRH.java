package com.mycompany.proton.model;

/**
 * Representa um registro de ambiente Fortes RH no sistema Proton.
 *
 * CORREÇÕES APLICADAS: - Adicionados campos: status, data_criacao, ip_publico,
 * ip_privado, versao, web_aplication - Esses campos eram acessados em
 * FortesRHDAO mas não existiam no modelo, causando erros de compilação "cannot
 * find symbol". - Construtor estendido para incluir os novos campos. -
 * Adicionados getters e setters para todos os campos.
 */
public class FortesRH {

    // --- Campos originais ---
    private int id;
    private String tipo_ambiente;
    private String cliente;
    private String cnpj_cpf;
    private String url_acesso;
    private String servidor_app;
    private String banco_dados;
    private String pasta_web;
    private String usuario_db;
    private String senha_db;
    private String load_balance;
    private String ip_load_balance;

    // --- Campos adicionados (CORREÇÃO) ---
    private String status;
    private String data_criacao;
    private String ip_publico;
    private String ip_privado;
    private String versao;
    private String web_aplication;

    /**
     * Construtor completo original (mantido para compatibilidade com o
     * PrimaryController que instancia FortesRH com 12 parâmetros).
     */
    public FortesRH(int id, String tipo_ambiente, String cliente, String cnpj_cpf,
            String url_acesso, String servidor_app, String banco_dados,
            String pasta_web, String usuario_db, String senha_db,
            String load_balance, String ip_load_balance) {
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
        // Campos extras inicializados com valor padrão seguro
        this.status = "Ativo";
        this.data_criacao = "";
        this.ip_publico = "N/A";
        this.ip_privado = "N/A";
        this.versao = "N/A";
        this.web_aplication = "N/A";
    }

    /**
     * Construtor estendido com todos os campos (NOVO — usado internamente).
     */
    public FortesRH(int id, String tipo_ambiente, String cliente, String cnpj_cpf,
            String url_acesso, String servidor_app, String banco_dados,
            String pasta_web, String usuario_db, String senha_db,
            String load_balance, String ip_load_balance,
            String status, String data_criacao, String ip_publico,
            String ip_privado, String versao, String web_aplication) {
        this(id, tipo_ambiente, cliente, cnpj_cpf, url_acesso, servidor_app,
                banco_dados, pasta_web, usuario_db, senha_db, load_balance, ip_load_balance);
        this.status = status != null ? status : "Ativo";
        this.data_criacao = data_criacao != null ? data_criacao : "";
        this.ip_publico = ip_publico != null ? ip_publico : "N/A";
        this.ip_privado = ip_privado != null ? ip_privado : "N/A";
        this.versao = versao != null ? versao : "N/A";
        this.web_aplication = web_aplication != null ? web_aplication : "N/A";
    }

    // ==================== GETTERS ORIGINAIS ====================
    public int getId() {
        return id;
    }

    public String getTipo_ambiente() {
        return tipo_ambiente;
    }

    public String getCliente() {
        return cliente;
    }

    public String getCnpj_cpf() {
        return cnpj_cpf;
    }

    public String getUrl_acesso() {
        return url_acesso;
    }

    public String getServidor_app() {
        return servidor_app;
    }

    public String getBanco_dados() {
        return banco_dados;
    }

    public String getPasta_web() {
        return pasta_web;
    }

    public String getUsuario_db() {
        return usuario_db;
    }

    public String getSenha_db() {
        return senha_db;
    }

    public String getLoad_balance() {
        return load_balance;
    }

    public String getIp_load_balance() {
        return ip_load_balance;
    }

    // ==================== GETTERS NOVOS (CORREÇÃO) ====================
    public String getStatus() {
        return status;
    }

    public String getData_criacao() {
        return data_criacao;
    }

    public String getIp_publico() {
        return ip_publico;
    }

    public String getIp_privado() {
        return ip_privado;
    }

    public String getVersao() {
        return versao;
    }

    public String getWeb_aplication() {
        return web_aplication;
    }

    // ==================== SETTERS (CORREÇÃO) ====================
    public void setStatus(String status) {
        this.status = status;
    }

    public void setData_criacao(String data_criacao) {
        this.data_criacao = data_criacao;
    }

    public void setIp_publico(String ip_publico) {
        this.ip_publico = ip_publico;
    }

    public void setIp_privado(String ip_privado) {
        this.ip_privado = ip_privado;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public void setWeb_aplication(String web_aplication) {
        this.web_aplication = web_aplication;
    }

    // Setters originais também adicionados para suportar edição
    public void setId(int id) {
        this.id = id;
    }

    public void setTipo_ambiente(String tipo_ambiente) {
        this.tipo_ambiente = tipo_ambiente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public void setCnpj_cpf(String cnpj_cpf) {
        this.cnpj_cpf = cnpj_cpf;
    }

    public void setUrl_acesso(String url_acesso) {
        this.url_acesso = url_acesso;
    }

    public void setServidor_app(String servidor_app) {
        this.servidor_app = servidor_app;
    }

    public void setBanco_dados(String banco_dados) {
        this.banco_dados = banco_dados;
    }

    public void setPasta_web(String pasta_web) {
        this.pasta_web = pasta_web;
    }

    public void setUsuario_db(String usuario_db) {
        this.usuario_db = usuario_db;
    }

    public void setSenha_db(String senha_db) {
        this.senha_db = senha_db;
    }

    public void setLoad_balance(String load_balance) {
        this.load_balance = load_balance;
    }

    public void setIp_load_balance(String ip_load_balance) {
        this.ip_load_balance = ip_load_balance;
    }
}

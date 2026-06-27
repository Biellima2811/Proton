package com.mycompany.proton.model;

/**
 * Representa um cliente no sistema Proton. Cada cliente possui informações de
 * infraestrutura, como quantidade de servidores, presença de Active Directory,
 * ambiente (produção/homologação), VPN, SGBD etc.
 *
 * @author gabriellevi_forteste
 */
public class Cliente {

    // Identificador único do cliente (geralmente chave primária no banco)
    private int id;

    // Razão social ou nome fantasia do cliente
    private String nome;

    // CNPJ do cliente (formato apenas números ou string)
    private String cnpj;

    // Quantidade de servidores pertencentes a este cliente
    private int qnt_server;

    // Indica se o cliente possui Active Directory (ex.: "Sim", "Não" ou domínio)
    private String active_directory;

    // Ambiente onde o cliente opera (ex.: "Produção", "Homologação")
    private String ambiente;

    // Se o cliente utiliza VPN (true = sim, false = não)
    private boolean vpn;

    // Sistema Gerenciador de Banco de Dados utilizado (ex.: "SQL Server", "Oracle", "MySQL")
    private String sgbd;

    /**
     * Construtor completo para instanciar um cliente com todos os atributos.
     *
     * @param id Código identificador
     * @param nome Nome do cliente
     * @param cnpj CNPJ
     * @param qnt_server Quantidade de servidores
     * @param active_directory Informação do Active Directory
     * @param ambiente Ambiente (produção/homologação)
     * @param vpn Utiliza VPN?
     * @param sgbd SGBD utilizado
     */
    public Cliente(int id, String nome, String cnpj, int qnt_server,
            String active_directory, String ambiente, boolean vpn, String sgbd) {
        // Atribui cada parâmetro ao campo correspondente da classe
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.qnt_server = qnt_server;
        this.active_directory = active_directory;
        this.ambiente = ambiente;
        this.vpn = vpn;
        this.sgbd = sgbd;
    }

    // ----- Métodos de acesso (getters) -----
    /**
     * @return O ID do cliente
     */
    public int getId() {
        return id;
    }

    /**
     * @return O nome do cliente
     */
    public String getNome() {
        return nome;
    }

    /**
     * @return O CNPJ do cliente
     */
    public String getCnpj() {
        return cnpj;
    }

    /**
     * @return A quantidade de servidores
     */
    public int getQnt_server() {
        return qnt_server;
    }

    /**
     * @return Informação sobre Active Directory
     */
    public String getActive_directory() {
        return active_directory;
    }

    /**
     * @return O ambiente (produção/homologação)
     */
    public String getAmbiente() {
        return ambiente;
    }

    /**
     * @return true se o cliente utiliza VPN, false caso contrário
     */
    public boolean isVpn() {
        return vpn;
    }

    /**
     * @return O SGBD utilizado pelo cliente
     */
    public String getSgbd() {
        return sgbd;
    }

}

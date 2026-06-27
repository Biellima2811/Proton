package com.mycompany.proton.model;

/**
 * Representa um cliente do tipo "Compartilhado" no sistema Proton. Contém
 * informações detalhadas como tipo de nuvem, POD, dados de contato, sistemas
 * utilizados, status, banco de dados, valor de seguro, entre outros.
 *
 * @author gabriellevi_forteste
 */
public class ClienteCompartilhado {

    // Identificador único do cliente compartilhado
    private int id;

    // Tipo de nuvem contratada (ex.: "Azure", "AWS")
    private String tipoNuvem;

    // Número do POD (ponto de entrega no datacenter)
    private int pod;

    // Data de criação do cadastro no sistema (formato String)
    private String dataCriacao;

    // Razão social da empresa cliente
    private String razaoSocial;

    // CPF ou CNPJ do cliente
    private String cpfCnpj;

    // Razão social ou CNPJ antigos (caso tenha havido alteração cadastral)
    private String razaoCnpjAntigos;

    // Código da agência (provavelmente relacionado a contrato ou unidade)
    private String codAg;

    // Caminho da pasta de rede associada ao cliente
    private String pastaRede;

    // Nome da pessoa de contato principal
    private String contato;

    // Quantidade de usuários que acessam o sistema
    private int usuarios;

    // Origem do cliente (ex.: indicação, site, prospecção)
    private String origem;

    // Telefone de contato
    private String telefone;

    // E-mail de contato
    private String email;

    // Sistemas que o cliente utiliza (descrição ou lista)
    private String sistemas;

    // Status atual do cliente (ex.: "Ativo", "Inativo", "Em análise")
    private String status;

    // Banco de dados utilizado pelo cliente (ex.: "SQL Server", "Oracle")
    private String bancoDados;

    // Valor do seguro contratado (armazenado como String, talvez valor monetário)
    private String ValorSeguro;

    /**
     * Construtor que inicializa todos os campos do cliente compartilhado.
     *
     * @param id Identificador único
     * @param tipoNuvem Tipo de nuvem
     * @param pod Número do POD
     * @param dataCriacao Data de criação
     * @param razaoSocial Razão social
     * @param cpfCnpj CPF/CNPJ
     * @param razaoCnpjAntigos Razão/CNPJ antigos
     * @param codAg Código da agência
     * @param pastaRede Pasta de rede
     * @param contato Contato principal
     * @param usuarios Número de usuários
     * @param origem Origem do cliente
     * @param telefone Telefone
     * @param email E-mail
     * @param sistemas Sistemas utilizados
     * @param status Status atual
     * @param bancoDados Banco de dados
     * @param ValorSeguro Valor do seguro
     */
    public ClienteCompartilhado(int id, String tipoNuvem, int pod, String dataCriacao,
            String razaoSocial, String cpfCnpj, String razaoCnpjAntigos,
            String codAg, String pastaRede, String contato, int usuarios,
            String origem, String telefone, String email, String sistemas,
            String status, String bancoDados, String ValorSeguro) {
        // Atribuição dos parâmetros recebidos às variáveis de instância
        this.id = id;
        this.tipoNuvem = tipoNuvem;
        this.pod = pod;
        this.dataCriacao = dataCriacao;
        this.razaoSocial = razaoSocial;
        this.cpfCnpj = cpfCnpj;
        this.razaoCnpjAntigos = razaoCnpjAntigos;
        this.codAg = codAg;
        this.pastaRede = pastaRede;
        this.contato = contato;
        this.usuarios = usuarios;
        this.origem = origem;
        this.telefone = telefone;
        this.email = email;
        this.sistemas = sistemas;
        this.status = status;
        this.bancoDados = bancoDados;
        this.ValorSeguro = ValorSeguro;
    }

    // ----- Métodos Getters (acesso aos dados) -----
    public int getId() {
        return id;
    }

    public String getTipoNuvem() {
        return tipoNuvem;
    }

    public int getPod() {
        return pod;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public String getRazaoCnpjAntigos() {
        return razaoCnpjAntigos;
    }

    public String getCodAg() {
        return codAg;
    }

    public String getPastaRede() {
        return pastaRede;
    }

    public String getContato() {
        return contato;
    }

    public int getUsuarios() {
        return usuarios;
    }

    public String getOrigem() {
        return origem;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public String getSistemas() {
        return sistemas;
    }

    public String getStatus() {
        return status;
    }

    public String getBancoDados() {
        return bancoDados;
    }

    public String getValorSeguro() {
        return ValorSeguro;
    }
}

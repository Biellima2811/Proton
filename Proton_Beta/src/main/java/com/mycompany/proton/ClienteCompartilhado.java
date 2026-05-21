/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proton;

/**
 *
 * @author gabriellevi_forteste
 */
public class ClienteCompartilhado {
    private int id;
    private String tipoNuvem;
    private int pod;
    private String dataCriacao;
    private String razaoSocial;
    private String cpfCnpj;
    private String razaoCnpjAntigos;
    private String codAg;
    private String pastaRede;
    private String contato;
    private int usuarios;
    private String origem;
    private String telefone;
    private String email;
    private String sistemas;
    private String status;
    private String bancoDados;

    public ClienteCompartilhado(int id, String tipoNuvem, int pod, String dataCriacao, String razaoSocial, String cpfCnpj, String razaoCnpjAntigos, String codAg, String pastaRede, String contato, int usuarios, String origem, String telefone, String email, String sistemas, String status, String bancoDados) {
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
    }

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
}

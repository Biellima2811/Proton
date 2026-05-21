/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proton;

/**
 *
 * @author gabriellevi_forteste
 */
public class Cliente {
    private int id;
    private String nome;
    private String cnpj;
    private int qnt_server;
    private String active_directory;
    private String ambiente;
    private boolean vpn;
    private String sgbd;

    public Cliente(int id, String nome, String cnpj, int qnt_server, String active_directory, String ambiente, boolean vpn, String sgbd) {
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.qnt_server = qnt_server;
        this.active_directory = active_directory;
        this.ambiente = ambiente;
        this.vpn = vpn;
        this.sgbd = sgbd;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public int getQnt_server() {
        return qnt_server;
    }

    public String getActive_directory() {
        return active_directory;
    }

    public String getAmbiente() {
        return ambiente;
    }

    public boolean isVpn() {
        return vpn;
    }

    public String getSgbd() {
        return sgbd;
    }
    
}

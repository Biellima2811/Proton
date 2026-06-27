package com.mycompany.proton.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PermissaoService {

    public static Set<String> getAcoesPermitidas(String nivel) {
        Set<String> permissoes = new HashSet<>();
        switch (nivel.toUpperCase()) {
            case "MASTER":
                permissoes.addAll(Arrays.asList("DEDICADOS", "COMPARTILHADOS", "FORTESRH", "CANCELADOS", "CONFIGURACOES", "EXCLUIR", "EDITAR", "CRIAR"));
                break;
            case "N2":
                permissoes.addAll(Arrays.asList("DEDICADOS", "COMPARTILHADOS", "FORTESRH", "CANCELADOS", "EDITAR", "CRIAR"));
                break;
            case "TECNICO":
                permissoes.addAll(Arrays.asList("DEDICADOS", "COMPARTILHADOS", "FORTESRH", "CANCELADOS"));
                break;
            default:
                break;
        }
        return permissoes;
    }
}
package com.mycompany.proton;

import com.mycompany.proton.util.GerenciadorDeLog;

/**
 *
 * @author gabriellevi_forteste
 */
public class Main {
    public static void main(String[] args) {
        // 1. INICIA O GERADOR DE LOGS ANTES DE QUALQUER OUTRA COISA
        GerenciadorDeLog.iniciar();
        App.main(args);
    }
}

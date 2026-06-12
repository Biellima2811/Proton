package com.mycompany.proton.util;

import com.mycompany.proton.App;
import com.mycompany.proton.controller.ConfigBancoController;
import java.io.FileWriter;                     // Escreve caracteres em um arquivo (modo append)
import java.io.IOException;                    // Exceção de entrada/saída
import java.io.PrintWriter;                    // Impressão formatada em um stream de caracteres
import java.sql.Connection;                    // Conexão JDBC com o banco
import java.sql.DriverManager;                 // Gerencia conexões JDBC
import java.sql.PreparedStatement;             // Executa comandos SQL parametrizados
import java.sql.SQLException;                  // Exceção de SQL
import java.sql.Timestamp;                     // Representa data/hora para o banco
import java.time.LocalDateTime;                // Data e hora atuais (Java 8+)
import java.time.format.DateTimeFormatter;     // Formata LocalDateTime para String

/**
 * Utilitário de auditoria centralizada.
 * 
 * Responsável por registrar ações importantes realizadas no sistema Proton.
 * Cada ação é gravada em dois locais:
 *   1. Arquivo local "auditoria_proton.log" (backup offline).
 *   2. Tabela "logs_auditoria" no banco de dados PostgreSQL (consulta na interface).
 * 
 * Uso: LoggerAuditoria.registrar("LOGIN", "Usuário acessou o sistema");
 */
public class LoggerAuditoria {

    /**
     * Registra uma ação de auditoria, capturando automaticamente o usuário logado
     * e a data/hora atual.
     *
     * @param acao     Descrição curta da ação (ex.: "LOGIN", "CADASTRO", "EXCLUSÃO")
     * @param detalhes Informações complementares (ex.: nome do cliente, e-mail)
     */
    public static void registrar(String acao, String detalhes) {
        // Obtém o e-mail do usuário autenticado; se nulo, usa "SISTEMA" (ações automáticas)
        String usuario = App.getUsuarioLogado() != null ? App.getUsuarioLogado() : "SISTEMA";

        // Captura o momento exato em que a ação ocorreu
        LocalDateTime agora = LocalDateTime.now();

        // Formata a data/hora para o padrão brasileiro (ex.: 25/12/2025 14:30:00)
        String dataHoraStr = agora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // ----- 1. Gravação no arquivo local (.log) -----
        // FileWriter com append = true adiciona ao final do arquivo existente
        try (FileWriter fw = new FileWriter("auditoria_proton.log", true);
             PrintWriter pw = new PrintWriter(fw)) {

            // Escreve uma linha formatada com timestamp, usuário, ação e detalhes
            pw.println("[" + dataHoraStr + "] USER: " + usuario +
                       " | ACAO: " + acao + " | DETALHES: " + detalhes);

        } catch (IOException e) {
            // Se o arquivo não puder ser escrito (permissão, disco cheio), registra no console
            System.out.println("Erro ao gravar log local: " + e.getMessage());
        }

        // ----- 2. Gravação no banco de dados PostgreSQL -----
        // Comando SQL para inserir um novo registro na tabela de logs
        String sql = "INSERT INTO logs_auditoria (usuario_email, acao, detalhes, data_hora) VALUES (?, ?, ?, ?)";

        // Monta a URL de conexão usando as configurações do ConfigBancoController
        String urlConexao = String.format("jdbc:postgresql://%s:5432/%s",
                ConfigBancoController.getIpServidor(),
                ConfigBancoController.getNomeBanco());

        // Bloco try-with-resources: garante que conexão e PreparedStatement serão fechados
        try (Connection conn = DriverManager.getConnection(urlConexao,
                                                          ConfigBancoController.getUsuarioBD(),
                                                          ConfigBancoController.getSenhaBD());
             PreparedStatement cmd = conn.prepareStatement(sql)) {

            // Preenche os parâmetros da consulta
            cmd.setString(1, usuario);                       // e-mail do usuário (ou "SISTEMA")
            cmd.setString(2, acao);                          // ação realizada
            cmd.setString(3, detalhes);                      // detalhes complementares
            cmd.setTimestamp(4, Timestamp.valueOf(agora));   // data/hora no formato SQL

            // Executa a inserção no banco
            cmd.executeUpdate();

        } catch (SQLException e) {
            // Em caso de falha na conexão ou na execução, exibe no console
            // (idealmente poderia ser logado em arquivo também)
            System.out.println("Erro ao gravar log no BD: " + e.getMessage());
        }
    }
}
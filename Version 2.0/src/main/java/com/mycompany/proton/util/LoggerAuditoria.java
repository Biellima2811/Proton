package com.mycompany.proton.util;

import com.mycompany.proton.App;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitário de auditoria centralizada.
 *
 * CORREÇÕES APLICADAS:
 * 1. Substituídas as chamadas diretas a DriverManager.getConnection() por
 *    DatabaseConnection.getConnection() — isso garante que todas as conexões
 *    usem as mesmas configurações centralizadas e evita conexões paralelas
 *    não rastreadas ao pool da aplicação.
 * 2. Removidos imports de ConfigBancoController e DriverManager que se tornaram
 *    desnecessários após a correção acima.
 * 3. Adicionado tratamento de SQLException isolado do IOException — antes,
 *    um erro de banco podia mascarar um erro de arquivo e vice-versa.
 */
public class LoggerAuditoria {

    /**
     * Registra uma ação de auditoria em arquivo local e no banco de dados.
     *
     * @param acao     Descrição curta da ação (ex.: "LOGIN", "EXCLUSÃO")
     * @param detalhes Informações complementares sobre o evento
     */
    public static void registrar(String acao, String detalhes) {
        String usuario = App.getUsuarioLogado() != null ? App.getUsuarioLogado() : "SISTEMA";
        LocalDateTime agora = LocalDateTime.now();
        String dataHoraStr = agora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // ----- 1. Gravação no arquivo local (.log) -----
        try (FileWriter fw = new FileWriter("auditoria_proton.log", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("[" + dataHoraStr + "] USER: " + usuario
                    + " | ACAO: " + acao
                    + " | DETALHES: " + detalhes);
        } catch (IOException e) {
            System.out.println("Erro ao gravar log local: " + e.getMessage());
        }

        // ----- 2. Gravação no banco de dados PostgreSQL -----
        // CORREÇÃO: usa DatabaseConnection.getConnection() em vez de
        // DriverManager.getConnection() direto — evita conexões paralelas não rastreadas.
        String sql = "INSERT INTO logs_auditoria (usuario_email, acao, detalhes, data_hora) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {

            cmd.setString(1, usuario);
            cmd.setString(2, acao);
            cmd.setString(3, detalhes);
            cmd.setTimestamp(4, Timestamp.valueOf(agora));
            cmd.executeUpdate();

        } catch (SQLException e) {
            // Falha no banco não deve impedir o funcionamento do sistema
            System.out.println("Erro ao gravar log no BD: " + e.getMessage());
        }
    }
}
package com.mycompany.proton.service;

import com.mycompany.proton.controller.ConfigBancoController;
import com.mycompany.proton.util.GerenciadorDeLog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Serviço de agendamento de backup automático do banco de dados.
 *
 * CORREÇÕES APLICADAS:
 * 1. Delay inicial alterado de 0 para 24 horas.
 *    Com initialDelay=0, o backup era executado imediatamente durante a
 *    inicialização do sistema — antes do usuário logar e antes das configurações
 *    de banco estarem prontas, causando erros silenciosos no log de erro.
 * 2. Adicionado GerenciadorDeLog para registrar resultado do backup no log do sistema.
 * 3. Adicionado tratamento para quando pg_dump não está no PATH do sistema
 *    (erro de IOException ao tentar criar o processo).
 * 4. Thread do scheduler nomeada para facilitar debug ("backup-scheduler").
 */
public class BackupScheduler {

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1, runnable -> {
                Thread t = new Thread(runnable, "backup-scheduler");
                t.setDaemon(true); // Encerra com a JVM sem bloquear o shutdown
                return t;
            });

    /**
     * Inicia o agendamento do backup automático.
     *
     * CORREÇÃO: initialDelay alterado de 0 para 24 — o primeiro backup ocorre
     * 24 horas após a inicialização, não imediatamente.
     */
    public static void iniciarAgendamento() {
        scheduler.scheduleAtFixedRate(
                BackupScheduler::realizarBackupAutomatico,
                24,   // CORREÇÃO: era 0 (executava imediatamente no boot)
                24,
                TimeUnit.HOURS
        );
        GerenciadorDeLog.info("Agendamento de backup automático iniciado. "
                + "Próximo backup em 24 horas.");
    }

    /**
     * Realiza o backup do banco via pg_dump.
     * Registra o resultado no log do sistema.
     */
    private static void realizarBackupAutomatico() {
        String pastaBackup = "C:/Backups/";
        try {
            new java.io.File(pastaBackup).mkdirs();
        } catch (Exception e) {
            GerenciadorDeLog.erro("Não foi possível criar pasta de backup: " + e.getMessage());
            return;
        }

        String nomeArquivo = "backup_automatico_" + LocalDate.now() + ".sql";
        String caminho = pastaBackup + nomeArquivo;

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Program Files\\PostgreSQL\\14\\bin\\pg_dump.exe",
                    "-U", ConfigBancoController.getUsuarioBD(),
                    "-h", ConfigBancoController.getIpServidor(),
                    "-p", "5432",
                    "-F", "c",
                    "-f", caminho,
                    ConfigBancoController.getNomeBanco()
            );
            pb.environment().put("PGPASSWORD", ConfigBancoController.getSenhaBD());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Lê saída do processo para diagnóstico
            StringBuilder saida = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    saida.append(linha).append("\n");
                }
            }

            // Aguarda até 10 minutos pelo backup
            boolean terminou = process.waitFor(10, TimeUnit.MINUTES);

            if (terminou && process.exitValue() == 0) {
                GerenciadorDeLog.info("Backup automático gerado com sucesso: " + caminho);
            } else {
                // CORREÇÃO: Registra o erro no log em vez de apenas imprimir no stderr
                String erro = saida.length() > 0 ? saida.toString() : "Código: " + process.exitValue();
                GerenciadorDeLog.erro("Falha no backup automático: " + erro);
            }

        } catch (java.io.IOException e) {
            // CORREÇÃO: Captura específica para quando pg_dump não está instalado/no PATH
            GerenciadorDeLog.erro("pg_dump não encontrado ou não acessível. "
                    + "Verifique se o PostgreSQL está instalado e no PATH do sistema. "
                    + "Detalhe: " + e.getMessage());
        } catch (Exception e) {
            GerenciadorDeLog.erro("Erro inesperado no backup automático: " + e.getMessage());
        }
    }
}
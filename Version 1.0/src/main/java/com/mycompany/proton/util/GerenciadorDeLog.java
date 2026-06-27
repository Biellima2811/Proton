package com.mycompany.proton.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilitário responsável por capturar toda a saída do console (IDE)
 * e gravar automaticamente em arquivos físicos de log diários.
 */
public class GerenciadorDeLog {

    public static void iniciar() {
        try {
            // 1. Cria a pasta "logs" na raiz de onde o sistema está rodando (.exe ou NetBeans)
            File pastaLogs = new File("logs");
            if (!pastaLogs.exists()) {
                pastaLogs.mkdir();
            }

            // 2. Define o nome do arquivo com a data de hoje (Ex: proton_log_2026-06-09.txt)
            String dataHoje = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            File arquivoLog = new File(pastaLogs, "proton_log_" + dataHoje + ".txt");

            // 3. Prepara para escrever no arquivo sem apagar o que já foi gravado hoje (append = true)
            FileOutputStream fos = new FileOutputStream(arquivoLog, true);

            // 4. Salva a saída original (Console da IDE)
            PrintStream consoleOut = System.out;
            PrintStream consoleErr = System.err;

            // 5. Cria Streams Duplos que escrevem tanto no Arquivo quanto na IDE simultaneamente
            PrintStream saidaDupla = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    try { fos.write(b); consoleOut.write(b); } catch (Exception e) {}
                }
                @Override
                public void write(byte[] b, int off, int len) {
                    try { fos.write(b, off, len); consoleOut.write(b, off, len); } catch (Exception e) {}
                }
            }, true, "UTF-8");

            PrintStream erroDuplo = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    try { fos.write(b); consoleErr.write(b); } catch (Exception e) {}
                }
                @Override
                public void write(byte[] b, int off, int len) {
                    try { fos.write(b, off, len); consoleErr.write(b, off, len); } catch (Exception e) {}
                }
            }, true, "UTF-8");

            // 6. MÁGICA: Redireciona toda a saída do Java para os nossos Streams Duplos!
            System.setOut(saidaDupla);
            System.setErr(erroDuplo);

            // Marca o início da sessão no log visualmente
            System.out.println("\n========================================================");
            System.out.println("▶ SESSÃO INICIADA: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            System.out.println("▶ OS: " + System.getProperty("os.name") + " | Java: " + System.getProperty("java.version"));
            System.out.println("========================================================");

        } catch (Exception e) {
            System.err.println("Aviso: Falha ao iniciar gravação de log em arquivo. " + e.getMessage());
        }
    }

    // ==========================================
    // MÉTODOS FACILITADORES (Helpers)
    // ==========================================
    
    public static void info(String mensagem) {
        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("[" + hora + "] INFO: " + mensagem);
    }

    public static void erro(String mensagem) {
        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.err.println("[" + hora + "] ERRO: " + mensagem);
    }
}
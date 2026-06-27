package com.mycompany.proton;

import com.mycompany.proton.service.BackupScheduler;
import com.mycompany.proton.util.DatabaseMigration;
import com.mycompany.proton.util.GerenciadorDeLog;
import com.mycompany.proton.util.GlobalExceptionHandler;

public class Main {
    public static void main(String[] args) {
        // 1. Inicia os interceptores de erro e logs
        GerenciadorDeLog.iniciar();
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        
        // 2. MAGIA: Valida e atualiza a estrutura do banco de dados sozinha
        DatabaseMigration.executarMigracoes(); 
        
        // 3. Inicia tarefas de background
        BackupScheduler.iniciarAgendamento();
        
        // 4. Abre a interface gráfica
        App.main(args);
    }
}
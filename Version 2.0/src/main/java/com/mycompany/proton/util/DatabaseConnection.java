package com.mycompany.proton.util;

import com.mycompany.proton.controller.ConfigBancoController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilitário de conexão com o banco de dados PostgreSQL.
 *
 * CORREÇÕES APLICADAS:
 * - Removido padrão singleton (campo static Connection connection).
 *   O singleton causava race condition entre transações abertas em DAOs diferentes
 *   (ex: ClienteDedicadoDAO usa setAutoCommit(false) e a mesma conexão poderia
 *   ser reutilizada pelo BackupScheduler em outra thread).
 * - getConnection() agora sempre retorna uma nova conexão independente.
 * - Adicionado método conectar() como alias para manter compatibilidade com
 *   PrimaryController que chamava DatabaseConnection.conectar().
 * - closeConnection() mantido para compatibilidade, mas não é mais necessário
 *   pois os DAOs usam try-with-resources.
 */
public class DatabaseConnection {

    /**
     * Retorna uma nova conexão JDBC com o banco configurado.
     * Cada chamada abre uma conexão nova — use try-with-resources para fechar.
     */
    public static Connection getConnection() throws SQLException {
        String url = String.format("jdbc:postgresql://%s:5432/%s",
                ConfigBancoController.getIpServidor(),
                ConfigBancoController.getNomeBanco());
        return DriverManager.getConnection(
                url,
                ConfigBancoController.getUsuarioBD(),
                ConfigBancoController.getSenhaBD()
        );
    }

    /**
     * Alias de getConnection() para compatibilidade com PrimaryController.
     * CORREÇÃO: PrimaryController chamava DatabaseConnection.conectar() que não existia.
     */
    public static Connection conectar() throws SQLException {
        return getConnection();
    }

    /**
     * Mantido por compatibilidade — com o novo modelo de conexão por demanda,
     * o fechamento deve ser feito via try-with-resources em cada DAO.
     * Este método não faz nada para não interferir com conexões abertas.
     */
    public static void closeConnection() {
        // Não utilizado com o modelo de conexão por demanda.
        // Cada DAO fecha sua própria conexão via try-with-resources.
    }
}
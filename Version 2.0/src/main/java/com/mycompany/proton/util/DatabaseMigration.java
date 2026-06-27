package com.mycompany.proton.util;

import com.mycompany.proton.controller.LoginController;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Motor de Migração de Banco de Dados.
 * Garante que a estrutura do PostgreSQL esteja sempre sincronizada com a versão do .exe.
 */
public class DatabaseMigration {

    public static void executarMigracoes() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Verifica se o banco é "virgem" (não possui a tabela controle_versao)
            boolean bancoVirgem = true;
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "controle_versao", null)) {
                if (rs.next()) {
                    bancoVirgem = false;
                }
            }

            // 2. Se o banco for virgem, roda o Dicionário Completo (V1.0)
            if (bancoVirgem) {
                GerenciadorDeLog.info("Banco de dados vazio detectado. Construindo Dicionário V1.0...");
                executarDicionarioBaseV1(stmt);
            }

            // 3. Descobre em qual versão o banco está agora
            String versaoAtualBanco = "1.0";
            try (ResultSet rs = stmt.executeQuery("SELECT versao_db FROM controle_versao ORDER BY id DESC LIMIT 1")) {
                if (rs.next()) {
                    versaoAtualBanco = rs.getString("versao_db");
                }
            }

            // 4. Compara a versão do Banco com a versão do Sistema (.exe)
            String versaoApp = LoginController.VERSAO_APP; // Ex: "2.0"

            if (versaoAtualBanco.equals("1.0") && versaoApp.equals("2.0")) {
                GerenciadorDeLog.info("Atualizando estrutura do banco de 1.0 para 2.0...");
                executarMigracaoV2(stmt);
                
                // Grava no banco que a atualização para a V2 foi um sucesso!
                String sqlAtualizaVersao = "INSERT INTO controle_versao (versao_db, descricao) VALUES (?, ?)";
                try (PreparedStatement cmd = conn.prepareStatement(sqlAtualizaVersao)) {
                    cmd.setString(1, versaoApp);
                    cmd.setString(2, "Atualização automática para V2.0 (Histórico e Notificações)");
                    cmd.executeUpdate();
                }
            }

            GerenciadorDeLog.info("Banco de dados sincronizado e pronto. Versão atual: v" + versaoApp);

        } catch (Exception e) {
            GerenciadorDeLog.erro("Erro crítico ao sincronizar o Banco de Dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * SCRIPT DE INICIALIZAÇÃO - ERP PROTON (V1 - DADOS COMPLETOS E ALINHADOS)
     */
    private static void executarDicionarioBaseV1(Statement stmt) throws Exception {
        String sqlV1 = 
            "CREATE TABLE IF NOT EXISTS usuarios_sistema (" +
            " id SERIAL PRIMARY KEY, email VARCHAR(255) UNIQUE NOT NULL, senha VARCHAR(255) NOT NULL, " +
            " nivel_acesso VARCHAR(50) DEFAULT 'TECNICO', status VARCHAR(20) DEFAULT 'ATIVO', data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP);" +

            "CREATE TABLE IF NOT EXISTS clientes_dedicados (" +
            " id SERIAL PRIMARY KEY, cliente VARCHAR(255) NOT NULL, cnpj_cpf VARCHAR(50), qnt_de_servs INT, " +
            " ad VARCHAR(100), ambiente VARCHAR(50), vpn BOOLEAN, data_criacao DATE DEFAULT CURRENT_DATE, " +
            " criado_por VARCHAR(255), hora_criacao TIME DEFAULT CURRENT_TIME);" +

            "CREATE TABLE IF NOT EXISTS servidores_clientes_dedicados (" +
            " id SERIAL PRIMARY KEY, cliente_id INTEGER REFERENCES clientes_dedicados(id) ON DELETE CASCADE, " +
            " tipo_servidor VARCHAR(50), ip_servidor VARCHAR(150), usuario VARCHAR(100), senha VARCHAR(150));" +

            "CREATE TABLE IF NOT EXISTS bancos_dedicados (" +
            " id SERIAL PRIMARY KEY, id_cliente INTEGER REFERENCES clientes_dedicados(id) ON DELETE CASCADE, " +
            " sgbd VARCHAR(100), hqbird VARCHAR(50), versao VARCHAR(100), usuario_banco VARCHAR(100), " +
            " senha_banco VARCHAR(255), produtos VARCHAR(500));" +

            "CREATE TABLE IF NOT EXISTS clientes_compartilhados (" +
            " id SERIAL PRIMARY KEY, tipo_nuvem VARCHAR(50), pod INT, data_criacao DATE DEFAULT CURRENT_DATE, " +
            " razao_social VARCHAR(255) NOT NULL, cpf_cnpj VARCHAR(50), razao_cnpj_antigos VARCHAR(255), " +
            " cod_ag VARCHAR(50), pasta_rede VARCHAR(150), contato VARCHAR(100), usuarios INT, origem VARCHAR(100), " +
            " telefone VARCHAR(50), email VARCHAR(150), sistemas VARCHAR(255), status VARCHAR(50), banco VARCHAR(50), " +
            " criado_por VARCHAR(255), hora_criacao TIME DEFAULT CURRENT_TIME, valor_seguro VARCHAR(100) DEFAULT 'N/A');" +

            "CREATE TABLE IF NOT EXISTS bancos_nuvem_compartilhada (" +
            " id SERIAL PRIMARY KEY, id_cliente INTEGER REFERENCES clientes_compartilhados(id) ON DELETE CASCADE, " +
            " sgbd VARCHAR(50), segmento VARCHAR(50), ip_servidor VARCHAR(100), nome_banco VARCHAR(150), " +
            " caminho_banco VARCHAR(255), caminho_conexao VARCHAR(255), versao VARCHAR(50), usuario_banco VARCHAR(100), " +
            " senha_banco VARCHAR(100), razao_social VARCHAR(255));" +

            "CREATE TABLE IF NOT EXISTS usuarios_nuvem_compartilhada (" +
            " id SERIAL PRIMARY KEY, id_cliente INTEGER REFERENCES clientes_compartilhados(id) ON DELETE CASCADE, " +
            " nome_usuario VARCHAR(255), email_usuario VARCHAR(255), perfil VARCHAR(100));" +

            "CREATE TABLE IF NOT EXISTS fortesrh (" +
            " id SERIAL PRIMARY KEY, tipo_ambiente VARCHAR(50), cliente VARCHAR(255) NOT NULL, cnpj_cpf VARCHAR(50), " +
            " url_acesso VARCHAR(255), servidor_app VARCHAR(150), banco_dados VARCHAR(150), pasta_web VARCHAR(150), " +
            " usuario_db VARCHAR(100), senha_db VARCHAR(100), load_balance VARCHAR(50), ip_load_balance VARCHAR(50), " +
            " status VARCHAR(50), data_criacao DATE DEFAULT CURRENT_DATE, ip_publico VARCHAR(50), ip_privado VARCHAR(50), " +
            " versao VARCHAR(50), web_aplication VARCHAR(255), criado_por VARCHAR(255), hora_criacao TIME DEFAULT CURRENT_TIME);" +

            "CREATE TABLE IF NOT EXISTS clientes_cancelados (" +
            " id SERIAL PRIMARY KEY, tipo_nuvem VARCHAR(50), pod INT, data_criacao DATE DEFAULT CURRENT_DATE, " +
            " cliente_razao VARCHAR(255) NOT NULL, status_antigo VARCHAR(50), inicio_cancelamento DATE, " +
            " final_cancelamento DATE, chamado VARCHAR(100), tecnico_responsavel VARCHAR(255), criado_por VARCHAR(255), " +
            " hora_criacao TIME DEFAULT CURRENT_TIME);" +

            "CREATE TABLE IF NOT EXISTS logs_auditoria (" +
            " id SERIAL PRIMARY KEY, usuario_email VARCHAR(255), acao VARCHAR(100), detalhes TEXT, " +
            " data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP);" +

            "CREATE TABLE IF NOT EXISTS controle_versao (" +
            " id SERIAL PRIMARY KEY, versao_db VARCHAR(50) NOT NULL, data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            " descricao TEXT);" +

            "CREATE TABLE IF NOT EXISTS webservices_compartilhados (" +
            " id SERIAL PRIMARY KEY, id_cliente INTEGER REFERENCES clientes_compartilhados(id) ON DELETE CASCADE, " +
            " integracao VARCHAR(150), servidor_ws VARCHAR(100), porta VARCHAR(20), endereco_soap VARCHAR(255), " +
            " endereco_wsdl VARCHAR(255), string_conexao_bd VARCHAR(255), usuario_bd VARCHAR(100), senha_bd VARCHAR(100));" +

            // Inserções Iniciais
            "INSERT INTO controle_versao (versao_db, descricao) VALUES ('1.0', 'Lançamento Inicial - Proton ERP V1');" +
            
            // ATENÇÃO: As senhas abaixo estão em TEXTO PURO. Se o seu sistema exige Bcrypt no primeiro login, ok.
            // Se exigir BCrypt direto, você deve colocar a string de Hash aqui em vez de 'fortes123'
            "INSERT INTO usuarios_sistema (email, senha, nivel_acesso) VALUES " +
            "('gabriellevi@fortestecnologia.com.br', '$2a$12$7k4uLzVb.M5D7/0qj.9G/uL5gH9x0yG1c1tXvT.1zZqZqZqZqZqZq', 'MASTER'), " +
            "('pauloteixeira@fortestecnologia.com.br', 'fortes123', 'MASTER'), " +
            "('wcordeiro@fortestecnologia.com.br', 'fortes123', 'MASTER'), " +
            "('vivianlima@fortestecnologia.com.br', 'fortes123', 'N2'), " +
            "('kenedysoares@fortestecnologia.com.br', 'fortes123', 'N2'), " +
            "('damiaosilva@fortestecnologia.com.br', 'fortes123', 'N2');";

        stmt.execute(sqlV1);
    }

    /**
     * SCRIPT DE ATUALIZAÇÃO PARA V2.0 (Executado apenas se o banco estiver na V1.0)
     */
    private static void executarMigracaoV2(Statement stmt) throws Exception {
        String sqlV2 = 
            "CREATE TABLE IF NOT EXISTS historico_alteracoes (" +
            " id SERIAL PRIMARY KEY, tabela VARCHAR(100), registro_id INTEGER, campo VARCHAR(100), " +
            " valor_antigo TEXT, valor_novo TEXT, usuario VARCHAR(255), data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP);" +
            
            "CREATE TABLE IF NOT EXISTS notificacoes (" +
            " id SERIAL PRIMARY KEY, mensagem TEXT, lida BOOLEAN DEFAULT FALSE, " +
            " data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        stmt.execute(sqlV2);
    }
}
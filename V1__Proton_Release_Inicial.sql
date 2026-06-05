-- ===================================================================================
-- SCRIPT DE INICIALIZAÇÃO - ERP PROTON (V1 - DADOS COMPLETOS E ALINHADOS)
-- ===================================================================================

CREATE TABLE IF NOT EXISTS usuarios_sistema (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    nivel_acesso VARCHAR(50) DEFAULT 'TECNICO',
    status VARCHAR(20) DEFAULT 'ATIVO',
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clientes_dedicados (
    id SERIAL PRIMARY KEY,
    cliente VARCHAR(255) NOT NULL,
    cnpj_cpf VARCHAR(50),
    qnt_de_servs INT,
    ad VARCHAR(100),
    ambiente VARCHAR(50),
    vpn BOOLEAN,
    data_criacao DATE DEFAULT CURRENT_DATE,
    criado_por VARCHAR(255),
    hora_criacao TIME DEFAULT CURRENT_TIME
);

CREATE TABLE IF NOT EXISTS servidores_clientes_dedicados (
    id SERIAL PRIMARY KEY,
    cliente_id INTEGER REFERENCES clientes_dedicados(id) ON DELETE CASCADE,
    tipo_servidor VARCHAR(50), 
    ip_servidor VARCHAR(150),
    usuario VARCHAR(100),
    senha VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS bancos_dedicados (
    id SERIAL PRIMARY KEY,
    id_cliente INTEGER REFERENCES clientes_dedicados(id) ON DELETE CASCADE,
    sgbd VARCHAR(100),
    hqbird VARCHAR(50),
    versao VARCHAR(100),
    usuario_banco VARCHAR(100),
    senha_banco VARCHAR(255),
    produtos VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS clientes_compartilhados (
    id SERIAL PRIMARY KEY,
    tipo_nuvem VARCHAR(50),
    pod INT,
    data_criacao DATE DEFAULT CURRENT_DATE,
    razao_social VARCHAR(255) NOT NULL,
    cpf_cnpj VARCHAR(50),
    razao_cnpj_antigos VARCHAR(255),
    cod_ag VARCHAR(50),
    pasta_rede VARCHAR(150),
    contato VARCHAR(100),
    usuarios INT,
    origem VARCHAR(100),
    telefone VARCHAR(50),
    email VARCHAR(150),
    sistemas VARCHAR(255),
    status VARCHAR(50),
    banco VARCHAR(50),
    criado_por VARCHAR(255),
    hora_criacao TIME DEFAULT CURRENT_TIME
);

-- CORRIGIDO: Agora possui ip_servidor, nome_banco, caminho_conexao
CREATE TABLE IF NOT EXISTS bancos_nuvem_compartilhada (
    id SERIAL PRIMARY KEY,
    id_cliente INTEGER REFERENCES clientes_compartilhados(id) ON DELETE CASCADE,
    sgbd VARCHAR(50),
    segmento VARCHAR(50),
    ip_servidor VARCHAR(100),
    nome_banco VARCHAR(150),
    caminho_banco VARCHAR(255),
    caminho_conexao VARCHAR(255),
    versao VARCHAR(50),
    usuario_banco VARCHAR(100),
    senha_banco VARCHAR(100),
    razao_social VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS usuarios_nuvem_compartilhada (
    id SERIAL PRIMARY KEY,
    id_cliente INTEGER REFERENCES clientes_compartilhados(id) ON DELETE CASCADE,
    nome_usuario VARCHAR(255),
    email_usuario VARCHAR(255),
    perfil VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS fortesrh (
    id SERIAL PRIMARY KEY,
    tipo_ambiente VARCHAR(50),
    cliente VARCHAR(255) NOT NULL,
    cnpj_cpf VARCHAR(50),
    url_acesso VARCHAR(255),
    servidor_app VARCHAR(150),
    banco_dados VARCHAR(150),
    pasta_web VARCHAR(150),
    usuario_db VARCHAR(100),
    senha_db VARCHAR(100),
    load_balance VARCHAR(50),
    ip_load_balance VARCHAR(50),
    status VARCHAR(50),
    data_criacao DATE DEFAULT CURRENT_DATE,
    ip_publico VARCHAR(50),
    ip_privado VARCHAR(50),
    versao VARCHAR(50),
    web_aplication VARCHAR(255),
    criado_por VARCHAR(255),
    hora_criacao TIME DEFAULT CURRENT_TIME
);

CREATE TABLE IF NOT EXISTS clientes_cancelados (
    id SERIAL PRIMARY KEY,
    tipo_nuvem VARCHAR(50),
    pod INT,
    data_criacao DATE DEFAULT CURRENT_DATE,
    cliente_razao VARCHAR(255) NOT NULL,
    status_antigo VARCHAR(50),
    inicio_cancelamento DATE,
    final_cancelamento DATE,
    chamado VARCHAR(100),
    tecnico_responsavel VARCHAR(255),
    criado_por VARCHAR(255),
    hora_criacao TIME DEFAULT CURRENT_TIME
);

INSERT INTO usuarios_sistema (email, senha, nivel_acesso) VALUES 
('gabriellevi@fortestecnologia.com.br', 'fortes123', 'MASTER'),
('pauloteixeira@fortestecnologia.com.br', 'fortes123', 'MASTER'),
('wcordeiro@fortestecnologia.com.br', 'fortes123', 'MASTER'),
('vivianlima@fortestecnologia.com.br', 'fortes123', 'N2'),
('kenedysoares@fortestecnologia.com.br', 'fortes123', 'N2'),
('damiaosilva@fortestecnologia.com.br', 'fortes123', 'N2');

ALTER TABLE clientes_compartilhados ADD COLUMN valor_seguro VARCHAR(100) DEFAULT 'N/A';

-- Define metade dos clientes como "Contábil"
UPDATE bancos_nuvem_compartilhada 
SET segmento = 'Contábil' 
WHERE id % 2 = 0;

-- Define a outra metade como "Corporativo"
UPDATE bancos_nuvem_compartilhada 
SET segmento = 'Corporativo' 
WHERE id % 2 != 0;


CREATE TABLE IF NOT EXISTS logs_auditoria (
    id SERIAL PRIMARY KEY,
    usuario_email VARCHAR(255),
    acao VARCHAR(100),
    detalhes TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS controle_versao (
    id SERIAL PRIMARY KEY,
    versao_db VARCHAR(50) NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    descricao TEXT
);

-- Registrando a versão inicial do banco de dados
INSERT INTO controle_versao (versao_db, descricao) 
VALUES ('1.0', 'Lançamento Inicial - Proton ERP V1');

CREATE TABLE IF NOT EXISTS controle_versao (
    id SERIAL PRIMARY KEY,
    versao_db VARCHAR(50) NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    descricao TEXT
);

-- Registrando a versão inicial do banco de dados
INSERT INTO controle_versao (versao_db, descricao) 
VALUES ('1.0', 'Lançamento Inicial - Proton ERP V1');
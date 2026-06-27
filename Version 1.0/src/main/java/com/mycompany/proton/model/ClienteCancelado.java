package com.mycompany.proton.model;

/**
 * Representa um cliente que passou pelo processo de cancelamento no sistema Proton.
 * Armazena dados sobre o tipo de nuvem, POD, datas de criação e cancelamento,
 * status anterior, chamado associado e técnico responsável pela operação.
 *
 * @author gabriellevi_forteste
 */
public class ClienteCancelado {

    // Identificador único do registro de cancelamento (chave primária)
    private int id;

    // Tipo de nuvem utilizada pelo cliente (ex.: "Azure", "AWS", "Google Cloud")
    private String tipo_nuvem;

    // Número do POD (Point of Delivery) – unidade de datacenter onde o cliente estava alocado
    private int pod;

    // Data de criação do cliente no sistema (formato String, ex.: "dd/MM/yyyy")
    private String data_criacao;

    // Razão social do cliente que foi cancelado
    private String cliente_razao;

    // Status que o cliente possuía antes de iniciar o cancelamento
    private String status_antigo;

    // Data em que o processo de cancelamento foi iniciado
    private String inicio_cancelamento;

    // Data em que o cancelamento foi finalizado (concluído)
    private String final_cancelamento;

    // Número ou identificador do chamado que originou o cancelamento
    private String chamado;

    // Nome do técnico responsável por executar o cancelamento
    private String tecnico_responsavel;

    /**
     * Construtor que inicializa todos os campos do cliente cancelado.
     *
     * @param id                  Identificador do registro
     * @param tipo_nuvem          Tipo de nuvem
     * @param pod                 Número do POD
     * @param data_criacao        Data de criação original
     * @param cliente_razao       Razão social do cliente
     * @param status_antigo       Status antes do cancelamento
     * @param inicio_cancelamento Data de início do cancelamento
     * @param final_cancelamento  Data de conclusão do cancelamento
     * @param chamado             Chamado associado
     * @param tecnico_responsavel Técnico responsável
     */
    public ClienteCancelado(int id, String tipo_nuvem, int pod, String data_criacao,
                            String cliente_razao, String status_antigo,
                            String inicio_cancelamento, String final_cancelamento,
                            String chamado, String tecnico_responsavel) {
        // Atribuição dos parâmetros aos campos correspondentes
        this.id = id;
        this.tipo_nuvem = tipo_nuvem;
        this.pod = pod;
        this.data_criacao = data_criacao;
        this.cliente_razao = cliente_razao;
        this.status_antigo = status_antigo;
        this.inicio_cancelamento = inicio_cancelamento;
        this.final_cancelamento = final_cancelamento;
        this.chamado = chamado;
        this.tecnico_responsavel = tecnico_responsavel;
    }

    // ----- Getters (métodos de acesso) -----

    /**
     * @return O ID do registro de cancelamento
     */
    public int getId() {
        return id;
    }

    /**
     * @return O tipo de nuvem utilizada
     */
    public String getTipo_nuvem() {
        return tipo_nuvem;
    }

    /**
     * @return O número do POD
     */
    public int getPod() {
        return pod;
    }

    /**
     * @return A data de criação do cliente no sistema
     */
    public String getData_criacao() {
        return data_criacao;
    }

    /**
     * @return A razão social do cliente cancelado
     */
    public String getCliente_razao() {
        return cliente_razao;
    }

    /**
     * @return O status que o cliente possuía antes do cancelamento
     */
    public String getStatus_antigo() {
        return status_antigo;
    }

    /**
     * @return A data de início do processo de cancelamento
     */
    public String getInicio_cancelamento() {
        return inicio_cancelamento;
    }

    /**
     * @return A data de finalização do cancelamento
     */
    public String getFinal_cancelamento() {
        return final_cancelamento;
    }

    /**
     * @return O chamado que motivou o cancelamento
     */
    public String getChamado() {
        return chamado;
    }

    /**
     * @return O nome do técnico responsável pelo cancelamento
     */
    public String getTecnico_responsavel() {
        return tecnico_responsavel;
    }
}
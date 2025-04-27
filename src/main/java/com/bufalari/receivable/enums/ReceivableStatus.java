// Path: src/main/java/com/bufalari/receivable/enums/ReceivableStatus.java
package com.bufalari.receivable.enums;

/**
 * Enum representing the possible statuses of an account receivable.
 * Enum representando os possíveis status de uma conta a receber.
 */
public enum ReceivableStatus {
    PENDING("Pending", "Pendente"),               // Invoice issued, waiting for payment / Fatura emitida, aguardando pagamento
    RECEIVED("Received", "Recebido"),             // Full payment received / Pagamento integral recebido
    PARTIALLY_RECEIVED("Partially Received", "Parcialmente Recebido"), // Partial payment received / Pagamento parcial recebido
    OVERDUE("Overdue", "Atrasado"),               // Past due date, not fully paid / Vencido, não totalmente pago
    IN_DISPUTE("In Dispute", "Em Disputa"),       // Client is disputing the charge / Cliente está contestando a cobrança
    WRITTEN_OFF("Written Off", "Baixado"),        // Deemed uncollectible / Considerado incobrável
    CANCELED("Canceled", "Cancelado");            // Invoice canceled before payment / Fatura cancelada antes do pagamento

    private final String descriptionEn;
    private final String descriptionPt;

    ReceivableStatus(String en, String pt) {
        this.descriptionEn = en;
        this.descriptionPt = pt;
    }

    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionPt() { return descriptionPt; }
}
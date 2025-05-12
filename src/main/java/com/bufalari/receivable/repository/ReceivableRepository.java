package com.bufalari.receivable.repository;

import com.bufalari.receivable.entity.ReceivableEntity;
import com.bufalari.receivable.enums.ReceivableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Spring Data JPA repository for Receivable entities (with UUID primary key).
 * Repositório Spring Data JPA para entidades Receivable (com chave primária UUID).
 */
@Repository
public interface ReceivableRepository extends JpaRepository<ReceivableEntity, UUID> { // <<<--- UUID ID Type

    /**
     * Finds all receivables for a specific client.
     * Encontra todas as contas a receber para um cliente específico.
     * @param clientId The UUID of the client. / O UUID do cliente.
     * @return List of receivables for the client. / Lista de contas a receber do cliente.
     */
    List<ReceivableEntity> findByClientId(UUID clientId); // <<<--- Changed to UUID

    /**
     * Finds all receivables for a specific project.
     * Encontra todas as contas a receber para um projeto específico.
     * @param projectId The ID of the project. / O ID do projeto.
     * @return List of receivables for the project. / Lista de contas a receber do projeto.
     */
    List<ReceivableEntity> findByProjectId(Long projectId); // Remains Long

    /**
     * Finds all receivables with a specific status.
     * Encontra todas as contas a receber com um status específico.
     * @param status The status to filter by. / O status para filtrar.
     * @return List of receivables with the given status. / Lista de contas a receber com o status fornecido.
     */
    List<ReceivableEntity> findByStatus(ReceivableStatus status);

    /**
     * Finds all receivables with a due date before a certain date and not in the specified statuses (e.g., find overdue).
     * Encontra todas as contas a receber com data de vencimento anterior a uma certa data e que não estão nos status especificados (ex: encontrar atrasadas).
     * @param date The date to compare the due date against. / A data para comparar o vencimento.
     * @param excludedStatuses List of statuses to exclude (e.g., RECEIVED, WRITTEN_OFF, CANCELED). / Lista de status a excluir (ex: RECEBIDO, BAIXADO, CANCELADO).
     * @return List of overdue receivables. / Lista de contas a receber atrasadas.
     */
    List<ReceivableEntity> findByDueDateBeforeAndStatusNotIn(LocalDate date, List<ReceivableStatus> excludedStatuses);

    /**
     * Finds all receivables that have a blocker reason set (useful for recovery tracking).
     * Finds where blockerReason is not null AND not an empty string.
     * Encontra todas as contas a receber que têm um motivo de bloqueio definido (útil para rastreamento de recuperação).
     * Encontra onde blockerReason não é nulo E não é uma string vazia.
     * @return List of receivables with blockers. / Lista de contas a receber com bloqueios.
     */
    List<ReceivableEntity> findByBlockerReasonIsNotNullAndBlockerReasonNot(String emptyReason);

    // Convenience method using the above for cleaner service layer calls
    // Método de conveniência usando o acima para chamadas mais limpas na camada de serviço
    default List<ReceivableEntity> findWithBlockers() {
        return findByBlockerReasonIsNotNullAndBlockerReasonNot(""); // Find where reason is not null and not empty string
    }

}
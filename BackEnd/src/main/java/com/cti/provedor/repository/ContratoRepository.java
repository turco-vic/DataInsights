package com.cti.provedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cti.provedor.model.Cliente;
import com.cti.provedor.model.Contrato;
import com.cti.provedor.model.Servico;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    boolean existsByClienteAndServico(Cliente cliente, Servico servico);

    List<Contrato> findByClienteId(Long clienteId);

    long countByServicoId(Long servicoId);

    long countByAtivoTrue();

    long countByClienteIdAndAtivoTrue(Long clienteId);
    /** Pares [clienteId, contratos ativos], numa consulta só (evita uma consulta por cliente). */
    @Query("select c.cliente.id, count(c) from Contrato c where c.ativo = true group by c.cliente.id")
    List<Object[]> contarAtivosPorCliente();

    void deleteByClienteId(Long clienteId);

    /** Pares [servicoId, quantidade de contratos]. */
    @Query("select c.servico.id, count(c) from Contrato c group by c.servico.id")
    List<Object[]> contarPorServico();

    /** Pares [nome do serviço, contratos ativos], do mais contratado para o menos. */
    @Query("select s.nome, count(c) from Contrato c join c.servico s where c.ativo = true group by s.nome order by count(c) desc")
    List<Object[]> rankingServicosAtivos();
}

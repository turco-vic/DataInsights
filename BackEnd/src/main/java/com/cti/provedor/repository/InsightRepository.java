package com.cti.provedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cti.provedor.model.Insight;

public interface InsightRepository extends JpaRepository<Insight, Long> {

    List<Insight> findAllByOrderByIdDesc();

    List<Insight> findByClienteIdOrderByIdDesc(Long clienteId);

    boolean existsByClienteIdAndTipo(Long clienteId, String tipo);
    /** Pares [clienteId, tipo] dos insights já existentes, numa consulta só. */
    @Query("select i.cliente.id, i.tipo from Insight i where i.cliente is not null")
    List<Object[]> clientesETipos();

    void deleteByClienteId(Long clienteId);

    void deleteByContratoId(Long contratoId);

    void deleteByContratoClienteId(Long clienteId);
}

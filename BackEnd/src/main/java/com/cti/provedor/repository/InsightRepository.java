package com.cti.provedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Insight;

public interface InsightRepository extends JpaRepository<Insight, Long> {

    List<Insight> findByClienteId(Long clienteId);

    void deleteByClienteId(Long clienteId);

    void deleteByContratoId(Long contratoId);

    void deleteByContratoClienteId(Long clienteId);
}

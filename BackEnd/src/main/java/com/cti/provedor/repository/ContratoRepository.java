package com.cti.provedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Cliente;
import com.cti.provedor.model.Contrato;
import com.cti.provedor.model.Servico;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    boolean existsByClienteAndServico(Cliente cliente, Servico servico);

    List<Contrato> findByClienteId(Long clienteId);

    long countByServicoId(Long servicoId);

    void deleteByClienteId(Long clienteId);
}

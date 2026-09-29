package com.cti.provedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Cliente;
import com.cti.provedor.model.Contrato;
import com.cti.provedor.model.Servico;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    boolean existsByClienteAndServico(Cliente cliente, Servico servico);
}

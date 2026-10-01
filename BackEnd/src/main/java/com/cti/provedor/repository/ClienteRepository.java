package com.cti.provedor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCodigo(String codigo);

    boolean existsByEmailIgnoreCaseAndCodigoNot(String email, String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    long countByConsultorId(Long consultorId);
}

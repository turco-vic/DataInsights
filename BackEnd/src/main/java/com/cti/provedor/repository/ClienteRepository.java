package com.cti.provedor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.cti.provedor.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    Optional<Cliente> findByCodigo(String codigo);

    boolean existsByEmailIgnoreCaseAndCodigoNot(String email, String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    long countByConsultorId(Long consultorId);

    /** Pares [consultorId, quantidade de clientes]. */
    @Query("select c.consultor.id, count(c) from Cliente c where c.consultor is not null group by c.consultor.id")
    List<Object[]> contarPorConsultor();
}

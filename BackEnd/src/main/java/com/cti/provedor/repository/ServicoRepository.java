package com.cti.provedor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Servico;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    Optional<Servico> findFirstByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}

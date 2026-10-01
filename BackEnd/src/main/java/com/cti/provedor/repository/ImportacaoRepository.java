package com.cti.provedor.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Importacao;

public interface ImportacaoRepository extends JpaRepository<Importacao, Long> {

    List<Importacao> findAllByOrderByEnviadaEmDesc();

    Optional<Importacao> findFirstByOrderByEnviadaEmDesc();

    List<Importacao> findByEnviadaEmAfterOrderByEnviadaEmDesc(LocalDateTime inicio);
}

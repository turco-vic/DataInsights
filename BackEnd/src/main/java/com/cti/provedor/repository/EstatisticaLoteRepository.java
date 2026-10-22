package com.cti.provedor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.EstatisticaLote;

public interface EstatisticaLoteRepository extends JpaRepository<EstatisticaLote, Long> {

    Optional<EstatisticaLote> findByLoteId(Long loteId);

    void deleteByLoteId(Long loteId);
}

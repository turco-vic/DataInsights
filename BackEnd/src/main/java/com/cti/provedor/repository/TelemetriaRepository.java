package com.cti.provedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Telemetria;

public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {

    List<Telemetria> findByClienteIdOrderByDataHoraDesc(Long clienteId);

    void deleteByClienteId(Long clienteId);
}

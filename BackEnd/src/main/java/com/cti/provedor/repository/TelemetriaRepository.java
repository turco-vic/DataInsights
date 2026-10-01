package com.cti.provedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Telemetria;

public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {

    List<Telemetria> findByClienteIdOrderByDataHoraDesc(Long clienteId);

    List<Telemetria> findTop500ByOrderByDataHoraDesc();

    List<Telemetria> findTop500ByEventoOrderByDataHoraDesc(String evento);

    void deleteByClienteId(Long clienteId);
}

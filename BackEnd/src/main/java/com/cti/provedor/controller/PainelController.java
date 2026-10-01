package com.cti.provedor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.dto.DashboardDTO;
import com.cti.provedor.dto.ResumoDTOs;
import com.cti.provedor.repository.ClienteRepository;
import com.cti.provedor.repository.ImportacaoRepository;
import com.cti.provedor.service.DashboardService;

@RestController
public class PainelController {

    private final DashboardService dashboardService;
    private final ImportacaoRepository importacaoRepository;
    private final ClienteRepository clienteRepository;

    public PainelController(DashboardService dashboardService, ImportacaoRepository importacaoRepository,
            ClienteRepository clienteRepository) {
        this.dashboardService = dashboardService;
        this.importacaoRepository = importacaoRepository;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/api/dashboard")
    public DashboardDTO dashboard() {
        return dashboardService.calcular();
    }

    /** Números da tela de login (sem dados de clientes, só totais). */
    @GetMapping("/api/publico/estatisticas")
    public ResumoDTOs.EstatisticasPublicas estatisticas() {
        return new ResumoDTOs.EstatisticasPublicas(importacaoRepository.count(), clienteRepository.count());
    }
}

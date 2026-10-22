package com.cti.provedor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.dto.DashboardDTO;
import com.cti.provedor.dto.EstatisticaRespostaDTO;
import com.cti.provedor.dto.ResumoDTOs;
import com.cti.provedor.repository.ClienteRepository;
import com.cti.provedor.repository.ImportacaoRepository;
import com.cti.provedor.service.DashboardService;
import com.cti.provedor.service.ImportacaoService;

@RestController
public class PainelController {

    private final DashboardService dashboardService;
    private final ImportacaoRepository importacaoRepository;
    private final ClienteRepository clienteRepository;
    private final ImportacaoService importacaoService;

    public PainelController(DashboardService dashboardService, ImportacaoRepository importacaoRepository,
            ClienteRepository clienteRepository, ImportacaoService importacaoService) {
        this.dashboardService = dashboardService;
        this.importacaoRepository = importacaoRepository;
        this.clienteRepository = clienteRepository;
        this.importacaoService = importacaoService;
    }

    @GetMapping("/api/dashboard")
    public DashboardDTO dashboard() {
        return dashboardService.calcular();
    }

    /** Estatísticas calculadas pelo Python para uma planilha (sem loteId: a mais recente). */
    @GetMapping("/api/estatisticas")
    public EstatisticaRespostaDTO estatisticas(@RequestParam(required = false) Long loteId) {
        return importacaoService.estatisticas(loteId);
    }

    /** Números da tela de login (sem dados de clientes, só totais). */
    @GetMapping("/api/publico/estatisticas")
    public ResumoDTOs.EstatisticasPublicas estatisticas() {
        return new ResumoDTOs.EstatisticasPublicas(importacaoRepository.count(), clienteRepository.count());
    }
}

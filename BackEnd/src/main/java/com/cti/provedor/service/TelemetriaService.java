package com.cti.provedor.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.TelemetriaDTO;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.model.Telemetria;
import com.cti.provedor.repository.TelemetriaRepository;

@Service
public class TelemetriaService {

    private final TelemetriaRepository repository;
    private final ClienteService clienteService;

    public TelemetriaService(TelemetriaRepository repository, ClienteService clienteService) {
        this.repository = repository;
        this.clienteService = clienteService;
    }

    /** Sem filtro de cliente, devolve só os 500 eventos mais recentes (opcionalmente de um tipo). */
    public List<Telemetria> listar(Long clienteId, String evento) {
        if (clienteId != null) {
            return repository.findByClienteIdOrderByDataHoraDesc(clienteId);
        }
        return evento == null || evento.isBlank()
                ? repository.findTop500ByOrderByDataHoraDesc()
                : repository.findTop500ByEventoOrderByDataHoraDesc(evento);
    }

    public Telemetria buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Telemetria não encontrada"));
    }

    @Transactional
    public Telemetria criar(TelemetriaDTO dto) {
        Telemetria telemetria = new Telemetria();
        preencher(telemetria, dto);
        return repository.save(telemetria);
    }

    @Transactional
    public Telemetria atualizar(Long id, TelemetriaDTO dto) {
        Telemetria telemetria = buscarPorId(id);
        preencher(telemetria, dto);
        return repository.save(telemetria);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarPorId(id));
    }

    private void preencher(Telemetria telemetria, TelemetriaDTO dto) {
        telemetria.setEvento(dto.getEvento().trim());
        telemetria.setDataHora(dto.getDataHora() == null ? LocalDateTime.now() : dto.getDataHora());
        telemetria.setMensagem(dto.getMensagem());
        telemetria.setCliente(dto.getClienteId() == null ? null : clienteService.buscarPorId(dto.getClienteId()));
    }
}

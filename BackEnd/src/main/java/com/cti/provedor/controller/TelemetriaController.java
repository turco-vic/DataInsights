package com.cti.provedor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.dto.TelemetriaDTO;
import com.cti.provedor.model.Telemetria;
import com.cti.provedor.service.TelemetriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/telemetrias")
public class TelemetriaController {

    private final TelemetriaService service;

    public TelemetriaController(TelemetriaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Telemetria criar(@Valid @RequestBody TelemetriaDTO dto) {
        return service.criar(dto);
    }

    @GetMapping
    public List<Telemetria> listar(@RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String evento) {
        return service.listar(clienteId, evento);
    }

    @GetMapping("/{id}")
    public Telemetria buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Telemetria atualizar(@PathVariable Long id, @Valid @RequestBody TelemetriaDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.dto.ConsultorDTO;
import com.cti.provedor.model.Consultor;
import com.cti.provedor.service.ConsultorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consultores")
public class ConsultorController {

    private final ConsultorService service;

    public ConsultorController(ConsultorService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Consultor criar(@Valid @RequestBody ConsultorDTO dto) {
        return service.criar(dto);
    }

    @GetMapping
    public List<Consultor> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Consultor buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Consultor atualizar(@PathVariable Long id, @Valid @RequestBody ConsultorDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}

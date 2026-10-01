package com.cti.provedor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.config.UsuarioLogado;
import com.cti.provedor.dto.ImportacaoDTO;
import com.cti.provedor.dto.ImportacaoRespostaDTOs;
import com.cti.provedor.service.ImportacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/importacoes")
public class ImportacaoController {

    private final ImportacaoService service;

    public ImportacaoController(ImportacaoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImportacaoRespostaDTOs.Detalhe importar(@AuthenticationPrincipal UsuarioLogado logado,
            @Valid @RequestBody ImportacaoDTO dto) {
        return service.registrar(dto, logado);
    }

    @GetMapping
    public List<ImportacaoRespostaDTOs.Resumo> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ImportacaoRespostaDTOs.Detalhe buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}

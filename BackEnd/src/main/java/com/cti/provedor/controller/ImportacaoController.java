package com.cti.provedor.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    /** Dataset limpo da planilha em CSV, para a etapa de Ciência de Dados (pandas.read_csv, por exemplo). */
    @GetMapping("/{id}/csv")
    public ResponseEntity<String> exportarCsv(@PathVariable Long id) {
        String csv = service.exportarCsv(id);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(service.nomeCsv(id), StandardCharsets.UTF_8).build().toString())
                .body(csv);
    }

    /** Envia a planilha de novo ao serviço Python (aula de Ciência de Dados). */
    @PostMapping("/{id}/etl")
    public ImportacaoRespostaDTOs.Resumo reprocessar(@PathVariable Long id) {
        return service.reprocessar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}

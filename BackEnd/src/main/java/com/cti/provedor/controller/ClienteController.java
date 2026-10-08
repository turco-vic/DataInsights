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

import com.cti.provedor.dto.ClienteDTO;
import com.cti.provedor.dto.ClienteImportacaoDTO;
import com.cti.provedor.dto.ImportacaoResultadoDTO;
import com.cti.provedor.model.Cliente;
import com.cti.provedor.service.ClienteService;
import com.cti.provedor.service.ClienteValidacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;
    private final ClienteValidacaoService validacao;

    public ClienteController(ClienteService service, ClienteValidacaoService validacao) {
        this.service = service;
        this.validacao = validacao;
    }

    /**
     * Valida e padroniza a lista sem gravar nada (aula de validação no backend).
     * Devolve a lista preparada, ou 400 com todas as inconsistências encontradas.
     * Não usa @Valid: a padronização ("IND." → "Indústria", "sp" → "SP") precisa acontecer
     * antes das anotações do DTO, e o ClienteValidacaoService valida item por item depois dela.
     */
    @PostMapping("/validar")
    public List<ClienteImportacaoDTO> validar(@RequestBody List<ClienteImportacaoDTO> clientes) {
        return validacao.validarEPreparar(clientes);
    }

    /** Importa só se o lote inteiro estiver consistente; com qualquer inconsistência, nada é gravado. */
    @PostMapping("/importar")
    public ImportacaoResultadoDTO importar(@RequestBody List<ClienteImportacaoDTO> dados) {
        return service.importar(dados);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente criar(@Valid @RequestBody ClienteDTO dto) {
        return service.criar(dto);
    }

    @GetMapping
    public List<Cliente> listar(@RequestParam(required = false) String busca,
            @RequestParam(required = false) String segmento,
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) Long consultorId) {
        return service.listar(busca, segmento, nivel, uf, consultorId);
    }

    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable Long id, @Valid @RequestBody ClienteDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}

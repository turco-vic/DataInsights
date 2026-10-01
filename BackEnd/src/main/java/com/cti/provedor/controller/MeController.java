package com.cti.provedor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.config.UsuarioLogado;
import com.cti.provedor.dto.PerfilDTO;
import com.cti.provedor.dto.PerfilRespostaDTO;
import com.cti.provedor.dto.SenhaDTOs;
import com.cti.provedor.service.PerfilService;

import jakarta.validation.Valid;

/** Perfil e senha de quem está logado. */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final PerfilService service;

    public MeController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    public PerfilRespostaDTO obter(@AuthenticationPrincipal UsuarioLogado logado) {
        return service.obter(logado);
    }

    @PutMapping
    public PerfilRespostaDTO atualizar(@AuthenticationPrincipal UsuarioLogado logado, @Valid @RequestBody PerfilDTO dto) {
        return service.atualizar(logado, dto);
    }

    @PutMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@AuthenticationPrincipal UsuarioLogado logado,
            @Valid @RequestBody SenhaDTOs.AlterarSenha dto) {
        service.alterarSenha(logado, dto);
    }
}

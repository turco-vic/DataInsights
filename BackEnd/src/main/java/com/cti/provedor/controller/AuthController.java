package com.cti.provedor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.config.UsuarioLogado;
import com.cti.provedor.dto.CadastroDTO;
import com.cti.provedor.dto.LoginDTO;
import com.cti.provedor.dto.LoginRespostaDTO;
import com.cti.provedor.dto.SenhaDTOs;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.service.ConsultorService;
import com.cti.provedor.service.RecuperacaoSenhaService;
import com.cti.provedor.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
public class AuthController {

    private final UsuarioService usuarioService;
    private final ConsultorService consultorService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;

    public AuthController(UsuarioService usuarioService, ConsultorService consultorService,
            RecuperacaoSenhaService recuperacaoSenhaService) {
        this.usuarioService = usuarioService;
        this.consultorService = consultorService;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
    }

    @PostMapping("/api/usuarios/login")
    public LoginRespostaDTO loginUsuario(@Valid @RequestBody LoginDTO dto) {
        return usuarioService.login(dto);
    }

    @PostMapping("/api/consultores/login")
    public LoginRespostaDTO loginConsultor(@Valid @RequestBody LoginDTO dto) {
        return consultorService.login(dto);
    }

    @PostMapping("/api/auth/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario cadastrar(@Valid @RequestBody CadastroDTO dto) {
        return usuarioService.cadastrar(dto);
    }

    @PostMapping("/api/auth/renovar")
    public LoginRespostaDTO renovar(@AuthenticationPrincipal UsuarioLogado logado) {
        return usuarioService.renovar(logado);
    }

    @PostMapping("/api/auth/esqueci-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void esqueciSenha(@Valid @RequestBody SenhaDTOs.EsqueciSenha dto) {
        recuperacaoSenhaService.solicitar(dto);
    }

    @PostMapping("/api/auth/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@Valid @RequestBody SenhaDTOs.RedefinirSenha dto) {
        recuperacaoSenhaService.redefinir(dto);
    }
}

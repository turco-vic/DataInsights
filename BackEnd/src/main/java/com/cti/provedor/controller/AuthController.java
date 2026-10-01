package com.cti.provedor.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.dto.LoginDTO;
import com.cti.provedor.dto.LoginRespostaDTO;
import com.cti.provedor.service.ConsultorService;
import com.cti.provedor.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
public class AuthController {

    private final UsuarioService usuarioService;
    private final ConsultorService consultorService;

    public AuthController(UsuarioService usuarioService, ConsultorService consultorService) {
        this.usuarioService = usuarioService;
        this.consultorService = consultorService;
    }

    @PostMapping("/api/usuarios/login")
    public LoginRespostaDTO loginUsuario(@Valid @RequestBody LoginDTO dto) {
        return usuarioService.login(dto);
    }

    @PostMapping("/api/consultores/login")
    public LoginRespostaDTO loginConsultor(@Valid @RequestBody LoginDTO dto) {
        return consultorService.login(dto);
    }
}

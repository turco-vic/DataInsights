package com.cti.provedor.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cti.provedor.dto.LoginDTO;
import com.cti.provedor.exception.AuthException;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;
import com.cti.provedor.service.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class AuthController {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository repository, PasswordEncoder encoder, JwtService jwtService) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginDTO dto) {
        Usuario u = repository.findByEmail(dto.getEmail())
                .orElseThrow(AuthException::new);

        if (encoder.matches(dto.getSenha(), u.getSenhaHash())) {
            return jwtService.gerar(u);
        }

        throw new AuthException();
    }
}

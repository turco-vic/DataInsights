package com.cti.provedor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
}

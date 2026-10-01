package com.cti.provedor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cti.provedor.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}

package com.cti.provedor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cti.provedor.model.Papel;
import com.cti.provedor.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    long countByPapel(Papel papel);

    List<Usuario> findByPrefResumoSemanalTrue();

    /** Usuários criados antes dos papéis existirem viram administradores. */
    @Modifying
    @Query("update Usuario u set u.papel = com.cti.provedor.model.Papel.ADMIN where u.papel is null")
    int definirAdminParaUsuariosSemPapel();
}

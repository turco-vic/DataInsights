package com.cti.provedor.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.model.Importacao;

public interface ImportacaoRepository extends JpaRepository<Importacao, Long> {

    List<Importacao> findAllByOrderByEnviadaEmDesc();

    Optional<Importacao> findFirstByOrderByEnviadaEmDesc();

    List<Importacao> findByEnviadaEmAfterOrderByEnviadaEmDesc(LocalDateTime inicio);

    /**
     * Muda só a etapa do Python, sem regravar a importação inteira (o Python também altera essas colunas).
     * Não sobrescreve um lote que o Python já marcou como PROCESSADO.
     */
    @Modifying
    @Transactional
    @Query("update Importacao i set i.statusEtl = :status, i.mensagemEtl = :mensagem "
            + "where i.id = :id and (i.statusEtl is null or i.statusEtl <> 'PROCESSADO')")
    int atualizarEtl(Long id, String status, String mensagem);
}

package com.cti.provedor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.repository.UsuarioRepository;

/** Na subida, dá o papel ADMIN às contas criadas antes de os papéis existirem. */
@Component
public class MigracaoPapeis implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracaoPapeis.class);

    private final UsuarioRepository usuarioRepository;

    public MigracaoPapeis(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int atualizados = usuarioRepository.definirAdminParaUsuariosSemPapel();
        if (atualizados > 0) {
            log.info("{} usuário(s) sem papel receberam o papel ADMIN", atualizados);
        }
    }
}

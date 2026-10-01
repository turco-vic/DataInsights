package com.cti.provedor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envia e-mails de texto. Sem servidor de e-mail configurado (SPRING_MAIL_HOST),
 * a mensagem vai para o log, o que basta para desenvolvimento.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remetente;

    public EmailService(ObjectProvider<JavaMailSender> mailSender, @Value("${app.mail.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    /** Devolve true se o e-mail saiu pelo servidor de e-mail. */
    public boolean enviar(String para, String assunto, String texto) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("E-mail não enviado (servidor de e-mail não configurado). Para: {} | Assunto: {}\n{}",
                    para, assunto, texto);
            return false;
        }
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(para);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);
        try {
            sender.send(mensagem);
            return true;
        } catch (MailException e) {
            log.warn("Falha ao enviar e-mail para {}: {}", para, e.getMessage());
            return false;
        }
    }
}

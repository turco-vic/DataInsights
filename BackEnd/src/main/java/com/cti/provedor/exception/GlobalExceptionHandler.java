package com.cti.provedor.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.cti.provedor.dto.ErroDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroDTO> conflito(ConflitoException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroDTO> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    /** A planilha tem inconsistências: devolve todas de uma vez, para o front listar linha a linha. */
    @ExceptionHandler(PlanilhaInconsistenteException.class)
    public ResponseEntity<ErroDTO> planilhaInconsistente(PlanilhaInconsistenteException e) {
        return ResponseEntity.badRequest()
                .body(new ErroDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage(), e.getInconsistencias()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroDTO> argumentoInvalido(IllegalArgumentException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Regra de validação não atendida: " + e.getMessage(), null);
    }

    @ExceptionHandler(ProibidoException.class)
    public ResponseEntity<ErroDTO> proibido(ProibidoException e) {
        return resposta(HttpStatus.FORBIDDEN, e.getMessage(), null);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErroDTO> auth(AuthException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroDTO> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + e.getName(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroDTO> integridade(DataIntegrityViolationException e) {
        return resposta(HttpStatus.CONFLICT, "Operação viola a integridade dos dados", null);
    }

    /**
     * Qualquer outro erro: a pessoa recebe uma mensagem simples, e o detalhe técnico fica só no log.
     * Erros que o Spring já sabe responder (rota inexistente, método errado...) mantêm o status deles,
     * e os de autenticação/permissão seguem para o Spring Security.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroDTO> inesperado(Exception e) throws Exception {
        if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
            throw e;
        }
        if (e instanceof ErrorResponse conhecido) {
            HttpStatus status = HttpStatus.valueOf(conhecido.getStatusCode().value());
            String mensagem = switch (status) {
                case NOT_FOUND -> "Rota não encontrada";
                case METHOD_NOT_ALLOWED -> "Método HTTP não permitido nesta rota";
                case UNSUPPORTED_MEDIA_TYPE -> "Formato do corpo não suportado; envie JSON";
                default -> "Requisição inválida";
            };
            return resposta(status, mensagem, null);
        }
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor. Tente novamente mais tarde.", null);
    }

    private ResponseEntity<ErroDTO> resposta(HttpStatus status, String mensagem, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ErroDTO(status.value(), mensagem, campos));
    }
}

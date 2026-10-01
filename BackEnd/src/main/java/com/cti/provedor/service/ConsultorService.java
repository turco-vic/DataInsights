package com.cti.provedor.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.ConsultorDTO;
import com.cti.provedor.dto.LoginDTO;
import com.cti.provedor.dto.LoginRespostaDTO;
import com.cti.provedor.dto.ResumoDTOs;
import com.cti.provedor.exception.AuthException;
import com.cti.provedor.exception.ConflitoException;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.model.Consultor;
import com.cti.provedor.repository.ClienteRepository;
import com.cti.provedor.repository.ConsultorRepository;

@Service
public class ConsultorService {

    private final ConsultorRepository repository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final EventoService eventos;

    public ConsultorService(ConsultorRepository repository, ClienteRepository clienteRepository,
            PasswordEncoder encoder, JwtService jwtService, EventoService eventos) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.eventos = eventos;
    }

    public LoginRespostaDTO login(LoginDTO dto) {
        String email = UsuarioService.normalizarEmail(dto.getEmail());
        Consultor consultor = repository.findByEmailIgnoreCase(email).orElse(null);
        if (consultor == null || consultor.getSenha() == null || !encoder.matches(dto.getSenha(), consultor.getSenha())) {
            eventos.registrar(EventoService.LOGIN_FALHOU, "Tentativa de login de consultor: " + email);
            throw new AuthException();
        }
        eventos.registrar(EventoService.LOGIN, "Consultor entrou: " + consultor.getEmail());
        String token = jwtService.gerar(consultor.getId(), consultor.getNome(), consultor.getEmail(),
                JwtService.TIPO_CONSULTOR, dto.isManterConectado());
        return new LoginRespostaDTO(token, consultor.getId(), consultor.getNome(), consultor.getEmail(),
                JwtService.TIPO_CONSULTOR, "CONSULTOR", dto.isManterConectado());
    }

    /** E-mail e senha são opcionais: sem eles o consultor existe, mas não entra na plataforma. */
    @Transactional
    public Consultor criar(ConsultorDTO dto) {
        Consultor consultor = new Consultor();
        preencher(consultor, dto);
        return repository.save(consultor);
    }

    public List<Consultor> listar() {
        return repository.findAll();
    }

    /** Lista com a quantidade de clientes de cada consultor. */
    public List<ResumoDTOs.Consultor> listarResumo() {
        Map<Long, Long> totais = new HashMap<>();
        for (Object[] linha : clienteRepository.contarPorConsultor()) {
            totais.put((Long) linha[0], (Long) linha[1]);
        }
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Consultor::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(c -> new ResumoDTOs.Consultor(c.getId(), c.getNome(), c.getMatricula(), c.getEmail(),
                        totais.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    public Consultor buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consultor não encontrado"));
    }

    @Transactional
    public Consultor atualizar(Long id, ConsultorDTO dto) {
        Consultor consultor = buscarPorId(id);
        preencher(consultor, dto);
        return repository.save(consultor);
    }

    @Transactional
    public void excluir(Long id) {
        Consultor consultor = buscarPorId(id);
        long clientes = clienteRepository.countByConsultorId(id);
        if (clientes > 0) {
            throw new ConflitoException("Consultor possui " + clientes + " cliente(s) vinculado(s)");
        }
        repository.delete(consultor);
    }

    private void preencher(Consultor consultor, ConsultorDTO dto) {
        String email = UsuarioService.vazioParaNulo(dto.getEmail());
        if (email != null) {
            email = email.toLowerCase();
            boolean emUso = consultor.getId() == null
                    ? repository.existsByEmailIgnoreCase(email)
                    : repository.existsByEmailIgnoreCaseAndIdNot(email, consultor.getId());
            if (emUso) {
                throw new ConflitoException("E-mail já cadastrado");
            }
        }
        boolean novaSenha = dto.getSenha() != null && !dto.getSenha().isBlank();
        if (novaSenha && email == null) {
            throw new RegraNegocioException("Informe o e-mail para o consultor poder entrar com a senha");
        }

        consultor.setNome(dto.getNome().trim());
        consultor.setMatricula(UsuarioService.vazioParaNulo(dto.getMatricula()));
        consultor.setEmail(email);
        if (novaSenha) {
            consultor.setSenha(encoder.encode(dto.getSenha()));
        } else if (email == null) {
            consultor.setSenha(null);
        }
    }
}

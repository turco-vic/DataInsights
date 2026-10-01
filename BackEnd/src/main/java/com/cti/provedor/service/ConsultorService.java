package com.cti.provedor.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.ConsultorDTO;
import com.cti.provedor.dto.LoginDTO;
import com.cti.provedor.dto.LoginRespostaDTO;
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

    public ConsultorService(ConsultorRepository repository, ClienteRepository clienteRepository,
            PasswordEncoder encoder, JwtService jwtService) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public LoginRespostaDTO login(LoginDTO dto) {
        Consultor consultor = repository.findByEmailIgnoreCase(dto.getEmail().trim())
                .orElseThrow(AuthException::new);
        if (consultor.getSenha() == null || !encoder.matches(dto.getSenha(), consultor.getSenha())) {
            throw new AuthException();
        }
        String token = jwtService.gerar(consultor.getId(), consultor.getNome(), consultor.getEmail(),
                JwtService.TIPO_CONSULTOR);
        return new LoginRespostaDTO(token, consultor.getId(), consultor.getNome(), consultor.getEmail(),
                JwtService.TIPO_CONSULTOR);
    }

    @Transactional
    public Consultor criar(ConsultorDTO dto) {
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new RegraNegocioException("Senha é obrigatória");
        }
        String email = normalizarEmail(dto.getEmail());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }

        Consultor consultor = new Consultor();
        preencher(consultor, dto, email);
        consultor.setSenha(encoder.encode(dto.getSenha()));
        return repository.save(consultor);
    }

    public List<Consultor> listar() {
        return repository.findAll();
    }

    public Consultor buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consultor não encontrado"));
    }

    @Transactional
    public Consultor atualizar(Long id, ConsultorDTO dto) {
        Consultor consultor = buscarPorId(id);
        String email = normalizarEmail(dto.getEmail());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflitoException("E-mail já cadastrado");
        }

        preencher(consultor, dto, email);
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            consultor.setSenha(encoder.encode(dto.getSenha()));
        }
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

    private void preencher(Consultor consultor, ConsultorDTO dto, String email) {
        consultor.setNome(dto.getNome().trim());
        consultor.setMatricula(dto.getMatricula());
        consultor.setEmail(email);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
